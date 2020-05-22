package cfg.ir.constructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import cfg.ir.CFGGraph;
import cfg.ir.nodes.CFGCallNode;
import cfg.ir.nodes.CFGIfNode;
import cfg.ir.nodes.CFGMemAssignNode;
import cfg.ir.nodes.CFGNode;
import cfg.ir.nodes.CFGReturnNode;
import cfg.ir.nodes.CFGSelfLoopNode;
import cfg.ir.nodes.CFGStubNode;
import cfg.ir.nodes.CFGVarAssignNode;
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
import cyr7.ir.nodes.IRStmt;
import cyr7.ir.nodes.IRTemp;
import cyr7.visitor.MyIRVisitor;
import graph.Edge;
import graph.GraphNode;
import java_cup.runtime.ComplexSymbolFactory.Location;
import polyglot.util.Pair;


public class NormalCFGConstructor {

    private NormalCFGConstructor() {}

    public static CFGGraph construct(IRStmt body) {
        final var generator = new CFGConstructorVisitor(body.location());
        final var cfg = generator.execute(body);
        cfg.clean();
        return cfg;
    }

    private static class CFGConstructorVisitor implements MyIRVisitor<GraphNode<CFGNode>> {

        private final Map<String, GraphNode<CFGNode>> labelToCFG;
        private final Queue<Pair<GraphNode<CFGNode>, String>> jumpTargetFromCFG;
        private final GraphNode<CFGNode> absoluteLastReturn = node(new CFGReturnNode(
                                    new Location(Integer.MAX_VALUE, Integer.MAX_VALUE)));
        private GraphNode<CFGNode> successor;

        /**
         * This boolean is for testing purposes, enforcing that IRSeq is only found
         * at the top-level of the IRTree.
         */
        private boolean hasEnteredIRSeq;

        private final CFGGraph cfg;

        private CFGConstructorVisitor(Location n) {
            this.labelToCFG = new HashMap<>();
            this.jumpTargetFromCFG = new LinkedList<>();
            this.hasEnteredIRSeq = false;
            this.successor = absoluteLastReturn;
            this.cfg = new CFGGraph(n);
        }

        private CFGStubNode createStubNode() {
            return new CFGStubNode();
        }

        private GraphNode<CFGNode> node(CFGNode n) {
            return new GraphNode<>(n);
        }

        public CFGGraph execute(IRStmt body) {
            body.accept(this);
            return this.cfg;
        }

        @Override
        public GraphNode<CFGNode> visit(IRSeq n) {
            if (this.hasEnteredIRSeq) {
                throw new UnsupportedOperationException(
                        "Cannot enter the IRSeq visitor twice");
            } else {
                this.hasEnteredIRSeq = true;
            }

            ArrayList<IRStmt> stmts = new ArrayList<>(n.stmts());
            for (int i = stmts.size() - 1; i >= 0; i--) {
                var stmt = stmts.get(i);
                successor = stmt.accept(this);
            }
            cfg.join(cfg.startNode(), successor);

            while (!this.jumpTargetFromCFG.isEmpty()) {
                var nextPair = this.jumpTargetFromCFG.poll();
                GraphNode<CFGNode> stub = nextPair.part1();
                String target = nextPair.part2();

                if (this.labelToCFG.containsKey(target)) {
                    GraphNode<CFGNode> targetNode = this.labelToCFG.get(target);
                    final var incomingNodes = cfg.incomingNodes(stub);
                    for (GraphNode<CFGNode> incoming: incomingNodes) {
                        // Target node may be itself, which
                        // indicates an empty loop coming from the parent node.
                        if (targetNode == stub) {
                            // Infinite loop...
                            var selfLoop = node(new CFGSelfLoopNode());
                            cfg.insert(selfLoop);
                            cfg.join(incoming, selfLoop);
                            cfg.join(selfLoop, selfLoop);
                        } else {
                            cfg.join(incoming, targetNode);
                        }
                    }
                } else {
                    throw new UnsupportedOperationException(
                            "Target label was never found in the program.");
                }
                cfg.remove(stub);
            }
            return successor;
        }

