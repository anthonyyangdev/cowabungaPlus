package cyr7.ast.stmt

import cyr7.ast.AbstractNode
import cyr7.ast.Node
import cyr7.ast.expr.ExprNode
import cyr7.visitor.AbstractVisitor
import java_cup.runtime.ComplexSymbolFactory

class ForLoopStmtNode(
        location: ComplexSymbolFactory.Location,
        val varDecl: StmtNode,
        val condition: ExprNode,
        val epilogue: StmtNode,
        val body: StmtNode
): AbstractNode(location), StmtNode {
    override fun <T : Any?> accept(visitor: AbstractVisitor<T>): T {
        return visitor.visit(this)
    }
    override fun getChildren(): MutableList<Node> {
        return mutableListOf(varDecl, condition, epilogue, body)
    }
}
