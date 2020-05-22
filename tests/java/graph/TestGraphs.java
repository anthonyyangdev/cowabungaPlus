package graph;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import cfg.ir.CFGGraph;
import cfg.ir.nodes.CFGNode;
import cfg.ir.nodes.CFGNodeFactory;
import cyr7.ir.nodes.IRCallStmt;
import cyr7.ir.nodes.IRConst;
import cyr7.ir.nodes.IRExpr;
import java_cup.runtime.ComplexSymbolFactory.Location;

class TestGraphs {

    private final Location LOC = new Location(-1, -1);
    private final CFGNodeFactory make = new CFGNodeFactory(new Location(-1, -1));

    private GraphNode<CFGNode> node(CFGNode n) {
        return new GraphNode<>(n);
    }

    private IRExpr constant() {
        return new IRConst(LOC, 0);
    }

    private GraphNode<CFGNode> call() {
        return node(make.Call(new IRCallStmt(LOC, List.of("_"), constant(), List.of())));
    }

    @Test
    void testInsertAndRemove() {
        final GenericGraph<CFGNode, Boolean> cfg = new GenericGraph<>();

        // Test Insertion
        final var startNode = node(make.Start());
        final var retNode = node(make.Return());
        final var ifConstNode = node(make.If(constant()));
        final var selfLoopNode = node(make.SelfLoop());
        final var callNode = call();

        cfg.insert(startNode);
        cfg.insert(retNode);
        cfg.insert(ifConstNode);
        cfg.insert(selfLoopNode);
        cfg.insert(callNode);

        // Contains insert nodes
        assertTrue(cfg.containsNode(startNode));
        assertTrue(cfg.containsNode(retNode));
        assertTrue(cfg.containsNode(ifConstNode));
        assertTrue(cfg.containsNode(selfLoopNode));
        assertTrue(cfg.containsNode(callNode));

        assertTrue(cfg.nodes().equals(Set.of(startNode, retNode,
                                      ifConstNode, selfLoopNode, callNode)));

        // CFGNodes are equal by memory location, not content.
        assertFalse(cfg.containsNode(node(make.If(new IRConst(LOC, 3)))));
        assertFalse(cfg.containsNode(call()));

        // No edges are suddenly created.
        assertTrue(cfg.edges().isEmpty());
        assertFalse(cfg.containsEdge(retNode, retNode));
        assertFalse(cfg.containsEdge(ifConstNode, retNode));
        assertFalse(cfg.containsEdge(selfLoopNode, retNode));
        assertFalse(cfg.containsEdge(callNode, retNode));


        // Test Joining graph nodes
        cfg.join(startNode, retNode);
        assertTrue(cfg.containsEdge(startNode, retNode));
        assertFalse(cfg.containsEdge(retNode, startNode));

        assertEquals(1, cfg.edges().size());
        assertTrue(cfg.edges().contains(new Edge<>(startNode, retNode)));
        assertTrue(cfg.containsEdge(startNode, retNode));
        assertFalse(cfg.edges().contains(new Edge<>(startNode, retNode, true)));
        assertFalse(cfg.containsEdge(new Edge<>(startNode, retNode, true)));
        assertFalse(cfg.edges().contains(new Edge<>(startNode, retNode, false)));
        assertFalse(cfg.containsEdge(new Edge<>(startNode, retNode, false)));

        cfg.join(new Edge<>(startNode, retNode, true));
        assertEquals(2, cfg.edges().size());
        assertTrue(cfg.edges().contains(new Edge<>(startNode, retNode)));
        assertTrue(cfg.containsEdge(startNode, retNode));
        assertTrue(cfg.edges().contains(new Edge<>(startNode, retNode, true)));
        assertTrue(cfg.containsEdge(new Edge<>(startNode, retNode, true)));
        assertFalse(cfg.edges().contains(new Edge<>(startNode, retNode, false)));
        assertFalse(cfg.containsEdge(new Edge<>(startNode, retNode, false)));


        cfg.join(startNode, retNode);
        assertEquals(2, cfg.edges().size());
        assertTrue(cfg.edges().contains(new Edge<>(startNode, retNode)));
        assertTrue(cfg.containsEdge(startNode, retNode));
        assertTrue(cfg.edges().contains(new Edge<>(startNode, retNode, true)));
        assertTrue(cfg.containsEdge(new Edge<>(startNode, retNode, true)));
        assertFalse(cfg.edges().contains(new Edge<>(startNode, retNode, false)));
        assertFalse(cfg.containsEdge(new Edge<>(startNode, retNode, false)));


        // Test unlink
        // Node is not in graph.
        assertThrows(NonexistentEdgeException.class, () -> cfg.unlink(new Edge<>(startNode, retNode, false)));
        assertEquals(2, cfg.edges().size());
        assertTrue(cfg.edges().contains(new Edge<>(startNode, retNode)));
        assertTrue(cfg.containsEdge(startNode, retNode));
        assertTrue(cfg.edges().contains(new Edge<>(startNode, retNode, true)));
        assertTrue(cfg.containsEdge(new Edge<>(startNode, retNode, true)));
        assertFalse(cfg.edges().contains(new Edge<>(startNode, retNode, false)));
        assertFalse(cfg.containsEdge(new Edge<>(startNode, retNode, false)));

        cfg.unlink(startNode, retNode);
        assertFalse(cfg.containsEdge(startNode, retNode));
        assertEquals(Collections.emptySet(), cfg.edges());
        assertFalse(cfg.edges().contains(new Edge<>(startNode, retNode)));
        assertFalse(cfg.containsEdge(startNode, retNode));
        assertFalse(cfg.edges().contains(new Edge<>(startNode, retNode, true)));
        assertFalse(cfg.containsEdge(new Edge<>(startNode, retNode, true)));
        assertFalse(cfg.edges().contains(new Edge<>(startNode, retNode, false)));
        assertFalse(cfg.containsEdge(new Edge<>(startNode, retNode, false)));

        cfg.join(startNode, retNode);
        cfg.join(new Edge<>(startNode, retNode, true));
        cfg.join(retNode, startNode);
        cfg.join(retNode, ifConstNode);
        cfg.join(callNode, selfLoopNode);

        // Test Removal
        cfg.remove(retNode);
        cfg.remove(ifConstNode);
        cfg.remove(selfLoopNode);
        cfg.remove(callNode);

        assertTrue(cfg.containsNode(startNode));
        assertFalse(cfg.containsNode(retNode));
        assertFalse(cfg.containsNode(ifConstNode));
        assertFalse(cfg.containsNode(selfLoopNode));
        assertFalse(cfg.containsNode(callNode));

        assertTrue(cfg.nodes().equals(Set.of(startNode)));
        assertEquals(Collections.emptySet(), cfg.edges());
        assertFalse(cfg.containsEdge(retNode, retNode));
        assertFalse(cfg.containsEdge(ifConstNode, retNode));
        assertFalse(cfg.containsEdge(selfLoopNode, retNode));
        assertFalse(cfg.containsEdge(callNode, retNode));
    }


