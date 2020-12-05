package cyr7.ir.lowering;

import java.util.Set;

import cyr7.ir.nodes.*;
import cyr7.visitor.MyIRVisitor;
import kotlin.NotImplementedError;
import org.jetbrains.annotations.NotNull;

class TempsAccessedExprVisitor implements MyIRVisitor<Set<IRExpr>> {

    @Override
    public Set<IRExpr> visit(IRBinOp n) {
        var left = n.left().accept(this);
        var right = n.right().accept(this);
        left.addAll(right);
        return left;
    }

    @Override
    public Set<IRExpr> visit(IRCall n) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<IRExpr> visit(IRInteger n) {
        return Set.of();
    }

    @Override
    public Set<IRExpr> visit(IRESeq n) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<IRExpr> visit(IRMem n) {
        var inner = n.expr().accept(this);
        inner.add(n);
        return inner;
    }

    @Override
    public Set<IRExpr> visit(IRName n) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<IRExpr> visit(IRTemp n) {
        IRNodeFactory make = new IRNodeFactory_c(n.location());
        return Set.of(make.IRTemp(n.name()));
    }

    @Override
    public Set<IRExpr> visit(IRCallStmt n) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<IRExpr> visit(IRCJump n) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<IRExpr> visit(IRCompUnit n) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<IRExpr> visit(IRExp n) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<IRExpr> visit(IRFuncDecl n) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<IRExpr> visit(IRJump n) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<IRExpr> visit(IRLabel n) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<IRExpr> visit(IRMove n) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<IRExpr> visit(IRReturn n) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<IRExpr> visit(IRSeq n) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<IRExpr> visit(@NotNull IRFloat n) {
        throw new NotImplementedError();
    }
}
