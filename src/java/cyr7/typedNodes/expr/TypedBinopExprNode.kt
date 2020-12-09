package cyr7.typedNodes.expr

import cyr7.semantics.types.XiType
import cyr7.typedNodes.TypedNode
import cyr7.typedNodes.visitor.ITypedAstVisitor
import java_cup.runtime.ComplexSymbolFactory.Location
import java.util.*

class TypedBinopExprNode(
        override val location: Location,
        val left: TypedExprNode,
        val right: TypedExprNode,
        val op: BinopType,
        override val type: XiType.ExpandedType
) : TypedExprNode {
    override val expandedType: XiType.ExpandedType = type
    enum class BinopType {
        ADD, SUB, MUL, DIV, REM, HIGH_MUL, LTE, LT, GTE, GT, NEQ, EQ, OR, AND, ARRAY_ACCESS;
    }
    override fun equals(other: Any?): Boolean {
        return other is TypedBinopExprNode
                && left == other.left
                && right == other.right
                && op == other.op
                && type == other.type
    }
    override fun hashCode() = Objects.hash(left, right, op, type)
    override fun <T> accept(visitor: ITypedAstVisitor<T>): T {
        return visitor.visit(this)
    }
    override fun children(): List<TypedNode> = listOf(left, right)
}
