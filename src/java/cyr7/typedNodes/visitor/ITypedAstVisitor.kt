package cyr7.typedNodes.visitor

import cyr7.typedNodes.expr.*
import cyr7.typedNodes.stmt.TypedDoWhileStmtNode
import cyr7.typedNodes.stmt.TypedIfElseStmtNode
import cyr7.typedNodes.stmt.TypedWhileStmtNode

interface ITypedAstVisitor<T> {
    fun visit(n: TypedLiteralIntNode): T
    fun visit(n: TypedLiteralFloatNode): T
    fun visit(n: TypedLiteralBoolNode): T
    fun visit(n: TypedLiteralArrayNode): T
    fun visit(n: TypedBinopExprNode): T
    fun visit(n: TypedFunctionCallNode): T
    fun visit(n: TypedUniopExprNode): T
    fun visit(n: TypedVariableExprNode): T

    fun visit(s: TypedIfElseStmtNode): T
    fun visit(s: TypedWhileStmtNode): T
    fun visit(s: TypedDoWhileStmtNode): T
}
