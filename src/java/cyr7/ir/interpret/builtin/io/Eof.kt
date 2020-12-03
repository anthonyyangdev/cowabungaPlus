package cyr7.ir.interpret.builtin.io

import cyr7.ir.interpret.builtin.LibraryFunction
import cyr7.ir.interpret.SimulatorSettings

class Eof(settings: SimulatorSettings): LibraryFunction(settings) {
    override fun callName() = "_Ieof_b"
    override fun moduleName() = "io"
    override fun execute(args: LongArray) = listOf((if (inReader.ready()) 0 else 1).toLong())
}
