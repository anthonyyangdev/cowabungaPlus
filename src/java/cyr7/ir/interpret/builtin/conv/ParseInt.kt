package cyr7.ir.interpret.builtin.conv

import cyr7.ir.interpret.builtin.LibraryFunction
import cyr7.ir.interpret.SimulatorSettings

class ParseInt(settings: SimulatorSettings): LibraryFunction(settings) {
    override fun callName() = "_IparseInt_t2ibai"
    override fun moduleName() = "conv"
    override fun execute(args: LongArray): List<Long> {
        val buf = StringBuilder()
        val ptr = args[0]
        val size = heap.read(ptr - ws)
        for (i in 0 until size) buf.append(heap.read(ptr + i * ws).toChar())
        val result = buf.toString().toLongOrNull()
        val success = if (result != null) 1L else 0L
        return listOf(result ?: 0L, success)
    }
}
