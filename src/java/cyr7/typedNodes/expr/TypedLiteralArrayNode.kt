package cyr7.typedNodes.expr

import cyr7.semantics.types.XiType
import cyr7.typedNodes.TypedNode
import cyr7.typedNodes.visitor.ITypedAstVisitor
import java_cup.runtime.ComplexSymbolFactory.Location

class TypedLiteralArrayNode(
        override val location: Location,
        override val type: XiType.ExpandedType,
        val value: List<TypedExprNode>,
): TypedExprNode {
    override val expandedType: XiType.ExpandedType = type
    constructor(location: Location,
                type: XiType.ExpandedType,
                vararg values: TypedExprNode
    ): this(location, type, values.toList())

    override fun equals(other: Any?) = other is TypedLiteralArrayNode && other.value == value
    override fun hashCode() = this.value.hashCode()
    override fun <T> accept(visitor: ITypedAstVisitor<T>): T = visitor.visit(this)
    override fun children(): List<TypedNode> = value.map { it }
}
