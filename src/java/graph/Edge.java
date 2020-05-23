package graph;

import java.util.Objects;
import java.util.Optional;

/**
 * A structure indicating a uni-directional edge of a graph.
 * Each edge has the structure {@code start} --> {@code end}.
 * @author ayang
 *
 * @param <V> The type of value stored in the graph nodes.
 */
public class Edge<V, E> {

    /**
     * The starting node of the edge.
     */
    public final GraphNode<V> start;

    /**
     * The end node of the edge.
     */
    public final GraphNode<V> end;

    public final Optional<E> value;

    public Edge(GraphNode<V> start, GraphNode<V> end) {
        this.start = start;
        this.end = end;
        this.value = Optional.empty();
    }

    public Edge(GraphNode<V> start, GraphNode<V> end, E value) {
        this.start = start;
        this.end = end;
        this.value = Optional.of(value);
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
        Edge<?,?> other = (Edge<?,?>) obj;
        return Objects.equals(end, other.end)
                && Objects.equals(start, other.start)
                && Objects.deepEquals(value, other.value);
    }

    @Override
    public String toString() {
        return this.start + " -" +
                this.value.map(v -> v.toString()).orElse("") + "-> " + this.end;
    }


}