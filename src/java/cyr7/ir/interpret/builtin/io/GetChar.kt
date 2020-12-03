package cyr7.ir.interpret.builtin.io

import cyr7.ir.interpret.builtin.LibraryFunction
import cyr7.ir.interpret.SimulatorSettings

class GetChar(settings: SimulatorSettings): LibraryFunction(settings) {
    override fun callName() = "_Igetchar_i"
    override fun moduleName() = "io"
    override fun execute(args: LongArray) = listOf(inReader.read().toLong())
}
