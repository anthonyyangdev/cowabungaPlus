package cyr7.ir.interpret

import edu.cornell.cs.cs4120.util.InternalCompilerError

sealed class NumericValue {
    data class VInteger(val value: Long): NumericValue()
    data class VFloat(val value: Double): NumericValue()
    fun plus(other: NumericValue): NumericValue {
        return when {
            this is VInteger && other is VInteger -> VInteger(value.plus(other.value))
            this is VInteger && other is VFloat -> VFloat(value.plus(other.value))
            this is VFloat && other is VInteger -> VFloat(value.plus(other.value))
            this is VFloat && other is VFloat -> VFloat(value.plus(other.value))
            else -> throw InternalCompilerError("Missing plus condition")
        }
    }
    fun minus(other: NumericValue): NumericValue {
        return when {
            this is VInteger && other is VInteger -> VInteger(value.minus(other.value))
            this is VInteger && other is VFloat -> VFloat(value.minus(other.value))
            this is VFloat && other is VInteger -> VFloat(value.minus(other.value))
            this is VFloat && other is VFloat -> VFloat(value.minus(other.value))
            else -> throw InternalCompilerError("Missing minus condition")
        }
    }
    fun times(other: NumericValue): NumericValue {
        return when {
            this is VInteger && other is VInteger -> VInteger(value.times(other.value))
            this is VInteger && other is VFloat -> VFloat(value.times(other.value))
            this is VFloat && other is VInteger -> VFloat(value.times(other.value))
            this is VFloat && other is VFloat -> VFloat(value.times(other.value))
            else -> throw InternalCompilerError("Missing times condition")
        }
    }
    fun div(other: NumericValue): NumericValue {
        return when {
            this is VInteger && other is VInteger -> VInteger(value.div(other.value))
            this is VInteger && other is VFloat -> VFloat(value.div(other.value))
            this is VFloat && other is VInteger -> VFloat(value.div(other.value))
            this is VFloat && other is VFloat -> VFloat(value.div(other.value))
            else -> throw InternalCompilerError("Missing division condition")
        }
    }
    fun rem(other: NumericValue): NumericValue {
        return when {
            this is VInteger && other is VInteger -> VInteger(value.rem(other.value))
            this is VInteger && other is VFloat -> VFloat(value.rem(other.value))
            this is VFloat && other is VInteger -> VFloat(value.rem(other.value))
            this is VFloat && other is VFloat -> VFloat(value.rem(other.value))
            else -> throw InternalCompilerError("Missing remainder condition")
        }
    }
    fun and(other: NumericValue): NumericValue = when {
        this is VInteger && other is VInteger -> VInteger(value.and(other.value))
        else -> throw InternalCompilerError("Missing and condition")
    }
    fun or(other: NumericValue): NumericValue = when {
        this is VInteger && other is VInteger -> VInteger(value.or(other.value))
        else -> throw InternalCompilerError("Missing or condition")
    }
    fun xor(other: NumericValue): NumericValue = when {
        this is VInteger && other is VInteger -> VInteger(value.xor(other.value))
        else -> throw InternalCompilerError("Missing xor condition")
    }
    fun shl(other: NumericValue): NumericValue = when {
        this is VInteger && other is VInteger -> VInteger(value.shl(other.value.toInt()))
        else -> throw InternalCompilerError("Missing shl condition")
    }
    fun shr(other: NumericValue): NumericValue = when {
        this is VInteger && other is VInteger -> VInteger(value.shl(other.value.toInt()))
        else -> throw InternalCompilerError("Missing shr condition")
    }
    fun ushr(other: NumericValue): NumericValue = when {
        this is VInteger && other is VInteger -> VInteger(value.ushr(other.value.toInt()))
        else -> throw InternalCompilerError("Missing ushr condition")
    }
    fun eq(other: NumericValue): NumericValue = VInteger(when {
        this is VInteger && other is VInteger -> if (value == other.value) 1 else 0
        this is VInteger && other is VFloat -> if (value.toDouble() == other.value) 1 else 0
        this is VFloat && other is VInteger -> if (value == other.value.toDouble()) 1 else 0
        this is VFloat && other is VFloat -> if (value == other.value) 1 else 0
        else -> throw InternalCompilerError("Missing compare condition")
    })
    fun neq(other: NumericValue): NumericValue = VInteger(when {
        this is VInteger && other is VInteger -> if (value != other.value) 1 else 0
        this is VInteger && other is VFloat -> if (value.toDouble() != other.value) 1 else 0
        this is VFloat && other is VInteger -> if (value != other.value.toDouble()) 1 else 0
        this is VFloat && other is VFloat -> if (value != other.value) 1 else 0
        else -> throw InternalCompilerError("Missing compare condition")
    })
    fun gt(other: NumericValue): NumericValue = VInteger(when {
        this is VInteger && other is VInteger -> if (value > other.value) 1 else 0
        this is VInteger && other is VFloat -> if (value > other.value) 1 else 0
        this is VFloat && other is VInteger -> if (value > other.value) 1 else 0
        this is VFloat && other is VFloat -> if (value > other.value) 1 else 0
        else -> throw InternalCompilerError("Missing compare condition")
    })
    fun lt(other: NumericValue): NumericValue = VInteger(when {
        this is VInteger && other is VInteger -> if (value < other.value) 1 else 0
        this is VInteger && other is VFloat -> if (value < other.value) 1 else 0
        this is VFloat && other is VInteger -> if (value < other.value) 1 else 0
        this is VFloat && other is VFloat -> if (value < other.value) 1 else 0
        else -> throw InternalCompilerError("Missing compare condition")
    })
    fun gte(other: NumericValue): NumericValue = VInteger(when {
        this is VInteger && other is VInteger -> if (value >= other.value) 1 else 0
        this is VInteger && other is VFloat -> if (value >= other.value) 1 else 0
        this is VFloat && other is VInteger -> if (value >= other.value) 1 else 0
        this is VFloat && other is VFloat -> if (value >= other.value) 1 else 0
        else -> throw InternalCompilerError("Missing compare condition")
    })
    fun lte(other: NumericValue): NumericValue = VInteger(when {
        this is VInteger && other is VInteger -> if (value <= other.value) 1 else 0
        this is VInteger && other is VFloat -> if (value <= other.value) 1 else 0
        this is VFloat && other is VInteger -> if (value <= other.value) 1 else 0
        this is VFloat && other is VFloat -> if (value <= other.value) 1 else 0
        else -> throw InternalCompilerError("Missing compare condition")
    })
    fun isZero(): Boolean = when(this) {
        is VFloat -> this.value == 0.0
        is VInteger -> this.value == 0L
    }
    fun assertInt(): Long = when(this) {
        is VInteger -> this.value
        else -> throw InternalCompilerError("Expected integer but got ${this.javaClass.name}")
    }
    fun assertFloat(): Double = when(this) {
        is VFloat -> this.value
        else -> throw InternalCompilerError("Expected float but got ${this.javaClass.name}")
    }
}
