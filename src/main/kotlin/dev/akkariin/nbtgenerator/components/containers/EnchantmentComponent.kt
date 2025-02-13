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
import dev.akkariin.nbtgenerator.util.JsonUtil.getObject

@Suppress("MemberVisibilityCanBePrivate")
class EnchantmentComponent(private val json: JsonObject) : ItemComponent.Container<EnchantmentComponent> {
    val showInTooltip = json
        .takeIf { json.has("show_in_tooltip") }
        ?.boolean("show_in_tooltip") ?: true
    val levels = json
        .getObject("levels")
        .asMap()
        .map { (key, value) -> Enchantment(key, value.asInt) }

    data class Enchantment(val name: String, val level: Int)

    override fun value() = this
    override fun originalData() = json

    override fun toStringHolder(expandDefault: Boolean): ToStringHolder {
        if (levels.isEmpty() && showInTooltip) {
            return if (expandDefault) EMPTY_HOLDER_WITH_EXPAND else EMPTY_HOLDER
        }
        val map = mutableMapOf<String, ToStringHolder>()
        if (expandDefault || !showInTooltip) {
            map["show_in_tooltip"] = ToStringHolder.singleton(showInTooltip)
        }
        map["levels"] = ToStringHolder.map(levels.associate { it.name to ToStringHolder.singleton(it.level) })
        return ToStringHolder.map(map.toMap())
    }

    companion object {
        val EMPTY_HOLDER by lazy { ToStringHolder.map(mapOf("levels" to ToStringHolder.emptyList())) }
        val EMPTY_HOLDER_WITH_EXPAND by lazy { ToStringHolder.map(mapOf(
            "show_in_tooltip" to ToStringHolder.singleton(true),
            "levels" to ToStringHolder.emptyList()
        )) }
    }
}