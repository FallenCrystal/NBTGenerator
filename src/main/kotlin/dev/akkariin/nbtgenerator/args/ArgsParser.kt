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

package dev.akkariin.nbtgenerator.args

class ArgsParser(args: Array<String>) {

    private val map = mutableMapOf<String, String>()

    companion object {
        private val SYNTAX_EXCEPTION = IllegalArgumentException("Args syntax: --<setting> <value>. Run with -h flags to get help.")
    }

    init {
        if (args.isNotEmpty()) {
            if (args.size % 2 != 0) {
                throw SYNTAX_EXCEPTION
            }
            var setting: String? = null
            for (arg in args) {
                if (arg.startsWith("--")) {
                    if (setting != null) {
                        throw SYNTAX_EXCEPTION
                    } else {
                        setting = arg.substring(2)
                    }
                } else if (setting != null) {
                    map[setting] = arg
                    setting = null
                } else {
                    throw SYNTAX_EXCEPTION
                }
            }
        }
    }

    fun <T> parse(arg: Arg<T>): T = map[arg.name]?.takeUnless { it == "default" }?.let(arg.func) ?: arg.defaultValue

}