package cyr7.ir.interpret.builtin.io

import cyr7.ir.interpret.SimulatorSettings
import cyr7.ir.interpret.builtin.LibraryFunction
import java.io.File

class AppendFile(settings: SimulatorSettings): LibraryFunction(settings) {
    override fun callName(): String {
        return "_IappendFile_paiai"
    }
    override fun moduleName(): String {
        return "io"
    }
    override fun execute(args: LongArray): List<Long> {
        val filename = heap.stringAt(args[0])
        val data = heap.stringAt(args[1])
        File(filename).appendText(data)
        return emptyList()
    }
}
