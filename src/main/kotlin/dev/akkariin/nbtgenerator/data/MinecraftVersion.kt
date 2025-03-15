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

package dev.akkariin.nbtgenerator.data

import com.google.gson.Gson
import com.google.gson.JsonObject
import dev.akkariin.nbtgenerator.util.JsonExtension.exceptedAsJsonObject
import dev.akkariin.nbtgenerator.util.JsonExtension.int
import dev.akkariin.nbtgenerator.util.JsonExtension.getObject
import dev.akkariin.nbtgenerator.util.JsonExtension.string
import dev.akkariin.nbtgenerator.util.readStringFromUrl
import java.util.concurrent.CompletableFuture

@Suppress("unused", "MemberVisibilityCanBePrivate")
data class MinecraftVersion(val id: String, val type: Type, val url: String, val releaseTime: String) {

    constructor(json: JsonObject) : this(
        json.string("id"),
        Type.valueOf(json.string("type").uppercase()),
        json.string("url"),
        json.string("releaseTime")
    )

    fun fetchData(): CompletableFuture<Data> {
        require(type != Type.OLD_BETA && type != Type.OLD_ALPHA)
        { "Unsupported type $type" }
        return CompletableFuture.supplyAsync {
            Data(this, Gson().fromJson(readStringFromUrl(url), JsonObject::class.java))
        }
    }

    enum class Type {
        RELEASE,
        SNAPSHOT,
        OLD_BETA,
        OLD_ALPHA
    }

    class Collector private constructor(val latestRelease: MinecraftVersion, val latestSnapshot: MinecraftVersion, val map: Map<String, MinecraftVersion>) {
        fun versions() = map.values
        fun version(name: String) = map[name]

        companion object {
            fun from(json: JsonObject): Collector {
                val map = json.getAsJsonArray("versions")
                    .map { MinecraftVersion(it.exceptedAsJsonObject()) }
                    .associateBy(MinecraftVersion::id)
                val latest = json.getObject("latest")
                return Collector(
                    map[latest.string("release")]!!,
                    map[latest.string("snapshot")]!!,
                    map
                )
            }
        }
    }

    data class Data(val minecraftVersion: MinecraftVersion, val javaVersion: JavaVersion, val downloads: Downloads) {
        constructor(minecraftVersion: MinecraftVersion, json: JsonObject) : this(
            minecraftVersion,
            JavaVersion(json.getObject("javaVersion")),
            Downloads(json.getObject("downloads")),
        )
    }

    data class JavaVersion(val component: String, val majorVersion: Int) {
        constructor(json: JsonObject) : this(json.string("component"), json.int("majorVersion"))
    }

    data class Downloads(
        val client: Source,
        val clientMappings: Source,
        val server: Source,
        val serverMappings: Source,
    ) {
        constructor(json: JsonObject) : this(
            Source(json.getObject("client")),
            Source(json.getObject("client_mappings")),
            Source(json.getObject("server")),
            Source(json.getObject("server_mappings"))
        )
    }

    data class Source(val sha1: String, val size: Int, val url: String) {
        constructor(json: JsonObject) : this(json.string("sha1"), json.int("size"), json.string("url"))
    }
}
