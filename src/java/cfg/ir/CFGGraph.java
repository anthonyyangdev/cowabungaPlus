package cfg.ir;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import cyr7.cfg.ir.nodes.CFGNode;
import graph.Edge;
import graph.Graph;
import graph.GraphNode;
import graph.NonexistentEdgeException;
import graph.NonexistentNodeException;

public class CFGGraph implements Graph<CFGNode> {

    private final Map<GraphNode<CFGNode>, Set<GraphNode<CFGNode>>> outgoingNodes;
    private final Map<GraphNode<CFGNode>, Set<GraphNode<CFGNode>>> incomingNodes;

    private final Map<GraphNode<CFGNode>, Set<Edge<CFGNode>>> neighborEdges;

    public CFGGraph() {
        this.outgoingNodes = new HashMap<>();
        this.incomingNodes = new HashMap<>();
        this.neighborEdges = new HashMap<>();
    }

    @Override
    public Set<GraphNode<CFGNode>> nodes() {
        return new HashSet<>(this.outgoingNodes.keySet());
    }

    @Override
    public Set<Edge<CFGNode>> edges() {
        Set<Edge<CFGNode>> edges = new HashSet<>();
        this.neighborEdges.keySet().forEach(n ->
                                edges.addAll(this.neighborEdges.get(n)));
        return edges;
    }

    @Override
    public boolean insert(GraphNode<CFGNode> node) {
        if (this.outgoingNodes.containsKey(node)) {
            return false;
        }
        this.incomingNodes.put(node, new HashSet<>());
        this.outgoingNodes.put(node, new HashSet<>());
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

        final var edge = new Edge<>(start, end);
        this.neighborEdges.get(start).add(edge);
        this.neighborEdges.get(end).add(edge);

        return true;
    }

    @Override
    public Edge<CFGNode> unlink(GraphNode<CFGNode> start,
            GraphNode<CFGNode> end) throws NonexistentEdgeException {
        final var removedEdge = new Edge<CFGNode>(start, end);
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
    public Edge<CFGNode> unlink(Edge<CFGNode> edge)
            throws NonexistentEdgeException {
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
    public boolean containsEdge(Edge<CFGNode> edge) {
        return this.outgoingNodes.get(edge.start).contains(edge.end);
    }

}
