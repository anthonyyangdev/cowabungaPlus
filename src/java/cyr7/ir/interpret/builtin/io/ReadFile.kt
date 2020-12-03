package cyr7.ir.interpret.builtin.io

import cyr7.ir.interpret.SimulatorSettings
import cyr7.ir.interpret.builtin.LibraryFunction
import java.io.File

class ReadFile(settings: SimulatorSettings): LibraryFunction(settings) {
    override fun callName(): String {
        return "_IreadFile_aiai"
    }
    override fun moduleName(): String {
        return "io"
    }
    override fun execute(args: LongArray): List<Long> {
        val filename = heap.stringAt(args[0])
        return listOf(heap.storeString(File(filename).readText()))
    }
}
