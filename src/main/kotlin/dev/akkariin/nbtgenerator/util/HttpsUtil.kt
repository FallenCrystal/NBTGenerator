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

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import javax.net.ssl.HttpsURLConnection

object HttpsUtil {

    fun readString(url: String): String {
        val connection = openConnection(url)
        connection.connectTimeout = 10000
        connection.readTimeout = 10000
        connection.requestMethod = "GET"
        if (connection.responseCode != 200) {
            val code = connection.responseCode
            val input = BufferedReader(InputStreamReader(if (code in 200..299) connection.inputStream else connection.errorStream))
            // Strange tricks to avoid empty body warnings
            while (input.readLine()?.also(::println) != null) continue
            throw UnsupportedOperationException("Unexpected response code: $code. (Excepted 200)")
        }
        val sb = StringBuilder()
        val input = BufferedReader(InputStreamReader(connection.inputStream))
        while (input.readLine()?.also(sb::append) != null) continue
        return sb.toString()
    }

    fun openConnection(url: String): HttpsURLConnection {
        println("Connecting to $url")
        val connection = URL(url).openConnection() as? HttpsURLConnection
            ?: throw IllegalArgumentException("Tried invoke URL#openConnection() but returned the object isn't HttpsURLConnection")
        connection.apply {
            connectTimeout = 10000
            readTimeout = 10000
        }
        return connection
    }
}