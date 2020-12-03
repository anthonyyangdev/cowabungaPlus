package cyr7.ir.interpret.builtin.io

import cyr7.ir.interpret.builtin.LibraryFunction
import cyr7.ir.interpret.SimulatorSettings

class ReadLn(settings: SimulatorSettings): LibraryFunction(settings) {
    override fun callName() = "_Ireadln_ai"
    override fun moduleName() = "io"
    override fun execute(args: LongArray): List<Long> {
        val line = inReader.readLine()
        return listOf(heap.addString(line))
    }
}
