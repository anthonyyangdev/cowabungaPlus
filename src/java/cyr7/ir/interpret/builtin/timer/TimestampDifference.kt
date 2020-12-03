package cyr7.ir.interpret.builtin.timer

import cyr7.ir.interpret.builtin.LibraryFunction
import cyr7.ir.interpret.SimulatorSettings
import java.text.SimpleDateFormat
import java.util.*

class TimestampDifference(settings: SimulatorSettings): LibraryFunction(settings) {
    override fun callName() = "_ItimestampDifference_iaiai"
    override fun moduleName() = "timer"
    override fun execute(args: LongArray): List<Long> {
        val ptr1 = args[0]
        val stamp1 = heap.stringAt(ptr1)
        val ptr2 = args[1]
        val stamp2 = heap.stringAt(ptr2)

        val sdf = SimpleDateFormat("M/dd/yyyy hh:mm:ss", Locale.getDefault())
        val date1 = sdf.parse(stamp1)
        val date2 = sdf.parse(stamp2)
        return listOf(date2.time - date1.time)
    }
}
