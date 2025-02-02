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

@Suppress("MemberVisibilityCanBePrivate")
object BlockDefinitions {
    val type = BlockDefinition.createString("type")
    val blockSetType = BlockDefinition.createString("block_set_type")
    val woodType = BlockDefinition.createString("wood_type")
    val tree = BlockDefinition.createString("tree")
    val fruit = BlockDefinition.createString("fruit")
    val seed = BlockDefinition.createString("seed")
    val stem = BlockDefinition.createString("stem")
    val color = BlockDefinition.createString("color")
    val candle = BlockDefinition.createString("candle")
    val dead = BlockDefinition.createString("dead")
    val feature = BlockDefinition.createString("feature")
    val concrete = BlockDefinition.createString("concrete")
    val weatheringState = BlockDefinition.createString("weathering_state")
    val plant = BlockDefinition.createString("plant")
    val kind = BlockDefinition.createString("kind")
    val growsOn = BlockDefinition.createString("grows_on")
    val host = BlockDefinition.createString("host")
    val potted = BlockDefinition.createString("potted")
    val interactions = BlockDefinition.createString("interactions")
    val precipitation = BlockDefinition.createString("precipitation")
    val attachedStem = BlockDefinition.createString("attached_stem")
    val fallingDustColor = BlockDefinition.createString("falling_dust_color")
    val fluid = BlockDefinition.createString("fluid")
    val particleOptions = BlockDefinition.createString("particle_options")

    val brushCompletedSound = BlockDefinition.createString("brush_completed_sound")
    @Suppress("SpellCheckingInspection") // Yes. This is a typo from Mojang. :P
    // For 1.21 later. Please use brushCompletedSound instead.
    val brushComletedSound = BlockDefinition.createString("brush_comleted_sound")
    val brushSound = BlockDefinition.createString("brush_sound")

    val turnsInto = BlockDefinition.createString("turns_into")
    val hook = BlockDefinition.createString("hook")

    val ticksToStayPressed = BlockDefinition.createInt("ticks_to_stay_pressed")
    val fireDamage = BlockDefinition.createInt("fire_damage")
    val chance = BlockDefinition.createInt("chance")
    val maxWeight = BlockDefinition.createInt("max_weight")

    val aabbOffset = BlockDefinition.createDouble("aabb_offset")
    val height = BlockDefinition.createDouble("height")

    val spawnParticles = BlockDefinition.createBoolean("spawn_particles")
    val automatic = BlockDefinition.createBoolean("automatic")
    val open = BlockDefinition.createBoolean("open")
    val sticky = BlockDefinition.createBoolean("sticky")

    val baseState = object : BlockDefinition<BaseState> {
        override val name = "base_state"
        override fun parse(element: JsonElement) = BaseState(element as JsonObject)
        override fun type() = BaseState::class.java
    }

    val suspiciousStewEffects = BlockDefinition.create("suspicious_stew_effects") {
        SuspiciousStewEffects.List(it.asJsonArray.map { SuspiciousStewEffects(it as JsonObject) })
    }

    val properties = BlockDefinition.create("properties") { PropertiesDefinition() }

    val experience = BlockDefinition.create("experience", ExperienceDefinition::parse)

    val particle = BlockDefinition.create("particle") { ParticleDefinition(it) }

    val map = arrayOf(
        type,
        blockSetType,
        woodType,
        tree,
        fruit,
        seed,
        stem,
        color,
        candle,
        dead,
        feature,
        concrete,
        weatheringState,
        plant,
        kind,
        growsOn,
        host,
        potted,
        interactions,
        precipitation,
        attachedStem,
        fallingDustColor,
        fluid,
        particleOptions,
        brushCompletedSound,
        brushComletedSound,
        brushSound,
        turnsInto,
        hook,
        ticksToStayPressed,
        fireDamage,
        chance,
        maxWeight,
        aabbOffset,
        height,
        spawnParticles,
        automatic,
        open,
        sticky,
        baseState,
        suspiciousStewEffects, // Weird Idea
        properties,
        experience,
        particle
    ).associateBy { it.name }
}