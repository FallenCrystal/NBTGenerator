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

package dev.akkariin.nbtgenerator.args

data class Arg<T>(val name: String, val description: String?,  val defaultValue: T, val func: (String)->T) {
    companion object {
        fun of(name: String, description: String?, defaultValue: String) = Arg(name, description, defaultValue) { it }
        fun of(name: String, description: String?, defaultValue: Boolean) = Arg(name, description, defaultValue) { it.toBooleanStrict() }
        fun of(name: String, description: String?, defaultValue: Int) = Arg(name, description, defaultValue) { it.toInt() }
    }
}
