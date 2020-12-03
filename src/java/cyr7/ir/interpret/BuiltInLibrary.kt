package cyr7.ir.interpret

import cyr7.ir.interpret.builtin.LibraryFunction
import cyr7.ir.interpret.builtin.assert.Assert
import cyr7.ir.interpret.builtin.conv.ParseInt
import cyr7.ir.interpret.builtin.conv.UnparseInt
import cyr7.ir.interpret.builtin.internal.Alloc
import cyr7.ir.interpret.builtin.internal.OutOfBounds
import cyr7.ir.interpret.builtin.io.*
import cyr7.ir.interpret.builtin.string.*
import cyr7.ir.interpret.builtin.timer.GetTimestamp
import cyr7.ir.interpret.builtin.timer.TimestampDifference

class BuiltInLibrary(settings: SimulatorSettings) {
    private val ioFunctions = listOf(
            Print(settings), PrintLn(settings), Eof(settings),
            ReadLn(settings), GetChar(settings), ReadFile(settings),
            AppendFile(settings), ReadFileLines(settings), WriteFile(settings)
    )
    private val convFunctions = listOf(
            UnparseInt(settings), ParseInt(settings)
    )
    private val internalFunctions = listOf(
            Alloc(settings), OutOfBounds(settings)
    )
    private val assertFunctions = listOf(Assert(settings))
    private val timerFunctions = listOf(GetTimestamp(settings), TimestampDifference(settings))
    private val stringFunctions = listOf(
            Substring(settings), CopyString(settings), CompareString(settings),
            IndexOfString(settings), LowercaseString(settings), UppercaseString(settings),
            TrimString(settings), TrimStringLeft(settings), TrimStringRight(settings)
    )
    private val libraryFunction = (ioFunctions + convFunctions + internalFunctions
            + assertFunctions + timerFunctions + stringFunctions).map { it.callName() to it }.toMap()
    fun contains(name: String) = libraryFunction.containsKey(name)

    /**
     * Simulate a library function call, returning the list of returned values
     * @param name name of the function call
     * @param args arguments to the function call, which may include
     *          the pointer to the location of multiple results
     */
    fun call(name: String, args: LongArray): List<Long>? {
        return libraryFunction[name]?.execute(args)
    }

    fun get(name: String): LibraryFunction? {
        return libraryFunction[name]
    }
}
