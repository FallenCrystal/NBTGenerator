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

package dev.akkariin.nbtgenerator.block.property

enum class ComparatorMode {
    COMPARE,
    SUBTRACT;

    companion object {
        val names = entries.map { it.name.lowercase() }

        fun parse(input: String) = when (input) {
            "compare" -> COMPARE
            "subtract" -> SUBTRACT
            else -> throw IllegalArgumentException("Unknown comparator mode: $input")
        }
    }
}