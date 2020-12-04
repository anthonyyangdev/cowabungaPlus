package cyr7.ast.stmt

import cyr7.ast.AbstractNode
import cyr7.ast.Node
import cyr7.ast.expr.ExprNode
import cyr7.visitor.AbstractVisitor
import java_cup.runtime.ComplexSymbolFactory.Location

class FreeStmtNode(location: Location, val expr: ExprNode): AbstractNode(location), StmtNode {
    override fun <T> accept(visitor: AbstractVisitor<T>): T {
        return visitor.visit(this)
    }

    override fun getChildren(): MutableList<Node> {
        return mutableListOf(expr)
    }
}
