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
import net.kyori.adventure.nbt.*

private typealias Tag = BinaryTag
private typealias ByteTag = ByteBinaryTag
private typealias ShortTag = ShortBinaryTag
private typealias IntTag = IntBinaryTag
private typealias LongTag = LongBinaryTag
private typealias FloatTag = FloatBinaryTag
private typealias DoubleTag = DoubleBinaryTag
private typealias StringTag = StringBinaryTag
private typealias ByteArrayTag = ByteArrayBinaryTag
private typealias IntArrayTag = IntArrayBinaryTag
private typealias LongArrayTag = LongArrayBinaryTag
private typealias CompoundTag = CompoundBinaryTag
private typealias ListTag = ListBinaryTag
private typealias TagTypes = BinaryTagTypes
private typealias TagType<T> = BinaryTagType<T>

@Suppress("MemberVisibilityCanBePrivate", "unused")
object NbtUtil {
    private inline fun <reified R : BinaryTag, reified N : Number, reified T> toBinaryTag(
        array: JsonArray,
        crossinline toBinaryTag: (T) -> R,
        crossinline toN: (JsonElement) -> N,
        crossinline toT: (Array<N>) -> T
    ) = toBinaryTag(toT(Array(array.size()) { toN(array[it]) }))

    val typeMap = mapOf(
        toTypePair(TagTypes.BYTE),
        toTypePair(TagTypes.SHORT),
        toTypePair(TagTypes.INT),
        toTypePair(TagTypes.LONG),
        toTypePair(TagTypes.FLOAT),
        toTypePair(TagTypes.DOUBLE),
        toTypePair(TagTypes.STRING),
        toTypePair(TagTypes.BYTE_ARRAY),
        toTypePair(TagTypes.INT_ARRAY),
        toTypePair(TagTypes.LONG_ARRAY),
        toTypePair(TagTypes.COMPOUND),
        toTypePair(TagTypes.LIST),
    )

    private inline fun <reified T : BinaryTag> toTypePair(type: TagType<T>) = type to T::class.simpleName

    fun Byte.toTag() = ByteTag.byteBinaryTag(this)
    fun Boolean.toTag() = (if (this) 1 else 0).toByte().toTag()
    fun Short.toTag() = ShortTag.shortBinaryTag(this)
    fun Int.toTag() = IntTag.intBinaryTag(this)
    fun Long.toTag() = LongTag.longBinaryTag(this)
    fun Float.toTag() = FloatTag.floatBinaryTag(this)
    fun Double.toTag() = DoubleTag.doubleBinaryTag(this)
    fun String.toTag() = StringTag.stringBinaryTag(this)
    fun ByteArray.toTag() = ByteArrayTag.byteArrayBinaryTag(*this)
    fun IntArray.toTag() = IntArrayTag.intArrayBinaryTag(*this)
    fun LongArray.toTag() = LongArrayTag.longArrayBinaryTag(*this)
    fun Array<Byte>.toTag() = toByteArray().toTag()
    fun Array<Int>.toTag() = toIntArray().toTag()
    fun Array<Long>.toTag() = toLongArray().toTag()
    inline fun <reified V : Tag> Map<String, V>.toTag() = CompoundTag.from(this)

    private inline fun <reified T : Tag> toListTag(type: TagType<T>, collection: Collection<T>) = ListTag.builder(type).also { collection.forEach(it::add) }.build()
    private inline fun <reified T, reified B : BinaryTag> Collection<T>.toListTag(type: TagType<B>, crossinline func: (T) -> B) = toListTag(type, map(func))

    fun Collection<IntTag>.toIntTag() = toListTag(TagTypes.INT, this)
    fun Collection<ByteTag>.toByteTag() = toListTag(TagTypes.BYTE, this)
    fun Collection<LongTag>.toLongTag() = toListTag(TagTypes.LONG, this)

    fun Collection<CompoundTag>.toListTag() = toListTag(TagTypes.COMPOUND, this)

    inline fun <reified T> compound(iterable: Iterable<T>, func: (T) -> Pair<String, BinaryTag>)
            = CompoundTag.builder().apply { iterable.map(func).forEach(::put) }.build()

    inline fun <reified K, reified V> List<Pair<K, V>>.forEach(func: (K, V) -> Unit)
            = forEach { (key, value) -> func(key, value) }

    inline fun <reified T : Number, reified B : BinaryTag> JsonArray.toBinaryTag(
        crossinline toNumber: (JsonElement) -> T,
        crossinline toTag: (Array<T>) -> B
    ) = toTag(Array(size()) { toNumber(this[it]) })

