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
import com.google.gson.JsonObject
import dev.akkariin.nbtgenerator.args.Arg
import dev.akkariin.nbtgenerator.args.ArgsParser
import dev.akkariin.nbtgenerator.tasks.Stage
import dev.akkariin.nbtgenerator.tasks.Task
import dev.akkariin.nbtgenerator.tasks.impl.RegistryGeneratorTask.ElementCleaner
import dev.akkariin.nbtgenerator.util.FileUtil.hasDirectories
import dev.akkariin.nbtgenerator.util.FileUtil.isMatched
import dev.akkariin.nbtgenerator.util.FileUtil.toFilePath
import dev.akkariin.nbtgenerator.util.NbtUtil.filterKeys
import dev.akkariin.nbtgenerator.util.NbtUtil.getExcepted
import dev.akkariin.nbtgenerator.util.NbtUtil.getExceptedCompound
import dev.akkariin.nbtgenerator.util.NbtUtil.getExceptedString
import dev.akkariin.nbtgenerator.util.NbtUtil.has
import dev.akkariin.nbtgenerator.util.NbtUtil.serializeTag
import dev.akkariin.nbtgenerator.util.NbtUtil.toCompoundList
import dev.akkariin.nbtgenerator.util.NbtUtil.toListTag
import dev.akkariin.nbtgenerator.util.NbtUtil.toTag
import net.kyori.adventure.nbt.*
import java.io.File
import java.io.FileReader
import java.util.concurrent.TimeUnit

private typealias Types = BinaryTagTypes

class RegistryGeneratorTask(folder: File, private val output: File) : Task(folder) {
   // Stages
   @Suppress("SpellCheckingInspection")
   private val lookingForPath = Stage("Looking for path", "Looking worldgen file path for generate codec") {
       listOf(
           "Make sure that the specified directory is a folder. and have a folder named \"generated\".",
           "Check which versions of Minecraft of data generator are supported."
       )
   }
   private val collectData = Stage("Collecting data", "Collecting registry data from snbt")
   private val applyCleaner = Stage("Cleaner", "Clean useless elements in nbt")
   private val runTest = Stage("Run tests", "Run tests for registry to prevent corrupted output")
   private val saveToFile = Stage("Save to file", "Save output as gzipped nbt file") {
       listOf("Make sure that the application can write to the file at the specified location.")
   }


    override fun initialize() = arrayOf(lookingForPath, collectData, applyCleaner, runTest, saveToFile)

    override fun execute(parser: ArgsParser) {
        setStage(lookingForPath)
        val path = searchPath()
        println("Set ${path.absolutePath} as registry path.")
        setStage(collectData)
        val pathFilter = PathFilter.ListPathFilter()
        val builder = CompoundBinaryTag.builder()
        for (folder in path.listFiles()!!) {
            if (!folder.isDirectory) continue
            searchFolder(null, folder, builder, pathFilter)
        }
        var compound = builder.build()
        setStage(applyCleaner)
        val cleaner = when (parser
            .parse(Arg.of("cleaner", "Default cleaner for registry output", "null"))
            .takeUnless { it == "null" }
            ?: run {
                println("What cleaner do you want to apply for output? (Timeout: 5s, Default value for SIMPLE)")
                println("0: NONE, 1: SIMPLE, 2: FULL")
                println("TIP: You can re-run the task with new choose at anytime.")
                println("TIP: You can run application with \"--cleaner [choose]\" parameters.")
                getInput(5, TimeUnit.SECONDS, "1") ?: "1"
            }
        ) {
            "0", "NONE", "none" -> PresentsCleaner.NONE
            "1", "SIMPLE", "simple" -> PresentsCleaner.SIMPLE
            "2", "FULL", "full" -> PresentsCleaner.FULL
            else -> {
                println("Unknown input. Selecting default value (SIMPLE).")
                PresentsCleaner.SIMPLE
            }
        }
        println("Apply ${cleaner.name} as element cleaner.")
        compound = cleaner.cleaner?.accept(compound) ?: compound
        setStage(runTest)
        runTests("Check minecraft:dimension_type") {
            val names = testGetTypeAsKeyList(compound, "minecraft:dimension_type")
            require(names.contains("minecraft:overworld")) { "Registries doesn't have minecraft:overworld in minecraft:dimension_type" }
            if (cleaner != PresentsCleaner.FULL) {
                require(names.contains("minecraft:the_nether")) { "Registries doesn't have minecraft:the_nether in minecraft:dimension_type" }
                require(names.contains("minecraft:the_end")) { "Registries doesn't have minecraft:the_end in minecraft:dimension_type" }
            }
        }
        runTests("Check minecraft:worldgen/biome") {
            val names = testGetTypeAsKeyList(compound, "minecraft:worldgen/biome")
            require(names.contains("minecraft:plains")) { "Registries doesn't have minecraft:plains in minecraft:worldgen/biome" }
            require(names.contains("minecraft:swamp")) { "Registries doesn't have minecraft:swamp in minecraft:worldgen/biome" }
        }
        runTests("Check minecraft:wolf_variant") {
            if (compound["minecraft:wolf_variant"] != null) {
                val names = testGetTypeAsKeyList(compound, "minecraft:wolf_variant")
                require(names.contains("minecraft:ashen")) { "Registries doesn't have minecraft:ashen in minecraft:wolf_variant" }
                val biomes = compound
                    .getCompound("minecraft:worldgen/biome")
                    .getExcepted("value", Types.LIST)
                    .toCompoundList()
                    .map { it.getExceptedString("name") }
                val variants = compound
                    .getCompound("minecraft:wolf_variant")
                    .getExcepted("value", Types.LIST)
                    .toCompoundList()
                    .associate { it.getExceptedString("name") to it.getExceptedCompound("element") }
                for ((name, element) in variants) {
                    val biome = element.getExceptedString("biomes")
                    if (biome.startsWith("#")) continue // Skipping tags
                    require(biomes.contains(biome)) { "Wolf variant ($name) that requires spawn at biome $biome, But not found in minecraft:worldgen/biome" }
                }
            }
        }
        setStage(saveToFile)
        BinaryTagIO.writer().write(compound, output.also(File::delete).also(File::createNewFile).toPath(), BinaryTagIO.Compression.GZIP)
    }

