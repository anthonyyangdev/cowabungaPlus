package cyr7.typedNodes.expr

import cyr7.semantics.types.XiType
import cyr7.typedNodes.TypedNode
import cyr7.typedNodes.visitor.ITypedAstVisitor
import java_cup.runtime.ComplexSymbolFactory.Location
import java.util.*

class TypedUniopExprNode(
        override val location: Location,
        val expr: TypedExprNode,
        val op: UniopType,
        override val type: XiType.ExpandedType
) : TypedExprNode {
    override val expandedType: XiType.ExpandedType = type
    enum class UniopType { LOGICAL_NEGATE, NEGATIVE }
    override fun equals(other: Any?): Boolean {
        return other is TypedUniopExprNode
                && expr == other.expr
                && op == other.op
                && type == other.type
    }
    override fun hashCode() = Objects.hash(expr, op, type)
    override fun <T> accept(visitor: ITypedAstVisitor<T>): T {
        return visitor.visit(this)
    }
    override fun children(): List<TypedNode> = listOf(expr)
}
