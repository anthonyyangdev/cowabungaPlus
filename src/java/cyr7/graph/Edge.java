package cyr7.graph;

import java.util.Objects;

public abstract class Edge<V> {

    protected final GraphNode<V> start;
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