        @Override
        public GraphNode<CFGNode> visit(IRCJump n) {
            // IR should be lowered, meaning false branches are fall-throughs.
            final String trueBranchLabel = n.trueLabel();
            final GraphNode<CFGNode> ifNode;
            final GraphNode<CFGNode> trueBranchNode;
            if (this.labelToCFG.containsKey(trueBranchLabel)) {
                ifNode = node(new CFGIfNode(n.location(), n.cond()));
                trueBranchNode = this.labelToCFG.get(trueBranchLabel);
            } else {
                // Create stub node, and connect target to the stub node.
                ifNode = node(new CFGIfNode(n.location(), n.cond()));
                trueBranchNode = node(this.createStubNode());
                this.jumpTargetFromCFG.add(new Pair<>(trueBranchNode, trueBranchLabel));
                cfg.insert(trueBranchNode);
            }
            cfg.insert(ifNode);
            cfg.join(new Edge<>(ifNode, trueBranchNode, true));
            cfg.join(new Edge<>(ifNode, successor, false));
            return ifNode;
        }


        @Override
        public GraphNode<CFGNode> visit(IRJump n) {
            if (n.target() instanceof IRName) {
                String target = ((IRName) n.target()).name();
                if (this.labelToCFG.containsKey(target)) {
                    // Make successor the target node.
                    return this.labelToCFG.get(target);
                } else {
                    // Create a stub node for later computation
                    final var stub = node(this.createStubNode());
                    this.jumpTargetFromCFG.add(new Pair<>(stub, target));
                    cfg.insert(stub);
                    return stub;
                }
            } else {
                throw new UnsupportedOperationException(
                        "IRJump contains a non-name target.");
            }
        }

        @Override
        public GraphNode<CFGNode> visit(IRCallStmt n) {
            final var callNode = node(new CFGCallNode(n.location(), n));
            cfg.insert(callNode);
            cfg.join(callNode, successor);
            return callNode;
        }

        @Override
        public GraphNode<CFGNode> visit(IRLabel n) {
            this.labelToCFG.put(n.name(), successor);
            return successor;
        }


        @Override
        public GraphNode<CFGNode> visit(IRMove n) {
            final GraphNode<CFGNode> assignNode;
            if (n.target() instanceof IRTemp) {
                final var temp = (IRTemp) n.target();
                assignNode = node(new CFGVarAssignNode(n.location(),
                                                temp.name(), n.source()));
            } else {
                assignNode = node(new CFGMemAssignNode(n.location(),
                                                n.target(), n.source()));
            }
            cfg.insert(assignNode);
            cfg.join(assignNode, successor);
            return assignNode;
        }


        @Override
        public GraphNode<CFGNode> visit(IRReturn n) {
            final var returnNode = node(new CFGReturnNode(n.location()));
            cfg.insert(returnNode);
            return returnNode;
        }


        @Override
        public GraphNode<CFGNode> visit(IRCompUnit n) {
            throw new UnsupportedOperationException(
                    "Cannot use IRCompUnit in this visitor.");
        }

        @Override
        public GraphNode<CFGNode> visit(IRExp n) {
            throw new UnsupportedOperationException("Cannot use IRExp in LIR.");
        }

        @Override
        public GraphNode<CFGNode> visit(IRFuncDecl n) {
            throw new UnsupportedOperationException(
                    "Cannot use IRFuncDecl in this visitor.");
        }

        @Override
        public GraphNode<CFGNode> visit(IRBinOp n) {
            throw new UnsupportedOperationException(
                    "Cannot use IR expressions in this visitor.");
        }

        @Override
        public GraphNode<CFGNode> visit(IRCall n) {
            throw new UnsupportedOperationException("Cannot use IRCall in LIR.");
        }

        @Override
        public GraphNode<CFGNode> visit(IRConst n) {
            throw new UnsupportedOperationException(
                    "Cannot use IR expressions in this visitor.");
        }

        @Override
        public GraphNode<CFGNode> visit(IRESeq n) {
            throw new UnsupportedOperationException("Cannot use IRESeq in LIR.");
        }

        @Override
        public GraphNode<CFGNode> visit(IRMem n) {
            throw new UnsupportedOperationException(
                    "Cannot use IR expressions in this visitor.");
        }

        @Override
        public GraphNode<CFGNode> visit(IRName n) {
            throw new UnsupportedOperationException(
                    "There are no reasons to use IRName other than it being inside of IRJump or IRCallStmt.");
        }

        @Override
        public GraphNode<CFGNode> visit(IRTemp n) {
            throw new UnsupportedOperationException(
                    "Cannot use IR expressions in this visitor.");
        }
    }

}
