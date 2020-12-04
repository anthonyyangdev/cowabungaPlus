package cyr7.ir.interpret.heap

import cyr7.ir.interpret.Configuration
import cyr7.ir.interpret.exception.Trap
import kotlin.math.ceil
import kotlin.math.roundToLong
import kotlin.random.Random
import kotlin.random.asJavaRandom

class XiHeap(private val heapSizeMax: Long): IXiHeap {
    private val r = Random.asJavaRandom()
    private val heap: ArrayList<Long> = ArrayList()
    private val ws = Configuration.WORD_SIZE

    override fun malloc(size: Long): Long {
        if (size < 0) throw Trap("Invalid size")
        val allocatedSize = when {
            size % ws == 0L -> size
            else -> ceil(size.toDouble() / ws).roundToLong() * ws
        }
        val retval: Long = heap.size.toLong()
        if (retval + allocatedSize > heapSizeMax)
            throw Trap("Out of heap!")
        for (i in 0 until allocatedSize) {
            heap.add(r.nextLong())
        }
        return retval
    }

    override fun calloc(size: Long): Long {
        val retval = malloc(size)
        for (i in retval.toInt() until retval + size) {
            heap[i.toInt()] = 0L
        }
        return retval
    }

    override fun read(addr: Long): Long {
        val i = getMemoryIndex(addr).toInt()
        if (i >= heap.size) throw Trap("Attempting to read past end of heap")
        return heap[i]
    }

    override fun stringAt(addr: Long): String {
        val size = read(addr - ws)
        // Ignore the last entry, which is 0.
        return LongRange(0, size - 1).map { i ->
            read(addr + i * ws).toChar()
        }.joinToString("")
    }

    override fun store(addr: Long, value: Long) {
        val i = getMemoryIndex(addr).toInt()
        if (i >= heap.size) throw Trap("Attempting to store past end of heap")
        heap[i] = value
    }

    override fun storeString(value: String): Long {
        val len = value.length
        val ptr = malloc(((len + 2) * ws).toLong())
        store(ptr, len.toLong())
        for (i in 0 until len) {
            store(ptr + (i + 1) * ws, value[i].toLong())
        }
        store(ptr + (len + 1) * ws, 0)
        return ptr + ws
    }

    override fun free(addr: Long) {}

    private fun getMemoryIndex(addr: Long): Long {
        if (addr % Configuration.WORD_SIZE != 0L)
            throw Trap("Unaligned memory access: $addr (word size=${Configuration.WORD_SIZE})")
        return addr / Configuration.WORD_SIZE
    }
}

