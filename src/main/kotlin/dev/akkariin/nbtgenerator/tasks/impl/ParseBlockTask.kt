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
import dev.akkariin.nbtgenerator.args.ArgsParser
import dev.akkariin.nbtgenerator.block.BlockData
import dev.akkariin.nbtgenerator.block.BlockState
import dev.akkariin.nbtgenerator.data.MultiException
import dev.akkariin.nbtgenerator.tasks.Stage
import dev.akkariin.nbtgenerator.tasks.Task
import dev.akkariin.nbtgenerator.util.FileUtil.toFilePath
import java.io.File
import java.io.FileReader

@Suppress("MemberVisibilityCanBePrivate")
class ParseBlockTask(folder: File, val launchTerminal: Boolean) : Task(folder) {

    // Stages
    private val findFile = Stage("Find file", "Finding file for blocks states data")
    private val readFile = Stage("Read file", "Reading the blocks states file")
    private val parseBlocks = Stage("Parse blocks", "Parse blocks states data")
    private val mappingId = Stage("Mapping ID", "Mapping block states for protocol id")

    val blocksData = mutableMapOf<String, BlockData>()
    val idMapping = mutableMapOf<Int, BlockState>()

    override fun initialize() = arrayOf(findFile, readFile, parseBlocks, mappingId)
    override fun execute(parser: ArgsParser) {
        setStage(findFile)
        val file = findFile()
        setStage(readFile)
        val jsonObject = Gson().fromJson(FileReader(file), JsonObject::class.java)
        setStage(parseBlocks)
        val exception = MultiException("Failed to parse blocks data")
        for ((k, v) in jsonObject.entrySet()) {
            try {
                blocksData[k] = BlockData(k, v.asJsonObject)
            } catch (t: Throwable) {
                exception.addException(t)
            }
        }
        if (exception.isNotEmpty()) throw exception
        setStage(mappingId)
        blocksData.values.forEach { block -> block.blockStates.forEach { idMapping[it.id] = it } }
    }

    private fun findFile(): File {
        return File(folder, "reports/blocks.json".toFilePath()).apply {
            require(exists()) { "File does not exist: $absolutePath" }
        }
    }

}