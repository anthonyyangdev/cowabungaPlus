package cyr7.semantics.types

sealed class XiType {
    sealed class ResultType: XiType() {
        object VoidType: ResultType()
        object UnitType: ResultType()
        fun leastUpperBound(r: ResultType): ResultType {
            return if (this is UnitType || r is UnitType) UnitType
            else VoidType
        }
    }
    sealed class ExpandedType: XiType() {
        sealed class OrdinaryType: ExpandedType() {
            object BoolType: OrdinaryType() {
                override fun isSubtypeOf(type: ExpandedType) = type.isTop() || type.isBool()
            }
            object IntType: OrdinaryType() {
                override fun isSubtypeOf(type: ExpandedType) = type.isTop() || type.isInt()
            }
            object FloatType: OrdinaryType() {
                override fun isSubtypeOf(type: ExpandedType) = type.isTop() || type.isFloat()
            }
            object TopType: OrdinaryType() { override fun isSubtypeOf(type: ExpandedType) = type.isTop() }
            object BotType: OrdinaryType() { override fun isSubtypeOf(type: ExpandedType) = true }
            data class ArrayType(val type: ExpandedType): OrdinaryType() {
                override fun isSubtypeOf(type: ExpandedType): Boolean {
                    return if (type !is ArrayType) false else this.type.isSubtypeOf(type.type)
                }
            }
        }
        data class UnionType(val possibleTypes: Set<ExpandedType>): ExpandedType() {
            override fun isSubtypeOf(type: ExpandedType): Boolean {
                return this.possibleTypes.any { it.isSubtypeOf(type) }
            }
        }
        data class TupleType(val types: List<ExpandedType>): ExpandedType() {
            override fun isSubtypeOf(type: ExpandedType): Boolean {
                return if (type !is TupleType) false
                else this.types.zip(type.types).all { it.first.isSubtypeOf(it.second) }
            }
        }
        fun extend(vararg type: ExpandedType): UnionType {
            val typeSet = hashSetOf(*type)
            return when (this) {
                is UnionType -> { typeSet.addAll(this.possibleTypes); UnionType(typeSet) }
                else -> { typeSet.add(this); UnionType(typeSet) }
            }
        }
        abstract fun isSubtypeOf(type: ExpandedType): Boolean
    }
    data class FunctionType(val input: ExpandedType, val output: ExpandedType): XiType()
    companion object {
        fun function(input: ExpandedType, output: ExpandedType) = FunctionType(input, output)
        fun void() = ResultType.VoidType
        fun unit() = ResultType.UnitType
        fun bool() = ExpandedType.OrdinaryType.BoolType
        fun int() = ExpandedType.OrdinaryType.IntType
        fun float() = ExpandedType.OrdinaryType.FloatType
        fun top() = ExpandedType.OrdinaryType.TopType
        fun bot() = ExpandedType.OrdinaryType.BotType
        fun array(type: ExpandedType) = ExpandedType.OrdinaryType.ArrayType(type)
        fun tuple(vararg types: ExpandedType) = ExpandedType.TupleType(types.toList())
        fun tuple(types: List<ExpandedType>) = ExpandedType.TupleType(types)
        fun union(vararg types: ExpandedType) = ExpandedType.UnionType(hashSetOf(*types))
    }
    fun isVoid() = this is ResultType.VoidType
    fun isUnit() = this is ResultType.UnitType
    fun isBool() = this is ExpandedType.OrdinaryType.BoolType
    fun isInt() = this is ExpandedType.OrdinaryType.IntType
    fun isFloat() = this is ExpandedType.OrdinaryType.FloatType
    fun isTop() = this is ExpandedType.OrdinaryType.TopType
    fun isBot() = this is ExpandedType.OrdinaryType.BotType
    fun isArray() = this is ExpandedType.OrdinaryType.ArrayType
    fun isTuple() = this is ExpandedType.TupleType
}
