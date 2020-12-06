package cyr7.ast

import cyr7.ast.expr.ExprNode
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
import cyr7.ast.type.PrimitiveEnum
import cyr7.ast.type.PrimitiveTypeNode
import cyr7.ast.type.TypeExprArrayNode
import cyr7.ast.type.TypeExprNode
import java_cup.runtime.ComplexSymbolFactory
import java.util.*

class ASTFactory(val location: ComplexSymbolFactory.Location) {

    fun intType() = PrimitiveTypeNode(location, PrimitiveEnum.INT)
    fun boolType() = PrimitiveTypeNode(location, PrimitiveEnum.BOOL)
    fun floatType() = PrimitiveTypeNode(location, PrimitiveEnum.FLOAT)
    fun arrayType(child: TypeExprNode, size: ExprNode) = TypeExprArrayNode(location, child, Optional.of(size))
    fun arrayType(child: TypeExprNode) = TypeExprArrayNode(location, child, Optional.empty())

    fun integer(value: String) = LiteralIntExprNode(location, value)
    fun integer(value: Int) = LiteralIntExprNode(location, value.toString())
    fun integer(value: Long) = LiteralIntExprNode(location, value.toString())
    fun bool(value: Boolean) = LiteralBoolExprNode(location, value)
    fun floating(value: Double) = LiteralFloatExprNode(location, value)
    fun string(value: String) = LiteralStringExprNode(location, value)
    fun character(value: String) = LiteralCharExprNode(location, value)
    fun character(value: Char) = LiteralCharExprNode(location, value.toString())
    fun array(vararg values: ExprNode) = LiteralArrayExprNode(location, listOf(*values))
    fun array(values: List<ExprNode>) = LiteralArrayExprNode(location, values)

    fun negateBool(value: ExprNode) = BoolNegExprNode(location, value)
    fun negateNumber(value: ExprNode) = IntNegExprNode(location, value)

    fun call(id: String, vararg parameters: ExprNode) = FunctionCallExprNode(location, id, listOf(*parameters))
    fun call(id: String, parameters: List<ExprNode>) = FunctionCallExprNode(location, id, parameters)
    fun length(value: ExprNode) = LengthExprNode(location, value)

    fun binop(op: BinOpExprNode.OpType, left: ExprNode, right: ExprNode) = BinOpExprNode(location, op, left, right)
    fun add(left: ExprNode, right: ExprNode) = binop(BinOpExprNode.OpType.ADD, left, right)
    fun sub(left: ExprNode, right: ExprNode) = binop(BinOpExprNode.OpType.SUB, left, right)
    fun mul(left: ExprNode, right: ExprNode) = binop(BinOpExprNode.OpType.MUL, left, right)
    fun div(left: ExprNode, right: ExprNode) = binop(BinOpExprNode.OpType.DIV, left, right)
    fun rem(left: ExprNode, right: ExprNode) = binop(BinOpExprNode.OpType.REM, left, right)
    fun highMul(left: ExprNode, right: ExprNode) = binop(BinOpExprNode.OpType.HIGH_MUL, left, right)
    fun and(left: ExprNode, right: ExprNode) = binop(BinOpExprNode.OpType.AND, left, right)
    fun or(left: ExprNode, right: ExprNode) = binop(BinOpExprNode.OpType.OR, left, right)
    fun gte(left: ExprNode, right: ExprNode) = binop(BinOpExprNode.OpType.GTE, left, right)
    fun gt(left: ExprNode, right: ExprNode) = binop(BinOpExprNode.OpType.GT, left, right)
    fun lte(left: ExprNode, right: ExprNode) = binop(BinOpExprNode.OpType.LTE, left, right)
    fun lt(left: ExprNode, right: ExprNode) = binop(BinOpExprNode.OpType.LT, left, right)
    fun eq(left: ExprNode, right: ExprNode) = binop(BinOpExprNode.OpType.EQ, left, right)
    fun neq(left: ExprNode, right: ExprNode) = binop(BinOpExprNode.OpType.NEQ, left, right)

    fun variable(id: String) = VariableAccessExprNode(location, id)
    fun arrayAccess(child: ExprNode, index: ExprNode) = ArrayAccessExprNode(location, child, index)

    fun arrayDecl(id: String, type: TypeExprArrayNode) = ArrayDeclStmtNode(location, id, type)
    fun assign(left: ExprNode, right: ExprNode) = AssignmentStmtNode(location, left, right)
    fun block(vararg stmts: StmtNode) = BlockStmtNode(location, listOf(*stmts))
    fun block(stmts: List<StmtNode>) = BlockStmtNode(location, stmts)

    fun doWhile(body: StmtNode, condition: ExprNode) = DoWhileStmtNode(location, body, condition)
    fun exprStmt(expr: ExprNode) = ExprStmtNode(location, expr)
    fun forLoop(varDecl: VarInitStmtNode, condition: ExprNode, epilogue: StmtNode, body: StmtNode): ForLoopStmtNode {
        return ForLoopStmtNode(location, varDecl, condition, epilogue, body)
    }
    fun free(arg: ExprNode) = FreeStmtNode(location, arg)
    fun ifElse(guard: ExprNode, ifBlock: StmtNode) = IfElseStmtNode(location, guard, ifBlock, Optional.empty())
    fun ifElse(guard: ExprNode, ifBlock: StmtNode, elseBlock: StmtNode): IfElseStmtNode {
        return IfElseStmtNode(location, guard, ifBlock, Optional.of(elseBlock))
    }
    fun multiAssign(varDecls: List<Optional<VarDeclNode>>,
                    initializer: FunctionCallExprNode) = MultiAssignStmtNode(location, varDecls, initializer)
    fun procedure(procedureCall: FunctionCallExprNode) = ProcedureStmtNode(location, procedureCall)
    fun returnStmt(retVal: List<ExprNode>) = ReturnStmtNode(location, retVal)
    fun returnStmt(vararg retVal: ExprNode) = ReturnStmtNode(location, listOf(*retVal))

    fun varDecl(id: String, typeExpr: TypeExprNode) = VarDeclNode(location, id, typeExpr)
    fun varDeclStmt(varDecl: VarDeclNode) = VarDeclStmtNode(location, varDecl)
    fun varInit(varDecl: VarDeclNode, expr: ExprNode) = VarInitStmtNode(location, varDecl, expr)
    fun whileLoop(guard: ExprNode, block: StmtNode) = WhileStmtNode(location, guard, block)

    fun funcDecl(header: FunctionHeaderDeclNode, block: BlockStmtNode) = FunctionDeclNode(location, header, block)
    fun funcHeader(id: String, args: List<VarDeclNode>, returnTypes: List<TypeExprNode>): FunctionHeaderDeclNode {
        return FunctionHeaderDeclNode(location, id, args, returnTypes)
    }
    fun ixiProgram(funcs: List<FunctionHeaderDeclNode>) = IxiProgramNode(location, funcs)
    fun ixiProgram(vararg funcs: FunctionHeaderDeclNode) = IxiProgramNode(location, listOf(*funcs))
    fun use(interfaceName: String) = UseNode(location, interfaceName)
    fun xiProgram(uses: List<UseNode>, functions: List<FunctionDeclNode>): XiProgramNode {
        return XiProgramNode(location, uses, functions)
    }
}
