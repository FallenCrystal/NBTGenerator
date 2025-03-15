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
import com.google.gson.JsonElement
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
    val thickness = BlockProperty.create("thickness", DripStoneThickness::parse) // Drip stone
    val verticalDirection = BlockProperty.create("vertical_direction", DripStoneVerticalDirection::parse) // Drip stone
    val leaves = BlockProperty.create("leaves", BambooLeaves::parse) // Bamboo
    val attachment = BlockProperty.create("attachment", BellAttachment::parse) // Bell
    val tilt = BlockProperty.create("tilt", DripLeafTilt::parse) // Drip Leaf
    val part = BlockProperty.create("part", BedPart::parse) // Bed
    val sculkSensorPhase = BlockProperty.create("sculk_sensor_phase", SculkSensorPhase::parse)
    val comparatorMode = BlockProperty.create("mode", ComparatorMode::parse)
    val structureBlockMode = BlockProperty.create("mode", StructureBlockMode::parse)
    val testBlockMode = BlockProperty.create("mode", TestBlockMode::parse)
    val orientation = BlockProperty.create("orientation", BlockOrientation::parse)
    val instrument = BlockProperty.create("instrument", NoteBlockInstrument::parse)
    val trialSpawnerState = BlockProperty.create("trial_spawner_state", TrialSpawnerState::parse) // Trial Spawner
    val vaultState = BlockProperty.create("vault_state", VaultState::parse) // Vault
    val creakingHeartState = BlockProperty.create("creaking_heart_state", CreakingHeartState::parse) // Creaking Heart

    // Creaking heart
    val creaking = BlockProperty.create("creaking", Creaking.Enum::parse) // Legacy
    val active = BlockProperty.createBoolean("active") // Modern

    val powered = BlockProperty.createBoolean("powered")
    val open = BlockProperty.createBoolean("open")
    val waterlogged = BlockProperty.createBoolean("waterlogged")
    val inWall = BlockProperty.createBoolean("in_wall")
    val attached = BlockProperty.createBoolean("attached")
    val persistent = BlockProperty.createBoolean("persistent")
    val natural = BlockProperty.createBoolean("natural")
    val snowy = BlockProperty.createBoolean("snowy")
    val occupied = BlockProperty.createBoolean("occupied") // Bed
    val lit = BlockProperty.createBoolean("lit")

    // Brewing Stand
    val hasBottle0 = BlockProperty.createBoolean("has_bottle_0")
    val hasBottle1 = BlockProperty.createBoolean("has_bottle_1")
    val hasBottle2 = BlockProperty.createBoolean("has_bottle_2")

    val drag = BlockProperty.createBoolean("drag") // Bubble Column (In water?)
    val signalFire = BlockProperty.createBoolean("signal_fire")
    val berries = BlockProperty.createBoolean("berries")
    val conditional = BlockProperty.createBoolean("conditional") // Command block
    val crafting = BlockProperty.createBoolean("crafting") // Crafter
    val inverted = BlockProperty.createBoolean("inverted") // Daylight Detector
    val cracked = BlockProperty.createBoolean("cracked") // Decorated Pot
    val triggered = BlockProperty.createBoolean("triggered")
    val eye = BlockProperty.createBoolean("eye") // End Portal Frame
    val enabled = BlockProperty.createBoolean("enabled") // Hopper
    val hasRecord = BlockProperty.createBoolean("has_record") // Jukebox
    val hanging = BlockProperty.createBoolean("hanging")
    val hasBook = BlockProperty.createBoolean("has_book") // Lectern
    val tip = BlockProperty.createBoolean("tip") // Pale Hanging Moss
    val bottom = BlockProperty.createBoolean("bottom")
    val extended = BlockProperty.createBoolean("extended") // Piston
    val short = BlockProperty.createBoolean("short") // Piston Head
    val bloom = BlockProperty.createBoolean("bloom") // Sculk Catalyst
    val canSummon = BlockProperty.createBoolean("can_summon") // Sculk Shrieker
    val unstable = BlockProperty.createBoolean("unstable") // TNT
    val ominous = BlockProperty.createBoolean("ominous") // Trial Spawner & Vault
    val disarmed = BlockProperty.createBoolean("disarmed") // Tripwire
    val locked = BlockProperty.createBoolean("locked") // Repeater
    val shrieking = BlockProperty.createBoolean("shrieking") // Sculk Shrieker

    val east = BlockProperty.createBoolean("east")
    val north = BlockProperty.createBoolean("north")
    val south = BlockProperty.createBoolean("south")
    val west = BlockProperty.createBoolean("west")
    val up = BlockProperty.createBoolean("up")
    val down = BlockProperty.createBoolean("down")

    val wallEast = BlockProperty.create("east", WallState::parse)
    val wallNorth = BlockProperty.create("north", WallState::parse)
    val wallSouth = BlockProperty.create("south", WallState::parse)
    val wallWest = BlockProperty.create("west", WallState::parse)

    val redstoneEast = BlockProperty.create("east", RedstoneDirection::parse)
    val redstoneNorth = BlockProperty.create("north", RedstoneDirection::parse)
    val redstoneSouth = BlockProperty.create("south", RedstoneDirection::parse)
    val redstoneWest = BlockProperty.create("west", RedstoneDirection::parse)

    val rotation = BlockProperty.createInt("rotation", 0, 15)
    val distance = BlockProperty.createInt("distance", 1, 7)
    val scaffoldingDistance = BlockProperty.createInt("distance", 0, 7)
    val stage = BlockProperty.createInt("stage", 0, 1)
    val honeyLevel = BlockProperty.createInt("honey_level", 0, 5)
    val candles = BlockProperty.createInt("candles", 1, 4) // Candle Count
    val bites = BlockProperty.createInt("bites", 0, 6) // Cake
    val power = BlockProperty.createInt("power", 0, 15)
    val composterLevel = BlockProperty.createInt("level", 0, 8)
    val cauldronLevel = BlockProperty.createInt("level", 1, 3)
    val level = BlockProperty.createInt("level", 0, 15)
    val moisture = BlockProperty.createInt("moisture", 0, 7) // Farmland
    val flowerAmount = BlockProperty.createInt("flower_amount", 1, 4) // Pink Petals
    val delay = BlockProperty.createInt("delay", 1, 4) // Repeater
    val charges = BlockProperty.createInt("charges", 0, 4) // Respawn Anchor
    val pickles = BlockProperty.createInt("pickles", 1, 4) // Sea Pickle
    val hatch = BlockProperty.createInt("hatch", 0, 2)
    val layers = BlockProperty.createInt("layers", 1, 8) // Snow
    val dusted = BlockProperty.createInt("dusted", 0, 3) // Suspicious Gravel & Sand
    val eggs = BlockProperty.createInt("eggs", 1, 4) // Turtle Egg
    val note = BlockProperty.createInt("note", 0, 24) // Note Block
    val segmentAmount = BlockProperty.createInt("segment_amount", 1, 4)

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

    fun getDirectionState(key: String, value: List<String>) = when {
        value.containsAll(WallState.names) -> when (key) {
            "east" -> wallEast
            "north" -> wallNorth
            "south" -> wallSouth
            "west" -> wallWest
            else -> throw IllegalArgumentException("Unknown key for wall direction: $key")
        }
        value.containsAll(RedstoneDirection.names) -> when (key) {
            "east" -> redstoneEast
            "north" -> redstoneNorth
            "south" -> redstoneSouth
            "west" -> redstoneWest
            else -> throw IllegalArgumentException("Unknown key for redstone direction: $key")
        }
        value.containsAll(listOf("true", "false")) -> when (key) {
            "east" -> east
            "north" -> north
            "south" -> south
            "west" -> west
            else -> throw IllegalArgumentException("Unknown key for direction: $key")
        }
        else -> throw IllegalArgumentException("Unknown key with value: $key - $value")
    }

    private val map = arrayOf(face, facing, hinge, thickness, verticalDirection, leaves,
        attachment, tilt, part, sculkSensorPhase, orientation, instrument, trialSpawnerState, vaultState, creakingHeartState, powered,
        waterlogged, inWall, attached, persistent, natural, snowy, occupied, lit, hasBottle0, hasBottle1, hasBottle2, drag, up, signalFire,
        berries, conditional, crafting, inverted, cracked, triggered, eye, enabled, hasRecord, hanging, hasBook, tip, bottom, extended,
        short, bloom, canSummon, unstable, ominous, disarmed, locked, shrieking, down, open, rotation, stage, honeyLevel, candles,
        bites, power, moisture, flowerAmount, delay, charges, pickles, hatch, layers, dusted, eggs, note, active, creaking,
        segmentAmount).associateBy { it.name() }

    fun getProperties(raw: JsonObject): List<BlockProperty<*>> {
        val properties = mutableListOf<BlockProperty<*>>()
        for ((key, array) in raw.entrySet().associate { (key, value) -> key to (value as JsonArray).map(JsonElement::getAsString) }) {
            val v = map[key]
            if (v != null) {
                properties.add(v)
                continue
            }
            properties.add(when (key) {
                "east", "north", "south", "west" -> getDirectionState(key, array)
                "shape" -> when {
                    array.containsAll(StairShape.names) -> stairShape
                    array.containsAll(RailShape.variantNames) -> railShape // Removed VariantsRailShape
                    array.containsAll(RailShape.names) -> railShape
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
                "mode" -> when {
                    array.containsAll(ComparatorMode.names) -> comparatorMode
                    array.containsAll(StructureBlockMode.names) -> structureBlockMode
                    array.containsAll(TestBlockMode.names) -> testBlockMode
                    else -> throw IllegalArgumentException("Unknown mode with value: $array")
                }
                "level" -> when(array.last().toInt()) {
                    3 -> cauldronLevel
                    8 -> composterLevel
                    15 -> level
                    else -> throw IllegalArgumentException("Unknown block level: $array")
                }
                "distance" -> when (array.first().toInt()) {
                    0 -> scaffoldingDistance
                    1 -> distance
                    else -> throw IllegalArgumentException("Unknown distance with value: $array")
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