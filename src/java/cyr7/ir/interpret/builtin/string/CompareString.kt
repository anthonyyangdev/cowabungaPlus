package cyr7.ir.interpret.builtin.string

import cyr7.ir.interpret.SimulatorSettings
import cyr7.ir.interpret.builtin.LibraryFunction

class CompareString(settings: SimulatorSettings): LibraryFunction(settings) {
    override fun callName(): String {
        return "_IcompareString_iaiai"
    }
    override fun moduleName(): String {
        return "string"
    }
    override fun execute(args: LongArray): List<Long> {
        val str1 = heap.stringAt(args[0])
        val str2 = heap.stringAt(args[1])
        return listOf(str1.compareTo(str2).toLong())
    }
}
