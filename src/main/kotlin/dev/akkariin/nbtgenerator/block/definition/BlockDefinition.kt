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
        fun createString(name: String) = create(name, JsonElement::getAsString)
        fun createInt(name: String) = create(name, JsonElement::getAsInt)
        fun createDouble(name: String) = create(name, JsonElement::getAsDouble)
        fun createBoolean(name: String) = create(name, JsonElement::getAsBoolean)
        inline fun <reified T : Any> create(name: String, crossinline func: (JsonElement) -> T) = object : BlockDefinition<T> {
            override val name = name
            override fun parse(element: JsonElement) = func(element)
            override fun type() = T::class.java
        }
    }
}
