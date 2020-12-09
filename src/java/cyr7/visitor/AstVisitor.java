package cyr7.visitor;

import cyr7.ast.stmt.VarDeclNode;
import cyr7.ast.expr.FunctionCallExprNode;
import cyr7.ast.expr.access.ArrayAccessExprNode;
import cyr7.ast.expr.access.VariableAccessExprNode;
import cyr7.ast.expr.binexpr.*;
import cyr7.ast.expr.literalexpr.*;
import cyr7.ast.expr.unaryexpr.BoolNegExprNode;
import cyr7.ast.expr.unaryexpr.IntNegExprNode;
import cyr7.ast.expr.unaryexpr.LengthExprNode;
import cyr7.ast.stmt.*;
import cyr7.ast.toplevel.*;
import cyr7.ast.type.PrimitiveTypeNode;
import cyr7.ast.type.TypeExprArrayNode;

public interface AstVisitor<T> {

    // top-level
    T visit(FunctionDeclNode n);
    T visit(FunctionHeaderDeclNode n);
    T visit(IxiProgramNode n);
    T visit(UseNode n);
    T visit(VarDeclNode n);
    T visit(XiProgramNode n);

    // type
    T visit(PrimitiveTypeNode n);
    T visit(TypeExprArrayNode n);

    // stmt
    T visit(ArrayDeclStmtNode n);
    T visit(AssignmentStmtNode n);
    T visit(BlockStmtNode n);
    T visit(ExprStmtNode n);
    T visit(IfElseStmtNode n);
    T visit(DoWhileStmtNode n);
    T visit(ForLoopStmtNode n);
    T visit(MultiAssignStmtNode n);
    T visit(ProcedureStmtNode n);
    T visit(FreeStmtNode n);
    T visit(ReturnStmtNode n);
    T visit(VarDeclStmtNode n);
    T visit(VarInitStmtNode n);
    T visit(WhileStmtNode n);

    // expr
    T visit(FunctionCallExprNode n);

    // access
    T visit(ArrayAccessExprNode n);
    T visit(VariableAccessExprNode n);

    // bin expr
    T visit(BinOpExprNode n);

    // literal expr
    T visit(LiteralArrayExprNode n);
    T visit(LiteralBoolExprNode n);
    T visit(LiteralCharExprNode n);
    T visit(LiteralIntExprNode n);
    T visit(LiteralFloatExprNode n);
    T visit(LiteralStringExprNode n);

    // unary expr
    T visit(BoolNegExprNode n);
    T visit(IntNegExprNode n);
    T visit(LengthExprNode n);
}
