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

package dev.akkariin.nbtgenerator

import dev.akkariin.nbtgenerator.args.Arg
import dev.akkariin.nbtgenerator.args.ArgsParser
import dev.akkariin.nbtgenerator.tasks.Stage
import dev.akkariin.nbtgenerator.tasks.Task
import dev.akkariin.nbtgenerator.tasks.impl.DownloadTask
import dev.akkariin.nbtgenerator.tasks.impl.RegistryGeneratorTask
import org.fusesource.jansi.Ansi
import org.fusesource.jansi.AnsiConsole
import java.io.File
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.system.exitProcess

private val inputQueue: ArrayDeque<CompletableFuture<String?>> = ArrayDeque()
private var stopped = false
private val inputThread = Thread {
    val scanner = Scanner(System.`in`)
    while (!stopped) {
        val input = try {
            scanner.nextLine()
        } catch (_: Exception) {
            null
        }
        var future: CompletableFuture<String?>? = null
        while (inputQueue.isNotEmpty()) {
            future = inputQueue.pollFirst()
            if (future == null || future.isCancelled || future.isCompletedExceptionally || future.isDone) {
                future = null
            } else break
        }
        future?.complete(input)
    }
}.apply {
    name = "InputWaiter"
    priority = Thread.MIN_PRIORITY
    isDaemon = true
}

fun main(args: Array<String>) {
    AnsiConsole.systemInstall()
    inputThread.start()
    val parser = ArgsParser(args)
    val version = parser.parse(Arg.of("version", "Specify the Minecraft version to download.", "null"))
    var folder = File(System.getProperty("user.dir"))
    val output = File(folder, parser.parse(Arg.of("output", "Output file name", if (version == "null") "output.nbt" else version.replace(".", "_") + ".nbt")))
    val skippingTest = parser.parse(Arg.of("skipTests", "", false))
    if (version != "null") {
        val task = DownloadTask(folder)
        runTask(task, parser, skippingTest)
        task.downloadFolder?.apply { folder = this }
        println("Redirect generated folder to $folder")
    }
    runTask(RegistryGeneratorTask(File(folder, "generated"), output), parser, skippingTest)
    AnsiConsole.systemUninstall()
}

private fun runTask(task: Task, parser: ArgsParser, skippingTest: Boolean) {
    val start = System.currentTimeMillis()
    val stages = task.initialize()
    task.skippingTest = skippingTest
    val map = stages.mapIndexed { i, stage -> stage to i }.toMap()
    var lastStage: Stage? = null
    task.setStageListener {
        lastStage = it
        println(Ansi.ansi().fg(Ansi.Color.CYAN).a("Stage: ${it.name} - ${(map[it] ?: -2) + 1}/${stages.size}").fg(Ansi.Color.DEFAULT))
    }
    try {
        task.execute(parser)
    } catch (t: Throwable) {
        println("========================================")
        println(Ansi.ansi().fg(Ansi.Color.RED).a("TASKS FAILED").fg(Ansi.Color.DEFAULT))
        println("")
        println("Tasks ${task::class.simpleName} has failed after ${(System.currentTimeMillis() - start).let { if (it >= 1000) it / 1000 else 0 }} second(s).")
        println("")
        if (lastStage != null && stages.isNotEmpty()) {
            val s = lastStage!!
            println(Ansi.ansi().fg(Ansi.Color.YELLOW).a("Stage:").fg(Ansi.Color.DEFAULT))
            println("  Name: ${s.name}")
            println("  Description: ${s.description}")
            println("  Position: ${(map[s] ?: -2) + 1}/${stages.size}")
            val suggestion = s.suggestion()
            if (suggestion != null) {
                println("  Suggestion(s):")
                suggestion.forEach { println("  - $it") }
            } else {
                println("  Suggestion: N/A")
            }
        } else {
            println(Ansi.ansi().fg(Ansi.Color.YELLOW).a("Stage:").fgBrightBlack().a(" N/A").fg(Ansi.Color.DEFAULT))
        }
        println("")
        var e: Throwable? = t
        while (e != null) {
            println("Caused by: ${e::class.java.name}: ${e.message ?: "null"}")
            println("Stacktrace:")
            for (stack in e.stackTrace) {
                val split = stack.className.split(".")
                val color = when {
                    split.contains("MainKt") -> Ansi.Color.MAGENTA
                    task::class.simpleName?.let { split.last().startsWith(it) } == true -> Ansi.Color.YELLOW
                    else -> Ansi.Color.CYAN
                }
                println(Ansi
                    .ansi()
                    .fg(color)
                    .a("  ${stack.classLoaderName}@${stack.className}#${stack.methodName}")
                    .fg(Ansi.Color.DEFAULT)
                    .a("(${if (stack.isNativeMethod) "Native Method" else "${stack.fileName ?: "?"}:${stack.lineNumber}"})")
                )
            }
            println("")
            e = e.cause
        }
        exitProcess(-1)
    }
    println("========================================")
    println(Ansi.ansi().fg(Ansi.Color.GREEN).a("Task successfully after ${(System.currentTimeMillis() - start).let { if (it >= 1000) it / 1000 else 0 }} second(s).").fg(Ansi.Color.DEFAULT))
    println("")
}

fun inputFuture() = CompletableFuture<String?>().apply(inputQueue::addLast)