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

enum class VariantsRailShape : IRailShape {
    NORTH_SOUTH,
    EAST_WEST,
    ASCENDING_EAST,
    ASCENDING_WEST,
    ASCENDING_NORTH,
    ASCENDING_SOUTH;

    override fun getRailShape() = map[this]!!

    companion object {
        val names = entries.map { it.name.lowercase() }

        private val map = mapOf(
            NORTH_SOUTH to RailShape.NORTH_SOUTH,
            EAST_WEST to RailShape.EAST_WEST,
            ASCENDING_EAST to RailShape.ASCENDING_EAST,
            ASCENDING_WEST to RailShape.ASCENDING_WEST,
            ASCENDING_NORTH to RailShape.ASCENDING_NORTH,
            ASCENDING_SOUTH to RailShape.ASCENDING_SOUTH
        )

        fun parse(input: String) = when (input) {
            "north_south" -> NORTH_SOUTH
            "east_south" -> EAST_WEST
            "ascending_east" -> ASCENDING_EAST
            "ascending_west" -> ASCENDING_WEST
            "ascending_north" -> ASCENDING_NORTH
            "ascending_south" -> ASCENDING_SOUTH
            else -> throw IllegalArgumentException("Unknown variants rail shape: $input")
        }
    }
}