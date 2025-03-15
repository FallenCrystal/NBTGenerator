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
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import dev.akkariin.nbtgenerator.args.Arg
import dev.akkariin.nbtgenerator.args.ArgsParser
import dev.akkariin.nbtgenerator.tasks.Stage
import dev.akkariin.nbtgenerator.tasks.Task
import dev.akkariin.nbtgenerator.tasks.impl.RegistryGeneratorTask.ElementCleaner
import dev.akkariin.nbtgenerator.util.*
import net.kyori.adventure.nbt.*
import org.fusesource.jansi.Ansi
import java.io.File
import java.io.FileReader
import java.util.concurrent.TimeUnit

private typealias Types = BinaryTagTypes

class RegistryGeneratorTask(
    folder: File,
    private val output: File?,
    private val cleaner: PresentsCleaner? = null,
    private val disablePathFilter: Boolean = false
) : Task(folder) {
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

    var result: CompoundBinaryTag? = null

    override fun execute(parser: ArgsParser) {
        setStage(lookingForPath)
        val path = searchPath()
        println("Set ${path.absolutePath} as registry path.")
        setStage(collectData)
        val pathFilter = if (this.disablePathFilter
            || parser.parse(Arg.of("disable-path-filter", "Disable the path filter for registry", false))) null
        else PathFilter.ListPathFilter()
        val builder = CompoundBinaryTag.builder()
        for (folder in path.listFiles()!!) {
            if (!folder.isDirectory) continue
            searchFolder(null, folder, builder, pathFilter)
        }
        var compound = builder.build()
        setStage(applyCleaner)
        val cleaner = this.cleaner
            ?: when (parser
                .parse(Arg.of("cleaner", "Default cleaner for registry output", "null"))
                .takeUnless { it == "null" }
                ?: run {
                    println("What cleaner do you want to apply for output? (Timeout: 5s, Default value for SIMPLE)")
                    println("0: NONE, 1: SIMPLE, 2: FULL, 3: ID_ONLY")
                    println("TIP: You can run application with \"--cleaner [choose]\" parameters. You can also specify the cleaner name instead of a number.")
                    println("TIP: Are you try to generate entire registry without path filter? Add \"--disable-path-filter true\" at the end.")
                    getInput(5, TimeUnit.SECONDS, "1") ?: "1"
                }
            ) {
                "0", "NONE", "none" -> PresentsCleaner.NONE
                "1", "SIMPLE", "simple" -> PresentsCleaner.SIMPLE
                "2", "FULL", "full" -> PresentsCleaner.FULL
                "3", "ID", "id", "ID_ONLY", "id_only" -> PresentsCleaner.ID_ONLY
                else -> {
                    println("Unknown input. Selecting default value (SIMPLE).")
                    PresentsCleaner.SIMPLE
                }
            }
        println("Apply ${cleaner.name} as element cleaner.")
        compound = cleaner.cleaner?.accept(compound) ?: compound
        setStage(runTest)
        doTests(cleaner, compound)
        result = compound
        setStage(saveToFile)
        if (output == null) {
            println(Ansi.ansi().fg(Ansi.Color.YELLOW).a("No output directory specified. Skipping saving to file.").fg(Ansi.Color.DEFAULT))
        } else {
            BinaryTagIO.writer().write(compound, output.removeAndCreate().toPath(), BinaryTagIO.Compression.GZIP)
        }
    }

    private fun doTests(cleaner: PresentsCleaner, compound: CompoundBinaryTag) {
        if (cleaner == PresentsCleaner.ID_ONLY) {
            println(Ansi.ansi().fg(Ansi.Color.GREEN).a("Skipping tests because cleaner is ID_ONLY").fg(Ansi.Color.DEFAULT))
            return
        }
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
            compound
                .getExceptedCompound("minecraft:worldgen/biome")
                .getExcepted("value", Types.LIST)
                .toCompoundList()
                .map { it.getExceptedString("name") to it.getExceptedCompound("element") }
                .forEach { (name, element) ->
                    require(element.has("temperature", Types.FLOAT)) { "$name doesn't have temperature as float." }
                    require(element.has("downfall", Types.FLOAT)) { "$name doesn't have downfall as float." }
                    val effects = element.getExceptedCompound("effects")
                    arrayOf("sky_color", "water_fog_color", "fog_color", "water_color").forEach {
                        require(effects.has(it, Types.INT)) { "$name doesn't have $it in their effects" }
                    }
                    val moodSound = effects.getExceptedCompound("mood_sound")
                    mapOf(
                        "tick_delay" to Types.INT,
                        "offset" to Types.DOUBLE,
                        "sound" to Types.STRING,
                        "block_search_extent" to Types.INT
                    ).forEach {
                        require(moodSound.has(it.key, it.value))
                        { "$name doesn't have mood_sound.${it.key} (${it.value::class.simpleName}) in their effects" }
                    }
                }
        }
        runTests("Check minecraft:wolf_variant") {
            if (compound["minecraft:wolf_variant"] != null) {
                val names = testGetTypeAsKeyList(compound, "minecraft:wolf_variant")
                require(names.contains("minecraft:ashen")) { "Registries doesn't have minecraft:ashen in minecraft:wolf_variant" }
                val registeredBiomes = compound
                    .getExceptedCompound("minecraft:worldgen/biome")
                    .getExcepted("value", Types.LIST)
                    .toCompoundList()
                    .map { it.getExceptedString("name") }
                val variants = compound
                    .getExceptedCompound("minecraft:wolf_variant")
                    .getExcepted("value", Types.LIST)
                    .toCompoundList()
                    .associate { it.getExceptedString("name") to it.getExceptedCompound("element") }
                for ((name, element) in variants) {
                    // Check biomes
                    try {
                        val biomes = if (element.has("spawn_conditions", BinaryTagTypes.LIST)) {
                            element
                                .getExcepted("spawn_conditions", BinaryTagTypes.LIST)
                                .asSequence()
                                .map { it as CompoundBinaryTag }
                                .apply { forEach { it.has("priority", BinaryTagTypes.INT) } }
                                .filter { it.has("condition", BinaryTagTypes.COMPOUND) }
                                .map { it.getExceptedCompound("condition") }
                                .filter { it.getExceptedString("type") != "minecraft:biome" }
                                .map { it.getExceptedString("biomes") }
                                .toList()
                        } else if (element.hasString("biomes")) {
                            listOf(element.getExceptedString("biomes"))
                        } else {
                            throw IllegalArgumentException("Cannot found biomes in $name")
                        }

                        for (biome in biomes) {
                            if (biome.startsWith("#")) continue
                            require(registeredBiomes.contains(biome)) { "Wolf variant $name that required $biome to spawn, But not found in registry." }
                        }

                        // Check assets
                        if (element.has("assets", BinaryTagTypes.COMPOUND)) {
                            val assets = element.getExceptedCompound("assets")
                            for (key in listOf("angry", "tame", "wild")) {
                                require(assets.hasString(key)) { "Not found key $key in assets in wolf variant $name" }
                            }
                        } else {
                            for (key in listOf("angry", "tame", "wild")) {
                                require(element.hasString("${key}_texture"))
                                { "Not found key ${key}_texture wolf variant $name" }
                            }
                        }
                    } catch (e: Exception) {
                        throw TestFailedException(e, "Failed to tests for $name")
                    }
                }
            }
        }
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
                    val json = Gson().fromJson(FileReader(file), JsonElement::class.java)
                    if (json !is JsonObject) continue
                    tags.add(mapOf(
                        "id" to index++.toTag(),
                        "name" to "minecraft:${file.name.removeSuffix(".json")}".toTag(),
                        "element" to json.serializeTag(lazilyFunc = { (key, number) ->
                            if (number.toString().contains("."))
                                if (key != null && FLOAT_KEYS.contains(key))
                                    number.toFloat().toTag()
                                else
                                    number.toDouble().toTag()
                            else number.toInt().toTag()
                        })
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
        val compound = tag.getExceptedCompound(type)
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
            override fun filter(file: File, type: String?) = when {
                file.isDirectory -> file.isMatched("datapacks", "tags")
                file.name == "zero.json" -> true
                type == null -> false
                else -> !filterList.any { it == type || (it.contains("/") && it.startsWith(type)) }
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
        ID_ONLY(ElementCleaner { _, original -> original.map { compound -> CompoundBinaryTag
            .builder()
            .put("name", compound.getExcepted("name", BinaryTagTypes.STRING))
            .put("id", compound.getExcepted("id", BinaryTagTypes.INT))
            .build()
        } }),
        FULL(ElementCleaner { type, original ->
            when (type) {
                "minecraft:chat_type", "minecraft:damage_type" -> original
                "minecraft:dimension_type" -> listOf(modifyElement(original.first { it.getExceptedString("name") == "minecraft:overworld" }, "monster_spawn_light_level") {
                    IntBinaryTag.intBinaryTag(0)
                })
                "minecraft:painting_variant" -> listOf(original.first())
                "minecraft:wolf_variant" -> {
                    val element = original.first { it.getExceptedString("name") == "minecraft:ashen" }.getExceptedCompound("element")
                    listOf(CompoundBinaryTag
                        .builder()
                        .putString("name", "minecraft:ashen")
                        .putInt("id", 0)
                        .put("element", if (element.has("spawn_conditions", BinaryTagTypes.LIST)) {
                            mapOf(
                                "assets" to element.getExceptedCompound("assets"),
                                "spawn_conditions" to mapOf("priority" to 1.toTag()).toTag().singletonTag(BinaryTagTypes.COMPOUND)
                            )
                        } else {
                            mapOf(
                                "angry_texture" to element.getExcepted("angry_texture", BinaryTagTypes.STRING),
                                "tame_texture" to element.getExcepted("tame_texture", BinaryTagTypes.STRING),
                                "wild_texture" to element.getExcepted("wild_texture", BinaryTagTypes.STRING),
                                "biomes" to "minecraft:plains".toTag()
                            )
                        }.toTag())
                        .build()
                    )
                }
                "minecraft:worldgen/biome" -> {
                    val lists = mutableListOf<CompoundBinaryTag>()
                    val ov = original.associate { (it.getExceptedString("name") to it.getExceptedCompound("element")) }
                    fun clean(o: CompoundBinaryTag) = o.removeKeys(USELESS_BIOME_ELEMENTS::contains)
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
                "minecraft:enchantment" -> original.map { o -> modifyElement(o, "effects") { (it as CompoundBinaryTag).removeKeys { key -> key == "minecraft:tick" } }}
                "minecraft:dimension_type" -> original.map { o -> modifyElement(o, "monster_spawn_light_level") { 0.toTag() } }
                else -> original
            }
        }),
        NONE(null)
    }

    companion object {
        private val USELESS_BIOME_ELEMENTS = setOf("features", "spawners", "carvers", "spawn_costs")
        private val FLOAT_KEYS = setOf(
            "depth", "temperature", "scale", "downfall", //biome
            "exhaustion", // damage type
            "ambient_light" // dimension type
        )

        fun modifyElement(original: CompoundBinaryTag, needModify: ((CompoundBinaryTag) -> Boolean)?, keyFilter: (String) -> Boolean): CompoundBinaryTag {
            val o = original.getExceptedCompound("element")
            if (needModify != null && !needModify(o)) return o
            return mapOf(
                "name" to original.getExcepted("name", Types.STRING),
                "id" to original.getExcepted("id", Types.INT),
                "element" to o.removeKeys(keyFilter)
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