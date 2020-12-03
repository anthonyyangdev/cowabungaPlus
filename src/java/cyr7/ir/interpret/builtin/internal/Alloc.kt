package cyr7.ir.interpret.builtin.internal

import cyr7.ir.interpret.builtin.LibraryFunction
import cyr7.ir.interpret.SimulatorSettings

class Alloc(settings: SimulatorSettings): LibraryFunction(settings) {
    override fun callName() = "_xi_alloc"
    override fun moduleName() = "internal"
    override fun execute(args: LongArray): List<Long> {
        return listOf(heap.calloc(args[0]))
    }
}
