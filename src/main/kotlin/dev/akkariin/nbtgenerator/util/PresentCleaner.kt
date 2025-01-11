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

package dev.akkariin.nbtgenerator.util

import dev.akkariin.nbtgenerator.filter.ElementCleaner
import dev.akkariin.nbtgenerator.filter.impl.RegistryFilter
import net.kyori.adventure.nbt.BinaryTagTypes
import net.kyori.adventure.nbt.ListBinaryTag
import net.kyori.adventure.nbt.CompoundBinaryTag

enum class PresentCleaner(val cleaner: ElementCleaner?) {
    NONE(null),
    REGISTRY(RegistryFilter),
    SIMPLE_BIOME(ElementCleaner { type, original, _ ->
        if (type != "minecraft:worldgen/biome") original else {
            val values = ListBinaryTag.builder(BinaryTagTypes.COMPOUND)
            for (o in original.getList("value", BinaryTagTypes.COMPOUND).map { it as CompoundBinaryTag }) {
                CompoundBinaryTag
                    .builder()
                    .put("name", o.get("name")!!)
                    .put("id", o.get("id")!!)
                    .put("element", CompoundBinaryTag.builder().apply {
                        for ((k, v) in o.get("element") as CompoundBinaryTag) {
                            if (RegistryFilter.USELESS_BIOME_ELEMENTS.contains(k)) continue
                            put(k, v)
                        }
                    }.build())
                    .build()
                    .let(values::add)
            }
            CompoundBinaryTag
                .builder()
                .putString("type", type)
                .put("value", values.build())
                .build()
        }
    })
}