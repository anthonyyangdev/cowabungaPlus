package cyr7.cfg.ir.opt;

import cyr7.cfg.ir.dfa.CCPAnalysis.LatticeElement;
import cyr7.ir.nodes.*;
import cyr7.visitor.MyIRVisitor;
import kotlin.NotImplementedError;
import org.jetbrains.annotations.NotNull;

public class IRTempToConstant {

    private IRTempToConstant() {}

    /**
     * Replaces all variables {@code v} used in {@code expr} using a
     * {@link LatticeElement lattice} value. If no values are replaced, then
     * the original {@code expr} is returned.
     *
     * @param expr The expression that may have temporaries to be replaced.
     * @param lattice The lattice mappings from conditional constant
     *                propagation.
     * @return The expression {@code expr} with temporaries replaced.
     */
    public static IRExpr replace(IRExpr expr, LatticeElement lattice) {
        return expr.accept(new IRReplaceTempVisitor(lattice));
    }

    private static class IRReplaceTempVisitor implements MyIRVisitor<IRExpr> {

        private final LatticeElement lattice;

        public IRReplaceTempVisitor(LatticeElement lattice) {
            this.lattice = lattice;
        }

        @Override
        public IRExpr visit(IRBinOp n) {
            var replaceLeft = n.left().accept(this);
            var replaceRight = n.right().accept(this);
            return new IRBinOp(n.location(), n.opType(), replaceLeft, replaceRight);
        }

        @Override
        public IRExpr visit(IRCall n) {
            throw new UnsupportedOperationException();
        }

        @Override
        public IRExpr visit(IRInteger n) {
            return n;
        }

        @Override
        public IRExpr visit(IRESeq n) {
            throw new UnsupportedOperationException();
        }

        @Override
        public IRExpr visit(IRMem n) {
            return new IRMem(n.location(), n.expr().accept(this));
        }

        @Override
        public IRExpr visit(IRName n) {
            return n;
        }

        @Override
        public IRExpr visit(IRTemp n) {
            String variable = n.name();
            if (this.lattice.variables().contains(variable)
                    && !this.lattice.getValue(variable).isBot()
                    && !this.lattice.getValue(variable).isTop()) {
                long constant = this.lattice.getValue(variable).value();
                return new IRInteger(n.location(), constant);
            }
            return n;
        }

        @Override
        public IRExpr visit(IRCallStmt n) {
            throw new UnsupportedOperationException();
        }

        @Override
        public IRExpr visit(IRCJump n) {
            throw new UnsupportedOperationException();
        }

        @Override
        public IRExpr visit(IRCompUnit n) {
            throw new UnsupportedOperationException();
        }

        @Override
        public IRExpr visit(IRExp n) {
            throw new UnsupportedOperationException();
        }

        @Override
        public IRExpr visit(IRFuncDecl n) {
            throw new UnsupportedOperationException();
        }

        @Override
        public IRExpr visit(IRJump n) {
            throw new UnsupportedOperationException();
        }

        @Override
        public IRExpr visit(IRLabel n) {
            throw new UnsupportedOperationException();
        }

        @Override
        public IRExpr visit(IRMove n) {
            throw new UnsupportedOperationException();
        }

        @Override
        public IRExpr visit(IRReturn n) {
            throw new UnsupportedOperationException();
        }

        @Override
        public IRExpr visit(IRSeq n) {
            throw new UnsupportedOperationException();
        }

        @Override
        public IRExpr visit(@NotNull IRFloat n) {
            throw new NotImplementedError();
        }

        @Override
        public IRExpr visit(@NotNull IRCast n) {
            throw new NotImplementedError();
        }
    }
}
