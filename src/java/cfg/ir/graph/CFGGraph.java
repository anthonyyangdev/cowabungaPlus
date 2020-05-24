package cfg.ir.graph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.Graphs;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;

import cfg.ir.nodes.CFGNode;
import cfg.ir.nodes.CFGStartNode;
import graph.Edge;
import graph.NonexistentEdgeException;
import graph.NonexistentNodeException;
import java_cup.runtime.ComplexSymbolFactory.Location;

/**
 * Graph structure for control-flow graphs. Edges optionally have a value for
 * if nodes to indicate which branch is the true branch and which is the false
 * branch.
 * @author ayang
 *
 */
public class CFGGraph {

    public final CFGNode startNode;
    private final MutableValueGraph<CFGNode, Optional<Boolean>> graph;

    public CFGGraph(Location n) {
        super();
        this.startNode = new CFGStartNode(n);
        this.graph = ValueGraphBuilder.directed().allowsSelfLoops(true).build();
        this.graph.addNode(startNode);
    }


    /**
     * Removes all nodes from the graph that are unreachable from the start
     * node.
     */
    public void clean() {

        Set<CFGNode> reachable =
                    Graphs.reachableNodes(this.graph.asGraph(), this.startNode);
        final var unreachable = new HashSet<>(this.graph.nodes());
        unreachable.removeAll(reachable);
        for (CFGNode node: unreachable) {
            this.remove(node);
        }
    }

    /**
     * For If node, a list is returned are returned such that the true branch
     * edges prioritizes the false branch.
     * <p>
     * For example, suppose the if node has two out nodes, then the first
     * element of the list if the true branch, and second element of the list
     * is the false branch.
     */
    public void join(Edge<CFGNode, Boolean> edge)
            throws NonexistentNodeException {
        final var start = edge.start;
        final var end = edge.end;
        this.graph.putEdgeValue(start, end, edge.value);
    }


    public void unlink(CFGNode start, CFGNode end) {
        if (this.graph.hasEdgeConnecting(start, end)) {
            this.graph.removeEdge(start, end);
            return;
        }
        throw new NonexistentEdgeException(new Edge<>(start, end));
    }


    public Set<CFGNode> nodes() {
        return this.graph.nodes();
    }


    public Set<EndpointPair<CFGNode>> edges() {
        return this.graph.edges();
    }


    public boolean insert(CFGNode node) {
        return this.graph.addNode(node);
    }


    public CFGNode remove(CFGNode node) {
        if (this.graph.removeNode(node)) {
            return node;
        }
        throw new NonexistentNodeException(node);
    }


    public List<CFGNode> outgoingNodes(CFGNode node) {
        return this.graph.successors(node).stream().sorted((x, y) -> {
            final var valToX = this.graph.edgeValue(node, x).get();
            final var valToY = this.graph.edgeValue(node, y).get();
            if (valToX.isEmpty()) {
                return 1;
            } else if (valToY.isEmpty()) {
                return -1;
            } else if (valToX.get()) {
                return -1;
            } else {
                return 1;
            }
        }).collect(Collectors.toList());
    }


    public List<CFGNode> incomingNodes(CFGNode node) {
        return new ArrayList<>(this.graph.predecessors(node));
    }


    public void join(CFGNode start, CFGNode end) {
        this.graph.putEdgeValue(start, end, Optional.empty());
    }


    public void join(CFGNode start, CFGNode end, Boolean value) {
        this.graph.putEdgeValue(start, end, Optional.of(value));
    }


    public void unlink(Edge<CFGNode, Boolean> edge) {
        if (this.containsEdge(edge)) {
            this.graph.removeEdge(edge.start, edge.end);
            return;
        }
        throw new NonexistentEdgeException(edge);
    }


    public boolean containsNode(CFGNode node) {
        return this.graph.nodes().contains(node);
    }


    public boolean containsEdge(CFGNode start, CFGNode end) {
        return this.graph.hasEdgeConnecting(start, end);
    }


    public boolean containsEdge(Edge<CFGNode, Boolean> edge) {
        final var start = edge.start;
        final var end = edge.end;
        return this.graph.hasEdgeConnecting(start, end)
                 && this.graph.edgeValue(start, end).get().equals(edge.value);
    }


    public int inDegree(CFGNode node) {
        return this.graph.inDegree(node);
    }

    public int outDegree(CFGNode node) {
        return this.graph.outDegree(node);
    }

}
