/*
 * Copyright (C) 2025 FallenCrystal / NBTGenerator Contributors
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

package dev.akkariin.nbtgenerator.tasks.impl

import com.google.gson.Gson
import com.google.gson.JsonObject
import dev.akkariin.nbtgenerator.args.Arg
import dev.akkariin.nbtgenerator.args.ArgsParser
import dev.akkariin.nbtgenerator.data.MinecraftVersion
import dev.akkariin.nbtgenerator.data.MinecraftVersion.Collector
import dev.akkariin.nbtgenerator.tasks.Stage
import dev.akkariin.nbtgenerator.tasks.Task
import dev.akkariin.nbtgenerator.util.FileUtil
import dev.akkariin.nbtgenerator.util.HttpsUtil
import org.fusesource.jansi.Ansi
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.channels.Channels

class DownloadTask(folder: File) : Task(folder) {

    private val internetSuggestion = listOf(
        "Check in your browser to see if you can browse the links mentioned in the logs.",
        "Check if your computer can connect to the internet without a proxy."
    )

    // Stage
    private val fetchVersions = Stage("Fetching versions", "Fetching versions data") {
        mutableListOf("Check if the entered version of Minecraft exists.").also { it.addAll(internetSuggestion) }
    }
    private val fetchData = Stage("Fetching version data", "Fetching specific version data") { internetSuggestion }
    private val downloadServer = Stage("Download server", "Download specific version of Minecraft server") { internetSuggestion }
    private val checkEnvironment = Stage("Check environment", "Check if the current environment can run Minecraft.") {
        listOf("Re-run the task using Java 21. If not. Then install it first.")
    }
    private val runServer = Stage("Run server", "Run the minecraft server with generator flag") {
        listOf(
            "Check if the vanilla server is printing any errors.",
            "Make sure that the application has access to the terminal and the location of the current Java runtime.",
            "Temporarily disable antivirus software. Or temporarily allow the current Java runtime to create processes."
        )
    }

    var downloadFolder: File? = null

    override fun initialize() = arrayOf(fetchVersions, fetchData, downloadServer, checkEnvironment, runServer)

    override fun execute(parser: ArgsParser) {
        val maxTries = parser.parse(Arg.of("max-download-tries", "Maximum number of tries to download.", 3))
        val version = when (val v = parser.parse(Arg.of("version", "Minecraft version to download", "null"))) {
            "release", "r", "rel" -> "release"
            "snapshot", "s", "snap" -> "snapshot"
            "null" -> throw IllegalArgumentException("Can't run the task without specifying a Minecraft version.")
            else -> v
        }
        setStage(fetchVersions)
        val collector = ofTries("fetch versions", maxTries) {
            Collector.from(Gson().fromJson(HttpsUtil.readString("https://piston-meta.mojang.com/mc/game/version_manifest.json"), JsonObject::class.java))
        }
        val wrappedVersion = when (version) {
            "release" -> collector.latestRelease
            "snapshot" -> collector.latestSnapshot
            "list" -> {
                printVersions(collector)
                return
            }
            else -> collector.version(version) ?: throw IllegalArgumentException("Can't find version $version")
        }
        setStage(fetchData)
        val data = ofTries("fetch data for version ${wrappedVersion.id}", maxTries) { wrappedVersion.fetchData().get() }
        setStage(downloadServer)
        val file = download(data, maxTries)
        setStage(checkEnvironment)
        checkEnvironment(data)
        setStage(runServer)
        val executable = "${System.getProperty("java.home")}/bin/${if (System.getProperty("os.name").contains("windows", ignoreCase = true)) "java.exe" else "java"}"
        val process = ProcessBuilder(listOf(executable, "-DbundlerMainClass=net.minecraft.data.Main", "-jar", file.absolutePath, "--all").also { println("Executing $it") })
            .directory(file.parentFile)
            .start()
        process.inputStream.bufferedReader().use { reader ->
            reader.forEachLine {
                if (!it.startsWith("Unpacking ")) {
                    println(Ansi.ansi().fg(Ansi.Color.WHITE).a("[*] ${it.replace("[ServerMain/INFO]: ", "")}").bg(Ansi.Color.DEFAULT))
                }
            }
        }
        process.errorStream.bufferedReader().use { reader -> reader.forEachLine { println(Ansi.ansi().fg(Ansi.Color.RED).a(it).bgDefault()) } }
        val exitCode = process.onExit().get().exitValue()
        require(exitCode == 0) { "Process exited with code $exitCode that is not 0." }
    }

    private fun printVersions(collector: Collector) {
        TODO("Cannot print versions for this time.")
    }

    private fun checkEnvironment(data: MinecraftVersion.Data) {
        val majorVersion = System.getProperty("java.version").let { it.split(".")[if (it.startsWith("1.")) 1 else 0].toInt() }
        if (majorVersion < data.javaVersion.majorVersion)
            throw RuntimeException("The current Java environment does not meet the running requirements. Please run the task with the jdk/jre required by Minecraft.")

    }

    private fun download(data: MinecraftVersion.Data, maxTries: Int): File {
        val file = File(File(folder, "download"), data.minecraftVersion.id)
            .also {
                if (!it.exists()) it.mkdirs()
                downloadFolder = it
                File(it, "generated").takeIf(File::exists)?.delete()
            }
            .let { File(it, "${data.minecraftVersion.id}.jar") }
        val server = data.downloads.server
        if (file.exists()) {
            val sha1 = FileUtil.getSha1(file)
            val exceptedSha1 = server.sha1
            if (sha1 != exceptedSha1) {
                println(Ansi.ansi().fg(Ansi.Color.YELLOW).a("Unable to check file integrity. Excepted sha1: $exceptedSha1 but got $sha1"))
                file.delete()
            } else {
                println(Ansi.ansi().fg(Ansi.Color.GREEN).a("Skipping download because local file is valid."))
                return file
            }
        }
        if (!file.exists()) file.createNewFile()
        ofTries("download server for version ${data.minecraftVersion.id}", maxTries, file::delete) {
            Channels.newChannel(HttpsUtil.openConnection(server.url).inputStream).use { channel ->
                FileOutputStream(file).use { output ->
                    output.channel.transferFrom(channel, 0, Long.MAX_VALUE)
                }
            }
            if (FileUtil.getSha1(file) != server.sha1)
                throw IOException("Failed file integrity check. Excepted ${server.sha1} but got ${FileUtil.getSha1(file)}")
        }
        return file
    }

    private fun <T> ofTries(n: String, maxTries: Int, onException: () -> Unit = {}, func: () -> T): T {
        var tries = 0
        var t: Throwable? = null
        while (tries < maxTries) {
            tries++
            try {
                return func()
            } catch (e: Exception) {
                onException()
                println("Failed to $n: ${e.message} (remain tries: ${(maxTries - tries)})")
                t = e
            }
        }
        throw IllegalStateException("Task has reached max tries: $maxTries.", t)
    }
}