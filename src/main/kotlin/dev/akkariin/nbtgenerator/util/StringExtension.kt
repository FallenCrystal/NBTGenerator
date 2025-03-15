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

private val namespaceWithoutPath = Regex("""^[a-z0-9]([a-z0-9_.-]*[a-z0-9])?:[a-z0-9]([a-z0-9_.-]*[a-z0-9])?$""")
private val namespaceWithPath = Regex("""^[a-z0-9]([a-z0-9_.-]*[a-z0-9])?:[a-z0-9]([a-z0-9_.-]*[a-z0-9])?(/[a-z0-9]([a-z0-9_.-]*[a-z0-9])?)*$""")

fun String.checkNamespace(path: Boolean = false) {
    val regex = if (path) namespaceWithPath else namespaceWithoutPath
    require(regex.matches(this)) { "String $this isn't a valid namespace (path: $path)" }
}