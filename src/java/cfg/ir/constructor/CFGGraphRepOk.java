package cfg.ir.constructor;

import java.util.ArrayDeque;
import java.util.stream.Collectors;

import cfg.ir.CFGGraph;
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
import graph.GraphNode;

public class CFGGraphRepOk {

    private CFGGraphRepOk() {}

    public static boolean repOk(CFGGraph cfg) {
        final var checker = new CFGGraphRepOkVisitor(cfg);
        final var queue = new ArrayDeque<>(cfg.nodes().stream()
                            .map(GraphNode<CFGNode>::value)
                            .collect(Collectors.toList()));

        while (!queue.isEmpty()) {
            final var node = queue.remove();
            node.accept(checker);
        }
        return true;
    }

    private static class CFGGraphRepOkVisitor implements IrCFGVisitor<Void> {

        private final CFGGraph cfg;
        private boolean foundStartNode;

        public CFGGraphRepOkVisitor(CFGGraph cfg) {
            this.cfg = cfg;
            this.foundStartNode = false;
        }

        private GraphNode<CFGNode> node(CFGNode n) {
            return new GraphNode<>(n);
        }

        private void generalRepOk(GraphNode<CFGNode> node) {
            assert cfg.nodes().contains(node);

            final var incoming = cfg.incomingNodes(node);
            final var outgoing = cfg.outgoingNodes(node);

            for (GraphNode<CFGNode> in: incoming) {
                assert cfg.nodes().contains(in);
                assert cfg.outgoingNodes(in).contains(node);
                assert cfg.containsEdge(in, node);
            }

            for (GraphNode<CFGNode> out: outgoing) {
                assert cfg.nodes().contains(out);
                assert cfg.incomingNodes(out).contains(node);
                assert cfg.containsEdge(node, out);

                if (node.value() instanceof CFGIfNode) {
                    assert !cfg.edges().contains(new Edge<>(node, out));
                } else {
                    assert cfg.edges().contains(new Edge<>(node, out));
                }
            }
        }

        @Override
        public Void visit(CFGCallNode n) {
            final var node = node(n);

            assert cfg.incomingNodes(node).size() >= 1;

            assert cfg.outgoingNodes(node).size() == 1;

            this.generalRepOk(node);
            return null;
        }

        @Override
        public Void visit(CFGIfNode n) {
            final var node = node(n);

            assert cfg.incomingNodes(node).size() >= 1;

            assert cfg.outgoingNodes(node).size() == 2;

            this.generalRepOk(node);

            final var outgoing = cfg.outgoingNodes(node);
            final var trueBranch = outgoing.get(0);
            final var falseBranch = outgoing.get(1);

            assert cfg.containsEdge(new Edge<>(node, trueBranch, true));
            assert cfg.containsEdge(new Edge<>(node, falseBranch, false));

            assert !cfg.containsEdge(new Edge<>(node, trueBranch));
            assert !cfg.containsEdge(new Edge<>(node, falseBranch));

            return null;
        }

        @Override
        public Void visit(CFGVarAssignNode n) {
            final var node = node(n);

            assert cfg.incomingNodes(node).size() >= 1;

            assert cfg.outgoingNodes(node).size() == 1;

            this.generalRepOk(node);
            return null;
        }

        @Override
        public Void visit(CFGMemAssignNode n) {
            final var node = node(n);

            assert cfg.incomingNodes(node).size() >= 1;

            assert cfg.outgoingNodes(node).size() == 1;

            this.generalRepOk(node);
            return null;
        }

        @Override
        public Void visit(CFGReturnNode n) {
            final var node = node(n);

            assert cfg.incomingNodes(node).size() >= 1;
            assert !cfg.incomingNodes(node).contains(node);

            assert cfg.outgoingNodes(node).size() == 0;

            this.generalRepOk(node);

            return null;
        }

        @Override
        public Void visit(CFGStartNode n) {
            assert !this.foundStartNode;

            final var node = node(n);

            assert cfg.incomingNodes(node).size() == 0;

            assert cfg.outgoingNodes(node).size() == 1;
            assert !cfg.outgoingNodes(node).contains(node);

            this.generalRepOk(node);
            return null;
        }

        @Override
        public Void visit(CFGSelfLoopNode n) {
            final var node = node(n);

            assert cfg.incomingNodes(node).size() >= 2;
            assert cfg.incomingNodes(node).contains(node);

            assert cfg.outgoingNodes(node).size() == 1;
            assert cfg.outgoingNodes(node).contains(node);
            this.generalRepOk(node);
            return null;
        }

    }

}
