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

package dev.akkariin.nbtgenerator.components.containers

import com.google.gson.JsonObject
import dev.akkariin.nbtgenerator.components.ItemComponent
import dev.akkariin.nbtgenerator.components.ToStringHolder
import dev.akkariin.nbtgenerator.util.JsonUtil.boolean
import dev.akkariin.nbtgenerator.util.JsonUtil.float
import dev.akkariin.nbtgenerator.util.JsonUtil.int

@Suppress("MemberVisibilityCanBePrivate")
class FoodComponent(private val json: JsonObject) : ItemComponent.Container<FoodComponent> {
    val nutrition = json.int("nutrition")
    val saturation = json.float("saturation")
    val canAlwaysEat = json.takeIf { it.has("can_always_eat") }?.boolean("can_always_eat") ?: false

    override fun value() = this

    override fun originalData() = json

    override fun toStringHolder(expandDefault: Boolean) = ToStringHolder.map(mutableMapOf<String, ToStringHolder>().apply {
        this["nutrition"] = ToStringHolder.singleton(nutrition)
        this["saturation"] = ToStringHolder.singleton(saturation)
        if (expandDefault || canAlwaysEat) this["can_always_eat"] = ToStringHolder.singleton(canAlwaysEat)
    })
}