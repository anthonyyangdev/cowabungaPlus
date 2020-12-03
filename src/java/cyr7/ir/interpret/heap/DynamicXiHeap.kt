package cyr7.ir.interpret.heap

import cyr7.ir.interpret.Configuration
import cyr7.ir.interpret.IRSimulator
import kotlin.math.ceil
import kotlin.math.roundToLong

class DynamicXiHeap(maxSize: Int): IXiHeap {
    private fun Long.asInts(): Pair<Int, Int> {
        val first = shr(32).toInt()
        val second = toInt()
        return first to second
    }
    private fun Long.sizeAndStatus(): Pair<Int, Boolean> {
        val isFree = and(1L) == 1L
        val size = shr(1).toInt()
        return size to isFree
    }
    private fun Int.extend(): Long {
        return this.toLong().shl(32).ushr(32)
    }
    private fun Long.sizeVal() = sizeAndStatus().first
    private fun Long.replaceHead(value: Int): Long {
        val left = value.toLong().shl(32)
        val right = this.toInt().extend()
        return left.or(right)
    }
    private fun Long.replaceEnd(value: Int): Long {
        val left = shr(32).shl(32)
        val right = value.extend()
        return left.or(right)
    }
    private fun asLong(int1: Int, int2: Int): Long {
        return int1.toLong().shl(32).or(int2.toLong())
    }
    private fun sizeAndStatus(size: Int, free: Boolean): Long {
        return size.toLong().shl(1).or(if (free) 1 else 0)
    }

    private fun getPrev(idx: Int): Int? {
        return heap[idx + 1].asInts().first.takeIf { it != nullPoint }
    }
    private fun getNext(idx: Int): Int? {
        return heap[idx + 1].asInts().second.takeIf { it != nullPoint }
    }
    private fun setPrev(idx: Int, prev: Int?) {
        heap[idx + 1] = heap[idx + 1].replaceHead(prev ?: nullPoint)
    }
    private fun setNext(idx: Int, next: Int?) {
        heap[idx + 1] = heap[idx + 1].replaceEnd(next ?: nullPoint)
    }
    private fun setSizeAndStatus(idx: Int, size: Int, free: Boolean) {
        heap[idx] = size.toLong().shl(1).or(if (free) 1 else 0)
        heap[idx + size - 1] = heap[idx]
    }

    private fun behindIsFree(idx: Int): Boolean {
        return heap[idx - 1].sizeAndStatus().second
    }
    private fun forwardIsFree(idx: Int): Boolean {
        return heap[idx + sizeof(idx)].sizeAndStatus().second
    }
    private fun sizeof(idx: Int) = heap[idx].sizeVal()

    private fun repOk() {
        assert(heap[1] == 0L)
        var prevIdx: Int? = null
        var idx = getHeadIdx()
        while (idx != null) {
            assert(heap[idx].and(1) == 1L)
            assert(prevIdx == getPrev(idx))
            assert(getPrev(idx) == heap[idx + 1].shr(32).toInt().takeIf { it != nullPoint })
            prevIdx = idx
            idx = getNext(idx)
        }
    }
    private val nullPoint = Int.MIN_VALUE
    private val ws = Configuration.WORD_SIZE

    private fun getHeadIdx() = heap[0].toInt().takeIf { it != nullPoint }
    private fun setHeadIdx(idx: Int?) { heap[0] = idx?.toLong() ?: nullPoint.toLong() }
    private val heap: LongArray = LongArray(maxSize)

    init {
        heap[1] = 0
        setSizeAndStatus(2, maxSize - 2, true)
        setPrev(2, nullPoint); setNext(2, nullPoint)
        setHeadIdx(2)
    }

    private data class FreeBlock(val blocks: Int, val prev: Int?, val next: Int?)
    private fun getBlock(idx: Int) = FreeBlock(sizeof(idx), getPrev(idx), getNext(idx))

