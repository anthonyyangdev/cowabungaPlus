package cfg.ir.constructor;

import java.util.ArrayDeque;
import java.util.Set;

import cfg.ir.graph.CFGGraph;
import cfg.ir.nodes.CFGCallNode;
import cfg.ir.nodes.CFGIfNode;
import cfg.ir.nodes.CFGMemAssignNode;
import cfg.ir.nodes.CFGNode;
import cfg.ir.nodes.CFGReturnNode;
import cfg.ir.nodes.CFGSelfLoopNode;
import cfg.ir.nodes.CFGStartNode;
import cfg.ir.nodes.CFGVarAssignNode;
import cfg.ir.visitor.IrCFGVisitor;
import graph.Edge;

public class CFGGraphRepOk {

    private CFGGraphRepOk() {}

    public static boolean repOk(CFGGraph cfg) {
        final var checker = new CFGGraphRepOkVisitor(cfg);
        final var queue = new ArrayDeque<>(cfg.nodes());

        while (!queue.isEmpty()) {
            final var node = queue.remove();
            node.accept(checker);
        }
        return true;
    }

    private static class CFGGraphRepOkVisitor implements IrCFGVisitor<Void> {

        private final CFGGraph cfg;
        private final Set<CFGNode> nodes;
        private boolean foundStartNode;

        public CFGGraphRepOkVisitor(CFGGraph cfg) {
            this.cfg = cfg;
            this.nodes = cfg.nodes();
            this.foundStartNode = false;
        }

        private void generalRepOk(CFGNode node) {
            assert nodes.contains(node);

            final var incoming = cfg.incomingNodes(node);
            final var outgoing = cfg.outgoingNodes(node);

            for (CFGNode in: incoming) {
                assert nodes.contains(in);
                assert cfg.outgoingNodes(in).contains(node);
                assert cfg.containsEdge(in, node);
            }

            for (CFGNode out: outgoing) {
                assert nodes.contains(out);
                assert cfg.incomingNodes(out).contains(node);
                assert cfg.containsEdge(node, out);
            }
        }

        @Override
        public Void visit(CFGCallNode n) {
            assert cfg.incomingNodes(n).size() >= 1;

            assert cfg.outgoingNodes(n).size() == 1;

            this.generalRepOk(n);
            return null;
        }

        @Override
        public Void visit(CFGIfNode n) {
            assert cfg.incomingNodes(n).size() >= 1;

            if (cfg.outgoingNodes(n).size() == 1) {
                this.generalRepOk(n);
                return null;
            }

            assert cfg.outgoingNodes(n).size() == 2;
            this.generalRepOk(n);

            final var outgoing = cfg.outgoingNodes(n);
            final var trueBranch = outgoing.get(0);
            final var falseBranch = outgoing.get(1);

            assert cfg.containsEdge(new Edge<>(n, trueBranch, true));
            assert cfg.containsEdge(new Edge<>(n, falseBranch, false));

            assert !cfg.containsEdge(new Edge<>(n, trueBranch));
            assert !cfg.containsEdge(new Edge<>(n, falseBranch));

            assert cfg.edgeValue(n, trueBranch).get().equals(Boolean.TRUE);
            assert cfg.edgeValue(n, falseBranch).get().equals(Boolean.FALSE);

            return null;
        }

        @Override
        public Void visit(CFGVarAssignNode n) {
            assert cfg.incomingNodes(n).size() >= 1;

            assert cfg.outgoingNodes(n).size() == 1;

            this.generalRepOk(n);
            return null;
        }

        @Override
        public Void visit(CFGMemAssignNode n) {


            assert cfg.incomingNodes(n).size() >= 1;

            assert cfg.outgoingNodes(n).size() == 1;

            this.generalRepOk(n);
            return null;
        }

        @Override
        public Void visit(CFGReturnNode n) {
            assert cfg.incomingNodes(n).size() >= 1;
            assert !cfg.incomingNodes(n).contains(n);

            assert cfg.outgoingNodes(n).size() == 0;

            this.generalRepOk(n);

            return null;
        }

        @Override
        public Void visit(CFGStartNode n) {
            assert !this.foundStartNode;

            assert cfg.incomingNodes(n).size() == 0;

            assert cfg.outgoingNodes(n).size() == 1;
            assert !cfg.outgoingNodes(n).contains(n);

            this.generalRepOk(n);
            return null;
        }

        @Override
        public Void visit(CFGSelfLoopNode n) {
            assert cfg.incomingNodes(n).size() >= 2;
            assert cfg.incomingNodes(n).contains(n);

            assert cfg.outgoingNodes(n).size() == 1;
            assert cfg.outgoingNodes(n).contains(n);
            this.generalRepOk(n);
            return null;
        }

    }

}