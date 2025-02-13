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

package dev.akkariin.nbtgenerator.util

@Suppress("unused")
object PrimitiveTypes {
    val byte = Byte::class.javaPrimitiveType!!
    val short = Short::class.javaPrimitiveType!!
    val int = Int::class.javaPrimitiveType!!
    val long = Long::class.javaPrimitiveType!!
    val float = Float::class.javaPrimitiveType!!
    val double = Double::class.javaPrimitiveType!!
    val char = Char::class.javaPrimitiveType!!
    val boolean = Boolean::class.javaPrimitiveType!!
}