package cyr7.ir.interpret.builtin.string

import cyr7.ir.interpret.SimulatorSettings
import cyr7.ir.interpret.builtin.LibraryFunction

class IndexOfString(settings: SimulatorSettings): LibraryFunction(settings) {
    override fun callName(): String {
        return "_IindexOfString_iaiai"
    }
    override fun moduleName(): String {
        return "string"
    }
    override fun execute(args: LongArray): List<Long> {
        val str = heap.stringAt(args[0])
        val sub = heap.stringAt(args[1])
        return listOf(str.indexOf(sub).toLong())
    }
}