    @Test
    void testCFGClean() {
        final CFGGraph cfg = new CFGGraph(LOC);

        // Test Insertion
        final var retNode = node(make.Return());
        final var ifConstNode = node(make.If(constant()));
        final var selfLoopNode = node(make.SelfLoop());
        final var callNode = call();

        cfg.insert(retNode);
        cfg.insert(ifConstNode);
        cfg.insert(selfLoopNode);
        cfg.insert(callNode);

        cfg.clean();
        assertTrue(cfg.nodes().equals(Set.of(cfg.startNode())));
        assertEquals(Collections.emptySet(), cfg.edges());
        assertFalse(cfg.containsEdge(retNode, retNode));
        assertFalse(cfg.containsEdge(ifConstNode, retNode));
        assertFalse(cfg.containsEdge(selfLoopNode, retNode));
        assertFalse(cfg.containsEdge(callNode, retNode));



        cfg.insert(retNode);
        cfg.insert(ifConstNode);
        cfg.insert(selfLoopNode);
        cfg.insert(callNode);

        cfg.join(cfg.startNode(), retNode);
        cfg.join(retNode, cfg.startNode());
        cfg.join(retNode, ifConstNode);

        cfg.clean();
        assertEquals(Set.of(cfg.startNode(), retNode, ifConstNode), cfg.nodes());
        assertEquals(3, cfg.edges().size());
        assertTrue(cfg.containsEdge(cfg.startNode(), retNode));
        assertTrue(cfg.containsEdge(retNode, cfg.startNode()));
        assertTrue(cfg.containsEdge(retNode, ifConstNode));

        assertFalse(cfg.containsEdge(retNode, retNode));
        assertFalse(cfg.containsEdge(ifConstNode, retNode));
        assertFalse(cfg.containsEdge(selfLoopNode, retNode));
        assertFalse(cfg.containsEdge(callNode, retNode));
    }


    @Test
    void testIncomingOutgoingNodes() {
        final GenericGraph<CFGNode, Boolean> cfg = new GenericGraph<>();

        // Test Insertion
        final var startNode = node(make.Start());
        final var retNode = node(make.Return());
        final var ifConstNode = node(make.If(constant()));
        final var selfLoopNode = node(make.SelfLoop());
        final var callNode = call();

        cfg.insert(startNode);
        cfg.insert(retNode);
        cfg.insert(ifConstNode);
        cfg.insert(selfLoopNode);
        cfg.insert(callNode);

        cfg.join(startNode, retNode);
        cfg.join(startNode, ifConstNode);
        cfg.join(startNode, callNode);


        assertEquals(Collections.emptyList(), cfg.incomingNodes(startNode));
        assertEquals(Collections.emptyList(), cfg.incomingNodes(selfLoopNode));
        assertEquals(List.of(startNode), cfg.incomingNodes(retNode));
        assertEquals(List.of(startNode), cfg.incomingNodes(callNode));
        assertEquals(List.of(startNode), cfg.incomingNodes(ifConstNode));

        assertEquals(Collections.emptyList(), cfg.outgoingNodes(retNode));
        assertEquals(Collections.emptyList(), cfg.outgoingNodes(selfLoopNode));
        assertEquals(Collections.emptyList(), cfg.outgoingNodes(callNode));
        assertEquals(Collections.emptyList(), cfg.outgoingNodes(ifConstNode));
        assertEquals(3, cfg.outgoingNodes(startNode).size());
        assertEquals(Set.of(retNode, ifConstNode, callNode),
                     new HashSet<>(cfg.outgoingNodes(startNode)));
    }

    @Test
    void testOutgoingNodeOrdering() {

    }

}
