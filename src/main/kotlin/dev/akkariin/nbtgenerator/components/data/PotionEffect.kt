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

package dev.akkariin.nbtgenerator.components.data

import com.google.gson.JsonObject
import dev.akkariin.nbtgenerator.components.ToStringHolder
import dev.akkariin.nbtgenerator.components.ToStringHolder.Companion.singleton
import dev.akkariin.nbtgenerator.util.JsonUtil.boolean
import dev.akkariin.nbtgenerator.util.JsonUtil.int
import dev.akkariin.nbtgenerator.util.JsonUtil.string

data class PotionEffect(
    val effect: String,
    val duration: Int = 0,
    val amplifier: Int = 0,
    val ambient: Boolean = false,
    val showParticles: Boolean = true,
    val showIcon: Boolean = true,
) {
    constructor(obj: JsonObject) : this(
        obj.string("id"),
        if (obj.has("duration")) obj.int("duration") else 0,
        if (obj.has("amplifier")) obj.int("amplifier") else 0,
        if (obj.has("ambient")) obj.boolean("ambient") else false,
        if (obj.has("show_particles")) obj.boolean("show_particles") else true,
        if (obj.has("show_icon")) obj.boolean("show_icon") else true
    )

    fun toStringHolder(expandDefault: Boolean): ToStringHolder.MapHolder {
        val map = mutableMapOf<String, ToStringHolder>()
        map["effect"] = singleton(effect)
        if (expandDefault) {
            map["duration"] = singleton(duration)
            map["amplifier"] = singleton(amplifier)
            map["ambient"] = singleton(ambient)
            map["show_particles"] = singleton(showParticles)
            map["show_icon"] = singleton(showIcon)
        } else {
            if (duration != 0) map["duration"] = singleton(duration)
            if (amplifier != 0) map["amplifier"] = singleton(amplifier)
            if (ambient) map["ambient"] = singleton(true)
            if (!showParticles) map["show_particles"] = singleton(false)
            if (!showIcon) map["show_icon"] = singleton(false)
        }
        return ToStringHolder.map(map)
    }
}