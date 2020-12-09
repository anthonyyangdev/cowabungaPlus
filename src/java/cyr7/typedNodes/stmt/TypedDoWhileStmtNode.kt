package cyr7.typedNodes.stmt

import cyr7.semantics.types.XiType
import cyr7.typedNodes.TypedNode
import cyr7.typedNodes.expr.TypedExprNode
import cyr7.typedNodes.visitor.ITypedAstVisitor
import java_cup.runtime.ComplexSymbolFactory.Location
import java.util.*

class TypedDoWhileStmtNode(
        override val location: Location,
        private val body: TypedStmtNode,
        val guard: TypedExprNode,
) : TypedStmtNode {
    override val resultType: XiType.ResultType = body.resultType
    override val type: XiType = resultType
    override fun equals(other: Any?): Boolean {
        return other is TypedDoWhileStmtNode
                && guard == other.guard
                && body == other.body
                && type == other.type
                && resultType == other.resultType
    }
    override fun hashCode(): Int {
        return Objects.hash(guard, body, type, resultType)
    }

    override fun <T> accept(visitor: ITypedAstVisitor<T>): T {
        return visitor.visit(this)
    }

    override fun children(): List<TypedNode> {
        return listOfNotNull(guard, body)
    }
}
