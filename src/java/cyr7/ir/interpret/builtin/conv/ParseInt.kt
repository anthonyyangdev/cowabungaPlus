package cyr7.ir.interpret.builtin.conv

import cyr7.ir.interpret.builtin.LibraryFunction
import cyr7.ir.interpret.SimulatorSettings

class ParseInt(settings: SimulatorSettings): LibraryFunction(settings) {
    override fun callName() = "_IparseInt_t2ibai"
    override fun moduleName() = "conv"
    override fun execute(args: LongArray): List<Long> {
        val result = heap.stringAt(args[0]).toLongOrNull()
        val success = if (result != null) 1L else 0L
        return listOf(result ?: 0L, success)
    }
}
