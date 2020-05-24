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

import cfg.ir.graph.CFGGraph;
import cfg.ir.nodes.CFGNode;
import cfg.ir.nodes.CFGNodeFactory;
import cyr7.ir.nodes.IRCallStmt;
import cyr7.ir.nodes.IRConst;
import cyr7.ir.nodes.IRExpr;
import java_cup.runtime.ComplexSymbolFactory.Location;

class TestGraphs {

    private final Location LOC = new Location(-1, -1);
    private final CFGNodeFactory make = new CFGNodeFactory(new Location(-1, -1));

    private IRExpr constant() {
        return new IRConst(LOC, 0);
    }

    private CFGNode call() {
        return make.Call(new IRCallStmt(LOC, List.of("_"), constant(), List.of()));
    }

    @Test
    void testInsertAndRemove() {
        final CFGGraph genericGraph = new CFGGraph(LOC);

        // Test Insertion
        final var retNode = make.Return();
        final var ifConstNode = make.If(constant());
        final var selfLoopNode = make.SelfLoop();
        final var callNode = call();

        genericGraph.insert(retNode);
        genericGraph.insert(ifConstNode);
        genericGraph.insert(selfLoopNode);
        genericGraph.insert(callNode);

        // Contains insert nodes
        assertTrue(genericGraph.containsNode(genericGraph.startNode));
        assertTrue(genericGraph.containsNode(retNode));
        assertTrue(genericGraph.containsNode(ifConstNode));
        assertTrue(genericGraph.containsNode(selfLoopNode));
        assertTrue(genericGraph.containsNode(callNode));

        assertTrue(genericGraph.nodes().equals(Set.of(genericGraph.startNode, retNode,
                                      ifConstNode, selfLoopNode, callNode)));

        // CFGNodes are equal by memory location, not content.
        assertFalse(genericGraph.containsNode(make.If(new IRConst(LOC, 3))));
        assertFalse(genericGraph.containsNode(call()));

        // No edges are suddenly created.
        assertTrue(genericGraph.edges().isEmpty());
        assertFalse(genericGraph.containsEdge(retNode, retNode));
        assertFalse(genericGraph.containsEdge(ifConstNode, retNode));
        assertFalse(genericGraph.containsEdge(selfLoopNode, retNode));
        assertFalse(genericGraph.containsEdge(callNode, retNode));


        // Test Joining graph nodes
        genericGraph.join(genericGraph.startNode, retNode);
        assertTrue(genericGraph.containsEdge(genericGraph.startNode, retNode));
        assertFalse(genericGraph.containsEdge(retNode, genericGraph.startNode));

        assertEquals(1, genericGraph.edges().size());
        assertTrue(genericGraph.containsEdge(genericGraph.startNode, retNode));
        assertFalse(genericGraph.containsEdge(new Edge<>(genericGraph.startNode, retNode, true)));
        assertFalse(genericGraph.containsEdge(new Edge<>(genericGraph.startNode, retNode, false)));

        genericGraph.join(new Edge<>(genericGraph.startNode, retNode, true));
        assertEquals(2, genericGraph.edges().size());
        assertTrue(genericGraph.containsEdge(genericGraph.startNode, retNode));
        assertTrue(genericGraph.containsEdge(new Edge<>(genericGraph.startNode, retNode, true)));
        assertFalse(genericGraph.containsEdge(new Edge<>(genericGraph.startNode, retNode, false)));


        genericGraph.join(genericGraph.startNode, retNode);
        assertEquals(2, genericGraph.edges().size());
        assertTrue(genericGraph.containsEdge(genericGraph.startNode, retNode));
        assertTrue(genericGraph.containsEdge(new Edge<>(genericGraph.startNode, retNode, true)));
        assertFalse(genericGraph.containsEdge(new Edge<>(genericGraph.startNode, retNode, false)));


        // Test unlink
        // Node is not in graph.
        assertThrows(NonexistentEdgeException.class, () -> genericGraph.unlink(new Edge<>(genericGraph.startNode, retNode, false)));
        assertEquals(2, genericGraph.edges().size());
        assertTrue(genericGraph.containsEdge(genericGraph.startNode, retNode));
        assertTrue(genericGraph.containsEdge(new Edge<>(genericGraph.startNode, retNode, true)));
        assertFalse(genericGraph.containsEdge(new Edge<>(genericGraph.startNode, retNode, false)));

        genericGraph.unlink(genericGraph.startNode, retNode);
        assertFalse(genericGraph.containsEdge(genericGraph.startNode, retNode));
        assertEquals(Collections.emptySet(), genericGraph.edges());
        assertFalse(genericGraph.containsEdge(genericGraph.startNode, retNode));
        assertFalse(genericGraph.containsEdge(new Edge<>(genericGraph.startNode, retNode, true)));
        assertFalse(genericGraph.containsEdge(new Edge<>(genericGraph.startNode, retNode, false)));

        genericGraph.join(genericGraph.startNode, retNode);
        genericGraph.join(new Edge<>(genericGraph.startNode, retNode, true));
        genericGraph.join(retNode, genericGraph.startNode);
        genericGraph.join(retNode, ifConstNode);
        genericGraph.join(callNode, selfLoopNode);

        // Test Removal
        genericGraph.remove(retNode);
        genericGraph.remove(ifConstNode);
        genericGraph.remove(selfLoopNode);
        genericGraph.remove(callNode);

        assertTrue(genericGraph.containsNode(genericGraph.startNode));
        assertFalse(genericGraph.containsNode(retNode));
        assertFalse(genericGraph.containsNode(ifConstNode));
        assertFalse(genericGraph.containsNode(selfLoopNode));
        assertFalse(genericGraph.containsNode(callNode));

        assertEquals(Set.of(genericGraph.startNode), genericGraph.nodes());
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
        final var retNode = make.Return();
        final var ifConstNode = make.If(constant());
        final var selfLoopNode = make.SelfLoop();
        final var callNode = call();

        cfg.insert(retNode);
        cfg.insert(ifConstNode);
        cfg.insert(selfLoopNode);
        cfg.insert(callNode);

        cfg.clean();
        assertTrue(cfg.nodes().equals(Set.of(cfg.startNode)));
        assertEquals(Collections.emptySet(), cfg.edges());
        assertFalse(cfg.containsEdge(retNode, retNode));
        assertFalse(cfg.containsEdge(ifConstNode, retNode));
        assertFalse(cfg.containsEdge(selfLoopNode, retNode));
        assertFalse(cfg.containsEdge(callNode, retNode));



        cfg.insert(retNode);
        cfg.insert(ifConstNode);
        cfg.insert(selfLoopNode);
        cfg.insert(callNode);

        cfg.join(cfg.startNode, retNode);
        cfg.join(retNode, cfg.startNode);
        cfg.join(retNode, ifConstNode);

        cfg.clean();
        assertEquals(Set.of(cfg.startNode, retNode, ifConstNode), cfg.nodes());
        assertEquals(3, cfg.edges().size());
        assertTrue(cfg.containsEdge(cfg.startNode, retNode));
        assertTrue(cfg.containsEdge(retNode, cfg.startNode));
        assertTrue(cfg.containsEdge(retNode, ifConstNode));

        assertFalse(cfg.containsEdge(retNode, retNode));
        assertFalse(cfg.containsEdge(ifConstNode, retNode));
        assertFalse(cfg.containsEdge(selfLoopNode, retNode));
        assertFalse(cfg.containsEdge(callNode, retNode));
    }