    fun JsonArray.toByteArrayTag() = toBinaryTag(JsonElement::getAsByte) { it.toTag() }
    fun JsonArray.toIntArrayTag() = toBinaryTag(JsonElement::getAsInt) { it.toTag() }
    fun JsonArray.toLongArrayTag() = toBinaryTag(JsonElement::getAsLong) { it.toTag() }

    fun JsonElement.serializeTag(
        objectKey: String? = null,
        lazilyFunc: ((Pair<String?, LazilyParsedNumber>) -> BinaryTag) = { (_, n) ->
            if (n.toString().contains(".")) n.toDouble().toTag() else n.toInt().toTag()
        }
    ): BinaryTag {
        when (this) {
            is JsonPrimitive -> return when {
                isNumber -> when (val number = asNumber) {
                    is Byte -> number.toTag()
                    is Short -> number.toTag()
                    is Int -> number.toTag()
                    is Long -> number.toTag()
                    is Float -> number.toTag()
                    is Double -> number.toTag()
                    is LazilyParsedNumber -> lazilyFunc(objectKey to number)
                    else -> throw IllegalArgumentException("Unknown number type $number")
                }
                isString -> asString.toTag()
                isBoolean -> asBoolean.toTag()
                else -> throw IllegalArgumentException("Unknown json primitive $this")
            }
            is JsonObject -> return compound(entrySet()) { (key, value) -> key to value.serializeTag(key, lazilyFunc) }
            is JsonArray -> {
                if (isEmpty) return ListTag.empty()
                val tagItems = mutableListOf<Tag>()
                var listTagType: TagType<out Tag>? = null
                forEach {
                    val tag = it.serializeTag()
                    tagItems.add(tag)
                    if (listTagType == null)
                        listTagType = tag.type()
                    else if (listTagType != tag.type())
                        listTagType = TagTypes.COMPOUND
                }
                when (listTagType!!) {
                    TagTypes.BYTE -> toByteArrayTag()
                    TagTypes.INT -> toIntArrayTag()
                    TagTypes.LONG -> toLongArrayTag()
                    TagTypes.COMPOUND -> tagItems.replaceAll {
                        it.takeIf { _ -> it.type() == TagTypes.COMPOUND } ?: mapOf("" to it).toTag()
                    }
                }
                return ListTag.from(tagItems)
            }
        }
        return EndBinaryTag.endBinaryTag()
    }

    fun CompoundTag.has(name: String, exceptedType: TagType<*>): Boolean {
        return (this[name] ?: return false).type() == exceptedType
    }

    inline fun <reified T : BinaryTag> CompoundTag.getExcepted(name: String, exceptedType: TagType<T>): T {
        val typeName by lazy { T::class.simpleName!! }
        val value = this[name] ?: throw IllegalArgumentException("Excepted $typeName but found null.")
        require(value.type() == exceptedType) { "Excepted $typeName but found $value" }
        return value as T
    }

    fun CompoundTag.getExceptedString(name: String) = getExcepted(name, TagTypes.STRING).value()
    fun CompoundTag.getExceptedCompound(name: String): CompoundTag = getExcepted(name, TagTypes.COMPOUND)

    fun CompoundTag.conditionMap(func: (Pair<String, BinaryTag>) -> Boolean)
    = filter { func(it.key to it.value) }.associate { it.key to it.value }.toTag()

    fun CompoundTag.removeKeys(func: (String) -> Boolean) = conditionMap { (key, _) -> !func(key) }

    fun ListBinaryTag.toCompoundList() = map { it as? CompoundBinaryTag ?: throw IllegalArgumentException("Excepted CompoundTag but found ${it::class.simpleName!!}") }

    fun BinaryTag.toCompactString(): String = when (this) {
        is IntTag -> value().toString()
        is StringTag -> "\"${value()}\""
        is LongTag -> value().toString() + "L"
        is FloatTag -> value().toString() + "f"
        is DoubleTag -> value().toString() + "d"
        is ByteTag -> value().toString() + "b"
        is ShortTag -> value().toString() + "s"
        is LongArrayTag -> "[${value().joinToString { it.toString() + "L" }}]"
        is IntArrayTag -> "[${value().joinToString()}]"
        is ByteArrayTag -> "[${value().joinToString { it.toString() + "b" }}]"
        is ListTag -> "[${joinToString { it.toCompactString() }}]"
        is CompoundTag -> {
            val sb = StringBuilder("{")
            val iterator = iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next()
                sb.append("${entry.key}: ${entry.value.toCompactString()}")
                if (iterator.hasNext()) sb.append(", ")
            }
            sb.append("}").toString()
        }
        else -> toString() // ?
    }
}