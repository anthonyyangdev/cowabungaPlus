package cyr7.ir.interpret.heap

import cyr7.ir.interpret.Configuration
import cyr7.ir.interpret.IRSimulator
import kotlin.collections.ArrayList
import kotlin.math.ceil
import kotlin.math.roundToLong

class DynamicXiHeap: IXiHeap {

    private fun Long.asInts(): Pair<Int, Int> {
        return ushr(32).toInt() to toInt()
    }

    private val nullPoint = Int.MIN_VALUE
    private val ws = Configuration.WORD_SIZE

    private val heap: ArrayList<Long> = arrayListOf()
    init {
        heap[0] = 2
        heap[1] = 0
        heap[2] = Long.MAX_VALUE
        heap[3] = nullPoint.toLong()
        heap[4] = nullPoint.toLong()
    }
    private data class FreeBlock(val size: Long, val prev: Int, val next: Int)
    private fun getBlock(idx: Int): FreeBlock {
        return FreeBlock(heap[idx], heap[idx + 1].toInt(), heap[idx + 2].toInt())
    }

    /**
     * Finds the next free block with enough requested `size`
     * @return index of the heap.
     */
    private fun allocate(size: Long): Int {
        assert(size % ws == 0L)

        val adjustedSize = size + ws
        var headIdx = heap[0].toInt()
        while (headIdx > 0) {
            val metadata = getBlock(headIdx)
            val availableSize = metadata.size
            if (availableSize >= adjustedSize) {
                assert(heap[headIdx - 1].asInts().first == 0)
                val remainingSpace = availableSize - adjustedSize
                if (remainingSpace >= ws * 3) {
                    // Can create new free block
                    // Buffer blocks
                    val nextIdx = headIdx + (adjustedSize % ws).toInt()
                    heap[headIdx - 1] = nextIdx.toLong()
                    heap[nextIdx - 1] = 0

                    val prev = metadata.prev
                    val next = metadata.next
                    heap[nextIdx] = remainingSpace
                    heap[nextIdx + 1] = prev.toLong()
                    heap[nextIdx + 2] = next.toLong()
                    if (prev != nullPoint) heap[prev + 2] = nextIdx.toLong()
                    if (next != nullPoint) heap[next + 1] = nextIdx.toLong()
                } else {
                    val nextIdx = headIdx + (availableSize % ws).toInt()
                    heap[headIdx - 1] = nextIdx.toLong()
                    heap[nextIdx - 1] = 0
                }
                return headIdx
            } else {
                headIdx = metadata.next
            }
        }
        throw IRSimulator.Trap("Out of heap!")
    }

    override fun free(addr: Long) {
        val idx = getMemoryIndex(addr) - 1
        val headIdx = heap[0].toInt()
        heap[headIdx + 1] = idx.toLong()
        heap[idx + 1] = nullPoint.toLong()
        heap[idx + 2] = headIdx.toLong()
        heap[0] = idx.toLong()

        val bufferBlock = heap[idx].asInts()
        heap[idx] = bufferBlock.second.toLong()

        val nextIdx = bufferBlock.second
        val nextBufferBlock = heap[nextIdx - 1].asInts()

        // Check if block after is free
        if (nextBufferBlock.second == 0) {
            heap[idx] += (heap[bufferBlock.second])
        }

        // Check if block before is free
        if (bufferBlock.first > 0) {
            heap[bufferBlock.first] += heap[idx]
        }
    }

    override fun malloc(size: Long): Long {
        val allocatedSize = when {
            size % ws == 0L -> size
            else -> ceil(size.toDouble() / ws).roundToLong() * ws
        }
        return allocate(allocatedSize).toLong()
    }

    override fun calloc(size: Long): Long {
        val ptr = malloc(size)
        for (i in ptr until ptr + size)
            heap[i.toInt()] = 0
        return ptr
    }

    override fun read(addr: Long): Long {
        val i = getMemoryIndex(addr)
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
        val i = getMemoryIndex(addr)
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

    private fun getMemoryIndex(addr: Long): Int {
        if (addr % Configuration.WORD_SIZE != 0L)
            throw IRSimulator.Trap("Unaligned memory access: $addr (word size=${Configuration.WORD_SIZE})")
        return (addr / Configuration.WORD_SIZE).toInt()
    }
}
