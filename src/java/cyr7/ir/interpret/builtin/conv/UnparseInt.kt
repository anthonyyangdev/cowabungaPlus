package cyr7.ir.interpret.builtin.conv

import cyr7.ir.interpret.builtin.LibraryFunction
import cyr7.ir.interpret.SimulatorSettings

class UnparseInt(settings: SimulatorSettings): LibraryFunction(settings) {
    override fun callName() = "_IunparseInt_aii"
    override fun moduleName() = "conv"
    override fun execute(args: LongArray): List<Long> {
        val line = args[0].toString()
        return listOf(heap.addString(line))
    }
}
