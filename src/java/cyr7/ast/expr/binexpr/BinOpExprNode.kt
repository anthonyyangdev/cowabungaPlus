package cyr7.ast.expr.binexpr

import cyr7.ast.Node
import cyr7.ast.expr.AbstractExprNode
import cyr7.ast.expr.ExprNode
import cyr7.visitor.AstVisitor
import java_cup.runtime.ComplexSymbolFactory

open class BinOpExprNode(
        loc: ComplexSymbolFactory.Location,
        val op: BinopType, val left: ExprNode, val right: ExprNode): AbstractExprNode(loc) {
    override fun equals(other: Any?): Boolean {
        return other is BinOpExprNode && other.op == op && other.left == left && other.right == right
    }
    enum class BinopType {
        ADD, SUB, MUL, DIV, REM, HIGH_MUL, LTE, LT, GTE, GT, NEQ, EQ, OR, AND;
        override fun toString(): String {
            return when (this) {
                ADD -> "+"
                SUB -> "-"
                MUL -> "*"
                DIV -> "/"
                REM -> "%"
                HIGH_MUL -> "*>>"
                LTE -> "<="
                LT -> "<"
                GTE -> ">="
                GT -> ">"
                NEQ -> "!="
                EQ -> "=="
                OR -> "|"
                AND -> "&"
            }
        }
    }


    override fun <T> accept(visitor: AstVisitor<T>): T {
        return visitor.visit(this)
    }

    override fun hashCode(): Int {
        var result = op.hashCode()
        result = 31 * result + left.hashCode()
        result = 31 * result + right.hashCode()
        return result
    }

    override val children: List<Node>
        get() = mutableListOf(left, right)
}
