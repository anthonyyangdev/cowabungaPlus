package cyr7.typedNodes.stmt

import cyr7.semantics.types.XiType
import cyr7.typedNodes.TypedNode
import cyr7.typedNodes.expr.TypedExprNode
import cyr7.typedNodes.visitor.ITypedAstVisitor
import java_cup.runtime.ComplexSymbolFactory.Location
import java.util.*

class TypedIfElseStmtNode(
        override val location: Location,
        val guard: TypedExprNode,
        private val ifBlock: TypedStmtNode,
        private val elseBlock: TypedStmtNode?,
) : TypedStmtNode {
    override val resultType: XiType.ResultType =
            elseBlock?.resultType?.leastUpperBound(ifBlock.resultType) ?: ifBlock.resultType
    override val type: XiType = resultType
    constructor(location: Location,
                guard: TypedExprNode,
                ifBlock: TypedStmtNode): this(location, guard, ifBlock, null)
    override fun equals(other: Any?): Boolean {
        return other is TypedIfElseStmtNode
                && guard == other.guard
                && ifBlock == other.ifBlock
                && elseBlock == other.elseBlock
                && type == other.type
                && resultType == other.resultType
    }

    override fun hashCode(): Int {
        return Objects.hash(guard, ifBlock, elseBlock, type, resultType)
    }

    override fun <T> accept(visitor: ITypedAstVisitor<T>): T {
        return visitor.visit(this)
    }

    override fun children(): List<TypedNode> {
        return listOfNotNull(guard, ifBlock, elseBlock)
    }
}
