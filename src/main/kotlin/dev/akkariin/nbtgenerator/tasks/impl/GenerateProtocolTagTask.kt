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
import dev.akkariin.nbtgenerator.tasks.impl.GenerateProtocolTagTask.IdGetter
import dev.akkariin.nbtgenerator.tasks.impl.GenerateProtocolTagTask.TagValue.*
import dev.akkariin.nbtgenerator.util.*
import dev.akkariin.nbtgenerator.util.JsonExtension.array
import net.kyori.adventure.nbt.BinaryTagIO
import net.kyori.adventure.nbt.BinaryTagTypes
import net.kyori.adventure.nbt.CompoundBinaryTag
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
                compound["minecraft:$category"] = tags.map { (key, value) -> key to value.toTag() }.toMap().toTag()
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
                } else if (registryCodec.has(path, BinaryTagTypes.COMPOUND)) {
                    println(Ansi.ansi().fg(Ansi.Color.YELLOW)
                        .a("[WARN] $path is found from registry codec. This means the protocol id will changes because it depends registry codec.")
                        .fg(Ansi.Color.DEFAULT))
                } else {
                    exception.addException(NullPointerException("Category $path are undefined in protocol registry and registry codec."))
                }
            }

            val gson = Gson()

            fun processFolder(prefix: String, category: String, folder: File) {
                folder.listFiles()!!.applyForEach {
                    if (isDirectory) {
                        if (hasNonDirectory())
                            processFolder("$prefix${this.name}/", category, this)
                        else listFiles()!!.forEach { subFolder -> processFolder("$prefix${this.name}/${subFolder.name}/", category, subFolder) }
                    } else if (!name.endsWith(".json"))
                        exception.addException(NullPointerException("Tag ${this.name} is not a json file."))
                    else {
                        map.computeIfAbsent(category) { mutableListOf() }.add(tryCatch(exception::addException) {
                            val json = gson.fromJson(FileReader(this), JsonObject::class.java)
                            val name = "${prefix}${this.name.removeSuffix(".json")}"
                            Tag(category, name, parseTags(name, json.array("values")), json["replace"]?.asBoolean ?: false)
                        } ?: return@applyForEach)
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
            for (tag in tags) check(category, tags, exception, tag)
        }
        exception.throwIfNotEmpty()
    }

    private class ParentHolder(val parent: ParentHolder?, val name: String, val value: Set<OtherTag>)

    private fun check(category: String, tags: List<Tag>, exception: MultiException, tag: Tag, parent: ParentHolder? = null) {
        val otherTagReferences = filterNotTagReferences(tag).takeUnless { it.isEmpty() } ?: return
        if (parent != null) {
            otherTagReferences.applyForEach {
                parent.invokeUntilNull(ParentHolder::parent) {
                    if (value.contains(this@applyForEach))
                        exception.addException(IllegalArgumentException("Tag ${this@applyForEach.tag} has illegal reference at ${parent.name}."))
                }
            }
        }
        for ((otherTag, name) in otherTagReferences.applyMap { tags.firstOrNull { it.name == this.tag } to this.tag }) {
            if (otherTag == null) {
                exception.addException(NullPointerException("Tag ${tag.name} tried reference tag $name, but not found at $category."))
            } else {
                check(category, tags, exception, otherTag, ParentHolder(parent, tag.name, HashSet(otherTagReferences)))
            }
        }
    }

    private fun mapping(): Map<String, Map<String, IntArray>> {
        val exception = MultiException()
        val remappedMap = mutableMapOf<String, MutableMap<String, IntArray>>()
        for ((category, tags) in this.map.entries) {
            val map = remappedMap.computeIfAbsent(category) { mutableMapOf() }
            val idGetter = tryCatch(exception::addException) { getIdGetter(category) } ?: continue
            for (tag in tags) {
                map[tag.name] = tryCatch(exception::addException) {
                    mutableSetOf<String>()
                        .apply {
                            fun fetch(rootTag: Tag) {
                                rootTag.value.exactTags().applyForEach { when (this) {
                                    is SimpleTag -> this@apply.add(this.tag)
                                    is OtherTag -> fetch(tags.firstOrNull { otherTag -> otherTag.name == this.tag }
                                        ?: throw NoSuchElementException("Tag ${rootTag.name} referenced tag ${this.tag}. But not found at category $category"))
                                }}
                            }
                            fetch(tag)
                        }
                        .map(idGetter::id)
                        .toIntArray()
                } ?: continue
            }
        }
        exception.throwIfNotEmpty()
        return remappedMap
    }

    fun interface IdGetter {  fun id(key: String): Int  }

    private fun getIdGetter(category: String): IdGetter {
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

    private fun filterNotTagReferences(tag: Tag) = tag.value.exactTags().filterIsInstance<OtherTag>()

    private data class Tag(val category: String, val name: String, val value: List<TagValue>, val replace: Boolean = false)

    private abstract class TagValue(private val valueAsString: String) {
       fun valueAsString(): String = this.valueAsString
        data class OtherTag(val tag: String) : TagValue("#$tag")
        data class SimpleTag(val tag: String) : TagValue(tag)
        data class ComplexTag(val tag: TagValue, val require: Boolean = false) : TagValue(tag.valueAsString())
    }

    private fun namespaceToTag(namespace: String) =
        if (namespace.startsWith("#")) {
            OtherTag(namespace.removePrefix("#").also { it.checkNamespace(true) })
        } else {
            SimpleTag(namespace.also { it.checkNamespace(false) })
        }

    private fun List<TagValue>.exactTags() = applyMap { if (this is ComplexTag) tag else this }

    private fun parseTags(name: String, values: JsonArray): List<TagValue> {
        val tags = mutableListOf<TagValue>()
        for (tag in values) {
            if (tag is JsonPrimitive) {
                require(tag.isString) { "Excepted string but found: ${tag.asString}" }
                tags.add(namespaceToTag(tag.asString))
            } else if (tag is JsonObject) {
                tags.add(ComplexTag(namespaceToTag(tag.asString), tag["require"]?.asBoolean ?: false))
            }
        }
        if (HashSet(tags).size != values.size()) {
            println(Ansi.ansi().fg(Ansi.Color.YELLOW).a("[WARN] $name contains duplicated keys: $values").fg(Ansi.Color.DEFAULT))
        }
        return tags
    }

}