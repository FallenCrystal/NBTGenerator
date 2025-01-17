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

import java.io.File
import java.security.MessageDigest

@Suppress("MemberVisibilityCanBePrivate")
object FileUtil {
    fun getSha1(file: File) = MessageDigest.getInstance("SHA1").digest(file.readBytes()).joinToString("") { "%02x".format(it) }

    fun String.toFilePath() = this.replace("/", File.separator)

    fun File.hasDirectory(folderName: String): Boolean {
        if (!this.exists() || !this.isDirectory) return false
        return File(this, folderName).let { it.exists() && it.isDirectory }
    }

    fun File.hasDirectories(vararg folders: String) = folders.all { this.hasDirectory(it) }

    fun File.isMatched(vararg names: String) = names.all { name == it }
}