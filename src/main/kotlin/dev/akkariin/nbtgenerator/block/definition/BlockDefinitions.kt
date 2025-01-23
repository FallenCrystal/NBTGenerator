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

package dev.akkariin.nbtgenerator.block.definition

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.lang.reflect.Modifier

@Suppress("unused")
object BlockDefinitions {
    val type = BlockDefinition.createString("type")
    val blockSetType = BlockDefinition.createString("block_set_type")
    val woodType = BlockDefinition.createString("wood_type")
    val tree = BlockDefinition.createString("tree")
    val fruit = BlockDefinition.createString("fruit")
    val seed = BlockDefinition.createString("seed")
    val stem = BlockDefinition.createString("stem")
    val ticksToStayPressed = BlockDefinition.createInt("ticks_to_stay_pressed")

    val aabbOffset = BlockDefinition.createDouble("aabb_offset")
    val height = BlockDefinition.createDouble("height")

    val baseState = object : BlockDefinition<BaseState> {
        override val name = "base_state"
        override fun parse(element: JsonElement) = BaseState(element as JsonObject)
        override fun type() = BaseState::class.java
    }

    val suspiciousStewEffects = object : BlockDefinition<SuspiciousStewEffects> {
        override val name = "suspicious_stew_effects"
        override fun parse(element: JsonElement) = SuspiciousStewEffects(element as JsonObject)
        override fun type() = SuspiciousStewEffects::class.java
    }

    val map by lazy { BlockDefinitions::class
        .java
        .fields
        .filter { BlockDefinition::class.java.isAssignableFrom(it.type) }
        .map { (it.get(if (Modifier.isStatic(it.modifiers)) null else this)) as BlockDefinition<*> }
        .associateBy { it.name }
    }
}