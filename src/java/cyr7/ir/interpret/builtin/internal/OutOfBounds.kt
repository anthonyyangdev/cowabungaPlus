package cyr7.ir.interpret.builtin.internal

import cyr7.ir.interpret.builtin.LibraryFunction
import cyr7.ir.interpret.SimulatorSettings
import cyr7.ir.interpret.exception.OutOfBoundTrap

class OutOfBounds(settings: SimulatorSettings): LibraryFunction(settings) {
    override fun callName() = "_xi_out_of_bounds"
    override fun moduleName() = "internal"
    override fun execute(args: LongArray): List<Long> {
        throw OutOfBoundTrap("Out of bounds!")
    }
}