    private fun searchFolder(prefix: String?, folder: File,
                             builder: CompoundBinaryTag.Builder, pathFilter: PathFilter?) {
        require(folder.isDirectory) { "Folder ${folder.absolutePath} is not a directory" }
        val type = "minecraft:${prefix ?: ""}${folder.name}"
        if (pathFilter?.filter(folder, type) == true) return
        var index = 0
        val tags = ListBinaryTag.builder()
        for (file in folder.listFiles()!!) {
            if (pathFilter?.filter(file, null) == true) continue
            if (file.isDirectory) {
                searchFolder("${type.removePrefix("minecraft:")}/", file, builder, pathFilter)
            } else if (file.name.endsWith(".json")) {
                try {
                    val json = Gson().fromJson(FileReader(file), JsonObject::class.java)
                    tags.add(mapOf(
                        "id" to index++.toTag(),
                        "name" to "minecraft:${file.name.removeSuffix(".json")}".toTag(),
                        "element" to json.serializeTag()
                    ).toTag())
                } catch (e: Exception) {
                    println("Failed to parse file ${file.absolutePath}")
                    e.printStackTrace()
                    continue
                }
            }
        }
        tags.build().takeUnless { it.size() == 0 }?.also { builder.put(type, ofValues(type, it)) }
    }

    private fun ofValues(type: String, binaryTag: ListBinaryTag) = CompoundBinaryTag
        .builder()
        .putString("type", type)
        .put("value", binaryTag)
        .build()

    private fun searchPath(): File {
        val v18 = File(folder, "reports/worldgen/minecraft".toFilePath())
        if (v18.hasDirectories("dimension", "dimension_type", "worldgen")) {
            return v18
        }
        val v19 = File(folder, "reports/minecraft".toFilePath())
        if (v19.hasDirectories("chat_type", "dimension_type", "worldgen")) {
            return v19
        }
        return File(folder, "data/minecraft".toFilePath()).apply {
            require(exists()) { "Registry folder not exist!" }
        }
    }

    // Directly use CompoundBinaryTag#get to prevent create the empty tag for excepted type.
    private fun testGetTypeAsKeyList(tag: CompoundBinaryTag, type: String): List<String> {
        val compound = tag.getExcepted(type, BinaryTagTypes.COMPOUND)
        val lists = compound.getExcepted("value", BinaryTagTypes.LIST)
        return lists
            .asSequence()
            .map { it as? CompoundBinaryTag ?: throw IllegalArgumentException("Excepted CompoundBinaryTag but found ${it::class.simpleName} in registry value element.") }
            .onEach {
                require(it.has("name", Types.STRING)) { "Excepted StringBinaryTag (name) but found ${it["name"]}" }
                require(it.has("id", Types.INT)) { "Excepted IntBinaryTag (id) but found ${it["id"]}" }
                require(it.has("element", Types.COMPOUND)) { "Excepted CompoundBinaryTag (element) but found ${it["element"]}" }
            }
            .map { it.getExceptedString("name") }
            .toList()
    }

