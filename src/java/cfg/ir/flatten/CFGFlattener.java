package cfg.ir.flatten;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.stream.Collectors;

import cfg.ir.CFGGraph;
import cfg.ir.nodes.CFGBlockNode;
import cfg.ir.nodes.CFGCallNode;
import cfg.ir.nodes.CFGIfNode;
import cfg.ir.nodes.CFGMemAssignNode;
import cfg.ir.nodes.CFGNode;
import cfg.ir.nodes.CFGReturnNode;
import cfg.ir.nodes.CFGSelfLoopNode;
import cfg.ir.nodes.CFGStartNode;
import cfg.ir.nodes.CFGVarAssignNode;
import cfg.ir.visitor.IrCFGVisitor;
import cyr7.ir.DefaultIdGenerator;
import cyr7.ir.IdGenerator;
import cyr7.ir.nodes.IRCompUnit;
import cyr7.ir.nodes.IRFuncDecl;
import cyr7.ir.nodes.IRJump;
import cyr7.ir.nodes.IRLabel;
import cyr7.ir.nodes.IRNodeFactory;
import cyr7.ir.nodes.IRNodeFactory_c;
import cyr7.ir.nodes.IRSeq;
import cyr7.ir.nodes.IRStmt;
import graph.GraphNode;
import java_cup.runtime.ComplexSymbolFactory.Location;
import polyglot.util.Pair;

public class CFGFlattener {

    private CFGFlattener() {}

    public static IRCompUnit flatten(Map<String, CFGGraph> cfgMap,
                IRCompUnit compUnit) {
        Map<String, IRFuncDecl> functions = new HashMap<>();
        cfgMap.forEach((functionName, cfg) -> {
            final var flattener = new FlattenCFGVisitor(cfg);
            IRSeq flattened = flattener.flatten();
            IRFuncDecl function = new IRFuncDecl(compUnit.location(),
                        functionName, flattened, compUnit.getFunction(functionName).type());
            functions.put(functionName, function);
        });
        return new IRCompUnit(compUnit.location(), compUnit.name(), functions);
    }

