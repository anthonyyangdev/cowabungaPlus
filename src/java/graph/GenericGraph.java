package graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class GenericGraph<V, E> implements Graph<V, E> {

    private final Map<GraphNode<V>, LinkedList<Edge<V, E>>> incomingEdges;
    private final Map<GraphNode<V>, LinkedList<Edge<V, E>>> outgoingEdges;

    public GenericGraph() {
        this.incomingEdges = new HashMap<>();
        this.outgoingEdges = new HashMap<>();
    }

    @Override
    public Set<GraphNode<V>> nodes() {
        return new HashSet<>(this.incomingEdges.keySet());
    }

    @Override
    public Set<Edge<V, E>> edges() {
        Set<Edge<V, E>> edges = new HashSet<>();
        this.incomingEdges.values().forEach(n -> edges.addAll(n));
        this.outgoingEdges.values().forEach(n -> edges.addAll(n));
        return edges;
    }

    @Override
    public boolean insert(GraphNode<V> node) {
        if (this.outgoingEdges.containsKey(node)) {
            return false;
        }
        this.incomingEdges.put(node, new LinkedList<>());
        this.outgoingEdges.put(node, new LinkedList<>());
        return true;
    }

    @Override
    public GraphNode<V> remove(GraphNode<V> node)
            throws NonexistentNodeException {
        if (!this.outgoingEdges.containsKey(node)) {
            throw new NonexistentNodeException(node);
        }
        if (this.containsEdge(node, node)) {
            this.unlink(node, node);
        }
        final var incoming = Set.copyOf(this.incomingEdges.get(node));
        incoming.forEach(in -> this.unlink(in));

        final var outgoing = Set.copyOf(this.outgoingEdges.get(node));
        outgoing.forEach(out -> this.unlink(out));

        this.incomingEdges.remove(node);
        this.outgoingEdges.remove(node);

        return node;
    }

    /**
     * For If node, a list is returned are returned such that the true branch
     * edges prioritizes the false branch.
     * <p>
     * For example, suppose the if node has two out nodes, then the first
     * element of the list if the true branch, and second element of the list
     * is the false branch.
     */
    @Override
    public List<GraphNode<V>> outgoingNodes(GraphNode<V> node)
            throws NonexistentNodeException {
        if (!this.outgoingEdges.containsKey(node)) {
            throw new NonexistentNodeException(node);
        }
        return this.outgoingEdges.get(node).stream()
                                           .map(e -> e.end)
                                           .collect(Collectors.toList());
    }

    @Override
    public List<GraphNode<V>> incomingNodes(GraphNode<V> node)
            throws NonexistentNodeException {
        if (!this.incomingEdges.containsKey(node)) {
            throw new NonexistentNodeException(node);
        }
        return this.incomingEdges.get(node).stream().map(e -> e.start)
                .collect(Collectors.toList());
    }

    @Override
    public boolean join(GraphNode<V> start, GraphNode<V> end)
            throws NonexistentNodeException {
        if (!this.outgoingEdges.containsKey(start)) {
            throw new NonexistentNodeException(start);
        } else if (!this.incomingEdges.containsKey(end)) {
            throw new NonexistentNodeException(end);
        }
        final var edge = new Edge<V, E>(start, end);
        this.outgoingEdges.get(start).add(edge);
        this.incomingEdges.get(end).add(edge);

        return true;
    }

    @Override
    public boolean join(Edge<V, E> edge)
            throws NonexistentNodeException {
        final var start = edge.start;
        final var end = edge.end;
        if (!this.outgoingEdges.containsKey(start)) {
            throw new NonexistentNodeException(start);
        } else if (!this.incomingEdges.containsKey(end)) {
            throw new NonexistentNodeException(end);
        }

        if (edge.value.isPresent()) {
            if (edge.value.get()) {
                this.outgoingEdges.get(start).addFirst(edge);
            } else {
                this.outgoingEdges.get(start).addLast(edge);
            }
        } else {
            this.outgoingEdges.get(start).add(edge);
        }
        this.incomingEdges.get(end).add(edge);

        return true;
    }


    @Override
    public Edge<V, E> unlink(GraphNode<V> start,
            GraphNode<V> end) throws NonexistentEdgeException {
        final var removedEdge = new Edge<V, E>(start, end);
        final var removedEdgeTrue = new Edge<V, E>(start, end, true);
        final var removedEdgeFalse = new Edge<V, E>(start, end, false);
        if (!this.outgoingEdges.containsKey(start)) {
            throw new NonexistentEdgeException(removedEdge);
        } else if (!this.incomingEdges.containsKey(end)) {
            throw new NonexistentEdgeException(removedEdge);
        }

        final var removedEdgeSet = new HashSet<Edge<V, E>>();
        removedEdgeSet.add(removedEdge);
        removedEdgeSet.add(removedEdgeTrue);
        removedEdgeSet.add(removedEdgeFalse);
        this.outgoingEdges.get(start).removeAll(removedEdgeSet);
        this.incomingEdges.get(end).removeAll(removedEdgeSet);

        return removedEdge;
    }

    @Override
    public Edge<V, E> unlink(Edge<V, E> edge)
            throws NonexistentEdgeException {
        if (!this.containsEdge(edge)) {
            throw new NonexistentEdgeException(edge);
        }
        final var start = edge.start;
        final var end = edge.end;
        if (!this.outgoingEdges.containsKey(start)) {
            throw new NonexistentEdgeException(edge);
        } else if (!this.incomingEdges.containsKey(end)) {
            throw new NonexistentEdgeException(edge);
        }
        this.outgoingEdges.get(start).remove(edge);
        this.incomingEdges.get(end).remove(edge);

        return edge;
    }

    @Override
    public boolean containsNode(GraphNode<V> node) {
        return this.outgoingEdges.containsKey(node);
    }

    @Override
    public boolean containsEdge(GraphNode<V> start,
            GraphNode<V> end) {
        if (this.outgoingEdges.containsKey(start)) {
            return this.outgoingEdges.get(start).contains(new Edge<>(start, end));
        }
        return false;
    }

    @Override
    public boolean containsEdge(Edge<V, E> edge) {
        if (this.outgoingEdges.containsKey(edge.start)) {
            return this.outgoingEdges.get(edge.start).contains(edge);
        }
        return false;
    }


}
