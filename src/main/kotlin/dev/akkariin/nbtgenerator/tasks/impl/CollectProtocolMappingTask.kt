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
import com.google.gson.JsonElement
import dev.akkariin.nbtgenerator.args.ArgsParser
import dev.akkariin.nbtgenerator.data.MultiException
import dev.akkariin.nbtgenerator.protocol.ProtocolData
import dev.akkariin.nbtgenerator.protocol.ProtocolRegistry
import dev.akkariin.nbtgenerator.tasks.Stage
import dev.akkariin.nbtgenerator.tasks.Task
import dev.akkariin.nbtgenerator.util.FileUtil.existOrThrow
import dev.akkariin.nbtgenerator.util.FileUtil.toFilePath
import dev.akkariin.nbtgenerator.util.JsonUtil.exceptedAsJsonObject
import dev.akkariin.nbtgenerator.util.JsonUtil.int
import dev.akkariin.nbtgenerator.util.JsonUtil.getObject
import dev.akkariin.nbtgenerator.util.JsonUtil.stringOrNull
import java.io.File
import java.io.FileReader

class CollectProtocolMappingTask(folder: File) : Task(folder) {

    // Stages
    private val findFile = Stage("Find file", "Finding file for protocol id mapping registry") {
        listOf(
            "Check that the data generator has been run. And point the path to the correct folder."
        )
    }
    private val readFile = Stage("Read file", "Reading the registries file") {
        listOf(
            "Check whether the target file has JSON syntax errors.",
            "Make sure application has permission to read the file."
        )
    }
    private val mappingObject = Stage("Mapping Object", "Mapping registries as object")

    val map = mutableMapOf<String, ProtocolRegistry>()

    override fun initialize() = arrayOf(findFile, readFile, mappingObject)

    override fun execute(parser: ArgsParser) {
        setStage(findFile)
        val file = File(folder, "reports/registries.json".toFilePath()).existOrThrow()
        setStage(readFile)
        val obj = Gson().fromJson(FileReader(file), JsonElement::class.java).exceptedAsJsonObject()
        setStage(mappingObject)
        val exception = MultiException("Failed to read registries.")
        val wrappedRegistry = mutableMapOf<String, ProtocolRegistry>()
        for ((key, value) in obj.entrySet().map { it.key to it.value.exceptedAsJsonObject() }) {
            try {
                val entries = value.getObject("entries")
                val wrappedEntries = mutableMapOf<String, ProtocolData>()
                for (entry in entries.entrySet()) {
                    val name = entry.key
                    val protocolId = entry.value.exceptedAsJsonObject().let {
                        if (it.size() != 1) throw IllegalArgumentException("Excepted 1 size but found ${it.size()} while reading $name.")
                        it.int("protocol_id")
                    }
                    wrappedEntries[name] = ProtocolData(name, protocolId)
                }
                val default = value.stringOrNull("default")?.let(wrappedEntries::get)
                wrappedRegistry[key] = ProtocolRegistry(key, default, wrappedEntries.values.toList())
            } catch (t: Throwable) {
                exception.addException(t)
            }
        }
        if (exception.isNotEmpty()) throw exception
        var categoryCount = 0
        var entriesCount = 0
        wrappedRegistry.values.forEach {
            categoryCount++
            entriesCount += it.size()
        }
        println("Collected $categoryCount registries with $entriesCount entries.")
    }

}