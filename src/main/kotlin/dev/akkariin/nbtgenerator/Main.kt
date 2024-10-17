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

import dev.akkariin.nbtgenerator.args.Arg
import dev.akkariin.nbtgenerator.args.ArgsParser
import dev.akkariin.nbtgenerator.util.PresentCleaner
import dev.akkariin.nbtgenerator.util.PresentCompression
import dev.akkariin.nbtgenerator.util.PresentFilter
import java.io.File

fun main(args: Array<String>) {
    if (args.size == 1 && (args[0] == "-h" || args[0] == "--help")) {
        listOf(
            "Run with no args to apply all default values. All args can accept \"default\" as the default argument.",
            "Available args:",
            "  --source <folder>    Point to the output folder of the data generator. The default is \"generated\\data\\minecraft\"",
            "  --output <name>    Specify the file name for the output. The default is \"output.nbt\".",
            "  --filter <ZERO/DATA_PACKETS/TAGS/REGISTRY/NULL>    Presents filter to skip file or tag. The default is \"REGISTRY\"",
            "  --cleaner <REGISTRY/NULL>    Presents element cleaner to clean some unless information in output. The default is \"REGISTRY\"",
            "  --compression <NONE/GZIP/ZLIB>    Presents compression method. The default is \"NONE\"",
            "To learn how to generate data through Data Generator.",
            "Please refer to https://zh.minecraft.wiki/w/Tutorial:%E8%BF%90%E8%A1%8C%E6%95%B0%E6%8D%AE%E7%94%9F%E6%88%90%E5%99%A8."
        ).forEach(::println)
        return
    }
    val parser = ArgsParser(args)
    val runDirectory = File(System.getProperty("user.dir"))
    val source = File(runDirectory, parser.parse(Arg.of("source", """generated\data\minecraft""")))
    if (!source.exists() || !source.isDirectory) {
        throw IllegalArgumentException("""Failed to read ${source.toPath()} as directory! Make sure it exists. Run with -h flags to get help.""")
    }
    val output = File(runDirectory, parser.parse(Arg.of("output", "output.nbt")))
    val filter = parser.parse(Arg("filter", PresentFilter.REGISTRY, PresentFilter::valueOf)).filter
    val compression = parser.parse(Arg("compression", PresentCompression.GZIP, PresentCompression::valueOf)).compression
    val cleaner = parser.parse(Arg("cleaner", PresentCleaner.REGISTRY, PresentCleaner::valueOf)).cleaner
    GenerateTask(source, output, compression, filter, cleaner).generate()
}