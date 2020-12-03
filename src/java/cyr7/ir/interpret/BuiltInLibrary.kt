package cyr7.ir.interpret

import cyr7.ir.interpret.IRSimulator.OutOfBoundTrap
import cyr7.ir.interpret.IRSimulator.Trap

abstract class LibraryFunction(settings: SimulatorSettings) {
    protected val heap = settings.heap
    protected val stdout = settings.stdout
    protected val inReader = settings.inReader
    abstract fun callName(): String
    abstract fun moduleName(): String
    abstract fun execute(args: LongArray): List<Long>
}

const val ws = Configuration.WORD_SIZE
class Print(settings: SimulatorSettings): LibraryFunction(settings) {
    override fun callName() = "_Iprint_pai"
    override fun moduleName() = "io"
    override fun execute(args: LongArray): List<Long> {
        val ptr = args[0]
        val size: Long = heap.read(ptr - ws)
        for (i in 0 until size)
            stdout.print(heap.read(ptr + i * ws).toChar())
        return emptyList()
    }
}

class PrintLn(settings: SimulatorSettings): LibraryFunction(settings) {
    override fun callName() = "_Iprintln_pai"
    override fun moduleName() = "io"
    override fun execute(args: LongArray): List<Long> {
        val ptr = args[0]
        val size = heap.read(ptr - ws)
        for (i in 0 until size)
            stdout.print(heap.read(ptr + i * ws).toChar())
        stdout.println()
        return emptyList()
    }
}

class ReadLn(settings: SimulatorSettings): LibraryFunction(settings) {
    override fun callName() = "_Ireadln_ai"
    override fun moduleName() = "io"
    override fun execute(args: LongArray): List<Long> {
        val line = inReader.readLine()
        val len = line.length
        val ptr = heap.malloc(((len + 1) * ws).toLong())
        heap.store(ptr, len.toLong())
        for (i in 0 until len)
            heap.store(ptr + (i + 1) * ws, line[i].toLong())
        return listOf(ptr + ws)
    }
}

class UnparseInt(settings: SimulatorSettings): LibraryFunction(settings) {
    override fun callName() = "_IunparseInt_aii"
    override fun moduleName() = "conv"
    override fun execute(args: LongArray): List<Long> {
        val line = args[0].toString()
        val len = line.length
        val ptr = heap.malloc(((len + 1) * ws).toLong())
        heap.store(ptr, len.toLong())
        for (i in 0 until len)
            heap.store(ptr + (i + 1) * ws, line[i].toLong())
        return listOf(ptr + ws)
    }
}

class GetChar(settings: SimulatorSettings): LibraryFunction(settings) {
    override fun callName() = "_Igetchar_i"
    override fun moduleName() = "io"
    override fun execute(args: LongArray) = listOf(inReader.read().toLong())
}

class Eof(settings: SimulatorSettings): LibraryFunction(settings) {
    override fun callName() = "_Ieof_b"
    override fun moduleName() = "io"
    override fun execute(args: LongArray) = listOf((if (inReader.ready()) 0 else 1).toLong())
}

class ParseInt(settings: SimulatorSettings): LibraryFunction(settings) {
    override fun callName() = "_IparseInt_t2ibai"
    override fun moduleName() = "conv"
    override fun execute(args: LongArray): List<Long> {
        val buf = StringBuilder()
        val ptr = args[0]
        val size = heap.read(ptr - ws)
        for (i in 0 until size) buf.append(heap.read(ptr + i * ws).toChar())
        val result = buf.toString().toLongOrNull()
        val success = if (result != null) 1L else 0L
        return listOf(result ?: 0L, success)
    }
}

class Alloc(settings: SimulatorSettings): LibraryFunction(settings) {
    override fun callName() = "_xi_alloc"
    override fun moduleName() = "internal"
    override fun execute(args: LongArray): List<Long> {
        return listOf(heap.calloc(args[0]))
    }
}

class OutOfBounds(settings: SimulatorSettings): LibraryFunction(settings) {
    override fun callName() = "_xi_out_of_bounds"
    override fun moduleName() = "internal"
    override fun execute(args: LongArray): List<Long> {
        throw OutOfBoundTrap("Out of bounds!")
    }
}

class Assert(settings: SimulatorSettings): LibraryFunction(settings) {
    override fun callName() = "_Iassert_pb"
    override fun moduleName() = "internal"
    override fun execute(args: LongArray): List<Long> {
        if (args[0] != 1L) throw Trap("Assertion error!")
        else return emptyList()
    }
}

class BuiltInLibrary(settings: SimulatorSettings) {
    private val libraryFunction = listOf(Print(settings), PrintLn(settings), Eof(settings),
            ReadLn(settings), GetChar(settings), UnparseInt(settings), ParseInt(settings),
            Alloc(settings), OutOfBounds(settings), Assert(settings))
            .map { it.callName() to it }.toMap()
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
}
