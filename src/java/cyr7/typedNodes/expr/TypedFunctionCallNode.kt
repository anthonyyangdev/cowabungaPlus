package cyr7.typedNodes.expr

import cyr7.semantics.types.XiType
import cyr7.typedNodes.TypedNode
import cyr7.typedNodes.visitor.ITypedAstVisitor
import java_cup.runtime.ComplexSymbolFactory.Location
import java.util.*

class TypedFunctionCallNode(
        override val location: Location,
        val name: String,
        val args: List<TypedExprNode>,
        override val type: XiType.ExpandedType
) : TypedExprNode {
    override val expandedType: XiType.ExpandedType = type
    override fun equals(other: Any?): Boolean {
        return other is TypedFunctionCallNode && name == other.name && args == other.args
    }
    override fun hashCode() = Objects.hash(name, args, type)
    override fun <T> accept(visitor: ITypedAstVisitor<T>): T {
        return visitor.visit(this)
    }
    override fun children(): List<TypedNode> = args.map { it }
}
