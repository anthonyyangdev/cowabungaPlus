package cyr7.ir.interpret.builtin.timer

import cyr7.ir.interpret.builtin.LibraryFunction
import cyr7.ir.interpret.SimulatorSettings
import java.text.SimpleDateFormat
import java.util.*

class GetTimestamp(settings: SimulatorSettings): LibraryFunction(settings) {
    override fun callName() = "_IgetTimestamp_ai"
    override fun moduleName() = "timer"
    override fun execute(args: LongArray): List<Long> {
        val formatter = SimpleDateFormat("M/dd/yyyy hh:mm:ss", Locale.getDefault())
        val currentDate = formatter.format(Calendar.getInstance().time)
        return listOf(heap.addString(currentDate))
    }
}
