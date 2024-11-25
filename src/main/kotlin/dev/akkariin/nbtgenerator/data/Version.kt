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
import dev.akkariin.nbtgenerator.util.HttpsUtil
import java.util.concurrent.CompletableFuture

@Suppress("unused", "MemberVisibilityCanBePrivate")
data class Version(val id: String, val type: Type, val url: String, val releaseTime: String) {

    constructor(json: JsonObject) : this(
        json.get("id").asString,
        Type.valueOf(json.get("type").asString.uppercase()),
        json.get("url").asString,
        json.get("releaseTime").asString
    )

    fun fetchData(): CompletableFuture<Data> {
        require(type != Type.OLD_BETA && type != Type.OLD_ALPHA)
        { "Unsupported type $type" }
        return CompletableFuture.supplyAsync {
            Data(this, Gson().fromJson(HttpsUtil.readString(url), JsonObject::class.java))
        }
    }

    enum class Type {
        RELEASE,
        SNAPSHOT,
        OLD_BETA,
        OLD_ALPHA
    }

    class Collector private constructor(val latestRelease: Version, val latestSnapshot: Version, val map: Map<String, Version>) {
        fun versions() = map.values
        fun version(name: String) = map[name]

        companion object {
            fun from(json: JsonObject): Collector {
                val map = json.getAsJsonArray("versions").map { Version(it.asJsonObject) }.associateBy { it.id }
                val latest = json.getAsJsonObject("latest")
                return Collector(
                    map[latest["release"]!!.asString]!!,
                    map[latest["snapshot"]!!.asString]!!,
                    map
                )
            }
        }
    }

    data class Data(val version: Version, val javaVersion: JavaVersion, val downloads: Downloads) {
        constructor(version: Version, json: JsonObject) : this(
            version,
            JavaVersion(json.getAsJsonObject("javaVersion")),
            Downloads(json.getAsJsonObject("downloads")),
        )
    }

    data class JavaVersion(val component: String, val majorVersion: Int) {
        constructor(json: JsonObject) : this(json.get("component").asString, json.get("majorVersion").asInt)
    }

    data class Downloads(
        val client: Source,
        val clientMappings: Source,
        val server: Source,
        val serverMappings: Source,
    ) {
        constructor(json: JsonObject) : this(
            Source(json.getAsJsonObject("client")),
            Source(json.getAsJsonObject("client_mappings")),
            Source(json.getAsJsonObject("server")),
            Source(json.getAsJsonObject("server_mappings"))
        )
    }

    data class Source(val sha1: String, val size: Int, val url: String) {
        constructor(json: JsonObject) : this(json.get("sha1").asString, json.get("size").asInt, json.get("url").asString)
    }
}
