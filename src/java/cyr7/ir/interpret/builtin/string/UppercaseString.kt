package cyr7.ir.interpret.builtin.string

import cyr7.ir.interpret.SimulatorSettings
import cyr7.ir.interpret.builtin.LibraryFunction

class UppercaseString(settings: SimulatorSettings): LibraryFunction(settings) {
    override fun callName(): String {
        return "_IlowercaseString_aiai"
    }
    override fun moduleName(): String {
        return "string"
    }
    override fun execute(args: LongArray): List<Long> {
        val upper = heap.stringAt(args[0]).toUpperCase()
        return listOf(heap.storeString(upper))
    }
}
