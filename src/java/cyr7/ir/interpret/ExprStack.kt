package cyr7.ir.interpret

import java.util.*

/**
 * While traversing the IR tree, we require a stack in order to hold
 * a number of single-word values (e.g., to evaluate binary expressions).
 * This also keeps track of whether a value was created by a TEMP
 * or MEM, or NAME reference, which is useful when executing moves.
 */
class ExprStack(private val debugLevel: Int) {
    private val stack: Stack<ExprStackItem> = Stack()
    fun popValue(): Long {
        val value = stack.pop().value
        if (debugLevel > 1) println("Popping value $value")
        return value
    }

    fun pop(): ExprStackItem {
        return stack.pop()
    }

    fun pushAddr(value: Long, addr: Long) {
        if (debugLevel > 1) println("Pushing MEM $value ($addr)")
        stack.push(ExprStackItem.MemoryItem(value, addr))
    }

    fun pushTemp(value: Long, temp: String) {
        if (debugLevel > 1) println("Pushing TEMP $value ($temp)")
        stack.push(ExprStackItem.TempItem(value, temp))
    }

    fun pushName(value: Long, name: String) {
        if (debugLevel > 1) println("Pushing NAME $value ($name)")
        stack.push(ExprStackItem.NameItem(value, name))
    }

    fun pushValue(value: Long) {
        if (debugLevel > 1) println("Pushing value $value")
        stack.push(ExprStackItem.ComputedItem(value))
    }

    sealed class ExprStackItem(open val value: Long) {
        data class ComputedItem(override val value: Long): ExprStackItem(value)
        data class MemoryItem(override val value: Long, val addr: Long): ExprStackItem(value)
        data class TempItem(override val value: Long, val temp: String): ExprStackItem(value)
        data class NameItem(override val value: Long, val name: String): ExprStackItem(value)
    }

}
