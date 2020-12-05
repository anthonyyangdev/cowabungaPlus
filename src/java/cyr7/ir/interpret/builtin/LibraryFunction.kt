package cyr7.ir.interpret.builtin

import cyr7.ir.interpret.Configuration
import cyr7.ir.interpret.NumericValue
import cyr7.ir.interpret.SimulatorSettings

abstract class LibraryFunction(settings: SimulatorSettings) {
    protected val heap = settings.heap
    protected val stdout = settings.stdout
    protected val inReader = settings.inReader
    protected val ws = Configuration.WORD_SIZE
    abstract fun callName(): String
    abstract fun moduleName(): String
    abstract fun execute(args: LongArray): List<NumericValue>
}
