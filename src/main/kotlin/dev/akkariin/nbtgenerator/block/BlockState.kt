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

import dev.akkariin.nbtgenerator.block.property.BlockProperty
import java.util.*

@Suppress("MemberVisibilityCanBePrivate")
class BlockState(
    val name: String,
    data: Collection<BlockProperty.Data<*>>,
    val id: Int,
    val default: Boolean
) {
    private val map = data.associate { it.property to it.value }

    fun <T : Any> getProperty(property: BlockProperty<T>): Optional<T> {
        val p = property as BlockProperty<*>
        val v = map[p]
        if (v != null)
            @Suppress("UNCHECKED_CAST")
            return Optional.of(v as T)
        return Optional.empty()
    }

    override fun toString() = "BlockState(id=$id, properties=[${map.entries.joinToString { "${it.key.name()}=${it.value}" }}])"

    fun equals(other: BlockState, ignoreDefault: Boolean, ignoreId: Boolean): Boolean {
        if (other.name != this.name) return false
        if (!ignoreId && other.id != this.id) return false
        if (!ignoreDefault && other.default != default) return false
        val map = other.map
        if (map.size != this.map.size) return false
        for ((key, value) in map) {
            val v = this.map[key] ?: return false
            if (value != v) return false
        }
        return true
    }

    override fun equals(other: Any?) = (other as? BlockState)?.let { equals(it,  ignoreDefault = false, ignoreId = false) } ?: false

    override fun hashCode(): Int {
        var result = 1
        result = 31 * result + name.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + default.hashCode()
        for ((key, value) in map) {
            result = 31 * result + key.hashCode()
            result = 31 * result + value.hashCode()
        }
        return result
    }
}
