package cyr7.ir.interpret.builtin.string

import cyr7.ir.interpret.SimulatorSettings
import cyr7.ir.interpret.builtin.LibraryFunction

class Substring(settings: SimulatorSettings): LibraryFunction(settings) {
    override fun callName(): String {
        return "_Isubstring_aiaiii"
    }
    override fun moduleName(): String {
        return "string"
    }
    override fun execute(args: LongArray): List<Long> {
        val ptr = args[0]
        val start = args[1].toInt()
        val end = args[2].toInt()
        val sub = heap.stringAt(ptr).substring(start, end)
        return listOf(heap.storeString(sub))
    }
}
