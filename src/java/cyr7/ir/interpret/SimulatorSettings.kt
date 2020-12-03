package cyr7.ir.interpret

import cyr7.ir.interpret.heap.XiHeap
import java.io.BufferedReader
import java.io.PrintStream

data class SimulatorSettings(
        val heap: XiHeap,
        val inReader: BufferedReader,
        val stdout: PrintStream
)
