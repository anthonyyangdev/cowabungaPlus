package cyr7.typedNodes.expr

import cyr7.semantics.types.XiType
import cyr7.typedNodes.TypedNode
import cyr7.typedNodes.visitor.ITypedAstVisitor
import java_cup.runtime.ComplexSymbolFactory.Location
import java.util.*

class TypedVariableExprNode(
        override val location: Location,
        val identifier: String,
        override val type: XiType.ExpandedType
) : TypedExprNode {
    override val expandedType: XiType.ExpandedType = type
    override fun equals(other: Any?): Boolean {
        return other is TypedVariableExprNode
                && other.identifier == identifier
                && other.type == type
    }
    override fun hashCode(): Int { return Objects.hash(identifier, type) }
    override fun <T> accept(visitor: ITypedAstVisitor<T>): T { return visitor.visit(this) }
    override fun children(): List<TypedNode> { return listOf() }
}
