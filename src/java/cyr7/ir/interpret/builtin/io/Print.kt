package cyr7.ir.interpret.builtin.io

import cyr7.ir.interpret.SimulatorSettings
import cyr7.ir.interpret.builtin.LibraryFunction

class Print(settings: SimulatorSettings): LibraryFunction(settings) {
    override fun callName() = "_Iprint_pai"
    override fun moduleName() = "io"
    override fun execute(args: LongArray): List<Long> {
        val ptr = args[0]
        val size: Long = heap.read(ptr - ws)
        for (i in 0 until size)
            stdout.print(heap.read(ptr + i * ws).toChar())
        return emptyList()
    }
}
