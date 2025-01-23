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

enum class NoteBlockInstrument {
    HARP,
    @Suppress("SpellCheckingInspection")
    BASEDRUM,
    SNARE,
    HAT,
    BASS,
    FLUTE,
    BELL,
    GUITAR,
    CHIME,
    XYLOPHONE,
    IRON_XYLOPHONE,
    COW_BELL,
    DIDGERIDOO,
    BIT,
    BANJO,
    @Suppress("SpellCheckingInspection")
    PLING,
    ZOMBIE,
    SKELETON,
    CREEPER,
    DRAGON,
    WITHER_SKELETON,
    @Suppress("SpellCheckingInspection")
    PIGLIN,
    CUSTOM_HEAD;

    companion object {
        fun parse(input: String) = when (input) {
            "harp" -> HARP
            "basedrum" -> BASEDRUM
            "snare" -> SNARE
            "hat" -> HAT
            "bass" -> BASS
            "flute" -> FLUTE
            "bell" -> BELL
            "guitar" -> GUITAR
            "chime" -> CHIME
            "xylophone" -> XYLOPHONE
            "iron_xylophone" -> IRON_XYLOPHONE
            "cow_bell" -> COW_BELL
            "didgeridoo" -> DIDGERIDOO
            "bit" -> BIT
            "banjo" -> BANJO
            "pling" -> PLING
            "zombie" -> ZOMBIE
            "skeleton" -> SKELETON
            "creeper" -> CREEPER
            "dragon" -> DRAGON
            "wither_skeleton" -> WITHER_SKELETON
            "piglin" -> PIGLIN
            "custom_head" -> CUSTOM_HEAD
            else -> throw IllegalArgumentException("Unknown note block instrument: $input")
        }
    }
}