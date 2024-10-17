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

package dev.akkariin.nbtgenerator.filter.impl

import dev.akkariin.nbtgenerator.filter.ElementCleaner
import dev.akkariin.nbtgenerator.util.ElementUtil
import net.kyori.adventure.nbt.BinaryTagTypes
import net.kyori.adventure.nbt.CompoundBinaryTag
import net.kyori.adventure.nbt.IntBinaryTag
import net.kyori.adventure.nbt.ListBinaryTag
import java.io.File

object RegistryFilter : ElementCleaner, ListPathFilter(listOf(
    "minecraft:trim_pattern",
    "minecraft:trim_material",
    "minecraft:instrument",
    "minecraft:banner_pattern",
    "minecraft:enchantment",
    "minecraft:chat_type",
    "minecraft:painting_variant",
    "minecraft:jukebox_song",
    "minecraft:dimension_type",
    "minecraft:damage_type",
    "minecraft:worldgen/biome",
    "minecraft:wolf_variant"
)) {

    @Suppress("SpellCheckingInspection")
    override fun filterFile(file: File): Boolean {
        return if (file.isDirectory) {
            file.name == "datapacks" || file.name == "tags"
        } else {
            file.name == "zero.json"
        }
    }

    override fun clean(type: String, original: CompoundBinaryTag): CompoundBinaryTag {
        return when (type) {
            "minecraft:worldgen/biome" -> cleanBiome(original)
            "minecraft:wolf_variant" -> ElementUtil.ofSingleElement(original) { it.getString("name") == "minecraft:ashen" }
            "minecraft:painting_variant" -> ElementUtil.ofSingleElement(original)
            "minecraft:damage_type" -> ElementUtil.ofSingleElement(original) { it.getString("name") == "minecraft:generic" }
            "minecraft:dimension_type" -> ElementUtil.ofSingleElement(original) { it.getString("name") == "minecraft:overworld" }
            "minecraft:chat_type" -> original
            else -> ElementUtil.empty(type)
        }
    }

    private fun cleanBiome(original: CompoundBinaryTag): CompoundBinaryTag {
        val value = ListBinaryTag.builder(BinaryTagTypes.COMPOUND)
        var index = 0
        val list = mutableListOf<String>()
        for ((name, element) in original.getList("value")
            .map { it as CompoundBinaryTag }
            .associate { it.getString("name") to it.getCompound("element") }
        ) {
            if (name.startsWith("minecraft:swamp") || name == "minecraft:plains" || name == "minecraft:snowy_taiga") {
                list.add(name)
                value.add(CompoundBinaryTag
                    .builder()
                    .putString("name", name)
                    .put("id", IntBinaryTag.intBinaryTag(index++))
                    .put("element", CompoundBinaryTag.builder().apply {
                        element
                            .filterNot { it.key == "features" || it.key == "spawners" }
                            .forEach { put(it.key, it.value) }
                    }.build())
                    .build()
                )
            }
        }
        require(list.contains("minecraft:plains")) { "Missing minecraft:plains in biome" }
        require(list.contains("minecraft:swamp")) { "Missing minecraft:swamp in biome" }
        return ElementUtil.ofValues("minecraft:worldgen/biome", value.build())
    }

}