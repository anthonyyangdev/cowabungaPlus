package cyr7.typecheck;

import cyr7.ast.VarDeclNode;
import cyr7.ast.expr.FunctionCallExprNode;
import cyr7.ast.expr.access.ArrayAccessExprNode;
import cyr7.ast.expr.access.VariableAccessExprNode;
import cyr7.ast.expr.binexpr.*;
import cyr7.ast.expr.literalexpr.*;
import cyr7.ast.expr.unaryexpr.BoolNegExprNode;
import cyr7.ast.expr.unaryexpr.IntNegExprNode;
import cyr7.ast.expr.unaryexpr.LengthExprNode;
import cyr7.ast.stmt.*;
import cyr7.ast.toplevel.FunctionDeclNode;
import cyr7.ast.toplevel.FunctionHeaderDeclNode;
import cyr7.ast.toplevel.IxiProgramNode;
import cyr7.ast.toplevel.UseNode;
import cyr7.ast.toplevel.XiProgramNode;
import cyr7.ast.type.PrimitiveTypeNode;
import cyr7.ast.type.TypeExprArrayNode;
import cyr7.visitor.AbstractVisitor;
import cyr7.visitor.PostOrderReduceTraversal;

class AstInvariantVisitor extends AbstractVisitor<Boolean>
    implements PostOrderReduceTraversal.ReduceVisitor<Boolean> {

    @Override
    public Boolean unit() {
        return true;
    }

    @Override
    public Boolean combine(Boolean left, Boolean right) {
        return left && right;
    }

    @Override
    public Boolean visit(FunctionDeclNode n) {
        return true;
    }

    @Override
    public Boolean visit(FunctionHeaderDeclNode n) {
        return n.getType() != null;
    }

    @Override
    public Boolean visit(IxiProgramNode n) {
        return true;
    }

    @Override
    public Boolean visit(UseNode n) {
        return true;
    }

    @Override
    public Boolean visit(VarDeclNode n) {
        return true;
    }

    @Override
    public Boolean visit(XiProgramNode n) {
        return true;
    }

    @Override
    public Boolean visit(PrimitiveTypeNode n) {
        return true;
    }

    @Override
    public Boolean visit(TypeExprArrayNode n) {
        return true;
    }

    @Override
    public Boolean visit(ArrayDeclStmtNode n) {
        return true;
    }

    @Override
    public Boolean visit(AssignmentStmtNode n) {
        return true;
    }

    @Override
    public Boolean visit(BlockStmtNode n) {
        return true;
    }

    @Override
    public Boolean visit(ExprStmtNode n) {
        return true;
    }

    @Override
    public Boolean visit(IfElseStmtNode n) {
        return true;
    }

    @Override
    public Boolean visit(DoWhileStmtNode n) { return true; }

    @Override
    public Boolean visit(ForLoopStmtNode n) { return true; }

    @Override
    public Boolean visit(MultiAssignStmtNode n) {
        return true;
    }

    @Override
    public Boolean visit(ProcedureStmtNode n) {
        return true;
    }

    @Override
    public Boolean visit(FreeStmtNode n) { return true; }

    @Override
    public Boolean visit(ReturnStmtNode n) {
        return true;
    }

    @Override
    public Boolean visit(VarDeclStmtNode n) {
        return true;
    }

    @Override
    public Boolean visit(VarInitStmtNode n) {
        return true;
    }

    @Override
    public Boolean visit(WhileStmtNode n) {
        return true;
    }

    // Expressions

    @Override
    public Boolean visit(FunctionCallExprNode n) {
        return n.getType() != null;
    }

    @Override
    public Boolean visit(ArrayAccessExprNode n) {
        return n.getType() != null;
    }

    @Override
    public Boolean visit(VariableAccessExprNode n) {
        return n.getType() != null;
    }

    @Override
    public Boolean visit(BinOpExprNode n) { return n.getType() != null; }

    @Override
    public Boolean visit(AddExprNode n) {
        return n.getType() != null;
    }

    @Override
    public Boolean visit(AndExprNode n) {
        return n.getType() != null;
    }

    @Override
    public Boolean visit(DivExprNode n) {
        return n.getType() != null;
    }

    @Override
    public Boolean visit(EqualsExprNode n) {
        return n.getType() != null;
    }

    @Override
    public Boolean visit(GTEExprNode n) {
        return n.getType() != null;
    }

    @Override
    public Boolean visit(GTExprNode n) {
        return n.getType() != null;
    }

    @Override
    public Boolean visit(HighMultExprNode n) {
        return n.getType() != null;
    }

    @Override
    public Boolean visit(LTEExprNode n) {
        return n.getType() != null;
    }

    @Override
    public Boolean visit(LTExprNode n) {
        return n.getType() != null;
    }

    @Override
    public Boolean visit(MultExprNode n) {
        return n.getType() != null;
    }

    @Override
    public Boolean visit(NotEqualsExprNode n) {
        return n.getType() != null;
    }

    @Override
    public Boolean visit(OrExprNode n) {
        return n.getType() != null;
    }

    @Override
    public Boolean visit(RemExprNode n) {
        return n.getType() != null;
    }

    @Override
    public Boolean visit(SubExprNode n) {
        return n.getType() != null;
    }

    @Override
    public Boolean visit(LiteralArrayExprNode n) {
        return n.getType() != null;
    }

    @Override
    public Boolean visit(LiteralBoolExprNode n) {
        return n.getType() != null;
    }

    @Override
    public Boolean visit(LiteralCharExprNode n) {
        return n.getType() != null;
    }

    @Override
    public Boolean visit(LiteralIntExprNode n) {
        return n.getType() != null;
    }

    @Override
    public Boolean visit(LiteralFloatExprNode n) { return n.getType() != null; }

    @Override
    public Boolean visit(LiteralStringExprNode n) {
        return n.getType() != null;
    }

    @Override
    public Boolean visit(BoolNegExprNode n) {
        return n.getType() != null;
    }

    @Override
    public Boolean visit(IntNegExprNode n) {
        return n.getType() != null;
    }

    @Override
    public Boolean visit(LengthExprNode n) {
        return n.getType() != null;
    }
}
