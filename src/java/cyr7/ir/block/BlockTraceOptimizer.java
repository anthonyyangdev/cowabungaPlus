package cyr7.ir.block;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import cyr7.cli.CLI;
import cyr7.ir.block.util.LabelsInJumpStmtsVisitor;
import cyr7.ir.nodes.*;
import cyr7.ir.nodes.IRBinOp.OpType;
import cyr7.visitor.MyIRVisitor;
import kotlin.NotImplementedError;
import org.jetbrains.annotations.NotNull;

final class BlockTraceOptimizer {

    /**
     * Removes useless jumps and organizes cjumps such that the false label
     * always occurs directly after the cjump
     *
     * @param traces
     */
    public static List<List<BasicBlock>> optimized(List<List<BasicBlock>> traces) {
        List<List<BasicBlock>> optimizedTraces = new ArrayList<>();

        for (List<BasicBlock> trace : traces) {
            List<BasicBlock> optimizedTrace = new ArrayList<>();

            for (int i = 0; i < trace.size() - 1; i++) {
                BasicBlock b = trace.get(i);

                BasicBlock nextBlock = trace.get(i + 1);
                List<IRStmt> replacement =
                    b.last().accept(new FinalBlockStmtVisitor(nextBlock));
                b = b.replacingLastStmtWith(replacement);

                optimizedTrace.add(b);
            }

            BasicBlock last = trace.get(trace.size() - 1);
            List<IRStmt> replacement =
                last.last().accept(new FinalBlockStmtVisitor(Optional.empty()));
            last = last.replacingLastStmtWith(replacement);
            optimizedTrace.add(last);

            optimizedTraces.add(optimizedTrace);
        }

        return optimizedTraces;
    }

    /**
     * Returns the list of statements that should replace the final statement of
     * a basic block, given the label of the next basic block.
     *
     */
    private static class FinalBlockStmtVisitor implements MyIRVisitor<List<IRStmt>> {

        private final String errorMsg = "The accessed node is an expression.";
        private final Optional<String> firstLabelOfNextBlock;

        public FinalBlockStmtVisitor(BasicBlock nextBlock) {
            this(nextBlock.first().map(IRLabel::name));
        }

        public FinalBlockStmtVisitor(Optional<String> firstLabelOfNextBlock) {
            this.firstLabelOfNextBlock = firstLabelOfNextBlock;
        }

        @Override
        public List<IRStmt> visit(IRBinOp n) {
            throw new UnsupportedOperationException(errorMsg);
        }

        @Override
        public List<IRStmt> visit(IRCall n) {
            throw new UnsupportedOperationException(errorMsg);
        }

        @Override
        public List<IRStmt> visit(IRInteger n) {
            throw new UnsupportedOperationException(errorMsg);
        }

        @Override
        public List<IRStmt> visit(IRESeq n) {
            throw new UnsupportedOperationException(errorMsg);
        }

        @Override
        public List<IRStmt> visit(IRMem n) {
            throw new UnsupportedOperationException(errorMsg);
        }

        @Override
        public List<IRStmt> visit(IRName n) {
            throw new UnsupportedOperationException(errorMsg);
        }

        @Override
        public List<IRStmt> visit(IRTemp n) {
            throw new UnsupportedOperationException(errorMsg);
        }

        @Override
        public List<IRStmt> visit(IRCallStmt n) {
            return List.of(n);
        }

        @Override
        public List<IRStmt> visit(IRCJump n) {
            IRNodeFactory make = new IRNodeFactory_c(n.location());

            String trueLabel = n.trueLabel();
            if (n.falseLabel().isEmpty()) {
                return List.of(n);
            }

            String falseLabel = n.falseLabel().get();

            if (firstLabelOfNextBlock.isPresent()) {
                if (falseLabel.equals(firstLabelOfNextBlock.get())) {
                    // the false label would fall through anyways
                    return List.of(
                        make.IRCJump(n.cond(), n.trueLabel())
                    );

                } else if (trueLabel.equals(firstLabelOfNextBlock.get())) {
                    // we swap false and true so we fall through to the true
                    IRExpr inverted = make.IRBinOp(
                        OpType.XOR,
                        make.IRInteger(1),
                        n.cond()
                    );

                    return List.of(make.IRCJump(inverted, falseLabel));
                }
            }

            return List.of(
                make.IRCJump(n.cond(), n.trueLabel()),
                make.IRJump(make.IRName(falseLabel)));
        }

        @Override
        public List<IRStmt> visit(IRCompUnit n) {
            throw new UnsupportedOperationException(errorMsg);
        }

        @Override
        public List<IRStmt> visit(IRExp n) {
            throw new UnsupportedOperationException(errorMsg);
        }

        @Override
        public List<IRStmt> visit(IRFuncDecl n) {
            throw new UnsupportedOperationException(errorMsg);
        }

        @Override
        public List<IRStmt> visit(IRJump n) {
            List<String> labels = n.accept(LabelsInJumpStmtsVisitor.instance);
            if (labels.isEmpty()) {
                CLI.debugPrint("Maybe something is not right?");
                return List.of(n);
            }

            String label = labels.get(0);
            if (firstLabelOfNextBlock.isPresent()
                && label.equals(firstLabelOfNextBlock.get())) {
                return List.of();
            } else {
                return List.of(n);
            }
        }

        @Override
        public List<IRStmt> visit(IRLabel n) {
            return List.of(n);
        }

        @Override
        public List<IRStmt> visit(IRMove n) {
            return List.of(n);
        }

        @Override
        public List<IRStmt> visit(IRReturn n) {
            return List.of(n);
        }

        @Override
        public List<IRStmt> visit(IRSeq n) {
            throw new UnsupportedOperationException(errorMsg);
        }

        @Override
        public List<IRStmt> visit(@NotNull IRFloat n) {
            throw new NotImplementedError();
        }
    }

}
