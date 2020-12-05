package cyr7.ast.expr.literalexpr

import cyr7.ast.Node
import cyr7.ast.expr.AbstractExprNode
import cyr7.visitor.AbstractVisitor
import java_cup.runtime.ComplexSymbolFactory.Location
import java.util.*

class LiteralFloatExprNode(location: Location, val value: Double): AbstractExprNode(location) {
    override fun getChildren() = mutableListOf<Node>()
    override fun <T> accept(visitor: AbstractVisitor<T>): T = visitor.visit(this)
    override fun equals(other: Any?) = other is LiteralFloatExprNode && other.value == value
    override fun hashCode() = Objects.hash(value)
}
