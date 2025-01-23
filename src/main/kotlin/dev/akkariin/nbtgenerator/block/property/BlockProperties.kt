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

import com.google.gson.JsonArray
import com.google.gson.JsonObject

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
    val leaves = BlockProperty.create("leaves", BambooLeaves::parse)

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

    val wallEast = BlockProperty.create("east", WallState::parse)
    val wallNorth = BlockProperty.create("north", WallState::parse)
    val wallSouth = BlockProperty.create("south", WallState::parse)
    val wallWest = BlockProperty.create("west", WallState::parse)
    val wallUp = BlockProperty.createBoolean("up")

    val rotation = BlockProperty.createInt("rotation", 0, 15)
    val distance = BlockProperty.createInt("distance", 1, 7)
    val stage = BlockProperty.createInt("stage", 0, 1)

    // Chiseled Bookshelf
    private val slotOccupied = arrayOf(0, 1, 2, 3, 4, 5).map {
        BlockProperty.createBoolean("slot_${it}_occupied")
    }

    fun getChiseledBookshelfSlot(slot: Int): BlockProperty<Boolean> {
        require(slot in 0..5) { "Slot must be between 0 and 5" }
        return slotOccupied[slot]
    }

    private val ageMap = mutableMapOf<Int, BlockProperty<Int>>()

    fun getAgeProperty(maxAge: Int) = ageMap.computeIfAbsent(maxAge) { BlockProperty.createInt("age", 0, it) }

    fun getDirectionState(key: String, value: List<String>): BlockProperty<*> {
        val b = when {
            value.containsAll(WallState.names) -> false
            value.containsAll(listOf("true", "false")) -> true
            else -> throw IllegalArgumentException("Unknown direction state: $value")
        }
        return when (key) {
            "east" -> if (b) east else wallEast
            "north" -> if (b) north else wallNorth
            "south" -> if (b) south else wallSouth
            "west" -> if (b) west else wallWest
            else -> throw IllegalArgumentException("Unknown key for direction: $key")
        }
    }

    fun getProperties(raw: JsonObject): List<BlockProperty<*>> {
        val properties = mutableListOf<BlockProperty<*>>()
        for ((key, array) in raw.asMap().mapValues { (_, value) -> (value as JsonArray).map { value.asString } }) {
            properties.add(when (key) {
                "face" -> face
                "facing" -> facing
                "hinge" -> hinge
                "thickness" -> thickness
                "vertical_direction" -> verticalDirection
                "leaves" -> leaves
                "powered" -> powered
                "waterlogged" -> waterlogged
                "in_wall" -> inWall
                "attached" -> attached
                "persistent" -> persistent
                "natural" -> natural
                "snowy" -> snowy
                "east", "north", "south", "west" -> getDirectionState(key, array)
                "up" -> wallUp
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
                "age" -> getAgeProperty(array.last().toInt())
                "slot_0_occupied" -> getChiseledBookshelfSlot(0)
                "slot_1_occupied" -> getChiseledBookshelfSlot(1)
                "slot_2_occupied" -> getChiseledBookshelfSlot(2)
                "slot_3_occupied" -> getChiseledBookshelfSlot(3)
                "slot_4_occupied" -> getChiseledBookshelfSlot(4)
                "slot_5_occupied" -> getChiseledBookshelfSlot(5)
                else -> throw IllegalArgumentException("Unknown property $key")
            })
        }
        return properties
    }
}