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

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import dev.akkariin.nbtgenerator.components.ItemComponent
import dev.akkariin.nbtgenerator.components.ToStringHolder
import dev.akkariin.nbtgenerator.components.containers.ConsumableComponent.ConsumeEffect.*
import dev.akkariin.nbtgenerator.components.data.PotionEffect
import dev.akkariin.nbtgenerator.components.enums.UseAnimation
import dev.akkariin.nbtgenerator.util.JsonUtil.array
import dev.akkariin.nbtgenerator.util.JsonUtil.exceptedAsJsonObject
import dev.akkariin.nbtgenerator.util.JsonUtil.float
import dev.akkariin.nbtgenerator.util.JsonUtil.string

@Suppress("MemberVisibilityCanBePrivate")
class ConsumableComponent(private val json: JsonObject) : ItemComponent.Container<ConsumableComponent> {
    val animation = readOptional("animation") { UseAnimation.valueOf(it.asString.uppercase()) } ?: UseAnimation.NONE
    val hasConsumeParticles = readOptional("has_consume_particles", JsonElement::getAsBoolean) ?: true
    val sound = readOptional("sound", JsonElement::getAsString) ?: "entity.generic.eat"
    val consumeSeconds = readOptional("consume_seconds", JsonElement::getAsFloat) ?: 1.6f
    val onConsumeEffects = readOptional("on_consume_effects", JsonElement::getAsJsonArray)
        ?.map { it as JsonObject }
        ?.map {
            val type = ConsumeEffectType.map[it.string("type")]
                ?: throw IllegalArgumentException("Undefined ConsumeEffectType: ${it.string("type")}")
            type.read(it)
        }

    private inline fun <reified T> readOptional(key: String, crossinline reader: (JsonElement) -> T): T? {
        if (!json.has(key)) return null
        return reader(json[key]!!)
    }

    override fun value() = this
    override fun originalData() = json
    override fun toStringHolder(expandDefault: Boolean): ToStringHolder {
        val map = mutableMapOf<String, ToStringHolder>()
        if (expandDefault) {
            map["animation"] = ToStringHolder.singleton(animation)
            map["has_consume_particles"] = ToStringHolder.singleton(hasConsumeParticles)
            map["sound"] = ToStringHolder.singleton(sound)
            map["consume_seconds"] = ToStringHolder.singleton(consumeSeconds)
        } else {
            if (animation != UseAnimation.NONE) map["animation"] = ToStringHolder.singleton(animation)
            if (!hasConsumeParticles) map["has_consume_particles"] = ToStringHolder.singleton(false)
            if (sound != "entity.generic.eat") map["sound"] = ToStringHolder.singleton(sound)
            if (consumeSeconds != 1.6f) map["consume_seconds"] = ToStringHolder.singleton(consumeSeconds)
        }
        if (onConsumeEffects != null) {
            val list = mutableListOf<ToStringHolder>()
            for (it in onConsumeEffects) {
                val m = mutableMapOf<String, ToStringHolder>()
                m["type"] = ToStringHolder.singleton(it.type().name())
                it.acceptStringHolder(m, expandDefault)
                list.add(ToStringHolder.map(m))
            }
        }
        return ToStringHolder.map(map)
    }

    interface ConsumeEffect<T : ConsumeEffect<T>> {
        fun type(): ConsumeEffectType<T>
        fun acceptStringHolder(map: MutableMap<String, ToStringHolder>, expandDefault: Boolean)

        class ApplyEffects(obj: JsonObject) : ConsumeEffect<ApplyEffects> {
            val probability = if (obj.has("probability")) obj.float("probability") else 1f
            val effects = obj.array("effects").map { PotionEffect(it.exceptedAsJsonObject()) }
            override fun type() = ConsumeEffectType.applyEffects
            override fun acceptStringHolder(map: MutableMap<String, ToStringHolder>, expandDefault: Boolean) {
                if (expandDefault || probability != 1f) map["probability"] = ToStringHolder.singleton(probability)
                if (expandDefault || effects.isNotEmpty()) {
                    map["effects"] = ToStringHolder.list(effects) { it.toStringHolder(expandDefault) }
                }
            }
        }

        class RemoveEffects(obj: JsonObject) : ConsumeEffect<RemoveEffects> {
            val effects = obj.string("effects")
            override fun type() = ConsumeEffectType.removeEffects
            override fun acceptStringHolder(map: MutableMap<String, ToStringHolder>, expandDefault: Boolean) {
                map["effects"] = ToStringHolder.singleton(effects)
            }
        }

        class ClearAllEffects : ConsumeEffect<ClearAllEffects> {
            override fun type() = ConsumeEffectType.clearAllEffects
            override fun acceptStringHolder(map: MutableMap<String, ToStringHolder>, expandDefault: Boolean) {}
        }

        class TeleportRandomly(obj: JsonObject) : ConsumeEffect<TeleportRandomly> {
            val diameter = obj.float("diameter")
            override fun type() = ConsumeEffectType.teleportRandomly
            override fun acceptStringHolder(map: MutableMap<String, ToStringHolder>, expandDefault: Boolean) {
                map["diameter"] = ToStringHolder.singleton(diameter)
            }
        }

        class PlaySound(obj: JsonObject) : ConsumeEffect<PlaySound> {
            val sound = obj.string("sound")
            override fun type() = ConsumeEffectType.playSound
            override fun acceptStringHolder(map: MutableMap<String, ToStringHolder>, expandDefault: Boolean) {
                map["sound"] = ToStringHolder.singleton(sound)
            }
        }
    }

    interface ConsumeEffectType<T : ConsumeEffect<T>> {
        fun name(): String
        fun read(obj: JsonObject): T
        companion object {
            val map = mutableMapOf<String, ConsumeEffectType<*>>()
            fun <T : ConsumeEffect<T>> create(name: String, read: (JsonObject) -> T) = object : ConsumeEffectType<T> {
                override fun name() = "minecraft:$name"
                override fun read(obj: JsonObject) = read(obj)
            }.also { map[it.name()] = it }

            val applyEffects = create("apply_effects", ::ApplyEffects)
            val removeEffects = create("remove_effects", ::RemoveEffects)
            val clearAllEffects = create("clear_all_effects") { ClearAllEffects() }
            val teleportRandomly = create("teleport_randomly", ::TeleportRandomly)
            val playSound = create("play_sound", ::PlaySound)
        }
    }
}