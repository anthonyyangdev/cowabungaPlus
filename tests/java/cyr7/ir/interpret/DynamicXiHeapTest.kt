package cyr7.ir.interpret

import cyr7.ir.interpret.heap.DynamicXiHeap
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class DynamicXiHeapTest {

    private val maxSize = 128 * 10240
    private val heap = DynamicXiHeap(maxSize)
    private val ws = Configuration.WORD_SIZE

    @Nested
    inner class FreeSomeData {
        @Test
        fun `basic malloc then free`() {
            val ptr = heap.malloc(6L * ws)
            heap.free(ptr)
        }

        @Test
        fun `calloc then free`() {
            val ptr = heap.calloc(6L * ws)
            heap.free(ptr)
        }

        @Test
        fun `store string then free`() {
            val ptr = heap.storeString("Hello World")
            heap.free(ptr - ws)
        }
    }

    @Nested
    inner class AllocateSomeData {
        @Test
        fun `basic malloc calls`() {
            val ptr = heap.malloc(6L * ws)
            assertEquals(0L, ptr % 8)
            heap.store(ptr, 4)
            heap.store(ptr + ws, 'T'.toLong())
            heap.store(ptr + 2*ws, 'E'.toLong())
            heap.store(ptr + 3*ws, 'S'.toLong())
            heap.store(ptr + 4*ws, 'T'.toLong())
            heap.store(ptr + 5*ws, 0)
            assertEquals("TEST", heap.stringAt(ptr + ws))
        }

        @Test
        fun `calloc call`() {
            val ptr = heap.calloc(6L * ws)
            assertEquals(0L, ptr % 8)
            for (i in 0 until 6)
                assertEquals(0L, heap.read(ptr + i * ws))
        }

        @Test
        fun `store some strings`() {
            val ptr = heap.storeString("TEST")
            assertEquals(0L, ptr % 8)
            assertEquals("TEST", heap.stringAt(ptr))

            val nextStringPtr = heap.storeString("Another String")
            assertEquals(0L, nextStringPtr % 8)
            assertEquals("Another String", heap.stringAt(nextStringPtr))
        }
    }

    @Nested
    inner class MassAllocation {
        @Test
        fun `allocate a lot of space and then free`() {
            val requestSize = 128L
            IntRange(0, 1000).map {
                heap.malloc(requestSize)
            }.forEach {
                heap.free(it)
            }
        }

        @Test
        fun `allocate a lot of space and then free randomly`() {
            val requestSize = 128L
            IntRange(0, 1000).map {
                heap.malloc(requestSize)
            }.shuffled().forEach {
                heap.free(it)
            }
        }

        @Test
        fun `allocate about 75 percent of the heap, release, and then reallocate`() {
            val firstAllocationPtr = heap.malloc(maxSize * 3L / 4L)
            assertEquals(0, firstAllocationPtr % ws)
            heap.free(firstAllocationPtr)
            val secondAllocationPtr = heap.malloc(maxSize * 3L / 4L)
            assertEquals(0, secondAllocationPtr % ws)
            heap.free(secondAllocationPtr)
        }
    }

}
