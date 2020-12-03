package cyr7.ir.interpret.builtin.string

import cyr7.ir.interpret.SimulatorSettings
import cyr7.ir.interpret.builtin.LibraryFunction

class CopyString(settings: SimulatorSettings): LibraryFunction(settings) {
    override fun callName(): String {
        return "_IcopyString_aiai"
    }

    override fun moduleName(): String {
        return "string"
    }

    override fun execute(args: LongArray): List<Long> {
        val ptr = args[0]
        val str = heap.stringAt(ptr)
        return listOf(heap.storeString(str))
    }
}
