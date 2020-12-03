package cyr7.ir.interpret.builtin.io

import cyr7.ir.interpret.builtin.LibraryFunction
import cyr7.ir.interpret.SimulatorSettings

class PrintLn(settings: SimulatorSettings): LibraryFunction(settings) {
    override fun callName() = "_Iprintln_pai"
    override fun moduleName() = "io"
    override fun execute(args: LongArray): List<Long> {
        val ptr = args[0]
        val size = heap.read(ptr - ws)
        for (i in 0 until size)
            stdout.print(heap.read(ptr + i * ws).toChar())
        stdout.println()
        return emptyList()
    }
}
