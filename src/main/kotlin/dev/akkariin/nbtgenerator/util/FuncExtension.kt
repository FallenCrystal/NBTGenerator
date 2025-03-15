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

import net.kyori.adventure.nbt.BinaryTag
import net.kyori.adventure.nbt.CompoundBinaryTag

inline fun <reified T> List<T>.applyForEach(crossinline block: T.() -> Unit): List<T> {
    forEach(block)
    return this
}

inline fun <reified T> Array<T>.applyForEach(crossinline block: T.() -> Unit): Array<T> {
    forEach(block)
    return this
}

inline fun <reified T : Any, reified R> List<T>.applyMap(crossinline block: T.() -> R) = this.map(block)

inline fun <reified T : Any> T.invokeUntilNull(
    crossinline parent: (T) -> T?,
    crossinline block: T.() -> Unit
) {
    var value: T? = this
    while (value != null) {
        block(value)
        value = parent(value)
    }
}

inline fun <reified E : Exception, reified T> tryCatch(crossinline onException: (E) -> Unit, crossinline block: () -> T): T? = try { block() } catch (e: Throwable) {
    if (e !is E) throw e
    onException(e)
    null
}

operator fun CompoundBinaryTag.Builder.set(key: String, tag: BinaryTag) = this.put(key, tag)
