package cyr7.ast.stmt

import cyr7.ast.AbstractNode
import cyr7.ast.Node
import cyr7.ast.expr.ExprNode
import cyr7.visitor.AbstractVisitor
import java_cup.runtime.ComplexSymbolFactory.Location

class FreeStmtNode(location: Location, val expr: ExprNode): AbstractNode(location), StmtNode {
    override val children: List<Node>
        get() = mutableListOf(expr);

    override fun equals(other: Any?): Boolean {
        return other is FreeStmtNode && other.expr == expr;
    }
    override fun <T> accept(visitor: AbstractVisitor<T>): T {
        return visitor.visit(this)
    }
    override fun hashCode(): Int {
        return expr.hashCode()
    }
}
