package cfg.ir.graph;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import cfg.ir.nodes.CFGNode;
import cfg.ir.nodes.CFGNodeFactory;
import cyr7.ir.nodes.IRCallStmt;
import cyr7.ir.nodes.IRConst;
import cyr7.ir.nodes.IRExpr;
import graph.Edge;
import graph.NonexistentEdgeException;
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

        // Contains insert nodes
        assertTrue(cfg.containsNode(cfg.startNode));
        assertTrue(cfg.containsNode(retNode));
        assertTrue(cfg.containsNode(ifConstNode));
        assertTrue(cfg.containsNode(selfLoopNode));
        assertTrue(cfg.containsNode(callNode));

        assertTrue(cfg.nodes().equals(Set.of(cfg.startNode, retNode,
                                      ifConstNode, selfLoopNode, callNode)));

        // CFGNodes are equal by memory location, not content.
        assertFalse(cfg.containsNode(make.If(new IRConst(LOC, 3))));
        assertFalse(cfg.containsNode(call()));

        // No edges are suddenly created.
        assertTrue(cfg.edges().isEmpty());
        assertFalse(cfg.containsEdge(retNode, retNode));
        assertFalse(cfg.containsEdge(ifConstNode, retNode));
        assertFalse(cfg.containsEdge(selfLoopNode, retNode));
        assertFalse(cfg.containsEdge(callNode, retNode));


        // Test Joining graph nodes
        cfg.join(cfg.startNode, retNode);
        assertTrue(cfg.containsEdge(cfg.startNode, retNode));
        assertFalse(cfg.containsEdge(retNode, cfg.startNode));

        assertEquals(1, cfg.edges().size());
        assertTrue(cfg.containsEdge(cfg.startNode, retNode));
        assertFalse(cfg.containsEdge(new Edge<>(cfg.startNode, retNode, true)));
        assertFalse(cfg.containsEdge(new Edge<>(cfg.startNode, retNode, false)));

        cfg.join(new Edge<>(cfg.startNode, retNode, true));
        assertEquals(1, cfg.edges().size());
        assertTrue(cfg.containsEdge(cfg.startNode, retNode));
        assertTrue(cfg.containsEdge(new Edge<>(cfg.startNode, retNode, true)));
        assertFalse(cfg.containsEdge(new Edge<>(cfg.startNode, retNode, false)));


        cfg.join(cfg.startNode, retNode);
        assertEquals(1, cfg.edges().size());
        assertTrue(cfg.containsEdge(cfg.startNode, retNode));
        assertFalse(cfg.containsEdge(new Edge<>(cfg.startNode, retNode, true)));
        assertFalse(cfg.containsEdge(new Edge<>(cfg.startNode, retNode, false)));


        // Test unlink
        // Node is not in graph.
        assertThrows(NonexistentEdgeException.class, () -> cfg.unlink(new Edge<>(cfg.startNode, retNode, false)));
        assertEquals(1, cfg.edges().size());
        assertTrue(cfg.containsEdge(cfg.startNode, retNode));
        assertFalse(cfg.containsEdge(new Edge<>(cfg.startNode, retNode, true)));
        assertFalse(cfg.containsEdge(new Edge<>(cfg.startNode, retNode, false)));

        cfg.unlink(cfg.startNode, retNode);
        assertFalse(cfg.containsEdge(cfg.startNode, retNode));
        assertEquals(Collections.emptySet(), cfg.edges());
        assertFalse(cfg.containsEdge(cfg.startNode, retNode));
        assertFalse(cfg.containsEdge(new Edge<>(cfg.startNode, retNode, true)));
        assertFalse(cfg.containsEdge(new Edge<>(cfg.startNode, retNode, false)));

        cfg.join(cfg.startNode, retNode);
        cfg.join(new Edge<>(cfg.startNode, retNode, true));
        cfg.join(retNode, cfg.startNode);
        cfg.join(retNode, ifConstNode);
        cfg.join(callNode, selfLoopNode);

        // Test Removal
        cfg.remove(retNode);
        cfg.remove(ifConstNode);
        cfg.remove(selfLoopNode);
        cfg.remove(callNode);

        assertTrue(cfg.containsNode(cfg.startNode));
        assertFalse(cfg.containsNode(retNode));
        assertFalse(cfg.containsNode(ifConstNode));
        assertFalse(cfg.containsNode(selfLoopNode));
        assertFalse(cfg.containsNode(callNode));

        assertEquals(Set.of(cfg.startNode), cfg.nodes());
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
        cfg.join(ifNode, ret, Optional.of(true));
        cfg.join(ifNode, assign, Optional.of(false));
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
