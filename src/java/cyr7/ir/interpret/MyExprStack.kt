package cyr7.ir.interpret

import java.util.*

/**
 * While traversing the IR tree, we require a stack in order to hold
 * a number of single-word values (e.g., to evaluate binary expressions).
 * This also keeps track of whether a value was created by a TEMP
 * or MEM, or NAME reference, which is useful when executing moves.
 */
class MyExprStack {
    private val stack: Deque<MyStackItem> = ArrayDeque()
    fun popValue(): NumericValue {
        val value = stack.pop().value
        if (MyIRSimulator.debugLevel > 1) println("Popping value $value")
        return value
    }

    fun pop(): MyStackItem {
        return stack.pop()
    }

    fun pushAddr(value: Long, addr: Long) {
        if (MyIRSimulator.debugLevel > 1) println("Pushing MEM $value ($addr)")
        stack.push(MyStackItem.AddressItem(NumericValue.VInteger(value), addr))
    }

    fun pushTemp(value: Long, temp: String) {
        if (MyIRSimulator.debugLevel > 1) println("Pushing TEMP $value ($temp)")
        stack.push(MyStackItem.TempItem(NumericValue.VInteger(value), temp))
    }

    fun pushTemp(value: NumericValue, temp: String) {
        if (MyIRSimulator.debugLevel > 1) println("Pushing TEMP $value ($temp)")
        stack.push(MyStackItem.TempItem(value, temp))
    }

    fun pushName(value: Long, name: String) {
        if (MyIRSimulator.debugLevel > 1) println("Pushing NAME $value ($name)")
        stack.push(MyStackItem.NameItem(NumericValue.VInteger(value), name))
    }

    fun pushValue(value: NumericValue) {
        if (MyIRSimulator.debugLevel > 1) println("Pushing value $value")
        stack.push(MyStackItem.ComputedItem(value))
    }
    fun pushValue(value: Long) {
        if (MyIRSimulator.debugLevel > 1) println("Pushing value $value")
        stack.push(MyStackItem.ComputedItem(NumericValue.VInteger(value)))
    }
    fun pushValue(value: Double) {
        if (MyIRSimulator.debugLevel > 1) println("Pushing value $value")
        stack.push(MyStackItem.ComputedItem(NumericValue.VFloat(value)))
    }
}

sealed class MyStackItem(val value: NumericValue) {
    data class ComputedItem(val v: NumericValue): MyStackItem(v)
    data class AddressItem(val v: NumericValue, val addr: Long): MyStackItem(v)
    data class TempItem(val v: NumericValue, val temp: String): MyStackItem(v)
    data class NameItem(val v: NumericValue, val name: String): MyStackItem(v)
}

//class StackItem {
//    enum class Kind {
//        COMPUTED, MEM, TEMP, NAME
//    }
//
//    val type: Kind
//    val value: Long
//    val addr: Long
//    val temp: String? = null
//    val name: String? = null
//
//    constructor(value: Long) {
//        type = Kind.COMPUTED
//        this.value = value
//    }
//
//    constructor(value: Long, addr: Long) {
//        type = Kind.MEM
//        this.value = value
//        this.addr = addr
//    }
//
//    constructor(type: Kind, value: Long, string: String?) {
//        this.type = type
//        this.value = value
//        if (type == Kind.TEMP) temp = string else name = string
//    }
//}
