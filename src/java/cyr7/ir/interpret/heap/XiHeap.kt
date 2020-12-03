package cyr7.ir.interpret.heap

import cyr7.ir.interpret.Configuration
import cyr7.ir.interpret.IRSimulator.Trap
import kotlin.math.ceil
import kotlin.math.roundToLong
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
        var allocatedSize = size
        if (size < 0) throw Trap("Invalid size")
        if (size % Configuration.WORD_SIZE != 0L) {
            allocatedSize = ceil(size.toDouble() / ws).roundToLong() * ws
        }
        val retval: Long = heap.size.toLong()
        if (retval + allocatedSize > heapSizeMax)
            throw Trap("Out of heap!")
        for (i in 0 until allocatedSize) {
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

    /**
     * Returns the string at the address in the heap.
     */
    fun stringAt(addr: Long): String {
        val size = read(addr - ws)
        // Ignore the last entry, which is 0.
        return LongRange(0, size - 1).map { i ->
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

    /**
     * Adds a string to the heap and returns the pointer to that string.
     */
    fun storeString(string: String): Long {
        val len = string.length
        val ptr = malloc(((len + 2) * ws).toLong())
        store(ptr, len.toLong())
        for (i in 0 until len) {
            store(ptr + (i + 1) * ws, string[i].toLong())
        }
        store(ptr + (len + 1) * ws, 0)
        return ptr + ws
    }

    private fun getMemoryIndex(addr: Long): Long {
        if (addr % Configuration.WORD_SIZE != 0L)
            throw Trap("Unaligned memory access: $addr (word size=${Configuration.WORD_SIZE})")
        return addr / Configuration.WORD_SIZE
    }
}

