package cyr7.cfg.ir.dfa;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import cyr7.cfg.ir.dfa.CCPAnalysis.LatticeElement;
import cyr7.cfg.ir.nodes.CFGCallNode;
import cyr7.cfg.ir.nodes.CFGIfNode;
import cyr7.cfg.ir.nodes.CFGMemAssignNode;
import cyr7.cfg.ir.nodes.CFGSelfLoopNode;
import cyr7.cfg.ir.nodes.CFGStartNode;
import cyr7.cfg.ir.nodes.CFGVarAssignNode;
import cyr7.ir.BinOpInterpreter;
import cyr7.ir.nodes.IRBinOp;
import cyr7.ir.nodes.IRCJump;
import cyr7.ir.nodes.IRCall;
import cyr7.ir.nodes.IRCallStmt;
import cyr7.ir.nodes.IRCompUnit;
import cyr7.ir.nodes.IRConst;
import cyr7.ir.nodes.IRESeq;
import cyr7.ir.nodes.IRExp;
import cyr7.ir.nodes.IRFuncDecl;
import cyr7.ir.nodes.IRJump;
import cyr7.ir.nodes.IRLabel;
import cyr7.ir.nodes.IRMem;
import cyr7.ir.nodes.IRMove;
import cyr7.ir.nodes.IRName;
import cyr7.ir.nodes.IRReturn;
import cyr7.ir.nodes.IRSeq;
import cyr7.ir.nodes.IRTemp;
import cyr7.util.Sets;
import cyr7.visitor.MyIRVisitor;

public enum CCPAnalysis implements ForwardDataflowAnalysis<LatticeElement> {
    INSTANCE;

    @Override
    public LatticeElement topValue() {
        return LatticeElement.top;
    }

    @Override
    public ForwardTransferFunction<LatticeElement> transfer() {
        return TransferFunction.INSTANCE;
    }

    @Override
    public LatticeElement meet(LatticeElement lhs, LatticeElement rhs) {
        if (lhs.unreachable() && rhs.unreachable()) {
            return LatticeElement.unreachable;
        }

        Set<String> variables = Sets.union(lhs.variables(), rhs.variables());

        Map<String, VLatticeElement> values = new HashMap<>(variables.size());
        for (String variable : variables) {
            values.put(variable, meet(lhs.getValue(variable),
                rhs.getValue(variable)));
        }

        return LatticeElement.reachable(values);
    }

    private static VLatticeElement meet(VLatticeElement lhs,
                                        VLatticeElement rhs) {
        if (lhs.isTop() || rhs.isBot()) {
            return rhs;
        } else if (lhs.isBot() || rhs.isTop()) {
            return lhs;
        } else {
            // they're both values
            long left = lhs.value(), right = rhs.value();

            if (left == right) {
                return VLatticeElement.value(left);
            } else {
                return VLatticeElement.bot;
            }
        }
    }

    public interface VLatticeElement {

        boolean isTop();

        boolean isBot();

        long value();

        VLatticeElement top = new VLatticeElement() {
            @Override
            public boolean isTop() {
                return true;
            }

            @Override
            public boolean isBot() {
                return false;
            }

            @Override
            public long value() {
                return 0;
            }

            @Override
            public String toString() {
                return "⊤";
            }
        };

        VLatticeElement bot = new VLatticeElement() {
            @Override
            public boolean isTop() {
                return false;
            }

            @Override
            public boolean isBot() {
                return true;
            }

            @Override
            public long value() {
                return 0;
            }

            @Override
            public String toString() {
                return "⊥";
            }
        };

        static VLatticeElement value(long val) {
            return new ValueVLatticeElement(val);
        }

    }

    private final static class ValueVLatticeElement implements VLatticeElement {

        private final long val;

        public ValueVLatticeElement(long val) {
            this.val = val;
        }

        @Override
        public boolean isTop() {
            return false;
        }

        @Override
        public boolean isBot() {
            return false;
        }

        @Override
        public long value() {
            return val;
        }

        @Override
        public String toString() {
            return Long.toString(val);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ValueVLatticeElement that = (ValueVLatticeElement) o;
            return val == that.val;
        }

        @Override
        public int hashCode() {
            return Objects.hash(val);
        }
    }

    public interface LatticeElement {

        static LatticeElement reachable(Map<String, VLatticeElement> values) {
            return new ReachableLatticeElement(values);
        }

        LatticeElement top = reachable(new HashMap<>(0));

        LatticeElement unreachable = new LatticeElement() {
            @Override
            public boolean unreachable() {
                return true;
            }

            @Override
            public Set<String> variables() {
                return Set.of();
            }

            @Override
            public VLatticeElement getValue(String variable) {
                return VLatticeElement.top;
            }

            @Override
            public LatticeElement modified(
                Consumer<Map<String, VLatticeElement>> modify) {
                throw new UnsupportedOperationException();
            }

            @Override
            public String toString() {
                return "unreachable";
            }
        };

        boolean unreachable();

        Set<String> variables();

        VLatticeElement getValue(String variable);

        LatticeElement modified(Consumer<Map<String, VLatticeElement>> modify);

    }

    private final static class ReachableLatticeElement implements LatticeElement {

        private final Map<String, VLatticeElement> immutableValues;

        public ReachableLatticeElement(Map<String, VLatticeElement> values) {
            this.immutableValues = Map.copyOf(values);
        }

        @Override
        public boolean unreachable() {
            return false;
        }

        @Override
        public Set<String> variables() {
            return immutableValues.keySet();
        }

