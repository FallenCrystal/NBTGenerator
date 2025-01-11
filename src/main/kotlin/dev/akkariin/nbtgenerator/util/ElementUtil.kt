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

import net.kyori.adventure.nbt.BinaryTag
import net.kyori.adventure.nbt.BinaryTagType
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
            .putString("type", type)
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
            ?.let { ofSingleElement(original.getString("type"), it.getString("name"), it.getCompound("element")) }
            ?: empty(original.getString("type"))
    }

    fun ofSingleElement(original: CompoundBinaryTag): CompoundBinaryTag {
        return ofSingleElement(original) { it.getInt("id") == 0 }
    }

    fun anyOf(compound: CompoundBinaryTag, type: String, elementName: String, predicate: (BinaryTag) -> Boolean): Boolean {
        val c = (compound.get(type) as? CompoundBinaryTag)
            ?.getList("value", BinaryTagTypes.COMPOUND)
            ?.mapNotNull { (it as? CompoundBinaryTag)?.get("element") as? CompoundBinaryTag }
            ?: return false
        for (element in c) {
            if (element.get(elementName)?.let(predicate) ?: continue) {
                return true
            }
        }
        return false
    }

    inline fun <reified T : BinaryTag> rewriteElement(
        original: CompoundBinaryTag,
        type: String,
        element: String,
        elementType: BinaryTagType<T>,
        func: (T) -> BinaryTag?
    ): CompoundBinaryTag {
        val builder = CompoundBinaryTag.builder()
        for ((k, v) in original) {
            if (k == type && v is CompoundBinaryTag) {
                val values = ListBinaryTag.builder(BinaryTagTypes.COMPOUND)
                for (value in v.getList("value", BinaryTagTypes.COMPOUND).map { it as CompoundBinaryTag }) {
                    val elements = CompoundBinaryTag.builder()
                    for (e in value.getCompound("element")) {
                        if (e.key == element && e.value.type() == elementType) {
                            (e.value as? T)?.let(func)?.also { elements.put(e.key, it) }
                        } else {
                            elements.put(e.key, e.value)
                        }
                    }
                    values.add(CompoundBinaryTag
                        .builder()
                        .putString("name", v.getString("name"))
                        .putInt("id", v.getInt("id"))
                        .put("element", elements.build())
                        .build()
                    )
                }
                builder.put(k, CompoundBinaryTag
                    .builder()
                    .putString("type", k)
                    .put("value", values.build())
                    .build()
                )
            } else {
                builder.put(k, v)
            }
        }
        return builder.build()
    }
}