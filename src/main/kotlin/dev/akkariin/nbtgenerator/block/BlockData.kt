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

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import dev.akkariin.nbtgenerator.block.definition.BlockDefinition
import dev.akkariin.nbtgenerator.block.definition.BlockDefinitions
import dev.akkariin.nbtgenerator.block.property.BlockProperties
import org.fusesource.jansi.Ansi
import java.util.*

@Suppress("unused", "MemberVisibilityCanBePrivate")
class BlockData(
    val name: String,
    private val json: JsonObject
) {
    val properties = (json
        .takeIf { it.has("properties") }
        ?.getAsJsonObject("properties")
        ?.let(BlockProperties::getProperties)
        ?: emptyList()).associateBy { it.name() }

    val blockStates = parseBlockState()
    val defaultBlockState = blockStates.find(BlockState::default) ?: throw IllegalArgumentException("Cannot found default block state")

    val definitions = json
        .getAsJsonObject("definition")
        .let(JsonObject::asMap)
        .mapNotNull { (key, value) -> BlockDefinitions.map[key]?.let { it to it.parse(value) } ?: run {
            println(Ansi.ansi().fg(Ansi.Color.YELLOW).a("Unknown definition $key with value $value").fg(Ansi.Color.DEFAULT))
            null
        } }
        .toMap()

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getDefinition(key: BlockDefinition<T>): Optional<T> {
        return Optional.ofNullable(definitions[key] as? T)
    }

    private fun parseBlockState(): List<BlockState> {
        val empty = properties.isEmpty()
        val list by lazy { mutableListOf<BlockState>() }
        val array = this.json.getAsJsonArray("states").map(JsonElement::getAsJsonObject)
        for (it in array) {
            if (empty) {
                require(it.has("default") && it.get("default").asBoolean) {
                    "Required default is true on 1st element when properties is empty."
                }
                require(!it.has("properties")) {
                    "Properties is empty. But found field in state."
                }
                return Collections.singletonList(BlockState(name, emptyList(), it.get("id").asInt, true))
            }

            val properties = it.getAsJsonObject("properties").asMap().entries.map { (key, value) ->
                (properties[key] ?: throw IllegalArgumentException("Unknown properties $key")).parseAsData(value)
            }
            val missing = this.properties.values.toMutableSet().apply { properties.forEach { remove(it.property) } }
            require(missing.isEmpty()) { "Missing field(s) in state(s): $missing" }
            list.add(BlockState(name, properties, it.get("id").asInt, if (it.has("default")) it.get("default").asBoolean else false))
        }
        return list
    }
}