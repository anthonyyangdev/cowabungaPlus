package cfg.ir.graph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.Graph;
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

    public Graph<CFGNode> graph() {
        return this.graph.asGraph();
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

    public Optional<Boolean> edgeValue(CFGNode start, CFGNode end) {
        final var value = this.graph.edgeValue(start, end);
        if (value.isPresent()) {
            return value.get();
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
        final var succs = this.graph.successors(node);
        if (succs.size() <= 1) { return List.copyOf(succs); }
        if (succs.size() == 2 ) {
            ArrayList<CFGNode> outs = new ArrayList<>(succs);
            final var first = outs.get(0);
            if (this.edgeValue(node, first).get()) {
                return outs;
            } else {
                outs.set(0, outs.get(1));
                outs.set(1, first);
                return outs;
            }
        }
        throw new AssertionError("Number of outgoing nodes cannot exceed two.");
    }


    public List<CFGNode> incomingNodes(CFGNode node) {
        return new ArrayList<>(this.graph.predecessors(node));
    }


    public void join(CFGNode start, CFGNode end) {
        this.graph.putEdgeValue(start, end, Optional.empty());
    }


    public void join(CFGNode start, CFGNode end, Optional<Boolean> value) {
        this.graph.putEdgeValue(start, end, value);
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


    /**
     * Replaces the node {@code prev} in the CFG with node {@code now}. Any
     * edges, including the values of those edges, that include the node
     * {@code prev} are replaced with edges with the same values that subtitute
     * occurrences of {@code prev} with {@code now}. The node {@code prev} is
     * also removed from the CFG entirely.
     *
     * @param old The node to be replaced and removed from the CFG.
     * @param current The node to be replace {@code old}. Can be a node that
     *                already exists in the CFG.
     */
    public void replaceNode(CFGNode prev, CFGNode now) {
        if (prev.equals(now)) {
            return;
        }
        if (!this.containsNode(now)) {
            this.insert(now);
        }
        this.graph.incidentEdges(prev).forEach(edge -> {
            if (edge.nodeV().equals(prev)) {
                this.join(edge.nodeU(), now,
                        this.edgeValue(edge.nodeU(), prev));
            } else {
                this.join(now, edge.nodeV(),
                        this.edgeValue(prev, edge.nodeV()));
            }
        });
        this.remove(prev);
    }


    /**
     * Inserts a new node with value {@code prev} before {@code node}.
     * This means all edges that went into {@code node} now point to
     * {@code prev}. Outgoing edges from {@code node} remain the same. Lastly,
     * an edge from {@code prev} to {@code node} is created.
     * @param prev
     * @param node
     */
    public void prependNode(CFGNode node, CFGNode... newNode) {
        if (newNode.length == 0) return;

        CFGNode firstInChain = newNode[0];
        this.insert(firstInChain);
        CFGNode lastInChain = firstInChain;
        for (int i = 1; i < newNode.length; i++) {
            final var nextGraphNode = newNode[i];
            this.insert(nextGraphNode);
            this.join(lastInChain, nextGraphNode);
            lastInChain = nextGraphNode;
        }

        final var incomingNodes = this.incomingNodes(node);
        incomingNodes.forEach(in -> {
            final var value = this.edgeValue(in, node);
            this.unlink(in, node);
            if (value.isPresent()) {
                this.join(in, firstInChain, value);
            } else {
                this.join(in, firstInChain);
            }
        });
        this.join(lastInChain, node);
    }



    /**
     * Inserts a new node with value {@code prev} before {@code node}.
     * This means all edges that went into {@code node} now point to
     * {@code prev}. Outgoing edges from {@code node} remain the same. Lastly,
     * an edge from {@code prev} to {@code node} is created.
     * @param prev
     * @param node
     */
    public void postpendNode(CFGNode node, CFGNode... newNode) {
        if (newNode.length == 0) return;

        CFGNode firstInChain = newNode[0];
        this.insert(firstInChain);
        CFGNode lastInChain = firstInChain;
        for (int i = 1; i < newNode.length; i++) {
            final var nextGraphNode = newNode[i];
            this.insert(nextGraphNode);
            this.join(lastInChain, nextGraphNode);
            lastInChain = nextGraphNode;
        }

        final var lastNode = lastInChain;
        final var outgoingNodes = this.incomingNodes(node);
        outgoingNodes.forEach(end -> {
            final var value = this.edgeValue(node, end);
            this.unlink(node, end);
            if (value.isPresent()) {
                this.join(lastNode, end, value);
            } else {
                this.join(lastNode, end);
            }
        });
        this.join(firstInChain, node);
    }



    /**
     * Inserts a new node with value {@code prev} before {@code node}.
     * This means all edges that went into {@code node} now point to
     * {@code prev}. Outgoing edges from {@code node} remain the same. Lastly,
     * an edge from {@code prev} to {@code node} is created.
     * @param prev
     * @param node
     */
    public void innerInsert(CFGNode start, CFGNode end, CFGNode... newNode) {
        if (newNode.length == 0) return;

        CFGNode firstInChain = newNode[0];
        this.insert(firstInChain);
        CFGNode lastInChain = firstInChain;
        for (int i = 1; i < newNode.length; i++) {
            final var nextGraphNode = newNode[i];
            this.insert(nextGraphNode);
            this.join(lastInChain, nextGraphNode);
            lastInChain = nextGraphNode;
        }

        this.unlink(start, end);
        this.join(start, firstInChain);
        this.join(lastInChain, end);
    }

}
