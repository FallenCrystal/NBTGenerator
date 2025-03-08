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

package dev.akkariin.nbtgenerator.tasks.impl

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import dev.akkariin.nbtgenerator.args.ArgsParser
import dev.akkariin.nbtgenerator.data.MultiException
import dev.akkariin.nbtgenerator.protocol.ProtocolRegistry
import dev.akkariin.nbtgenerator.tasks.Stage
import dev.akkariin.nbtgenerator.tasks.Task
import dev.akkariin.nbtgenerator.util.FileUtil.existOrThrow
import dev.akkariin.nbtgenerator.util.FileUtil.hasNonDirectory
import dev.akkariin.nbtgenerator.util.FileUtil.removeAndCreate
import dev.akkariin.nbtgenerator.util.FileUtil.toFilePath
import dev.akkariin.nbtgenerator.util.JsonUtil.array
import dev.akkariin.nbtgenerator.util.NbtUtil.getExcepted
import dev.akkariin.nbtgenerator.util.NbtUtil.getExceptedCompound
import dev.akkariin.nbtgenerator.util.NbtUtil.getExceptedString
import dev.akkariin.nbtgenerator.util.NbtUtil.has
import dev.akkariin.nbtgenerator.util.StringUtil.checkNamespace
import net.kyori.adventure.nbt.BinaryTagIO
import net.kyori.adventure.nbt.BinaryTagTypes
import net.kyori.adventure.nbt.CompoundBinaryTag
import net.kyori.adventure.nbt.IntArrayBinaryTag
import org.fusesource.jansi.Ansi
import java.io.File
import java.io.FileReader

