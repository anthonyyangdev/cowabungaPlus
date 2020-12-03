package cyr7.ir.interpret

import cyr7.ir.interpret.heap.IXiHeap
import java.io.BufferedReader
import java.io.PrintStream

data class SimulatorSettings(
        val heap: IXiHeap,
        val inReader: BufferedReader,
        val stdout: PrintStream
)
