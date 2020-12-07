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
import java_cup.runtime.ComplexSymbolFactory.Location

/**
 * A visitor that returns True if the expression/statement is pure. Returns False otherwise.
 */
class PureCheckVisitor: AbstractVisitor<Location?>() {
    override fun visit(n: FunctionDeclNode): Location? {
        return if (n.header.isPure) null else n.location
    }

    override fun visit(n: FunctionHeaderDeclNode): Location? {
        return if (n.isPure) null else n.location
    }

    override fun visit(n: IxiProgramNode): Location? {
        return n.functionDeclarations.find { !it.isPure }?.location
    }

    override fun visit(n: UseNode): Location? {
        throw UnsupportedOperationException("Cannot check purity of use node.")
    }

    override fun visit(n: VarDeclNode): Location? {
        return null
    }

    override fun visit(n: XiProgramNode): Location? {
        return n.functions.find { if (it.header.isPure) it.block.accept(this) != null else true }?.location
    }

    override fun visit(n: PrimitiveTypeNode): Location? {
        return null
    }

    override fun visit(n: TypeExprArrayNode): Location? {
        return n.child.accept(this) ?: n.size.map { it.accept(this) }?.orElse(null)
    }

    override fun visit(n: ArrayDeclStmtNode): Location? {
        return n.type.accept(this)
    }

    override fun visit(n: AssignmentStmtNode): Location? {
        return if (n.lhs is ArrayAccessExprNode) n.lhs.location
        else n.lhs.accept(this) ?: n.rhs.accept(this)
    }

    override fun visit(n: BlockStmtNode): Location? {
        return n.statements.find { it.accept(this) != null }?.location
    }

    override fun visit(n: ExprStmtNode): Location? {
        return n.expr.accept(this)
    }

    override fun visit(n: IfElseStmtNode): Location? {
        return n.guard.accept(this)
                ?: n.ifBlock.accept(this)
                ?: n.elseBlock.map { it.accept(this) }.orElse(null)
    }

    override fun visit(n: DoWhileStmtNode): Location? {
        return n.condition.accept(this) ?: n.body.accept(this)
    }

    override fun visit(n: ForLoopStmtNode): Location? {
        return n.varDecl.accept(this)
                ?: n.condition.accept(this)
                ?: n.epilogue.accept(this)
                ?: n.body.accept(this)
    }

    override fun visit(n: MultiAssignStmtNode): Location? {
        return n.varDecls.find { it.map { e -> e.accept(this) != null }.orElse(false) }
                ?.get()?.location
                ?: n.initializer.accept(this)
    }

    override fun visit(n: ProcedureStmtNode): Location? {
        return if (n.procedureCall.functionType.map { it.isPure }.orElse(false))
                null else n.location
    }

    override fun visit(n: FreeStmtNode): Location? {
        return n.expr.accept(this)
    }

    override fun visit(n: ReturnStmtNode): Location? {
        return n.exprs.find { it.accept(this) != null }?.location
    }

    override fun visit(n: VarDeclStmtNode): Location? {
        return n.varDecl.accept(this)
    }

    override fun visit(n: VarInitStmtNode): Location? {
        return n.varDecl.accept(this) ?: n.initializer.accept(this)
    }

    override fun visit(n: WhileStmtNode): Location? {
        return n.guard.accept(this) ?: n.block.accept(this)
    }

    override fun visit(n: FunctionCallExprNode): Location? {
        return if (n.functionType.map { it.isPure }.orElse(false))
            null else n.location
    }

    override fun visit(n: ArrayAccessExprNode): Location? {
        return n.child.accept(this) ?: n.index.accept(this)
    }

    override fun visit(n: VariableAccessExprNode): Location? {
        return null
    }

    override fun visit(n: BinOpExprNode): Location? {
        return n.left.accept(this) ?: n.right.accept(this)
    }

    override fun visit(n: LiteralArrayExprNode): Location? {
        return null
    }

    override fun visit(n: LiteralBoolExprNode): Location? {
        return null
    }

    override fun visit(n: LiteralCharExprNode): Location? {
        return null
    }

    override fun visit(n: LiteralIntExprNode): Location? {
        return null
    }

    override fun visit(n: LiteralFloatExprNode): Location? {
        return null
    }

    override fun visit(n: LiteralStringExprNode): Location? {
        return null
    }

    override fun visit(n: BoolNegExprNode): Location? {
        return n.expr.accept(this)
    }

    override fun visit(n: IntNegExprNode): Location? {
        return n.expr.accept(this)
    }

    override fun visit(n: LengthExprNode): Location? {
        return n.expr.accept(this)
    }
}
