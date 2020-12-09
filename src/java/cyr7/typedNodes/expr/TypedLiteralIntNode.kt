package cyr7.typedNodes.expr

import cyr7.semantics.types.XiType
import cyr7.typedNodes.TypedNode
import cyr7.typedNodes.visitor.ITypedAstVisitor
import java_cup.runtime.ComplexSymbolFactory.Location

class TypedLiteralIntNode(override val location: Location, val value: String) : TypedExprNode {
    override fun equals(other: Any?) = other is TypedLiteralIntNode && other.value == value
    override fun hashCode() = this.value.hashCode()
    override val type = XiType.int()
    override val expandedType: XiType.ExpandedType = type
    override fun <T> accept(visitor: ITypedAstVisitor<T>): T = visitor.visit(this)
    override fun children(): List<TypedNode> = listOf()
}
