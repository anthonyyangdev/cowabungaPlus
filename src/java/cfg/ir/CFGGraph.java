package cfg.ir;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import cfg.ir.nodes.CFGNode;
import cfg.ir.nodes.CFGStartNode;
import graph.Edge;
import graph.Graph;
import graph.GraphNode;
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
public class CFGGraph implements Graph<CFGNode, Boolean> {

    private final Map<GraphNode<CFGNode>, List<GraphNode<CFGNode>>> outgoingNodes;
    private final Map<GraphNode<CFGNode>, List<GraphNode<CFGNode>>> incomingNodes;

    private final Map<GraphNode<CFGNode>, Set<Edge<CFGNode, Boolean>>> neighborEdges;

    private final GraphNode<CFGNode> startNode;

    public CFGGraph(Location n) {
        this.outgoingNodes = new HashMap<>();
        this.incomingNodes = new HashMap<>();
        this.neighborEdges = new HashMap<>();
        this.startNode = new GraphNode<>(new CFGStartNode(n));
    }

    public GraphNode<CFGNode> startNode() {
        return startNode;
    }

    @Override
    public Set<GraphNode<CFGNode>> nodes() {
        return new HashSet<>(this.outgoingNodes.keySet());
    }

    @Override
    public Set<Edge<CFGNode, Boolean>> edges() {
        Set<Edge<CFGNode, Boolean>> edges = new HashSet<>();
        this.neighborEdges.keySet().forEach(n ->
                                edges.addAll(this.neighborEdges.get(n)));
        return edges;
    }


    /**
     * Removes all nodes from the graph that are unreachable from the start
     * node.
     */
    public void clean() {
        Set<GraphNode<CFGNode>> reachable = new HashSet<>();
        Queue<GraphNode<CFGNode>> worklist = new ArrayDeque<>();
        worklist.add(startNode);
        while (!worklist.isEmpty()) {
            final var node = worklist.remove();
            reachable.add(node);
            for (GraphNode<CFGNode> out: this.outgoingNodes.get(node)) {
                if (!reachable.contains(out)) {
                    worklist.add(out);
                }
            }
        }
        final var unreachable = nodes();
        unreachable.removeAll(reachable);
        for (GraphNode<CFGNode> node: unreachable) {
            this.remove(node);
        }
    }

    @Override
    public boolean insert(GraphNode<CFGNode> node) {
        if (this.outgoingNodes.containsKey(node)) {
            return false;
        }
        this.incomingNodes.put(node, new ArrayList<>());
        this.outgoingNodes.put(node, new ArrayList<>());
        this.neighborEdges.put(node, new HashSet<>());
        return true;
    }

    @Override
    public GraphNode<CFGNode> remove(GraphNode<CFGNode> node)
            throws NonexistentNodeException {
        if (!this.outgoingNodes.containsKey(node)) {
            throw new NonexistentNodeException(node);
        }
        if (this.outgoingNodes.get(node).contains(node)) {
            this.incomingNodes.get(node).remove(node);
            this.outgoingNodes.get(node).remove(node);
        }
        final var incoming = this.incomingNodes.get(node);
        incoming.forEach(in -> this.outgoingNodes.get(in).remove(node));

        final var outgoing = this.outgoingNodes.get(node);
        outgoing.forEach(out -> this.incomingNodes.get(out).remove(node));

        this.incomingNodes.remove(node);
        this.outgoingNodes.remove(node);
        this.neighborEdges.remove(node);

        return node;
    }

    @Override
    public Set<GraphNode<CFGNode>> outgoingNodes(GraphNode<CFGNode> node)
            throws NonexistentNodeException {
        if (!this.outgoingNodes.containsKey(node)) {
            throw new NonexistentNodeException(node);
        }
        return new HashSet<>(this.outgoingNodes.get(node));
    }

    @Override
    public Set<GraphNode<CFGNode>> incomingNodes(GraphNode<CFGNode> node)
            throws NonexistentNodeException {
        if (!this.incomingNodes.containsKey(node)) {
            throw new NonexistentNodeException(node);
        }
        return new HashSet<>(this.incomingNodes.get(node));
    }

    @Override
    public boolean join(GraphNode<CFGNode> start, GraphNode<CFGNode> end)
            throws NonexistentNodeException {
        if (!this.outgoingNodes.containsKey(start)) {
            throw new NonexistentNodeException(start);
        } else if (!this.incomingNodes.containsKey(end)) {
            throw new NonexistentNodeException(end);
        }
        this.outgoingNodes.get(start).add(end);
        this.incomingNodes.get(end).add(start);

        final var edge = new Edge<CFGNode, Boolean>(start, end);
        this.neighborEdges.get(start).add(edge);
        this.neighborEdges.get(end).add(edge);

        return true;
    }


    @Override
    public boolean join(Edge<CFGNode, Boolean> edge)
            throws NonexistentNodeException {
        final var start = edge.start;
        final var end = edge.end;
        if (!this.outgoingNodes.containsKey(start)) {
            throw new NonexistentNodeException(start);
        } else if (!this.incomingNodes.containsKey(end)) {
            throw new NonexistentNodeException(end);
        }
        this.outgoingNodes.get(start).add(end);
        this.incomingNodes.get(end).add(start);

        this.neighborEdges.get(start).add(edge);
        this.neighborEdges.get(end).add(edge);

        return true;
    }


    @Override
    public Edge<CFGNode, Boolean> unlink(GraphNode<CFGNode> start,
            GraphNode<CFGNode> end) throws NonexistentEdgeException {
        final var removedEdge = new Edge<CFGNode, Boolean>(start, end);
        if (!this.outgoingNodes.containsKey(start)) {
            throw new NonexistentEdgeException(removedEdge);
        } else if (!this.incomingNodes.containsKey(end)) {
            throw new NonexistentEdgeException(removedEdge);
        }

        this.outgoingNodes.get(start).remove(end);
        this.incomingNodes.get(end).remove(start);
        this.neighborEdges.get(start).remove(removedEdge);
        this.neighborEdges.get(end).remove(removedEdge);

        return removedEdge;
    }

    @Override
    public Edge<CFGNode, Boolean> unlink(Edge<CFGNode, Boolean> edge)
            throws NonexistentEdgeException {
        if (!this.containsEdge(edge)) {
            throw new NonexistentEdgeException(edge);
        }
        return this.unlink(edge.start, edge.end);
    }

    @Override
    public boolean containsNode(GraphNode<CFGNode> node) {
        return this.outgoingNodes.containsKey(node);
    }

    @Override
    public boolean containsEdge(GraphNode<CFGNode> start,
            GraphNode<CFGNode> end) {
        return this.outgoingNodes.get(start).contains(end);
    }

    @Override
    public boolean containsEdge(Edge<CFGNode, Boolean> edge) {
        return this.neighborEdges.get(edge.start).contains(edge);
    }


}
