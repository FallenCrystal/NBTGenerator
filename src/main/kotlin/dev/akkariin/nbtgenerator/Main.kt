/*
 * Copyright (C) 2024 FallenCrystal / NBTGenerator Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package dev.akkariin.nbtgenerator

import com.google.gson.Gson
import com.google.gson.JsonObject
import dev.akkariin.nbtgenerator.args.Arg
import dev.akkariin.nbtgenerator.args.ArgsParser
import dev.akkariin.nbtgenerator.data.Version
import dev.akkariin.nbtgenerator.util.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.channels.Channels
import java.util.*


fun main(args: Array<String>) {
    if (args.size == 1 && (args[0] == "-h" || args[0] == "--help")) {
        listOf(
            "Run with no args to apply all default values. All args can accept \"default\" as the default argument.",
            "Available args:",
            "  --source <folder>    Point to the output folder of the data generator. The default is \"generated\\data\\minecraft\"",
            "  --output <name>    Specify the file name for the output. The default is \"output.nbt\".",
            "  --filter <ZERO/DATA_PACKETS/TAGS/REGISTRY/NULL>    Presents filter to skip file or tag. The default is \"REGISTRY\"",
            "  --cleaner <REGISTRY/NONE>    Presents element cleaner to clean some unless information in output. The default is \"REGISTRY\"",
            "  --compression <NONE/GZIP/ZLIB>    Presents compression method. The default is \"GZIP\" (Vanilla behavior)",
            "  --allow-modify <true/false>    Allow filter modify output element. The default is \"false\" (Experimental)",
            "  --version <list/release/snapshot/...>    Show minecraft versions, Fetch data from specified version or latest release/snapshot (Optional)",
            "To learn how to generate data through Data Generator.",
            "Please refer to https://zh.minecraft.wiki/w/Tutorial:%E8%BF%90%E8%A1%8C%E6%95%B0%E6%8D%AE%E7%94%9F%E6%88%90%E5%99%A8"
        ).forEach(::println)
        return
    }
    val fileSeparator = File.separator
    val parser = ArgsParser(args)
    val runDirectory = File(System.getProperty("user.dir"))
    var source = File(runDirectory, parser.parse(Arg.of("source", """generated${fileSeparator}data${fileSeparator}minecraft""")))
    val version = parser.parse(Arg("version", null as String?) { it })
    if (version != null) {
        val collector = Version.Collector.from(Gson().fromJson(HttpsUtil.readString("https://piston-meta.mojang.com/mc/game/version_manifest.json"), JsonObject::class.java))
        if (version == "list") {
            printVersions(collector)
            return
        } else {
            val parsedVersion = when (version) {
                "release" -> collector.latestRelease
                "snapshot" -> collector.latestSnapshot
                else -> collector.version(version)
            }
            if (parsedVersion == null) {
                println("Unknown version $version. Check available version with \"--version list\"")
                return
            }
            source = File(download(parsedVersion), """data${fileSeparator}minecraft""")
            println("Source changed to ${source.absolutePath}")
        }
    }
    if (!source.exists() || !source.isDirectory) {
        throw IllegalArgumentException("""Failed to read ${source.toPath()} as directory! Make sure it exists. Run with -h flags to get help.""")
    }
    val output = File(runDirectory, parser.parse(Arg.of("output", "output.nbt")))
    val filter = parser.parse(Arg("filter", PresentFilter.REGISTRY, PresentFilter::valueOf)).filter
    val compression = parser.parse(Arg("compression", PresentCompression.GZIP, PresentCompression::valueOf)).compression
    val cleaner = parser.parse(Arg("cleaner", PresentCleaner.REGISTRY, PresentCleaner::valueOf)).cleaner
    val allowModify = parser.parse(Arg("allow-modify", false, String::toBooleanStrict))
    if (allowModify) {
        System.err.println("allowModify is a experimental feature! " +
                "The feature may not work properly or provide invalid output as the version changes. " +
                "By using it, you are responsible for any issues caused by it!"
        )
    }
    GenerateTask(source, output, allowModify, compression, filter, cleaner).generate()
}

fun printVersions(collector: Version.Collector) {
    println("Minecraft versions:")
    println("----------------------------------------")
    println("Latest release version: ${collector.latestRelease.id} (${collector.latestRelease.releaseTime})")
    println("Latest snapshot version: ${collector.latestSnapshot.id} (${collector.latestSnapshot.releaseTime})")
    println("----------------------------------------")
    println("Available versions: (Only print 10 first version. Press enter to print more or execute exit to exit. You can check out it on https://piston-meta.mojang.com/mc/game/version_manifest.json anytime.)")
    val iterator = collector.versions().iterator()
    fun print() {
        var count = 0
        while (iterator.hasNext() && count <= 10) {
            count++
            val version = iterator.next()
            println("${version.id} (${version.releaseTime}, ${version.type.name.lowercase()})")
        }
    }
    print()
    val scanner = Scanner(System.`in`)
    while (true) {
        val line = try { scanner.nextLine() } catch (_: NoSuchElementException) { break }
        if (line == "exit")
            break
        print()
        if (!iterator.hasNext()) {
            println("No more versions found.")
            break
        }
    }
}

fun download(version: Version): File {
    println("Fetching data...")
    val future = version.fetchData()
    println("Checking files...")
    val file = File(File(System.getProperty("user.dir"), "download"), version.id)
        .also { if (!it.exists()) it.mkdirs() }
        .let { File(it, "${version.id.replace(" ", "-")}.jar") }
    var download = true
    if (file.exists()) {
        println("File already exists. File integrity is being checking...")
        val fileSha1 = FileUtil.getSHA1(file)
        println("File sha1: $fileSha1")
        val exceptedSha1 = future.get().downloads.server.sha1
        println("Excepted sha1: $exceptedSha1")
        if (fileSha1 == exceptedSha1) {
            println("The file validation was successful. Skip the download.")
            download = false
        } else {
            System.err.println("File validation failed.")
            file.delete()
        }
    }
    if (download) {
        if (!file.exists()) file.createNewFile()
        println("Waiting for fetching data...")
        if (getRuntimeMajorVersion() < future.get().javaVersion.majorVersion)
            throw UnsupportedOperationException("Target require major java version ${future.get().javaVersion.majorVersion} " +
                    "but this application is running with ${getRuntimeMajorVersion()}")
        val url = future.get().downloads.server.url
        println("Downloading...")
        val connection = HttpsUtil.openConnection(url)
        Channels.newChannel(connection.inputStream).use { channel ->
            FileOutputStream(file).use { outputStream ->
                outputStream.getChannel().transferFrom(channel, 0, Long.MAX_VALUE)
            }
        }
        if (FileUtil.getSHA1(file) != future.get().downloads.server.sha1)
            throw IOException("Failed sha1 validation. excepted ${future.get().downloads.server.sha1} but downloaded file sha1 is ${FileUtil.getSHA1(file)}")
        println("Download completed.")
    }
    val generated = File(file.parentFile, "generated")
    if (generated.exists()) {
        println("Deleting old generated files...")
        generated.delete()
    }
    println("Starting minecraft server...")
    try {
        val executable = "${System.getProperty("java.home")}/bin/${if (System.getProperty("os.name").contains("windows", ignoreCase = true)) "java.exe" else "java"}"
        val process = ProcessBuilder(listOf(executable, "-DbundlerMainClass=net.minecraft.data.Main", "-jar", file.absolutePath, "--all").also { println("Executing: $it") })
            .directory(file.parentFile)
            .start()
        process.inputStream.bufferedReader().use { it.forEachLine(::println) }
        process.errorStream.bufferedReader().use { it.forEachLine(System.err::println) }
        val exitCode = process.waitFor()
        if (exitCode != 0) {
            throw IllegalArgumentException("Process returned non-zero exit code: $exitCode")
        }
        if (!generated.exists() || !generated.isDirectory) throw IllegalArgumentException("${generated.absolutePath} is not a directory!")
    } catch (t: Throwable) {
        throw UnsupportedOperationException("Failed to generate files minecraft server.", t)
    }
    println("Successfully generated files.")
    return generated
}


fun getRuntimeMajorVersion() = System.getProperty("java.version").let { it.split(".")[if (it.startsWith("1.")) 1 else 0].toInt() }
