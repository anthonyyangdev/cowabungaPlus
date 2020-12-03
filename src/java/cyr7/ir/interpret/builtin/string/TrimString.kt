package cyr7.ir.interpret.builtin.string

import cyr7.ir.interpret.SimulatorSettings
import cyr7.ir.interpret.builtin.LibraryFunction

class TrimString(settings: SimulatorSettings): LibraryFunction(settings) {
    override fun callName(): String {
        return "_ItrimString_aiai"
    }
    override fun moduleName(): String {
        return "string"
    }
    override fun execute(args: LongArray): List<Long> {
        val trimmed = heap.stringAt(args[0]).trim()
        return listOf(heap.storeString(trimmed))
    }
}
