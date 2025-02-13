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
import dev.akkariin.nbtgenerator.components.enums.attribute.ModifierSlot
import dev.akkariin.nbtgenerator.components.enums.attribute.Operation
import dev.akkariin.nbtgenerator.util.JsonUtil.array
import dev.akkariin.nbtgenerator.util.JsonUtil.boolean
import dev.akkariin.nbtgenerator.util.JsonUtil.double
import dev.akkariin.nbtgenerator.util.JsonUtil.exceptedAsJsonObject
import dev.akkariin.nbtgenerator.util.JsonUtil.string

@Suppress("MemberVisibilityCanBePrivate")
class AttributeModifiersComponent(private val json: JsonObject) : ItemComponent.Container<AttributeModifiersComponent> {
    val showInTooltip = json.takeIf { it.has("show_in_tooltip") }?.boolean("show_in_tooltip") ?: true
    val modifiers = json.array("modifiers").map { it.exceptedAsJsonObject() }.map(::Modifier)

    override fun value() = this
    override fun originalData() = json
    override fun toStringHolder(expandDefault: Boolean): ToStringHolder {
        if (modifiers.isEmpty() && showInTooltip) {
            return if (expandDefault) EMPTY_HOLDER_EXPAND else EMPTY_HOLDER
        }
        val map = mutableMapOf<String, ToStringHolder>()
        if (expandDefault || !showInTooltip) {
            map["show_in_tooltip"] = ToStringHolder.singleton(showInTooltip)
        }
        map["modifiers"] = ToStringHolder.list(modifiers, Modifier::toStringHolder)
        return ToStringHolder.map(map)
    }

    data class Modifier(
        val type: String,
        val id: String,
        val amount: Double,
        val operation: Operation,
        val slot: ModifierSlot
    ) {
        constructor(json: JsonObject) : this(
            json.string("type"),
            json.string("id"),
            json.double("amount"),
            Operation.valueOf(json.string("operation").uppercase()),
            ModifierSlot.valueOf(json.string("slot").uppercase())
        )

        fun toStringHolder() = ToStringHolder.map(mapOf(
            "type" to ToStringHolder.singleton(type),
            "id" to ToStringHolder.singleton(id),
            "amount" to ToStringHolder.singleton(amount),
            "operation" to ToStringHolder.singleton(operation),
            "slot" to ToStringHolder.singleton(slot)
        ))
    }

    companion object {
        private val EMPTY_HOLDER by lazy { ToStringHolder.map(mapOf(
            "modifiers" to ToStringHolder.emptyList()
        )) }
        private val EMPTY_HOLDER_EXPAND by lazy { ToStringHolder.map(mapOf(
            "modifiers" to ToStringHolder.emptyList(),
            "show_in_tooltip" to ToStringHolder.singleton(true),
        ))}
    }
}