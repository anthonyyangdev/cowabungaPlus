package cyr7.ir.visit;

import cyr7.ir.nodes.*;
import cyr7.util.Sets;
import cyr7.visitor.MyIRVisitor;
import kotlin.NotImplementedError;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public enum IRExprVarsVisitor implements MyIRVisitor<Set<String>> {

    INSTANCE;

    @Override
    public Set<String> visit(IRBinOp n) {
        return Sets.union(n.left().accept(this), n.right().accept(this));
    }

    @Override
    public Set<String> visit(IRCall n) {
        throw new AssertionError();
    }

    @Override
    public Set<String> visit(IRInteger n) {
        return Set.of();
    }

    @Override
    public Set<String> visit(IRESeq n) {
        throw new AssertionError();
    }

    @Override
    public Set<String> visit(IRMem n) {
        return n.expr().accept(this);
    }

    @Override
    public Set<String> visit(IRName n) {
        return Set.of();
    }

    @Override
    public Set<String> visit(IRTemp n) {
        return Set.of(n.name());
    }

    @Override
    public Set<String> visit(IRCallStmt n) {
        throw new AssertionError();
    }

    @Override
    public Set<String> visit(IRCJump n) {
        return n.cond().accept(this);
    }

    @Override
    public Set<String> visit(IRCompUnit n) {
        throw new AssertionError();
    }

    @Override
    public Set<String> visit(IRExp n) {
        throw new AssertionError();
    }

    @Override
    public Set<String> visit(IRFuncDecl n) {
        throw new AssertionError();
    }

    @Override
    public Set<String> visit(IRJump n) {
        throw new AssertionError();
    }

    @Override
    public Set<String> visit(IRLabel n) {
        return Set.of();
    }

    @Override
    public Set<String> visit(IRMove n) {
        throw new AssertionError();
    }

    @Override
    public Set<String> visit(IRReturn n) {
        throw new AssertionError();
    }

    @Override
    public Set<String> visit(IRSeq n) {
        throw new AssertionError();
    }

    @Override
    public Set<String> visit(@NotNull IRFloat n) { throw new NotImplementedError(); }
}
