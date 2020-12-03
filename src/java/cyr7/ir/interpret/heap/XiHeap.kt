package cyr7.ir.interpret.heap

import cyr7.ir.interpret.Configuration
import cyr7.ir.interpret.IRSimulator.Trap
import kotlin.random.Random
import kotlin.random.asJavaRandom

open class XiHeap(private val heapSizeMax: Long) {
    private val r = Random.asJavaRandom()
    val heap: ArrayList<Long> = ArrayList()
    private val ws = Configuration.WORD_SIZE

    /**
     * Allocate a specified amount of bytes on the heap
     * @param size the number of bytes to be allocated
     * @return the starting address of the allocated region on the heap
     */
    fun malloc(size: Long): Long {
        if (size < 0) throw Trap("Invalid size")
        if (size % Configuration.WORD_SIZE != 0L)
            throw Trap("Can only allocate in chunks of ${Configuration.WORD_SIZE} bytes!")
        val retval: Long = heap.size.toLong()
        if (retval + size > heapSizeMax)
            throw Trap("Out of heap!")
        for (i in 0 until size) {
            heap.add(r.nextLong())
        }
        return retval
    }

    /**
     * Allocate a specified amount of bytes on the heap and initialize them to 0.
     * @param size the number of bytes to be allocated
     * @return the starting address of the allocated region on the heap
     */
    fun calloc(size: Long): Long {
        val retval = malloc(size)
        for (i in retval.toInt() until retval + size) {
            heap[i.toInt()] = 0L
        }
        return retval
    }

    /**
     * Read a value at the specified location on the heap
     * @param addr the address to be read
     * @return the value at `addr`
     */
    fun read(addr: Long): Long {
        val i = getMemoryIndex(addr).toInt()
        if (i >= heap.size) throw Trap("Attempting to read past end of heap")
        return heap[i]
    }

    fun stringAt(addr: Long): String {
        val size = read(addr - ws)
        return LongRange(0, size).map { i ->
            read(addr + i * ws).toChar()
        }.joinToString("")
    }

    /**
     * Write a value at the specified location on the heap
     * @param addr the address to be written
     * @param value the value to be written
     */
    fun store(addr: Long, value: Long) {
        val i = getMemoryIndex(addr).toInt()
        if (i >= heap.size) throw Trap("Attempting to store past end of heap")
        heap[i] = value
    }

    fun addString(string: String): Long {
        val len = string.length
        val ptr = malloc(((len + 1) * ws).toLong())
        store(ptr, len.toLong())
        for (i in 0 until len)
            store(ptr + (i + 1) * ws, string[i].toLong())
        return ptr + ws
    }

    fun getMemoryIndex(addr: Long): Long {
        if (addr % Configuration.WORD_SIZE != 0L)
            throw Trap("Unaligned memory access: $addr (word size=${Configuration.WORD_SIZE})")
        return addr / Configuration.WORD_SIZE
    }
}