// TODO: Rewrite it someday
// Generate mapping from CollectProtocolMappingTask first
class GenerateProtocolTagTask(
    folder: File,
    private val protocolRegistries: Map<String, ProtocolRegistry>,
    private val registryCodec: CompoundBinaryTag? = null,
    private val output: File? = null
) : Task(folder) {

    private val map = mutableMapOf<String, MutableList<Tag>>()

    // Stages
    private val findFolder = Stage("Find folder", "Finding folder for tags") {
        listOf("Check that the data generator has been run. And point the path to the correct folder.")
    }
    private val collectTag = Stage("Collect Tags", "Collect tags from json file")
    private val checkValid = Stage("Check Valid", "Check if tags has no problem (e.x Depend loops)") {
        listOf("Check whether there is a problem with the tag based on the information provided by the exception.")
    }
    private val remapping = Stage("Remapping", "Remapping tags context to protocol id.")
    private val saveToFile = Stage("Save to file", "Save tags as int array into nbt file.") {
        listOf("Make sure that the application can write to the file at the specified location.")
    }


    override fun initialize() = arrayOf(findFolder, collectTag, checkValid, remapping, saveToFile)

    override fun execute(parser: ArgsParser) {
        setStage(findFolder)
        val folder = findFolder()
        setStage(collectTag)
        collectTag(folder)
        setStage(checkValid)
        checkValid()
        setStage(remapping)
        val result = mapping()
        setStage(saveToFile)
        if (output == null) {
            println(Ansi.ansi().fg(Ansi.Color.YELLOW).a("Skipping saving because output is null.").fg(Ansi.Color.DEFAULT))
        } else {
            val compound = CompoundBinaryTag.builder()
            for ((category, tags) in result) {
                val c2 = CompoundBinaryTag.builder()
                for ((tag, value) in tags) {
                    c2.put(tag, IntArrayBinaryTag.intArrayBinaryTag(*value))
                }
                compound.put("minecraft:$category", c2.build())
            }
            BinaryTagIO.writer().write(compound.build(), output.removeAndCreate().toPath(), BinaryTagIO.Compression.GZIP)
        }
    }

    private fun findFolder() = File(folder, "data/minecraft/tags".toFilePath()).existOrThrow()

    private fun collectTag(tagFolder: File, prefix: String = "") {
        println("Collecting tags from $tagFolder (prefix: $prefix)")
        val exception = MultiException()

        val categoryAndFolder = mutableMapOf<String, File>()
        for (folder in tagFolder.listFiles()!!) {
            require(folder.isDirectory) { "Folder ${folder.toPath()} is not a directory" }
            if (folder.hasNonDirectory()) {
                categoryAndFolder["$prefix${folder.name}"] = folder
            } else {
                try {
                    collectTag(folder, "$prefix${folder.name}/")
                } catch (e: Exception) {
                    if (e is MultiException) {
                        e.getExceptions().forEach(exception::addException)
                    } else {
                        exception.addException(e)
                    }
                }
            }
        }

        for (entry in categoryAndFolder.entries) {
            if (!entry.value.isDirectory) {
                exception.addException(IllegalArgumentException("File ${entry.key} is not a directory."))
                continue
            }
            val path = "minecraft:${entry.key}"
            if (protocolRegistries[path] == null) {
                if (registryCodec == null) {
                    exception.addException(NullPointerException("Category $path is undefined in protocol registries. And the registry codec is not set."))
                    continue
                } else if (registryCodec.has(path, BinaryTagTypes.COMPOUND)) {
                    println(Ansi.ansi().fg(Ansi.Color.YELLOW)
                        .a("[WARN] $path is found from registry codec. This means the protocol id will changes because it depends registry codec.")
                        .fg(Ansi.Color.DEFAULT))
                } else {
                    exception.addException(NullPointerException("Category $path are undefined in protocol registry and registry codec."))
                    continue
                }
            }

            val gson = Gson()

            fun processFolder(prefix: String, category: String, folder: File) {
                for (tag in folder.listFiles()!!) {
                    if (tag.isDirectory) {
                        if (tag.hasNonDirectory()) {
                            processFolder("$prefix${tag.name}/", category, tag)
                        } else {
                            for (subFolder in tag.listFiles()!!) {
                                processFolder("$prefix${tag.name}/${subFolder.name}/", category, subFolder)
                            }
                        }
                        continue
                    }
                    if (!tag.name.endsWith(".json")) {
                        exception.addException(NullPointerException("Tag ${tag.name} is not a json file."))
                        continue
                    }
                    try {
                        val name = "${prefix}${tag.name.removeSuffix(".json")}"
                        val json = gson.fromJson(FileReader(tag), JsonObject::class.java)
                        val replace = json["replace"]?.asBoolean ?: false
                        val values = json.array("values")
                        map.computeIfAbsent(category) { mutableListOf() }.add(Tag(category, name, TagValue.parse(name, values), replace))
                    } catch (e: Exception) {
                        exception.addException(e)
                        continue
                    }
                }
            }

            processFolder("minecraft:", entry.key, entry.value)
        }
        exception.throwIfNotEmpty()
    }


    // Check if any tag(s) reference they parent(s).
    private fun checkValid() {
        val exception = MultiException()
        for ((category, tags) in map.entries) {
            for (tag in tags) {
                class Parent(val parent: Parent?, val name: String, val value: Set<TagValue.OtherTag>)

                fun check(tag: Tag, parent: Parent? = null) {
                    val value = filterNotTag(tag)
                    if (value.isEmpty()) return
                    if (parent != null) {
                        for (otherTag in value) {
                            var p = parent
                            while (p != null) {
                                if (p.value.contains(otherTag)) {
                                    exception.addException(IllegalArgumentException("Tag ${otherTag.tag} has illegal reference at ${parent.name}."))
                                }
                                p = p.parent
                            }
                        }
                    }
                    for ((otherTag, name) in value.map { otherTag -> tags.firstOrNull { it.name == otherTag.tag } to otherTag.tag }) {
                        if (otherTag == null) {
                            exception.addException(NullPointerException("Tag ${tag.name} tried reference tag $name, but not found at $category."))
                            continue
                        }
                        check(otherTag, Parent(parent, tag.name, HashSet(value)))
                    }
                }

                check(tag)
            }
        }
        exception.throwIfNotEmpty()
    }

    private fun mapping(): Map<String, Map<String, IntArray>> {
        val exception = MultiException()
        val remappedMap = mutableMapOf<String, MutableMap<String, IntArray>>()
        for ((category, tags) in this.map.entries) {
            val map = remappedMap.computeIfAbsent(category) { mutableMapOf() }

            fun getIdGetter(): IdGetter {
                val path = "minecraft:$category"
                val pr = protocolRegistries[path]
                if (pr != null) return IdGetter { key -> pr[key]!! }
                val idMap = mutableMapOf<String, Int>()
                registryCodec!!
                    .getExceptedCompound(path)
                    .getExcepted("value", BinaryTagTypes.LIST)
                    .map { it as CompoundBinaryTag }
                    .forEach { idMap[it.getExceptedString("name")] = it.getExcepted("id", BinaryTagTypes.INT).value() }
                return IdGetter { key -> idMap[key]!! }
            }

            val idGetter = try { getIdGetter() } catch (e: Exception) {
                exception.addException(e)
                continue
            }

            for (tag in tags) {
                try {
                    val list = mutableSetOf<String>()
                    fun fetch(t: Tag) {
                        val m = t.value.map { if (it is TagValue.ComplexTag) it.tag else it }
                        for (it in m) {
                            if (it is TagValue.SimpleTag) {
                                list.add(it.tag)
                            } else if (it is TagValue.OtherTag) {
                                fetch(tags.firstOrNull { otherTag -> otherTag.name == it.tag }
                                    ?: throw NoSuchElementException("Tag ${t.name} referenced tag ${it.tag}. But not found at category $category"))
                            }
                        }
                    }
                    fetch(tag)
                    map[tag.name] = list.map(idGetter::id).toIntArray()
                } catch (e: Exception) {
                    exception.addException(e)
                    continue
                }
            }
        }
        exception.throwIfNotEmpty()
        return remappedMap
    }

    fun interface IdGetter {
        fun id(key: String): Int
    }

    private fun filterNotTag(tag: Tag) = tag
        .value
        .map { if (it is TagValue.ComplexTag) it.tag else it }
        .mapNotNull { it as? TagValue.OtherTag }

    private data class Tag(val category: String, val name: String, val value: List<TagValue>, val replace: Boolean = false)

    private interface TagValue {
        fun valueAsString(): String

        data class OtherTag(val tag: String) : TagValue {
            override fun valueAsString() = "#$tag"
        }

        data class SimpleTag(val tag: String) : TagValue {
            override fun valueAsString() = tag
        }

        data class ComplexTag(val tag: TagValue, val require: Boolean = false) : TagValue by tag

        companion object {
            private fun namespaceToTag(namespace: String): TagValue {
                if (namespace.startsWith("#")) {
                    val removePrefix = namespace.removePrefix("#")
                    removePrefix.checkNamespace(true)
                    return OtherTag(removePrefix)
                } else {
                    namespace.checkNamespace(false)
                    return SimpleTag(namespace)
                }
            }

            fun parse(name: String, values: JsonArray): List<TagValue> {
                val tags = mutableListOf<TagValue>()
                for (tag in values) {
                    if (tag is JsonPrimitive) {
                        require(tag.isString) { "Excepted string but found: ${tag.asString}" }
                        tags.add(namespaceToTag(tag.asString))
                    } else if (tag is JsonObject) {
                        val id = tag.asString
                        val require = tag["require"]?.asBoolean ?: false
                        tags.add(ComplexTag(namespaceToTag(id), require))
                    }
                }
                if (HashSet(tags).size != values.size()) {
                    println(Ansi.ansi().fg(Ansi.Color.YELLOW).a("[WARN] $name contains duplicated keys: $values").fg(Ansi.Color.DEFAULT))
                }
                return tags
            }
        }
    }

}