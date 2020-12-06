package cyr7.ast.stmt

import cyr7.ast.AbstractNode
import cyr7.ast.Node
import cyr7.ast.expr.ExprNode
import cyr7.visitor.AbstractVisitor
import java_cup.runtime.ComplexSymbolFactory

class ForLoopStmtNode(
        location: ComplexSymbolFactory.Location,
        val varDecl: VarInitStmtNode,
        val condition: ExprNode,
        val epilogue: StmtNode,
        val body: StmtNode
): AbstractNode(location), StmtNode {
    override fun equals(other: Any?): Boolean {
        return other is ForLoopStmtNode
                && other.varDecl == varDecl
                && other.condition == condition
                && other.epilogue == epilogue
                && other.body == body
    }
    override fun hashCode(): Int {
        var result = varDecl.hashCode()
        result = 31 * result + condition.hashCode()
        result = 31 * result + epilogue.hashCode()
        result = 31 * result + body.hashCode()
        return result
    }
    override fun <T> accept(visitor: AbstractVisitor<T>): T {
        return visitor.visit(this)
    }
    override val children: MutableList<Node>
        get() = mutableListOf(varDecl, condition, epilogue, body)
}