    private static class FlattenCFGVisitor
        implements IrCFGVisitor<Optional<GraphNode<CFGNode>>> {

        /**
         * A wrapper class so that stmts can be compared via pointer addresses,
         * instead of their overwritten equals() methods.
         */
        private class IRStmtWrapper {

            private Optional<IRLabel> label;
            private Optional<IRJump> jump;

            protected final List<IRStmt> stmt;

            public IRStmtWrapper(List<IRStmt> stmts) {
                this.stmt = Collections.unmodifiableList(stmts);
                this.label = Optional.empty();
                this.jump = Optional.empty();
            }

            public IRStmtWrapper(IRStmt stmt) {
                this.stmt = List.of(stmt);
                this.label = Optional.empty();
                this.jump = Optional.empty();
            }

            public IRStmtWrapper() {
                this.stmt = List.of();
                this.label = Optional.empty();
                this.jump = Optional.empty();
            }

            public Location location() {
                if (stmt.isEmpty())
                    return new Location(-1, -1);
                else
                    return stmt.get(0).location();
            }

            public void setLabel(String label) {
                if (this.label.isPresent()) {
                    throw new UnsupportedOperationException(
                            "Error: Cannot set label twice.");
                }
                var make = new IRNodeFactory_c(this.location());
                this.label = Optional.of(make.IRLabel(label));
            }

            public void setJump(String target) {
                if (this.jump.isPresent()) {
                    throw new UnsupportedOperationException(
                            "Error: Cannot set jump twice.");
                }
                var make = new IRNodeFactory_c(this.location());
                this.jump = Optional.of(make.IRJump(make.IRName(target)));
            }
        }

        /**
         * This is the list of statements corresponding to this control flow graph.
         */
        private final List<IRStmtWrapper> stmts;

        /**
         * This is the set of nodes already visited by the visitor class. Each visit
         * method should add the current node to this set.
         */
        private final HashSet<CFGNode> visitedNodes;

        /**
         * Mapping from a CFGNode to a IRLabel, if such a label has been created at
         * this particular program point. When an IRLabel is inserted into the list
         * of statements {@code stmts}, the pair of CFGNode and that label name
         * should be added to this map.
         */
        private final IdentityHashMap<CFGNode, String> cfgNodeToLabels;

        /**
         * Mapping from a CFGNode to an IRStmt. If a CFGNode generates an IRNode in
         * a visit method, then the pairing should be added to this mapping.
         */
        private final IdentityHashMap<CFGNode, IRStmtWrapper> cfgNodeToIRStmt;

        /**
         * Queue of untransformed true branches, created from CFGIfNodes. When
         * visiting a CFGIfNode, add the true branch to this queue, and continue
         * propogating/transforming via the false branch.
         * <p>
         * The CFGNode sub-graphs in this collection are then transformed via the
         * same strategy. The particular collection implementation used here does
         * not matter; the order in which these CFGNode sub-graphs are transformed
         * is not important.
         */
        private final Queue<Pair<GraphNode<CFGNode>, String>> trueBranches;

        /**
         * The previous IRStmt added to the list of statements when traversing
         * through CFG sub-graph.
         */
        private IRStmtWrapper predecessor;

        private final IdGenerator generator;

        private final CFGGraph cfg;

        protected FlattenCFGVisitor(CFGGraph cfg) {
            this.visitedNodes = new HashSet<>();
            this.cfgNodeToLabels = new IdentityHashMap<>();
            this.cfgNodeToIRStmt = new IdentityHashMap<>();
            this.trueBranches = new ArrayDeque<>();
            this.stmts = new ArrayList<>();
            this.generator = new DefaultIdGenerator();
            this.cfg = cfg;
        }

        /**
         * Sets node {@code n} to be visited and the predecessor node for the next
         * node.
         *
         * @param n
         */
        private void epilogueProcess(CFGNode n, IRStmtWrapper stmt) {
            this.visitedNodes.add(n);
            this.predecessor = stmt;
        }

        private IRNodeFactory createMake(CFGNode n) {
            return new IRNodeFactory_c(n.location());
        }

        private IRStmtWrapper wrapStmt(IRStmt s) {
            return new IRStmtWrapper(s);
        }

        private IRStmtWrapper wrapStmt(List<IRStmt> s) {
            return new IRStmtWrapper(s);
        }

        private IRStmtWrapper wrapStmt() {
            return new IRStmtWrapper();
        }

        private void insertJumpForNode(IRStmtWrapper from, String target) {
            from.setJump(target);
        }

        /**
         * Inserts a newly-created label before the IRStmt associated to node
         * {@code n}.
         *
         * @param n
         */
        private String insertLabelForNode(CFGNode n) {
            final var stmt = this.cfgNodeToIRStmt.get(n);
            final String label = generator.newLabel();
            stmt.setLabel(label);
            this.cfgNodeToLabels.put(n, label);
            return label;
        }

        protected List<IRStmt> getFunctionBody() {
            return this.stmts.stream().flatMap(wrapper -> {
                final List<IRStmt> content = new ArrayList<>();
                wrapper.label.ifPresent(lbl -> content.add(lbl));
                wrapper.stmt.forEach(s -> content.add(s));
                wrapper.jump.ifPresent(jump -> content.add(jump));
                return content.stream();
            }).collect(Collectors.toList());
        }

        private boolean performProcessIfVisited(CFGNode n) {
            if (this.visitedNodes.contains(n)) {
                if (this.cfgNodeToLabels.containsKey(n)) {
                    final String target = this.cfgNodeToLabels.get(n);
                    this.insertJumpForNode(this.predecessor, target);
                } else {
                    String target = this.insertLabelForNode(n);
                    this.insertJumpForNode(this.predecessor, target);
                }
                return true;
            } else {
                return false;
            }
        }

        private GraphNode<CFGNode> nextNode(CFGNode n) {
            return cfg.outgoingNodes(new GraphNode<>(n)).get(0);
        }

        private Pair<GraphNode<CFGNode>, GraphNode<CFGNode>> ifBranches(CFGIfNode n) {
            final var outNodes = cfg.outgoingNodes(new GraphNode<>(n));
            assert outNodes.size() == 2;
            return new Pair<>(outNodes.get(0), outNodes.get(1));
        }


        /**
         * Adds the statement {@code stmt} to the end of the list of statements.
         * <p>
         * Associates node {@code n} to this statement {@code stmt} in
         * {@code cfgNodeToIRStmt}.
         *
         * @param n
         * @param stmt
         */
        private void appendStmt(CFGNode n, IRStmtWrapper stmt) {
            final var wrappedStmt = stmt;
            this.stmts.add(wrappedStmt);
            this.wrapStmt().label.ifPresent(lbl ->
            this.cfgNodeToLabels.put(n, lbl.name()));
            this.cfgNodeToIRStmt.put(n, wrappedStmt);
        }

        @Override
        public Optional<GraphNode<CFGNode>> visit(CFGCallNode n) {
            if (!this.performProcessIfVisited(n)) {
                final var stmt = this.wrapStmt(n.call);
                this.appendStmt(n, stmt);
                this.epilogueProcess(n, stmt);
                return Optional.of(this.nextNode(n));
            }
            return Optional.empty();
        }

        @Override
        public Optional<GraphNode<CFGNode>> visit(CFGIfNode n) {
            if (!this.performProcessIfVisited(n)) {
                final var branches = this.ifBranches(n);
                final var trueBranchNode = branches.part1();
                final String trueLabel = generator.newLabel();
                this.trueBranches.add(new Pair<>(trueBranchNode, trueLabel));

                final var make = this.createMake(n);
                final var falseBranchNode = branches.part2();
                final var stmt = this.wrapStmt(make.IRCJump(n.cond, trueLabel));
                this.appendStmt(n, stmt);
                this.epilogueProcess(n, stmt);
                return Optional.of(falseBranchNode);
            }
            return Optional.empty();
        }

        @Override
        public Optional<GraphNode<CFGNode>> visit(CFGVarAssignNode n) {
            if (!this.performProcessIfVisited(n)) {
                final var make = this.createMake(n);
                final var stmt = this
                        .wrapStmt(make.IRMove(make.IRTemp(n.variable), n.value));
                this.appendStmt(n, stmt);
                this.epilogueProcess(n, stmt);
                return Optional.of(this.nextNode(n));
            }
            return Optional.empty();
        }

        @Override
        public Optional<GraphNode<CFGNode>> visit(CFGMemAssignNode n) {
            if (!this.performProcessIfVisited(n)) {
                final var make = this.createMake(n);
                final var stmt = this.wrapStmt(make.IRMove(n.target, n.value));
                this.appendStmt(n, stmt);
                this.epilogueProcess(n, stmt);
                return Optional.of(this.nextNode(n));
            }
            return Optional.empty();
        }

        /**
         * Three possibilities:
         * <ol>
         * <li>This node has never been visited.
         * <li>This node has been visited but there is no associated label to jump
         * to.
         * <li>This node has been visited and there is an associated label.
         * </ol>
         */
        @Override
        public Optional<GraphNode<CFGNode>> visit(CFGReturnNode n) {
            if (!this.performProcessIfVisited(n)) {
                final var make = this.createMake(n);
                var stmt = this.wrapStmt(make.IRReturn());
                this.appendStmt(n, stmt);
                this.epilogueProcess(n, stmt);
            }
            return Optional.empty();
        }


        public IRSeq flatten() {
            final var startNode = cfg.startNode().value();
            var make = this.createMake(startNode);
            Optional<GraphNode<CFGNode>> next =
                        Optional.of(cfg.outgoingNodes(cfg.startNode()).get(0));
            while (next.isPresent()) {
                next = next.get().value().accept(this);
            }

            while (!this.trueBranches.isEmpty()) {
                final Pair<GraphNode<CFGNode>, String> nextTrueBranch =
                                                        trueBranches.remove();
                next = Optional.of(nextTrueBranch.part1());
                make = this.createMake(next.get().value());
                final String trueLabel = nextTrueBranch.part2();

                var stmt = this.wrapStmt(make.IRLabel(trueLabel));
                this.predecessor = stmt;
                this.stmts.add(stmt);
                while (next.isPresent()) {
                    next = next.get().value().accept(this);
                }
            }
            return new IRSeq(startNode.location(), this.getFunctionBody());
        }


        /**
         * There are no labels or statements associated with the start node.
         */
        @Override
        public Optional<GraphNode<CFGNode>> visit(CFGStartNode n) {
            if (!this.performProcessIfVisited(n)) {
                var startElement = this.wrapStmt();
                this.appendStmt(n, startElement);
                this.epilogueProcess(n, startElement);
                return Optional.of(cfg.outgoingNodes(cfg.startNode()).get(0));
            }
            return Optional.empty();
        }

        @Override
        public Optional<GraphNode<CFGNode>> visit(CFGSelfLoopNode n) {
            if (!this.performProcessIfVisited(n)) {
                final var make = this.createMake(n);
                final var labelString = generator.newLabel();
                final var stmt = this.wrapStmt(make.IRJump(make.IRName(labelString)));
                stmt.setLabel(labelString);
                this.appendStmt(n, stmt);
                this.epilogueProcess(n, stmt);
            }
            return Optional.empty();
        }

        @Override
        public Optional<GraphNode<CFGNode>> visit(CFGBlockNode n) {
            if (!this.performProcessIfVisited(n)) {
                List<IRStmt> stmts = new FlattenCFGBlockVisitor().getStmts(n.block);
                final var stmt = this.wrapStmt(stmts);
                this.appendStmt(n, stmt);
                this.epilogueProcess(n, stmt);
                return Optional.of(this.nextNode(n));
            }
            return Optional.empty();
        }



    }




}