    @Test
    void testIncomingOutgoingNodes() {
        final CFGGraph cfg = new CFGGraph(LOC);

        // Test Insertion
        final var retNode = make.Return();
        final var ifConstNode = make.If(constant());
        final var selfLoopNode = make.SelfLoop();
        final var callNode = call();

        cfg.insert(retNode);
        cfg.insert(ifConstNode);
        cfg.insert(selfLoopNode);
        cfg.insert(callNode);

        cfg.join(cfg.startNode, retNode);
        cfg.join(cfg.startNode, ifConstNode);
        cfg.join(cfg.startNode, callNode);


        assertEquals(Collections.emptyList(), cfg.incomingNodes(cfg.startNode));
        assertEquals(Collections.emptyList(), cfg.incomingNodes(selfLoopNode));
        assertEquals(List.of(cfg.startNode), cfg.incomingNodes(retNode));
        assertEquals(List.of(cfg.startNode), cfg.incomingNodes(callNode));
        assertEquals(List.of(cfg.startNode), cfg.incomingNodes(ifConstNode));

        assertEquals(Collections.emptyList(), cfg.outgoingNodes(retNode));
        assertEquals(Collections.emptyList(), cfg.outgoingNodes(selfLoopNode));
        assertEquals(Collections.emptyList(), cfg.outgoingNodes(callNode));
        assertEquals(Collections.emptyList(), cfg.outgoingNodes(ifConstNode));
        assertEquals(3, cfg.outgoingNodes(cfg.startNode).size());
        assertEquals(Set.of(retNode, ifConstNode, callNode),
                     new HashSet<>(cfg.outgoingNodes(cfg.startNode)));
    }

    @Test
    void testIncomingOutgoingNodeOrdering() {
        CFGGraph cfg = new CFGGraph(LOC);

        final var ifNode = make.If(constant());
        final var assign = make.VarAssign("ok", constant());
        final var ret = make.Return();

        cfg.insert(ifNode);
        cfg.insert(assign);
        cfg.insert(ret);

        cfg.join(cfg.startNode, ifNode);
        cfg.join(ifNode, ret, true);
        cfg.join(ifNode, assign, false);
        cfg.join(assign, ret);

        final var outgoingIf = cfg.outgoingNodes(ifNode);
        assertEquals(2, outgoingIf.size());
        assertEquals(outgoingIf.get(0), ret);
        assertEquals(outgoingIf.get(1), assign);

        final var incomingIf = cfg.incomingNodes(ifNode);
        assertEquals(1, incomingIf.size());
        assertEquals(incomingIf.get(0), cfg.startNode);

        final var incomingAssign = cfg.incomingNodes(assign);
        assertEquals(1, incomingAssign.size());
        assertEquals(incomingAssign.get(0), ifNode);

        final var incomingReturn = cfg.incomingNodes(ret);
        assertEquals(2, incomingReturn.size());
        assertTrue(incomingReturn.contains(ifNode));
        assertTrue(incomingReturn.contains(assign));

        final var incomingStart = cfg.incomingNodes(cfg.startNode);
        assertEquals(0, incomingStart.size());

    }

}