    fun interface PathFilter {
        fun filter(file: File, type: String?): Boolean

        open class ListPathFilter(private val filterList: Collection<String> = listOf(
            "minecraft:trim_pattern",
            "minecraft:trim_material",
            "minecraft:instrument",
            "minecraft:banner_pattern",
            "minecraft:enchantment",
            "minecraft:chat_type",
            "minecraft:painting_variant",
            "minecraft:jukebox_song",
            "minecraft:dimension_type",
            "minecraft:damage_type",
            "minecraft:worldgen/biome",
            "minecraft:wolf_variant"
        )) : PathFilter {
            override fun filter(file: File, type: String?): Boolean {
                if (file.isDirectory) {
                    if (file.isMatched("datapacks", "tags"))
                        return true
                } else if (file.name == "zero.json")
                    return true
                if (type != null) {
                    for (it in filterList) {
                        if (it.contains("/")) {
                            if (it.startsWith(type))
                                return false
                        } else if (it == type)
                            return false
                    }
                    return true
                }
                return false
            }
        }
    }

    fun interface ElementCleaner {
        fun clean(type: String, original: Collection<CompoundBinaryTag>): Collection<CompoundBinaryTag>?
        fun accept(original: CompoundBinaryTag) = original
            .associate { (type, tag) -> type to (tag as CompoundBinaryTag).getExcepted("value", Types.LIST) }
            .mapValues { (key, value) -> clean(key, value.map { it as CompoundBinaryTag }) }
            .mapNotNull { (key, value) -> if (value == null) null else key to value }
            .associate { (key, value) -> key to mapOf("type" to key.toTag(), "value" to value.toListTag()).toTag() }
            .toTag()
    }

    enum class PresentsCleaner(val cleaner: ElementCleaner? = null) {
        FULL(ElementCleaner { type, original ->
            when (type) {
                "minecraft:chat_type", "minecraft:damage_type" -> original
                "minecraft:dimension_type" -> listOf(modifyElement(original.first { it.getExceptedString("name") == "minecraft:overworld" }, "monster_spawn_light_level") {
                    IntBinaryTag.intBinaryTag(0)
                })
                "minecraft:painting_variant" -> listOf(original.first())
                "minecraft:wolf_variant" -> listOf(modifyElement(original.first { it.getExceptedString("name") == "minecraft:ashen" }, "biomes") { "minecraft:plains".toTag() })
                "minecraft:worldgen/biome" -> {
                    val lists = mutableListOf<CompoundBinaryTag>()
                    val ov = original.associate { (it.getExceptedString("name") to it.getExceptedCompound("compound")) }
                    fun clean(o: CompoundBinaryTag) = o.filterKeys(USELESS_BIOME_ELEMENTS::contains)
                    lists.add(mapOf("name" to "minecraft:plains".toTag(), "id" to 0.toTag(), "element" to clean(ov["minecraft:plains"]!!)).toTag())
                    lists.add(mapOf("name" to "minecraft:swamp".toTag(), "id" to 1.toTag(), "element" to clean(ov["minecraft:swamp"]!!)).toTag())
                    lists
                }
                else -> emptyList()
            }
        }),
        SIMPLE(ElementCleaner { type, original ->
            when (type) {
                "minecraft:worldgen/biome" -> original.map { o -> modifyElement(o, null, USELESS_BIOME_ELEMENTS::contains) }
                "minecraft:enchantment" -> original.map { o -> modifyElement(o, "effects") { (it as CompoundBinaryTag).filterKeys { key -> key == "minecraft:tick" } }}
                "minecraft:dimension_type" -> original.map { o -> modifyElement(o, "monster_spawn_light_level") { 0.toTag() } }
                else -> original
            }
        }),
        NONE(null)
    }

    companion object {
        private val USELESS_BIOME_ELEMENTS = setOf("features", "spawners", "carvers", "spawn_costs")

        fun modifyElement(original: CompoundBinaryTag, needModify: ((CompoundBinaryTag) -> Boolean)?, keyFilter: (String) -> Boolean): CompoundBinaryTag {
            val o = original.getExceptedCompound("element")
            if (needModify != null && !needModify(o)) return o
            return mapOf(
                "name" to original.getExcepted("name", Types.STRING),
                "id" to original.getExcepted("id", Types.INT),
                "element" to o.filterKeys(keyFilter)
            ).toTag()
        }

        fun modifyElement(original: CompoundBinaryTag, keyToModify: String, modifier: (BinaryTag) -> BinaryTag) =
            mapOf(
                "name" to original.getExcepted("name", Types.STRING),
                "id" to original.getExcepted("id", Types.INT),
                "element" to original
                    .getExceptedCompound("element")
                    .associate { (key, value) -> key to if (key == keyToModify) modifier(value) else value }
                    .toTag(),
            ).toTag()
    }

}