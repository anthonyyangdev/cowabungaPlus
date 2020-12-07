package cyr7.typecheck

import cyr7.ast.expr.FunctionCallExprNode
import cyr7.ast.expr.access.ArrayAccessExprNode
import cyr7.ast.expr.access.VariableAccessExprNode
import cyr7.ast.expr.binexpr.BinOpExprNode
import cyr7.ast.expr.literalexpr.*
import cyr7.ast.expr.unaryexpr.BoolNegExprNode
import cyr7.ast.expr.unaryexpr.IntNegExprNode
import cyr7.ast.expr.unaryexpr.LengthExprNode
import cyr7.ast.stmt.*
import cyr7.ast.toplevel.*
import cyr7.ast.type.PrimitiveTypeNode
import cyr7.ast.type.TypeExprArrayNode
import cyr7.visitor.AbstractVisitor

/**
 * A visitor that returns True if the expression/statement is pure. Returns False otherwise.
 */
class PureCheckVisitor: AbstractVisitor<Boolean>() {
    override fun visit(n: FunctionDeclNode): Boolean {
        return n.header.isPure
    }

    override fun visit(n: FunctionHeaderDeclNode): Boolean {
        return n.isPure
    }

    override fun visit(n: IxiProgramNode): Boolean {
        return n.functionDeclarations.all { it.isPure }
    }

    override fun visit(n: UseNode): Boolean {
        throw UnsupportedOperationException("Cannot check purity of use node.")
    }

    override fun visit(n: VarDeclNode): Boolean {
        return true
    }

    override fun visit(n: XiProgramNode): Boolean {
        return n.functions.all { it.header.isPure || it.block.accept(this) }
    }

    override fun visit(n: PrimitiveTypeNode): Boolean {
        return true
    }

    override fun visit(n: TypeExprArrayNode): Boolean {
        return n.child.accept(this) && n.size.map { it.accept(this) }.orElse(true)
    }

    override fun visit(n: ArrayDeclStmtNode): Boolean {
        return n.type.accept(this)
    }

    override fun visit(n: AssignmentStmtNode): Boolean {
        if (n.lhs is ArrayAccessExprNode) return false
        else return n.lhs.accept(this) && n.rhs.accept(this)
    }

    override fun visit(n: BlockStmtNode): Boolean {
        return n.statements.all { it.accept(this) }
    }

    override fun visit(n: ExprStmtNode): Boolean {
        return n.expr.accept(this)
    }

    override fun visit(n: IfElseStmtNode): Boolean {
        return n.guard.accept(this)
                && n.ifBlock.accept(this)
                && n.elseBlock.map { it.accept(this) }.orElse(true)
    }

    override fun visit(n: DoWhileStmtNode): Boolean {
        return n.condition.accept(this)
                && n.body.accept(this)
    }

    override fun visit(n: ForLoopStmtNode): Boolean {
        return n.varDecl.accept(this)
                && n.condition.accept(this)
                && n.epilogue.accept(this)
                && n.body.accept(this)
    }

    override fun visit(n: MultiAssignStmtNode): Boolean {
        return n.varDecls.all { it.map { e -> e.accept(this) }.orElse(true) }
                && n.initializer.accept(this)
    }

    override fun visit(n: ProcedureStmtNode): Boolean {
        return n.procedureCall.functionType.map { it.isPure }.orElse(false)
    }

    override fun visit(n: FreeStmtNode): Boolean {
        return n.expr.accept(this)
    }

    override fun visit(n: ReturnStmtNode): Boolean {
        return n.exprs.all { it.accept(this) }
    }

    override fun visit(n: VarDeclStmtNode): Boolean {
        return n.varDecl.accept(this)
    }

    override fun visit(n: VarInitStmtNode): Boolean {
        return n.varDecl.accept(this) && n.initializer.accept(this)
    }

    override fun visit(n: WhileStmtNode): Boolean {
        return n.guard.accept(this) && n.guard.accept(this)
    }

    override fun visit(n: FunctionCallExprNode): Boolean {
        return n.functionType.map { it.isPure }.orElse(false)
    }

    override fun visit(n: ArrayAccessExprNode): Boolean {
        return n.child.accept(this) && n.index.accept(this)
    }

    override fun visit(n: VariableAccessExprNode): Boolean {
        return true
    }

    override fun visit(n: BinOpExprNode): Boolean {
        return n.left.accept(this) && n.right.accept(this)
    }

    override fun visit(n: LiteralArrayExprNode): Boolean {
        return true
    }

    override fun visit(n: LiteralBoolExprNode): Boolean {
        return true
    }

    override fun visit(n: LiteralCharExprNode): Boolean {
        return true
    }

    override fun visit(n: LiteralIntExprNode): Boolean {
        return true
    }

    override fun visit(n: LiteralFloatExprNode): Boolean {
        return true
    }

    override fun visit(n: LiteralStringExprNode): Boolean {
        return true
    }

    override fun visit(n: BoolNegExprNode): Boolean {
        return n.expr.accept(this)
    }

    override fun visit(n: IntNegExprNode): Boolean {
        return n.expr.accept(this)
    }

    override fun visit(n: LengthExprNode): Boolean {
        return n.expr.accept(this)
    }
}
