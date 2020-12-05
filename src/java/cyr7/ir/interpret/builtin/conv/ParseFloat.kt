package cyr7.ir.interpret.builtin.conv

import cyr7.ir.interpret.SimulatorSettings
import cyr7.ir.interpret.builtin.LibraryFunction

class ParseFloat(settings: SimulatorSettings): LibraryFunction(settings) {
    override fun callName() = "_IparseFloat_fai"
    override fun moduleName() = "conv"
    override fun execute(args: LongArray): List<Long> {
        val result = heap.stringAt(args[0]).toDouble().toRawBits()
        return listOf(result)
    }
}
