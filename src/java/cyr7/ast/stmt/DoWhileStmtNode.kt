package cyr7.ast.stmt

import cyr7.ast.AbstractNode
import cyr7.ast.expr.ExprNode
import cyr7.visitor.AstVisitor
import java_cup.runtime.ComplexSymbolFactory.Location

class DoWhileStmtNode(
        location: Location,
        val body: StmtNode,
        val condition: ExprNode
): AbstractNode(location), StmtNode {
    override fun equals(other: Any?): Boolean {
        return other is DoWhileStmtNode && other.body == body && other.condition == condition
    }
    override fun hashCode(): Int {
        var result = body.hashCode()
        result = 31 * result + condition.hashCode()
        return result
    }
    override fun <T : Any?> accept(visitor: AstVisitor<T>): T {
        return visitor.visit(this);
    }
    override val children get() = mutableListOf(body, condition)
}
