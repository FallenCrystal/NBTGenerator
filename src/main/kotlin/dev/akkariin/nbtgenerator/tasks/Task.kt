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

package dev.akkariin.nbtgenerator.tasks

import dev.akkariin.nbtgenerator.args.ArgsParser
import dev.akkariin.nbtgenerator.inputFuture
import org.fusesource.jansi.Ansi
import java.io.File
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

@Suppress("unused")
abstract class Task(val folder: File) {

    private var stageListener: (Stage) -> Unit = {}
    @Suppress("MemberVisibilityCanBePrivate")
    var skippingTest = false

    private var totalTests = 0
    private var testFailed = 0
    private var testSuccess = 0
    private var testSkipped = 0

    abstract fun initialize(): Array<Stage>
    abstract fun execute(parser: ArgsParser)

    fun setStage(stage: Stage) {
        this.stageListener(stage)
    }

    fun setStageListener(listener: (Stage) -> Unit) {
        this.stageListener = listener
    }

    fun getInput() = try { inputFuture().get() } catch (e: Exception) { null }

    fun getInput(timeout: Long, timeUnit: TimeUnit, defaultValue: String): String? {
        val future = inputFuture()
        return try {
            future.get(timeout, timeUnit)
        } catch (e: TimeoutException) {
            future.complete(defaultValue)
            defaultValue
        } catch (e: Exception) {
            null
        }
    }

    fun runTests(testName: String, task: () -> Unit) {
        fun printResult(result: String, color: Ansi.Color) = println(Ansi
            .ansi()
            .fg(color)
            .a("  $result")
            .fg(Ansi.Color.WHITE)
            .a(" - Executed $totalTests tests. Skipped: $testSkipped, Success: $testSuccess, Failed: $testFailed")
        )
        println(Ansi.ansi().fg(Ansi.Color.WHITE).a("==================== T E S T S ====================").fg(Ansi.Color.DEFAULT))
        println("Executing \"$testName\" tests")
        if (!skippingTest) {
            try {
                task()
                testSuccess++
                printResult("TESTS SUCCESS", Ansi.Color.GREEN)
            } catch(t: Throwable) {
                testFailed++
                t.printStackTrace()
                printResult("TESTS FAILED", Ansi.Color.RED)
                throw TestFailedException(t)
            }
        } else {
            testSkipped++
            printResult("TESTS SKIPPED", Ansi.Color.YELLOW)
        }
    }

    class TestFailedException(cause: Throwable) : RuntimeException(cause)
}