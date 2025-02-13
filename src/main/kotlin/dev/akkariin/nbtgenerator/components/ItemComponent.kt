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

package dev.akkariin.nbtgenerator.components

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer

interface ItemComponent<T : ItemComponent.Container<*>> {
    fun name(): String
    fun read(element: JsonElement): T

    interface Container<T> {
        fun value(): T
        fun originalData(): JsonElement
        fun toStringHolder(expandDefault: Boolean): ToStringHolder
    }

    abstract class PrimitiveContainer<T : Any>(private val primitive: JsonPrimitive, private val value: T) : Container<T> {
        override fun value() = value
        open fun valueAsString() = value.toString()
        override fun originalData() = primitive
        override fun toStringHolder(expandDefault: Boolean) = ToStringHolder.singleton(value::class.java, valueAsString())
    }

    class IntContainer(primitive: JsonPrimitive) : PrimitiveContainer<Int>(primitive, primitive.asInt)

    class StringContainer(primitive: JsonPrimitive) : PrimitiveContainer<String>(primitive, primitive.asString)

    class EnumContainer<T : Enum<T>>(private val primitive: JsonPrimitive, private val value: T) : Container<T> {
        override fun value() = value
        override fun originalData() = primitive
        override fun toStringHolder(expandDefault: Boolean) = ToStringHolder.singleton(value::class.java, value.name.lowercase())
    }

    class TextComponentContainer(private val element: JsonElement) : Container<Component> {
        private val component = GsonComponentSerializer.gson().deserialize(element.asString)
        override fun value() = component
        override fun originalData() = element
        override fun toStringHolder(expandDefault: Boolean) = ToStringHolder.singleton(Component::class.java, MiniMessage.miniMessage().serialize(component))
    }

    class SingletonFieldContainer<T : Any>(private val obj: JsonObject, private val name: String, reader: (JsonElement) -> T) : Container<T> {
        private val value = reader(obj["name"] ?: throw IllegalArgumentException("Cannot found field $name from JsonObject"))
        override fun value() = value
        override fun originalData() = obj
        override fun toStringHolder(expandDefault: Boolean) = ToStringHolder.map(mapOf(
            name to ToStringHolder.singleton(value)
        ))
    }

    companion object {
        fun <T : Container<*>> create(name: String, read: (JsonElement) -> T) = object : ItemComponent<T> {
            override fun name() = name
            override fun read(element: JsonElement) = read(element)
        }
    }
}