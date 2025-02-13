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

package dev.akkariin.nbtgenerator.components

import dev.akkariin.nbtgenerator.util.PrimitiveTypes
import java.util.*

interface ToStringHolder {
    class SingletonHolder(val type: Class<*>, val value: String) : ToStringHolder
    class MapHolder(val values: Map<String, ToStringHolder>) : ToStringHolder
    class ListHolder(val value: List<ToStringHolder>) : ToStringHolder

    companion object {
        fun singleton(type: Class<*>, value: String) = SingletonHolder(type, value)
        fun singleton(value: Int) = SingletonHolder(PrimitiveTypes.int, value.toString())
        fun singleton(value: Float) = SingletonHolder(PrimitiveTypes.float, value.toString())
        fun singleton(value: Boolean) = SingletonHolder(PrimitiveTypes.boolean, value.toString())
        fun singleton(value: String) = SingletonHolder(String::class.java, value)
        fun singleton(value: Enum<*>) = SingletonHolder(value::class.java, value.name.lowercase())
        fun singleton(value: Any) = SingletonHolder(value::class.java, value.toString())
        fun map(map: Map<String, ToStringHolder>) = MapHolder(map.toMap())
        fun <T> list(collection: Collection<T>, map: (T) -> ToStringHolder) = if (collection.isEmpty()) emptyList() else ListHolder(collection.map(map))
        fun emptyList() = ListHolder(Collections.emptyList())
    }
}