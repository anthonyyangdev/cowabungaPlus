package cyr7.ir.interpret.heap

class XiHeapFactory {
    companion object {
        /**
         * Creates the heap instance.
         */
        fun createInstance(heapSize: Int): IXiHeap {
            return DynamicXiHeap(heapSize.toLong())
        }
    }
}
