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
        final GenericGraph<CFGNode, Boolean> genericGraph = new GenericGraph<>();

        // Test Insertion
        final var startNode = node(make.Start());
        final var retNode = node(make.Return());
        final var ifConstNode = node(make.If(constant()));
        final var selfLoopNode = node(make.SelfLoop());
        final var callNode = call();

        genericGraph.insert(startNode);
        genericGraph.insert(retNode);
        genericGraph.insert(ifConstNode);
        genericGraph.insert(selfLoopNode);
        genericGraph.insert(callNode);

        // Contains insert nodes
        assertTrue(genericGraph.containsNode(startNode));
        assertTrue(genericGraph.containsNode(retNode));
        assertTrue(genericGraph.containsNode(ifConstNode));
        assertTrue(genericGraph.containsNode(selfLoopNode));
        assertTrue(genericGraph.containsNode(callNode));

        assertTrue(genericGraph.nodes().equals(Set.of(startNode, retNode,
                                      ifConstNode, selfLoopNode, callNode)));

        // CFGNodes are equal by memory location, not content.
        assertFalse(genericGraph.containsNode(node(make.If(new IRConst(LOC, 3)))));
        assertFalse(genericGraph.containsNode(call()));

        // No edges are suddenly created.
        assertTrue(genericGraph.edges().isEmpty());
        assertFalse(genericGraph.containsEdge(retNode, retNode));
        assertFalse(genericGraph.containsEdge(ifConstNode, retNode));
        assertFalse(genericGraph.containsEdge(selfLoopNode, retNode));
        assertFalse(genericGraph.containsEdge(callNode, retNode));


        // Test Joining graph nodes
        genericGraph.join(startNode, retNode);
        assertTrue(genericGraph.containsEdge(startNode, retNode));
        assertFalse(genericGraph.containsEdge(retNode, startNode));

        assertEquals(1, genericGraph.edges().size());
        assertTrue(genericGraph.edges().contains(new Edge<>(startNode, retNode)));
        assertTrue(genericGraph.containsEdge(startNode, retNode));
        assertFalse(genericGraph.edges().contains(new Edge<>(startNode, retNode, true)));
        assertFalse(genericGraph.containsEdge(new Edge<>(startNode, retNode, true)));
        assertFalse(genericGraph.edges().contains(new Edge<>(startNode, retNode, false)));
        assertFalse(genericGraph.containsEdge(new Edge<>(startNode, retNode, false)));

        genericGraph.join(new Edge<>(startNode, retNode, true));
        assertEquals(2, genericGraph.edges().size());
        assertTrue(genericGraph.edges().contains(new Edge<>(startNode, retNode)));
        assertTrue(genericGraph.containsEdge(startNode, retNode));
        assertTrue(genericGraph.edges().contains(new Edge<>(startNode, retNode, true)));
        assertTrue(genericGraph.containsEdge(new Edge<>(startNode, retNode, true)));
        assertFalse(genericGraph.edges().contains(new Edge<>(startNode, retNode, false)));
        assertFalse(genericGraph.containsEdge(new Edge<>(startNode, retNode, false)));


        genericGraph.join(startNode, retNode);
        assertEquals(2, genericGraph.edges().size());
        assertTrue(genericGraph.edges().contains(new Edge<>(startNode, retNode)));
        assertTrue(genericGraph.containsEdge(startNode, retNode));
        assertTrue(genericGraph.edges().contains(new Edge<>(startNode, retNode, true)));
        assertTrue(genericGraph.containsEdge(new Edge<>(startNode, retNode, true)));
        assertFalse(genericGraph.edges().contains(new Edge<>(startNode, retNode, false)));
        assertFalse(genericGraph.containsEdge(new Edge<>(startNode, retNode, false)));


        // Test unlink
        // Node is not in graph.
        assertThrows(NonexistentEdgeException.class, () -> genericGraph.unlink(new Edge<>(startNode, retNode, false)));
        assertEquals(2, genericGraph.edges().size());
        assertTrue(genericGraph.edges().contains(new Edge<>(startNode, retNode)));
        assertTrue(genericGraph.containsEdge(startNode, retNode));
        assertTrue(genericGraph.edges().contains(new Edge<>(startNode, retNode, true)));
        assertTrue(genericGraph.containsEdge(new Edge<>(startNode, retNode, true)));
        assertFalse(genericGraph.edges().contains(new Edge<>(startNode, retNode, false)));
        assertFalse(genericGraph.containsEdge(new Edge<>(startNode, retNode, false)));

        genericGraph.unlink(startNode, retNode);
        assertFalse(genericGraph.containsEdge(startNode, retNode));
        assertEquals(Collections.emptySet(), genericGraph.edges());
        assertFalse(genericGraph.edges().contains(new Edge<>(startNode, retNode)));
        assertFalse(genericGraph.containsEdge(startNode, retNode));
        assertFalse(genericGraph.edges().contains(new Edge<>(startNode, retNode, true)));
        assertFalse(genericGraph.containsEdge(new Edge<>(startNode, retNode, true)));
        assertFalse(genericGraph.edges().contains(new Edge<>(startNode, retNode, false)));
        assertFalse(genericGraph.containsEdge(new Edge<>(startNode, retNode, false)));

        genericGraph.join(startNode, retNode);
        genericGraph.join(new Edge<>(startNode, retNode, true));
        genericGraph.join(retNode, startNode);
        genericGraph.join(retNode, ifConstNode);
        genericGraph.join(callNode, selfLoopNode);

        // Test Removal
        genericGraph.remove(retNode);
        genericGraph.remove(ifConstNode);
        genericGraph.remove(selfLoopNode);
        genericGraph.remove(callNode);

        assertTrue(genericGraph.containsNode(startNode));
        assertFalse(genericGraph.containsNode(retNode));
        assertFalse(genericGraph.containsNode(ifConstNode));
        assertFalse(genericGraph.containsNode(selfLoopNode));
        assertFalse(genericGraph.containsNode(callNode));

        assertEquals(Set.of(startNode), genericGraph.nodes());
        assertEquals(Collections.emptySet(), genericGraph.edges());
        assertFalse(genericGraph.containsEdge(retNode, retNode));
        assertFalse(genericGraph.containsEdge(ifConstNode, retNode));
        assertFalse(genericGraph.containsEdge(selfLoopNode, retNode));
        assertFalse(genericGraph.containsEdge(callNode, retNode));
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
    void testIncomingOutgoingNodeOrdering() {
        CFGGraph cfg = new CFGGraph(LOC);

        final var ifNode = node(make.If(constant()));
        final var assign = node(make.VarAssign("ok", constant()));
        final var ret = node(make.Return());

        cfg.insert(ifNode);
        cfg.insert(assign);
        cfg.insert(ret);

        cfg.join(cfg.startNode(), ifNode);
        cfg.join(ifNode, ret, true);
        cfg.join(ifNode, assign, false);
        cfg.join(assign, ret);

        final var outgoingIf = cfg.outgoingNodes(ifNode);
        assertEquals(2, outgoingIf.size());
        assertEquals(outgoingIf.get(0), ret);
        assertEquals(outgoingIf.get(1), assign);

        final var incomingIf = cfg.incomingNodes(ifNode);
        assertEquals(1, incomingIf.size());
        assertEquals(incomingIf.get(0), cfg.startNode());

        final var incomingAssign = cfg.incomingNodes(assign);
        assertEquals(1, incomingAssign.size());
        assertEquals(incomingAssign.get(0), ifNode);

        final var incomingReturn = cfg.incomingNodes(ret);
        assertEquals(2, incomingReturn.size());
        assertTrue(incomingReturn.contains(ifNode));
        assertTrue(incomingReturn.contains(assign));

        final var incomingStart = cfg.incomingNodes(cfg.startNode());
        assertEquals(0, incomingStart.size());

    }

}
