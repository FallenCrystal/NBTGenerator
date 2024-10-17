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

import net.kyori.adventure.nbt.BinaryTagTypes
import net.kyori.adventure.nbt.CompoundBinaryTag
import net.kyori.adventure.nbt.ListBinaryTag

@Suppress("MemberVisibilityCanBePrivate")
object ElementUtil {
    fun empty(type: String): CompoundBinaryTag {
        return ofValues(type, ListBinaryTag.empty())
    }

    fun ofValues(type: String, binaryTag: ListBinaryTag): CompoundBinaryTag {
        return CompoundBinaryTag
            .builder()
            .putString("name", type)
            .put("value", binaryTag)
            .build()
    }

    fun ofSingleElement(type: String, elementName: String, element: CompoundBinaryTag): CompoundBinaryTag {
        return ofValues(type, ListBinaryTag.builder(BinaryTagTypes.COMPOUND).add(
            CompoundBinaryTag
            .builder()
            .putString("name", elementName)
            .putInt("id", 0)
            .put("element", element)
            .build()
        ).build())
    }

    fun ofSingleElement(original: CompoundBinaryTag, predicate: (CompoundBinaryTag) -> Boolean): CompoundBinaryTag {
        val value = original.getList("value")
        return value
            .takeUnless { it.size() == 0 }
            ?.map { it as CompoundBinaryTag }
            ?.firstOrNull(predicate)
            ?.let { ofSingleElement(original.getString("name"), it.getString("name"), it.getCompound("element")) }
            ?: empty(original.getString("name"))
    }

    fun ofSingleElement(original: CompoundBinaryTag): CompoundBinaryTag {
        return ofSingleElement(original) { it.getInt("id") == 0 }
    }
}