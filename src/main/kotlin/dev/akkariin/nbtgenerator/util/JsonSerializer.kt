/*
 * Copyright (C) 2024 FallenCrystal / NBTGenerator Contributors
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
import net.kyori.adventure.nbt.*

object JsonSerializer {
    // Reference https://github.com/jonesdevelopment/sonar/blob/main/common/src/main/java/xyz/jonesdev/sonar/common/util/ComponentHolder.java
    fun serialize(json: JsonElement): BinaryTag {
        when (json) {
            is JsonPrimitive -> return when {
                json.isNumber -> when (json.asNumber) {
                    is Byte -> ByteBinaryTag.byteBinaryTag(json.asNumber as Byte)
                    is Short -> ShortBinaryTag.shortBinaryTag(json.asNumber as Short)
                    is Int -> IntBinaryTag.intBinaryTag(json.asNumber as Int)
                    is Long -> LongBinaryTag.longBinaryTag(json.asNumber as Long)
                    is Float -> FloatBinaryTag.floatBinaryTag(json.asNumber as Float)
                    is Double -> DoubleBinaryTag.doubleBinaryTag(json.asNumber as Double)
                    is LazilyParsedNumber -> IntBinaryTag.intBinaryTag(json.asNumber.toInt())
                    else -> throw IllegalArgumentException("Unknown number type ${json.asNumber}")
                }
                json.isString -> StringBinaryTag.stringBinaryTag(json.asString)
                json.isBoolean -> ByteBinaryTag.byteBinaryTag(if (json.asBoolean) 1 else 0)
                else -> throw IllegalArgumentException("Unknown json primitive $json")
            }
            is JsonObject -> return CompoundBinaryTag.builder().apply {
                json.entrySet().forEach { (key, value) -> put(key, serialize(value)) }
            }.build()
            is JsonArray -> {
                val array = json.asJsonArray
                if (array.isEmpty) return ListBinaryTag.empty()
                val tagItems = mutableListOf<BinaryTag>()
                var listType: BinaryTagType<out BinaryTag>? = null
                array.forEach {
                    val tag = serialize(it)
                    tagItems.add(tag)

                    if (listType == null)
                        listType = tag.type()
                    else if (listType != tag.type()) {
                        listType = BinaryTagTypes.COMPOUND
                    }
                }
                when (listType!!.id()) {
                    BinaryTagTypes.BYTE.id() -> return ByteArrayBinaryTag.byteArrayBinaryTag(*array.toArray(JsonElement::getAsByte).toByteArray())
                    BinaryTagTypes.INT.id() -> return IntArrayBinaryTag.intArrayBinaryTag(*array.toArray(JsonElement::getAsInt).toIntArray())
                    BinaryTagTypes.LONG.id() -> return LongArrayBinaryTag.longArrayBinaryTag(*array.toArray(JsonElement::getAsLong).toLongArray())
                    BinaryTagTypes.COMPOUND.id() -> tagItems.replaceAll {
                        if (it.type() == BinaryTagTypes.COMPOUND) it else CompoundBinaryTag.from(mapOf("" to it))
                    }
                }
                return ListBinaryTag.from(tagItems)
            }
        }
        return EndBinaryTag.endBinaryTag()
    }

    private inline fun <reified T : Number> JsonArray.toArray(converter: (JsonElement) -> T)
            = Array(this.size()) { index -> converter.invoke(this[index]) }
}