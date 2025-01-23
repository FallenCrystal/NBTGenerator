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
package dev.akkariin.nbtgenerator.block.definition

import com.google.gson.JsonElement

interface BlockDefinition<T : Any> {
    val name: String
    fun parse(element: JsonElement): T
    fun type(): Class<T>

    companion object {
        fun createString(name: String) = object : BlockDefinition<String> {
            override val name = name
            override fun parse(element: JsonElement) = element.asString
            override fun type() = String::class.java
        }

        fun createInt(name: String) = object : BlockDefinition<Int> {
            override val name = name
            override fun parse(element: JsonElement) = element.asInt
            override fun type() = Int::class.java
        }

        fun createDouble(name: String) = object : BlockDefinition<Double> {
            override val name = name
            override fun parse(element: JsonElement) = element.asDouble
            override fun type() = Double::class.java
        }
    }
}