    /**
     * Finds the next free block index with enough requested `size`
     * @param blocks the number of blocks requested.
     * @return index of the heap.
     */
    private fun allocate(blocks: Int): Int {
        val blocksNeeded = blocks + 2
        var headIdx: Int? = getHeadIdx()
        while (headIdx != null) {
            val metadata = getBlock(headIdx)
            val availableBlocks = metadata.blocks
            if (availableBlocks >= blocksNeeded) {
                val remainingSpace = availableBlocks - blocksNeeded
                val prev = metadata.prev
                val next = metadata.next
                if (remainingSpace >= 4) {
                    // Can create new free block
                    // Buffer blocks
                    setSizeAndStatus(headIdx, blocksNeeded, false)
                    val nextIdx = headIdx + blocksNeeded
                    setSizeAndStatus(nextIdx, remainingSpace, true)
                    setPrev(nextIdx, prev); setNext(nextIdx, next)

                    if (prev != null) setNext(prev, nextIdx) else setHeadIdx(nextIdx)
                    if (next != null) setPrev(next, nextIdx)
                } else {
                    setSizeAndStatus(headIdx, availableBlocks, false)
                    if (prev != null) setNext(prev, next) else setHeadIdx(next)
                    if (next != null) setPrev(next, prev)
                }
                repOk()
                return headIdx + 1
            } else {
                headIdx = metadata.next
            }
        }
        throw IRSimulator.Trap("Out of heap!")
    }

    override fun free(addr: Long) {
        val idx = getMemoryIndex(addr) - 1
        var idxToAdd = idx

        // Check if block after is free
        if (forwardIsFree(idx)) {
            val size = sizeof(idx); val forwardIdx = idx + size
            setSizeAndStatus(idx, sizeof(idx) + sizeof(forwardIdx), true)
            getNext(forwardIdx)?.let { next ->
                getPrev(forwardIdx)?.let { prev ->
                    setNext(prev, next); setPrev(next, prev)
                }
            }
        }
        if (behindIsFree(idx)) {
            val size = sizeof(idx); val behindIdx = idx - heap[idx - 1].sizeVal()
            setSizeAndStatus(behindIdx, size + sizeof(behindIdx), true)
            getNext(behindIdx)?.let { next ->
                getPrev(behindIdx)?.let { prev ->
                    setNext(prev, next); setPrev(next, prev)
                }
            }
            idxToAdd = behindIdx
        }
        val headIdx = getHeadIdx()
        setPrev(idxToAdd, null); setNext(idxToAdd, headIdx)
        if (headIdx != null) setPrev(headIdx, idxToAdd)
        setHeadIdx(idxToAdd)
        repOk()
    }

    override fun malloc(size: Long): Long {
        val allocatedSize = when {
            size % ws == 0L -> size
            else -> ceil(size.toDouble() / ws).roundToLong() * ws
        } / 8L
        val ptr = allocate(allocatedSize.toInt()).toLong() * ws
        repOk()
        return ptr
    }

    override fun calloc(size: Long): Long {
        val ptr = malloc(size)
        val start = getMemoryIndex(ptr)
        for (i in start until start + (size / ws))
            heap[i.toInt()] = 0
        repOk()
        return ptr
    }

    override fun read(addr: Long): Long {
        val i = getMemoryIndex(addr)
        repOk()
        return heap[i]
    }

    override fun stringAt(addr: Long): String {
        val size = read(addr - ws)
        // Ignore the last entry, which is 0.
        val str = LongRange(0, size - 1).map { i ->
            read(addr + i * ws).toChar()
        }.joinToString("")
        repOk()
        return str
    }

    override fun store(addr: Long, value: Long) {
        val i = getMemoryIndex(addr)
        heap[i] = value
        repOk()
    }

    override fun storeString(value: String): Long {
        val len = value.length
        val ptr = malloc(((len + 2) * ws).toLong())
        store(ptr, len.toLong())
        for (i in 0 until len) {
            store(ptr + (i + 1) * ws, value[i].toLong())
        }
        store(ptr + (len + 1) * ws, 0)
        repOk()
        return ptr + ws
    }

    private fun getMemoryIndex(addr: Long): Int {
        if (addr % Configuration.WORD_SIZE != 0L)
            throw IRSimulator.Trap("Unaligned memory access: $addr (word size=${Configuration.WORD_SIZE})")
        return (addr / Configuration.WORD_SIZE).toInt()
    }
}
