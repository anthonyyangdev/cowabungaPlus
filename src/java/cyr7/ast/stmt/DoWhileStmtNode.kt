package cyr7.ast.stmt

import cyr7.ast.AbstractNode
import cyr7.ast.Node
import cyr7.ast.expr.ExprNode
import cyr7.visitor.AbstractVisitor
import java_cup.runtime.ComplexSymbolFactory.Location

class DoWhileStmtNode(
        location: Location,
        val body: StmtNode,
        val condition: ExprNode
): AbstractNode(location), StmtNode {
    override fun <T : Any?> accept(visitor: AbstractVisitor<T>): T {
        return visitor.visit(this);
    }
    override fun getChildren(): MutableList<Node> {
        return mutableListOf(body, condition)
    }
}
