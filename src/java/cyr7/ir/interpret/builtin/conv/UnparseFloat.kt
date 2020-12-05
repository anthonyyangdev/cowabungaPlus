package cyr7.ir.interpret.builtin.conv

import cyr7.ir.interpret.SimulatorSettings
import cyr7.ir.interpret.builtin.LibraryFunction

class UnparseFloat(settings: SimulatorSettings): LibraryFunction(settings) {
    override fun callName() = "_IunparseFloat_aif"
    override fun moduleName() = "conv"
    override fun execute(args: LongArray): List<Long> {
        val value = Double.fromBits(args[0]).toString()
        return listOf(heap.storeString(value))
    }
}
