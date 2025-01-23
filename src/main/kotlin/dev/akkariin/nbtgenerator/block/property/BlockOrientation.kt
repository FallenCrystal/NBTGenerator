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

enum class BlockOrientation {
    DOWN_EAST,
    DOWN_NORTH,
    DOWN_SOUTH,
    DOWN_WEST,
    UP_EAST,
    UP_NORTH,
    UP_SOUTH,
    UP_WEST,
    WEST_UP,
    EAST_UP,
    NORTH_UP,
    SOUTH_UP;

    companion object {
        fun parse(input: String) = when (input) {
            "down_east" -> DOWN_EAST
            "down_north" -> DOWN_NORTH
            "down_south" -> DOWN_SOUTH
            "down_west" -> DOWN_WEST
            "up_east" -> UP_EAST
            "up_north" -> UP_NORTH
            "up_south" -> UP_SOUTH
            "up_west" -> UP_WEST
            "west_up" -> WEST_UP
            "east_up" -> EAST_UP
            "north_up" -> NORTH_UP
            "south_up" -> SOUTH_UP
            else -> throw IllegalArgumentException("Unknown block orientation: $input")
        }
    }
}