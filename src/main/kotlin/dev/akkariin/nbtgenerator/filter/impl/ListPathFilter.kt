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

package dev.akkariin.nbtgenerator.filter.impl

import dev.akkariin.nbtgenerator.filter.PathFilter
import java.io.File

abstract class ListPathFilter(private val filterList: List<String>) : PathFilter {

    override fun filter(file: File, type: String?): Boolean {
        if (filterFile(file)) {
            return true
        } else if (type != null) {
            for (it in filterList) {
                if (it.contains("/")) {
                    if (it.startsWith(type)) {
                        return false
                    }
                } else if (it == type) {
                    return false
                }
            }
            return true
        }
        return false
    }

    abstract fun filterFile(file: File): Boolean

}