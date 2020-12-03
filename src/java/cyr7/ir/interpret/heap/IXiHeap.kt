package cyr7.ir.interpret.heap

interface IXiHeap {
    /**
     * Allocate a specified amount of bytes on the heap
     * @param size the number of bytes to be allocated
     * @return the starting address of the allocated region on the heap
     */
    fun malloc(size: Long): Long

    /**
     * Allocate a specified amount of bytes on the heap and initialize them to 0.
     * @param size the number of bytes to be allocated
     * @return the starting address of the allocated region on the heap
     */
    fun calloc(size: Long): Long

    /**
     * Read a value at the specified location on the heap
     * @param addr the address to be read
     * @return the value at `addr`
     */
    fun read(addr: Long): Long

    /**
     * Retrieves the string at the address in the heap.
     * @param addr the address of the string to obtain
     * @return the string value at `addr`
     */
    fun stringAt(addr: Long): String

    /**
     * Write a value at the specified location on the heap
     * @param addr the address to be written
     * @param value the value to be written
     */
    fun store(addr: Long, value: Long)

    /**
     * Adds a string to the heap and returns the pointer to that string.
     * @param value the string to be written.
     * @return the address to the newly written string.
     */
    fun storeString(value: String): Long

    /**
     * Frees the memory at `addr`.
     * @param addr the address with allocated blocks to free
     */
    fun free(addr: Long)
}
