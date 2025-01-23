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

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import dev.akkariin.nbtgenerator.block.property.*

@Suppress("MemberVisibilityCanBePrivate")
object BlockProperties {
    val face = BlockProperty.create("face", BlockFace::parse)
    val facing = BlockProperty.create("facing", BlockFacing::parse)
    val doorHalf = BlockProperty.create("half", DoorHalf::parse)
    val blockHalf = BlockProperty.create("half", BlockHalf::parse)
    val hinge = BlockProperty.create("hinge", BlockHinge::parse)
    val slabType = BlockProperty.create("type", SlabType::parse)
    val chestType = BlockProperty.create("type", ChestType::parse)
    val pistonType = BlockProperty.create("type", PistonType::parse)
    val axis = BlockProperty.create("axis", BlockAxis::parse)
    val portalAxis = BlockProperty.create("axis", PortalAxis::parse)
    val stairShape = BlockProperty.create("shape", StairShape::parse)
    val railShape = BlockProperty.create("shape", RailShape::parse)
    val variantsRail = BlockProperty.create("shape", VariantsRailShape::parse)
    val thickness = BlockProperty.create("thickness", DripStoneThickness::parse)
    val verticalDirection = BlockProperty.create("vertical_direction", DripStoneVerticalDirection::parse)

    val powered = BlockProperty.createBoolean("powered")
    val open = BlockProperty.createBoolean("open")
    val waterlogged = BlockProperty.createBoolean("waterlogged")
    val inWall = BlockProperty.createBoolean("in_wall")
    val attached = BlockProperty.createBoolean("attached")
    val persistent = BlockProperty.createBoolean("persistent")
    val natural = BlockProperty.createBoolean("natural")
    val snowy = BlockProperty.createBoolean("snowy")

    val east = BlockProperty.createBoolean("east")
    val north = BlockProperty.createBoolean("north")
    val south = BlockProperty.createBoolean("south")
    val west = BlockProperty.createBoolean("west")

    val rotation = BlockProperty.createInt("rotation", 0, 15)
    val distance = BlockProperty.createInt("distance", 1, 7)
    val stage = BlockProperty.createInt("stage", 0, 1)

    fun getProperties(raw: JsonObject): List<BlockProperty<*>> {
        val properties = mutableListOf<BlockProperty<*>>()
        for ((key, array) in raw.asMap().mapValues { (_, value) -> (value as JsonArray).map { value.asString } }) {
            properties.add(when (key) {
                "face" -> face
                "facing" -> facing
                "hinge" -> hinge
                "thickness" -> thickness
                "vertical_direction" -> verticalDirection
                "powered" -> powered
                "waterlogged" -> waterlogged
                "in_wall" -> inWall
                "attached" -> attached
                "persistent" -> persistent
                "natural" -> natural
                "snowy" -> snowy
                "east" -> east
                "north" -> north
                "south" -> south
                "west" -> west
                "open" -> open
                "rotation" -> rotation
                "distance" -> distance
                "stage" -> stage
                "shape" -> when {
                    array.containsAll(StairShape.names) -> stairShape
                    array.containsAll(RailShape.names) -> railShape
                    array.containsAll(VariantsRailShape.names) -> variantsRail
                    else -> throw IllegalArgumentException("Unknown shapes with value: $array")
                }
                "half" -> when {
                    array.containsAll(DoorHalf.names) -> doorHalf
                    array.containsAll(BlockHalf.names) -> blockHalf
                    else -> throw IllegalArgumentException("Unknown half with value: $array")
                }
                "type" -> when {
                    array.containsAll(SlabType.names) -> slabType
                    array.containsAll(ChestType.names) -> chestType
                    array.containsAll(PistonType.names) -> pistonType
                    else -> throw IllegalArgumentException("Unknown type with value: $array")
                }
                "axis" -> when {
                    array.containsAll(BlockAxis.names) -> axis
                    array.containsAll(PortalAxis.names) -> portalAxis
                    else -> throw IllegalArgumentException("Unknown axis with value: $array")
                }
                else -> throw IllegalArgumentException("Unknown property $key")
            })
        }
        return properties
    }
}