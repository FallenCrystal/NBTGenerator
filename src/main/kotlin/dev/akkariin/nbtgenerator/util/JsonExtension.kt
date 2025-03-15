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

package dev.akkariin.nbtgenerator.util

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.google.gson.internal.LazilyParsedNumber

@Suppress("unused", "MemberVisibilityCanBePrivate")
object JsonExtension {

    private fun string0(excepted: String) = "Excepted $excepted but found {0} while getting {1}"

    private fun JsonObject.formatMessage(message: String, key: String) = message
        .replace("{0}", this[key]?.let {
            if (it is JsonPrimitive) "JsonPrimitive (${
                when {
                    it.isString -> "String"
                    it.isBoolean -> "Boolean"
                    it.isNumber -> it.asNumber::class.simpleName
                    else -> "undefined"
                }
            })" else it::class.simpleName
        } ?: "null")
        .replace("{1}", key)

    fun JsonElement.exceptedAsJsonObject(message: String = "Excepted JsonObject but found ${this::class.simpleName}") =
        this as? JsonObject ?: throw IllegalArgumentException(message)

    fun JsonObject.getObject(key: String, message: String = string0("JsonObject")) =
        this[key] as? JsonObject ?: throw IllegalArgumentException(formatMessage(message, key))

    fun JsonObject.array(key: String, message: String = string0("JsonArray")) =
        this[key] as? JsonArray ?: throw IllegalArgumentException(formatMessage(message, key))

    fun JsonObject.string(key: String, message: String = string0("JsonPrimitive (String)")): String =
        getJsonPrimitive(key, JsonPrimitive::isString, JsonPrimitive::getAsString, message)

    fun JsonObject.stringOrNull(key: String, message: String = string0("JsonPrimitive (String)")) =
        this.takeIf { has(key) }?.string(key, string0(message))

    fun JsonObject.boolean(key: String, message: String = string0("JsonPrimitive (Boolean)")) =
        getJsonPrimitive(key, JsonPrimitive::isBoolean, JsonPrimitive::getAsBoolean, message)

    fun JsonObject.number(key: String, message: String = string0("JsonPrimitive (Number)")) =
        this.getNumber<Number>(key, message) { it }

    fun JsonObject.int(key: String, message: String = string0("JsonPrimitive (Int)")) =
        getNumber<Int>(key, message, LazilyParsedNumber::toInt)

    fun JsonObject.double(key: String, message: String = string0("JsonPrimitive (Double)")) =
        getNumber<Double>(key, message, LazilyParsedNumber::toDouble)

    fun JsonObject.long(key: String, message: String = string0("JsonPrimitive (Long)")) =
        getNumber<Long>(key, message, LazilyParsedNumber::toLong)

    private inline fun <reified T : Number> JsonObject.getNumber(key: String, message: String, func: (LazilyParsedNumber) -> T): T {
        val number = getJsonPrimitive(key, JsonPrimitive::isNumber, JsonPrimitive::getAsNumber, message)
        if (number is LazilyParsedNumber) return func(number)
        return number as? T ?: throw IllegalArgumentException(formatMessage(message, key))
    }

    private inline fun <reified T : Any> JsonObject.getJsonPrimitive(
        key: String,
        crossinline predicate: (JsonPrimitive) -> Boolean,
        crossinline func: (JsonPrimitive) -> T,
        message: String
    ): T {
        val message0 by lazy { formatMessage(message, key) }
        val primitive = this[key] as? JsonPrimitive ?: throw IllegalArgumentException(message0)
        if (!predicate(primitive)) throw IllegalArgumentException(message0)
        return func(primitive)
    }
}