package cyr7.graph;

import java.util.Set;

public interface Graph<V> {

    public Set<GraphNode<V>> nodes();

    public Set<Edge<V>> edges();

    public boolean insert(GraphNode<V> value);

    public GraphNode<V> remove(GraphNode<V> value);

    public Set<GraphNode<V>> outgoingNodes(GraphNode<V> node);

    public Set<GraphNode<V>> incomingNodes(GraphNode<V> node);

    public boolean join(GraphNode<V> start, GraphNode<V> end);

    public Edge<V> unlink(GraphNode<V> start, GraphNode<V> end);

    public boolean containsNode(GraphNode<V> node);

    public boolean containsEdge(Edge<V> edge);

    public boolean containsEdge(GraphNode<V> start, GraphNode<V> end);

}