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

enum class TrialSpawnerState {
    INACTIVE,
    WAITING_FOR_PLAYERS,
    ACTIVE,
    WAITING_FOR_REWARD_EJECTION,
    EJECTING_REWARD,
    COOLDOWN;

    companion object {
        fun parse(input: String) = when (input) {
            "inactive" -> INACTIVE
            "waiting_for_players" -> WAITING_FOR_PLAYERS
            "active" -> ACTIVE
            "waiting_for_reward_ejection" -> WAITING_FOR_REWARD_EJECTION
            "ejecting_reward" -> EJECTING_REWARD
            "cooldown" -> COOLDOWN
            else -> throw IllegalArgumentException("Unknown trial spawner state: $input")
        }
    }
}