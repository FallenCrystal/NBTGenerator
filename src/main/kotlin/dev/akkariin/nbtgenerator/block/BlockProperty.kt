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

package dev.akkariin.nbtgenerator.block

interface BlockProperty<T> {
    fun name(): String
    fun parse(input: String): T

    companion object {
        fun <T : Enum<T>> create(name: String, parse: (String) -> T) = object : BlockProperty<T> {
            override fun name() = name
            override fun parse(input: String) = parse(input)
        }

        fun createBoolean(name: String) = object : BlockProperty<Boolean> {
            override fun name() = name
            override fun parse(input: String) = input.toBooleanStrict()
        }

        fun createInt(name: String, min: Int, max: Int) = object : BlockProperty<Int> {
            override fun name() = name
            override fun parse(input: String): Int {
                val value = input.toInt()
                require(value in min..max) { "$input must be in $min to $max" }
                return value
            }
        }
    }
}