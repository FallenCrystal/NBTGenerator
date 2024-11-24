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

import com.google.gson.*
import dev.akkariin.nbtgenerator.filter.ElementCleaner
import dev.akkariin.nbtgenerator.filter.PathFilter
import dev.akkariin.nbtgenerator.util.ElementUtil
import dev.akkariin.nbtgenerator.util.JsonSerializer.serialize
import net.kyori.adventure.nbt.*
import net.kyori.adventure.nbt.BinaryTagIO.Compression
import java.io.File
import java.io.FileReader

class GenerateTask(
    private val folder: File,
    private val fileToSave: File,
    private val allowModify: Boolean,
    private val compression: Compression,
    private val filter: PathFilter?,
    private val cleaner: ElementCleaner?
) {

    private val root = CompoundBinaryTag.builder()

    fun generate() {
        require(folder.isDirectory) { "Folder is must be a directory!" }
        if (filter != null) println("Apply $filter as path filter")
        for (folder in folder.listFiles()!!) {
            if (!folder.isDirectory) continue
            searchFolder(null, folder)
        }
        val compound = cleaner?.let {
            println("Passed original root tags with cleaner $cleaner (allowModify: $allowModify)")
            val cleaned = CompoundBinaryTag.builder()
            root.build().forEach { (key, binaryTag) ->
                cleaned.put(key, it.clean(key, binaryTag as CompoundBinaryTag, allowModify))
            }
            cleaned.build()
        } ?: root.build()
        println("Generated ${compound.size()} types")
        if (fileToSave.exists()) {
            println("Files with name ${fileToSave.toPath()} is already exist. Deleting...")
            fileToSave.delete()
        }
        fileToSave.createNewFile()
        println("Save to file ${fileToSave.toPath()}")
        BinaryTagIO.writer().write(compound, fileToSave.toPath(), compression)
    }

    private fun searchFolder(prefix: String?, folder: File) {
        require(folder.isDirectory) { "Folder is must be a directory!" }
        val type = "minecraft:${prefix ?: ""}${folder.name}"
        if (filter?.filter(folder, type) == true) {
            println("Skipping ${folder.toPath()}")
            return
        }
        var index = 0
        val tags = ListBinaryTag.builder()
        for (file in folder.listFiles()!!) {
            if (filter?.filter(file, null) == true) {
                println("Skipping ${file.toPath()}")
                continue
            }
            if (file.isDirectory) {
                searchFolder("${type.removePrefix("minecraft:")}/", file)
            } else if (file.name.endsWith(".json")) {
                try {
                    val json = Gson().fromJson(FileReader(file), JsonObject::class.java)
                    val compound = CompoundBinaryTag.builder()
                    compound.putInt("id", index++)
                    compound.putString("name", "minecraft:${file.name.removeSuffix(".json")}")
                    compound.put("element", serialize(json))
                    tags.add(compound.build())
                    println("Generated from ${file.toPath()} with type $type ($index)")
                } catch (e: Exception) {
                    println("Failed to parse file ${file.toPath()}:")
                    e.printStackTrace()
                    continue
                }
            }
        }
        tags.build().takeUnless { it.size() == 0 }?.also { root.put(type, ElementUtil.ofValues(type, it)) }
    }
}