package cyr7.graph;

import java.util.Objects;

/**
 * A node in a graph structure.
 * @author ayang
 *
 * @param <V> The type of value stored by this node.
 */
public abstract class GraphNode<V> {

    private final V value;

    public GraphNode(V value) {
        this.value = value;
    }

    /**
     * Returns the value stored in this node.
     */
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