package graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class GenericGraph<V, E> implements Graph<V, E> {

    protected final Map<GraphNode<V>, LinkedList<Edge<V, E>>> incomingEdges;
    protected final Map<GraphNode<V>, LinkedList<Edge<V, E>>> outgoingEdges;

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
        return this.join(new Edge<V, E>(start, end));
    }

    @Override
    public boolean join(GraphNode<V> start, GraphNode<V> end, E value)
            throws NonexistentNodeException {
        return this.join(new Edge<V, E>(start, end, value));
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

        this.outgoingEdges.get(start).add(edge);
        this.incomingEdges.get(end).add(edge);

        return true;
    }


    @Override
    public Edge<V, E> unlink(GraphNode<V> start,
            GraphNode<V> end) throws NonexistentEdgeException {
        final var removedEdge = new Edge<V, E>(start, end);
        if (!this.outgoingEdges.containsKey(start)) {
            throw new NonexistentEdgeException(removedEdge);
        } else if (!this.incomingEdges.containsKey(end)) {
            throw new NonexistentEdgeException(removedEdge);
        }

        final var removedEdgeSet = new HashSet<Edge<V, E>>();
        removedEdgeSet.add(removedEdge);

        this.outgoingEdges.put(start,
                new LinkedList<>(this.outgoingEdges.get(start)
                        .stream().filter(e -> {
                            return !e.start.equals(start) && !e.end.equals(end);
                        }).collect(Collectors.toList())));
        this.incomingEdges.put(end,
                new LinkedList<>(this.outgoingEdges.get(end)
                        .stream().filter(e -> {
                            return !e.start.equals(start) && !e.end.equals(end);
                        }).collect(Collectors.toList())));

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
            return this.outgoingEdges.get(start).stream().anyMatch(e -> {
                return e.end.equals(end);
            });
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

    @Override
    public int inDegree(GraphNode<V> node) {
        if (this.incomingEdges.containsKey(node)) {
            return this.incomingEdges.get(node).size();
        } else {
            throw new NonexistentNodeException(node);
        }
    }


    /**
     * Returns a new subgraph of {@code this} graph that contains only the
     * nodes in {@code subNodes}. Any edges from this graph are carried to the
     * new graph.
     * <p>
     * Changes to edge relations in {@code this} graph are not
     * reflected in the new graph, but changes to the nodes will appear in
     * the new graph.
     * @param subNodes
     * @throws NonexistentNodeException if a node in {@code subNodes} is not
     *                                  contained in {@code this} graph.
     */
    public GenericGraph<V, E> subGraph(Set<GraphNode<V>> subNodes) {
        GenericGraph<V, E> subGraph = new GenericGraph<>();
        subNodes.forEach(subGraph::insert);
        subNodes.forEach(node -> {
            if (!this.containsNode(node))
                throw new NonexistentNodeException(node);
            this.incomingEdges.get(node).stream().filter(e ->
                subNodes.contains(e.start) && subNodes.contains(e.end)
            ).forEach(subGraph::join);
        });
        return subGraph;
    }


    /**
     * Inserts a new node with value {@code prev} before {@code node}.
     * This means all edges that went into {@code node} now point to
     * {@code prev}. Outgoing edges from {@code node} remain the same. Lastly,
     * an edge from {@code prev} to {@code node} is created.
     * @param prev
     * @param node
     */
    @SuppressWarnings("unchecked")
    public void prependNode(GraphNode<V> node, V... newNode) {
        if (newNode.length == 0) return;

        GraphNode<V> firstInChain = new GraphNode<>(newNode[0]);
        this.insert(firstInChain);
        GraphNode<V> lastInChain = firstInChain;
        for (int i = 1; i < newNode.length; i++) {
            final var nextGraphNode = new GraphNode<>(newNode[i]);
            this.insert(nextGraphNode);
            this.join(lastInChain, nextGraphNode);
            lastInChain = nextGraphNode;
        }

        final var incomingNodes = Set.copyOf(this.incomingEdges.get(node));
        incomingNodes.forEach(e -> {
            this.unlink(e.start, node);
            if (e.value.isPresent()) {
                this.join(e.start, firstInChain, e.value.get());
            } else {
                this.join(e.start, firstInChain);
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
    @SuppressWarnings("unchecked")
    public void postpendNode(GraphNode<V> node, V... newNode) {
        if (newNode.length == 0) return;

        GraphNode<V> firstInChain = new GraphNode<V>(newNode[0]);
        this.insert(firstInChain);
        GraphNode<V> lastInChain = firstInChain;
        for (int i = 1; i < newNode.length; i++) {
            final var nextGraphNode = new GraphNode<>(newNode[i]);
            this.insert(nextGraphNode);
            this.join(lastInChain, nextGraphNode);
            lastInChain = nextGraphNode;
        }

        final var lastNode = lastInChain;
        final var outgoingNodes = Set.copyOf(this.incomingEdges.get(node));
        outgoingNodes.forEach(e -> {
            this.unlink(node, e.end);
            if (e.value.isPresent()) {
                this.join(lastNode, e.end, e.value.get());
            } else {
                this.join(lastNode, e.end);
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
    @SuppressWarnings("unchecked")
    public void innerInsert(GraphNode<V> start, GraphNode<V> end, V... newNode) {
        if (newNode.length == 0) return;

        GraphNode<V> firstInChain = new GraphNode<V>(newNode[0]);
        this.insert(firstInChain);
        GraphNode<V> lastInChain = firstInChain;
        for (int i = 1; i < newNode.length; i++) {
            final var nextGraphNode = new GraphNode<>(newNode[i]);
            this.insert(nextGraphNode);
            this.join(lastInChain, nextGraphNode);
            lastInChain = nextGraphNode;
        }

        this.unlink(start, end);
        this.join(start, firstInChain);
        this.join(lastInChain, end);
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
    public void replaceNode(GraphNode<V> prev, GraphNode<V> now) {
        final var incomingEdgesToPrev = this.incomingEdges.get(prev);
        final var outgoingEdgesFromPrev = this.outgoingEdges.get(prev);

        final var incomingToNow = incomingEdgesToPrev.stream().map(e -> {
            final var start = e.start.equals(prev) ? now : e.start;
            if (e.value.isPresent()) {
                return new Edge<V, E>(start, now, e.value.get());
            } else {
                return new Edge<V, E>(start, now);
            }
        }).collect(Collectors.toList());

        final var outgoingFromNow = outgoingEdgesFromPrev.stream().map(e -> {
            final var end = e.end.equals(prev) ? now : e.end;
            if (e.value.isPresent()) {
                return new Edge<V, E>(now, end, e.value.get());
            } else {
                return new Edge<V, E>(now, end);
            }
        }).collect(Collectors.toList());

        this.remove(prev);

        if (!this.containsNode(now)) {
            this.insert(now);
        }
        incomingToNow.forEach(this::join);
        outgoingFromNow.forEach(this::join);
    }

}
