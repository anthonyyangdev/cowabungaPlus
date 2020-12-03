package cyr7.ir.interpret.builtin.io

import cyr7.ir.interpret.SimulatorSettings
import cyr7.ir.interpret.builtin.LibraryFunction
import java.io.File

class ReadFileLines(settings: SimulatorSettings): LibraryFunction(settings) {
    override fun callName(): String {
        return "_IreadFileLines_aaiai"
    }
    override fun moduleName(): String {
        return "io"
    }
    override fun execute(args: LongArray): List<Long> {
        val filename = heap.stringAt(args[0])
        val lines = File(filename).readLines()
        val ptr = heap.malloc((lines.size.toLong() + 2) * ws)
        heap.store(ptr, lines.size.toLong())
        val linePtrs = File(filename).readLines().map {
            heap.storeString(it)
        }
        for (i in linePtrs.indices) {
            heap.store(ptr + ws * (i + 1), linePtrs[i])
        }
        heap.store(ptr + ws * (linePtrs.size + 1), 0)
        return listOf(ptr + ws)
    }
}
