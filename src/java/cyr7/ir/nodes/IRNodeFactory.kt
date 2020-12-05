package cyr7.ir.nodes

import cyr7.semantics.types.FunctionType
import cyr7.semantics.types.PrimitiveType
import java.util.*

interface IRNodeFactory {
    fun IRBinOp(type: IRBinOp.OpType, left: IRExpr, right: IRExpr): IRBinOp

    /**
     *
     * @param target address of the code for this function call
     * @param args arguments of this function call
     */
    fun IRCall(target: IRExpr, vararg args: IRExpr): IRCall

    /**
     *
     * @param target address of the code for this function call
     * @param args arguments of this function call
     */
    fun IRCall(target: IRExpr, args: List<IRExpr>): IRCall
    fun IRCall(target: IRExpr, args: List<IRExpr>, numOfRV: Int): IRCall

    /**
     * Construct a CJUMP instruction with fall-through on false.
     *
     * @param expr      the condition for the jump
     * @param trueLabel the destination of the jump if `expr` evaluates to
     * true
     */
    fun IRCJump(expr: IRExpr, trueLabel: String): IRCJump

    /**
     *
     * @param expr the condition for the jump
     * @param trueLabel the destination of the jump if `expr` evaluates
     * to true
     * @param falseLabel the destination of the jump if `expr` evaluates
     * to false
     */
    fun IRCJump(expr: IRExpr, trueLabel: String, falseLabel: String): IRCJump
    fun IRCJump(expr: IRExpr, trueLabel: String, falseLabel: Optional<String>): IRCJump
    fun IRCompUnit(name: String): IRCompUnit
    fun IRCompUnit(name: String, functions: Map<String, IRFuncDecl>): IRCompUnit

    /**
     *
     * @param value value of this constant
     */
    fun IRInteger(value: Long): IRInteger
    fun IRFloat(value: Double): IRFloat

    fun IRCast(value: IRExpr, fromType: PrimitiveType, targetType: PrimitiveType): IRCast

    /**
     *
     * @param stmt IR statement to be evaluated for side effects
     * @param expr IR expression to be evaluated after `stmt`
     */
    fun IRESeq(stmt: IRStmt, expr: IRExpr): IRESeq

    /**
     *
     * @param expr the expression to be evaluated and result discarded
     */
    fun IRExp(expr: IRExpr): IRExp
    fun IRFuncDecl(name: String, stmt: IRStmt): IRFuncDecl
    fun IRFuncDecl(name: String, stmt: IRStmt, type: FunctionType): IRFuncDecl

    /**
     *
     * @param expr the destination of the jump
     */
    fun IRJump(expr: IRExpr): IRJump

    /**
     *
     * @param name name of this memory address
     */
    fun IRLabel(name: String): IRLabel

    /**
     *
     * @param expr the address of this memory location
     */
    fun IRMem(expr: IRExpr): IRMem

    /**
     * @param collectors a list of temporary names to assign to.
     * @param target address of the code for this function call
     * @param args arguments of this function call
     */
    fun IRCallStmt(collectors: List<String>, target: IRExpr,
                   args: List<IRExpr>): IRCallStmt

    fun IRCallStmt(target: IRExpr): IRCallStmt

    /**
     *
     * @param target the destination of this move
     * @param expr the expression whose value is to be moved
     */
    fun IRMove(target: IRExpr, expr: IRExpr): IRMove

    /**
     *
     * @param name name of this memory address
     */
    fun IRName(name: String): IRName
    fun IRReturn(): IRReturn

    /**
     * @param stmts the statements
     */
    fun IRSeq(vararg stmts: IRStmt): IRSeq

    /**
     * Create a SEQ from a list of statements.
     * The list should not be modified subsequently.
     * @param stmts the sequence of statements
     */
    fun IRSeq(stmts: List<IRStmt>): IRSeq

    /**
     *
     * @param name name of this temporary register
     */
    fun IRTemp(name: String): IRTemp
}
