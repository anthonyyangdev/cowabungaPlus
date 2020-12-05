package cyr7.visitor

import cyr7.ir.nodes.*

/**
 *
 * Called by the IRNode's {@link IRNode.accept(IRVisitor)} method.
 *
 * The returned value is relative to the callee node. For example, in an
 * implementation for constant folding, the return type may be IRNode, in which
 * the returned IRNode represents a constant-folded version of that node.
 *
 * @author ayang
 *
 * @param <T>
 */
interface MyIRVisitor<T> {

    // Expressions

    fun visit(n: IRBinOp): T

    fun visit(n: IRCall): T

    fun visit(n: IRInteger): T

    fun visit(n: IRFloat): T

    fun visit(n: IRESeq): T

    fun visit(n: IRMem): T

    fun visit(n: IRName): T

    fun visit(n: IRTemp): T

    // Statements

    fun visit(n: IRCallStmt): T

    fun visit(n: IRCJump): T

    fun visit(n: IRCompUnit): T

    fun visit(n: IRExp): T

    fun visit(n: IRFuncDecl): T

    fun visit(n: IRJump): T

    fun visit(n: IRLabel): T

    fun visit(n: IRMove): T

    fun visit(n: IRReturn): T

    fun visit(n: IRSeq): T


}
