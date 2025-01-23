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

enum class RailShape {
    NORTH_SOUTH,
    EAST_WEST,
    ASCENDING_EAST,
    ASCENDING_WEST,
    ASCENDING_NORTH,
    ASCENDING_SOUTH,
    SOUTH_EAST,
    SOUTH_WEST,
    NORTH_WEST,
    NORTH_EAST;

    companion object {
        val names = entries.map { it.name.lowercase() }
        val variantNames = arrayOf(
            NORTH_SOUTH,
            EAST_WEST,
            ASCENDING_EAST,
            ASCENDING_WEST,
            ASCENDING_NORTH,
            ASCENDING_SOUTH
        ).map { it.name.lowercase() }

        fun parse(input: String) = when (input) {
            "north_south" -> NORTH_SOUTH
            "east_west" -> EAST_WEST
            "ascending_east" -> ASCENDING_EAST
            "ascending_west" -> ASCENDING_WEST
            "ascending_north" -> ASCENDING_NORTH
            "ascending_south" -> ASCENDING_SOUTH
            "south_east" -> SOUTH_EAST
            "south_west" -> SOUTH_WEST
            "north_west" -> NORTH_WEST
            "north_east" -> NORTH_EAST
            else -> throw IllegalArgumentException("Unknown rail shape: $input")
        }
    }
}