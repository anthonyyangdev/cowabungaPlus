package cyr7.graph;

import java.util.Objects;

/**
 * A structure indicating a uni-directional edge of a graph.
 * Each edge has the structure {@code start} --> {@code end}.
 * @author ayang
 *
 * @param <V> The type of value stored in the graph nodes.
 */
public abstract class Edge<V> {

    /**
     * The starting node of the edge.
     */
    protected final GraphNode<V> start;

    /**
     * The end node of the edge.
     */
    protected final GraphNode<V> end;

    public Edge(GraphNode<V> start, GraphNode<V> end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public int hashCode() {
        return Objects.hash(end, start);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Edge)) {
            return false;
        }
        Edge<?> other = (Edge<?>) obj;
        return Objects.equals(end, other.end)
                && Objects.equals(start, other.start);
    }


}