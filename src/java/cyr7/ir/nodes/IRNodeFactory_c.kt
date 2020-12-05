package cyr7.ir.nodes

import cyr7.semantics.types.FunctionType
import cyr7.semantics.types.PrimitiveType
import java_cup.runtime.ComplexSymbolFactory
import java.util.*

class IRNodeFactory_c(private val location: ComplexSymbolFactory.Location) : IRNodeFactory {
    override fun IRBinOp(type: IRBinOp.OpType, left: IRExpr, right: IRExpr): IRBinOp {
        return IRBinOp(location, type, left, right)
    }

    override fun IRCall(target: IRExpr, vararg args: IRExpr): IRCall {
        return IRCall(location, target, *args)
    }

    override fun IRCall(target: IRExpr, args: List<IRExpr>): IRCall {
        return IRCall(location, target, args, 10)
    }

    override fun IRCall(target: IRExpr, args: List<IRExpr>, numOfRV: Int): IRCall {
        return IRCall(location, target, args, numOfRV)
    }

    override fun IRCJump(expr: IRExpr, trueLabel: String): IRCJump {
        return IRCJump(location, expr, trueLabel)
    }

    override fun IRCJump(expr: IRExpr, trueLabel: String, falseLabel: String): IRCJump {
        return IRCJump(location, expr, trueLabel, falseLabel)
    }

    override fun IRCJump(expr: IRExpr, trueLabel: String, falseLabel: Optional<String>): IRCJump {
        return IRCJump(location, expr, trueLabel, falseLabel)
    }

    override fun IRCompUnit(name: String): IRCompUnit {
        return IRCompUnit(location, name)
    }

    override fun IRCompUnit(name: String,
                            functions: Map<String, IRFuncDecl>): IRCompUnit {
        return IRCompUnit(location, name, functions)
    }

    override fun IRInteger(value: Long): IRInteger {
        return IRInteger(location, value)
    }

    override fun IRFloat(value: Double): IRFloat {
        return IRFloat(location, value)
    }

    override fun IRCast(value: IRExpr, fromType: PrimitiveType, targetType: PrimitiveType): IRCast {
        return IRCast(location, value, fromType, targetType)
    }

    override fun IRESeq(stmt: IRStmt, expr: IRExpr): IRESeq {
        return IRESeq(location, stmt, expr)
    }

    override fun IRExp(expr: IRExpr): IRExp {
        return IRExp(location, expr)
    }

    override fun IRFuncDecl(name: String, stmt: IRStmt, type: FunctionType): IRFuncDecl {
        return IRFuncDecl(location, name, stmt, type)
    }

    override fun IRFuncDecl(name: String, stmt: IRStmt): IRFuncDecl {
        return IRFuncDecl(location, name, stmt)
    }

    override fun IRJump(expr: IRExpr): IRJump {
        return IRJump(location, expr)
    }

    override fun IRLabel(name: String): IRLabel {
        return IRLabel(location, name)
    }

    override fun IRMem(expr: IRExpr): IRMem {
        return IRMem(location, expr)
    }

    override fun IRCallStmt(collectors: List<String>, target: IRExpr,
                            args: List<IRExpr>): IRCallStmt {
        return IRCallStmt(location, collectors, target, args)
    }

    override fun IRCallStmt(target: IRExpr): IRCallStmt {
        return IRCallStmt(location, emptyList(), target, emptyList())
    }

    override fun IRMove(target: IRExpr, expr: IRExpr): IRMove {
        return IRMove(location, target, expr)
    }

    override fun IRName(name: String): IRName {
        return IRName(location, name)
    }

    override fun IRReturn(): IRReturn {
        return IRReturn(location)
    }

    override fun IRSeq(vararg stmts: IRStmt): IRSeq {
        return IRSeq(location, *stmts)
    }

    override fun IRSeq(stmts: List<IRStmt>): IRSeq {
        return IRSeq(location, stmts)
    }

    override fun IRTemp(name: String): IRTemp {
        return IRTemp(location, name)
    }
}