        @Override
        public VLatticeElement getValue(String variable) {
            return immutableValues.getOrDefault(variable,
                VLatticeElement.top);
        }

        @Override
        public LatticeElement modified(Consumer<Map<String,
            VLatticeElement>> modify) {
            Map<String, VLatticeElement> copy =
                new HashMap<>(immutableValues);
            modify.accept(copy);
            return LatticeElement.reachable(copy);
        }

        @Override
        public String toString() {
            return immutableValues.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ReachableLatticeElement that = (ReachableLatticeElement) o;
            return Objects.equals(immutableValues, that.immutableValues);
        }

        @Override
        public int hashCode() {
            return Objects.hash(immutableValues);
        }

    }

    private enum TransferFunction implements ForwardTransferFunction<LatticeElement> {

        INSTANCE;

        @Override
        public LatticeElement transfer(CFGCallNode n, LatticeElement in) {
            if (in.unreachable()) {
                return LatticeElement.unreachable;
            }

            return in.modified(values -> {
                for (String variable : n.call.collectors()) {
                    values.put(variable, VLatticeElement.bot);
                }
            });
        }

        @Override
        public LatticeElement transferTrue(CFGIfNode n, LatticeElement in) {
            if (in.unreachable()) {
                return LatticeElement.unreachable;
            }

            VLatticeElement result =
                n.cond.accept(new AbstractInterpreter(in));
            if (result.isTop()) {
                // William: We can do whatever, so I guess we take the true
                // branch?
                return in;
            } else if (result.isBot()) {
                return in;
            } else {
                long value = result.value();
                if (value != 0) {
                    return in;
                } else {
                    return LatticeElement.unreachable;
                }
            }
        }

        @Override
        public LatticeElement transferFalse(CFGIfNode n, LatticeElement in) {
            if (in.unreachable()) {
                return LatticeElement.unreachable;
            }

            VLatticeElement result =
                n.cond.accept(new AbstractInterpreter(in));
            if (result.isTop()) {
                // William: We can do whatever, so I guess we take the true
                // branch?
                return LatticeElement.unreachable;
            } else if (result.isBot()) {
                return in;
            } else {
                long value = result.value();
                if (value != 0) {
                    return LatticeElement.unreachable;
                } else {
                    return in;
                }
            }
        }

        @Override
        public LatticeElement transfer(CFGMemAssignNode n, LatticeElement in) {
            if (in.unreachable()) {
                return LatticeElement.unreachable;
            }

            return in;
        }

        @Override
        public LatticeElement transfer(CFGStartNode n, LatticeElement in) {
            if (in.unreachable()) {
                return LatticeElement.unreachable;
            }

            return LatticeElement.top;
        }

        @Override
        public LatticeElement transfer(CFGVarAssignNode n, LatticeElement in) {
            if (in.unreachable()) {
                return LatticeElement.unreachable;
            }

            return in.modified(values -> {
                VLatticeElement result =
                    n.value.accept(new AbstractInterpreter(in));
                values.put(n.variable, result);
            });
        }

        @Override
        public LatticeElement transfer(CFGSelfLoopNode n, LatticeElement in) {
            if (in.unreachable()) {
                return LatticeElement.unreachable;
            }

            return in;
        }

    }

    private static final class AbstractInterpreter implements MyIRVisitor<VLatticeElement> {

        private final LatticeElement env;

        private AbstractInterpreter(LatticeElement env) {
            this.env = env;
        }

        @Override
        public VLatticeElement visit(IRBinOp n) {
            VLatticeElement
                left = n.left().accept(this),
                right = n.right().accept(this);

            if (left.isTop() || right.isTop()) {
                return VLatticeElement.top;
            } else if (left.isBot() || right.isBot()) {
                return VLatticeElement.bot;
            } else {
                // they're both values
                long value = BinOpInterpreter.interpret(
                    n.opType(),
                    left.value(),
                    right.value());
                return VLatticeElement.value(value);
            }
        }

        @Override
        public VLatticeElement visit(IRCall n) {
            throw new AssertionError();
        }

        @Override
        public VLatticeElement visit(IRConst n) {
            return VLatticeElement.value(n.constant());
        }

        @Override
        public VLatticeElement visit(IRESeq n) {
            throw new AssertionError();
        }

        @Override
        public VLatticeElement visit(IRMem n) {
            return VLatticeElement.bot;
        }

        @Override
        public VLatticeElement visit(IRName n) {
            throw new AssertionError();
        }

        @Override
        public VLatticeElement visit(IRTemp n) {
            return env.getValue(n.name());
        }

        @Override
        public VLatticeElement visit(IRCallStmt n) {
            throw new AssertionError();
        }

        @Override
        public VLatticeElement visit(IRCJump n) {
            throw new AssertionError();
        }

        @Override
        public VLatticeElement visit(IRCompUnit n) {
            throw new AssertionError();
        }

        @Override
        public VLatticeElement visit(IRExp n) {
            throw new AssertionError();
        }

        @Override
        public VLatticeElement visit(IRFuncDecl n) {
            throw new AssertionError();
        }

        @Override
        public VLatticeElement visit(IRJump n) {
            throw new AssertionError();
        }

        @Override
        public VLatticeElement visit(IRLabel n) {
            throw new AssertionError();
        }

        @Override
        public VLatticeElement visit(IRMove n) {
            throw new AssertionError();
        }

        @Override
        public VLatticeElement visit(IRReturn n) {
            throw new AssertionError();
        }

        @Override
        public VLatticeElement visit(IRSeq n) {
            throw new AssertionError();
        }

    }

}
