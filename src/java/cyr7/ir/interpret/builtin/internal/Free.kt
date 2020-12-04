package cyr7.ir.interpret.builtin.internal

import cyr7.ir.interpret.SimulatorSettings
import cyr7.ir.interpret.builtin.LibraryFunction

class Free(settings: SimulatorSettings): LibraryFunction(settings) {
    override fun callName() = "_free_memory_"

    override fun moduleName() = "internal"

    override fun execute(args: LongArray): List<Long> {
        val ptr = args[0]
        heap.free(ptr - ws)
        return emptyList()
    }
}
