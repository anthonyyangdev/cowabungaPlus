package cyr7.graph;

import java.util.Objects;

public abstract class GraphNode<V> {

    private final V value;

    public GraphNode(V value) {
        this.value = value;
    }

    public final V value() {
        return this.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof GraphNode)) {
            return false;
        }
        GraphNode<?> other = (GraphNode<?>) obj;
        return Objects.equals(value, other.value);
    }

}