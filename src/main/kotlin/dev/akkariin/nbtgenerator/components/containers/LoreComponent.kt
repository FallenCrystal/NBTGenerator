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

import com.google.gson.JsonArray
import dev.akkariin.nbtgenerator.components.ItemComponent
import dev.akkariin.nbtgenerator.components.ToStringHolder
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer

@Suppress("MemberVisibilityCanBePrivate")
class LoreComponent(private val json: JsonArray) : ItemComponent.Container<LoreComponent> {
    val lore = json.map(GsonComponentSerializer.gson()::deserializeFromTree)

    override fun value() = this
    override fun originalData() = json
    override fun toStringHolder(expandDefault: Boolean) = ToStringHolder.list(lore) {
        ToStringHolder.singleton(MiniMessage.miniMessage()::serialize)
    }
}