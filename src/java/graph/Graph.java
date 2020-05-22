package graph;

import java.util.Set;

/**
 * An interface for constructing and operating on graph structures.
 * @author ayang
 * @param <V> The type of value stored in each graph node.
 */
public interface Graph<V> {

    /**
     * Returns a set of graph nodes inserted into this graph. Nodes are
     * inserted into this graph when {@link #insert(GraphNode) insert} is used
     * and are removed when {@link #remove(GraphNode) remove} is used.
     * <p>
     * Inserting/Removing graph nodes into/from this returned set of graph
     * nodes does not affect the structure of the graph.
     * <p>
     * Likewise, inserting/removing nodes into/from the graph after this
     * method is called does not affect the returned set of nodes.
     */
    public Set<GraphNode<V>> nodes();

    /**
     * Returns a set of edges used in this graph. All edges in this graph are
     * unidirectional, i.e. there is a start graph node and an end graph node
     * for each edge. Edges are formed when
     * {@link #join(GraphNode, GraphNode) join} is used and are removed
     * when {@link #unlink(GraphNode, GraphNode) unlink} is used. They may also
     * be removed when {@link #remove(GraphNode) remove} is called, where edges
     * that include the removed node are also removed from the graph.
     * <p>
     * The edges {a --> b} and {b --> a} where {@code b.equal(a)} is
     * {@code false} are distinguishable edges.
     * <p>
     * Inserting/Removing graph edges into/from this returned set of graph
     * nodes does not affect the structure of the graph.
     * <p>
     * Likewise, inserting/removing edges into/from the graph after this
     * method is called does not affect the returned set of edges.
     */
    public Set<Edge<V>> edges();

    /**
     * Inserts {@code node} into the graph.
     * @return {@code true} is {@code node} is successfully added to the graph
     * and becomes a new node. Returns {@code false} if {@code node} is already
     * a node in the graph, i.e. {@code containsNode(node) == true}.
     */
    public boolean insert(GraphNode<V> node);

    /**
     * Removes {@code node}, as well as any edges that includes {@code node} as
     * an end, from the graph.
     * @return {@code node} if {@code node} is a node in the graph, i.e.
     * {@code nodes().contains(node) == true}.
     * @throws NonexistantNodeException if {@code node} is not a node
     * in the graph, i.e. {@code containsNode(node) == false}.
     */
    public GraphNode<V> remove(GraphNode<V> node);

    /**
     * Returns the set of graph nodes {@code n} where {@code node} is a start
     * node in some edge {@code e} in the graph and {@code n} is the end node
     * of that edge {@code e}.
     * <p>
     * Inserting/Removing graph nodes into/from this returned set of graph
     * nodes does not affect the structure of the graph.
     * <p>
     * Likewise, inserting/removing nodes into/from the graph after this
     * method is called does not affect the returned set of nodes.
     * @param node
     * @throws NonexistantNodeException if {@code node} is not a node
     * in the graph, i.e. {@code containsNode(node) == false}.
     */
    public Set<GraphNode<V>> outgoingNodes(GraphNode<V> node);

    /**
     * Returns the set of graph nodes {@code n} where {@code node} is an end
     * node in some edge {@code e} in the graph and {@code n} is the start node
     * of that edge {@code e}.
     * <p>
     * Inserting/Removing graph nodes into/from this returned set of graph
     * nodes does not affect the structure of the graph.
     * <p>
     * Likewise, inserting/removing nodes into/from the graph after this
     * method is called does not affect the returned set of nodes.
     * @param node
     * @throws NonexistantNodeException if {@code node} is not a node
     * in the graph, i.e. {@code containsNode(node) == false}.
     */
    public Set<GraphNode<V>> incomingNodes(GraphNode<V> node);

    /**
     * Inserts an edge from {@code start} to {@code end} into the graph.
     * @param start
     * @param end
     * @return Returns {@code true} if the edge does not exist in the graph,
     * i.e. {@code containsEdge(new Edge(start, end)) == false}. Returns
     * {@code false} if the edge does exist in the graph, i.e.
     * {@code containsEdge(new Edge(start, end)) == true}.
     */
    public boolean join(GraphNode<V> start, GraphNode<V> end);

    /**
     * Removes the edge from {@code start} to {@code end} from the graph.
     * @param start
     * @param end
     * @return Returns the edge in the graph from {@code start} to {@code end}
     * if it exists.
     * @throws NonexistentEdgeException if the edge from {@code start} to
     * {@code end} does not exist in the graph, i.e.
     * {@code containsEdge(new Edge(start, end)) == false}.
     */
    public Edge<V> unlink(GraphNode<V> start, GraphNode<V> end);

    /**
     * Removes the edge from {@code start} to {@code end} from the graph.
     * @param edge
     * @return Returns the edge in the graph from {@code edge.start} to
     * {@code edge.end} if it exists.
     * @throws NonexistentEdgeException if the edge from {@code edge.start} to
     * {@code edge.end} does not exist in the graph, i.e.
     * {@code containsEdge(edge) == false}.
     */
    public Edge<V> unlink(Edge<V> edge);

    /**
     * {@code true} if {@code node} is a node in the graph.
     * Returns {@code false} if {@code node} is not a node in the graph.
     */
    public boolean containsNode(GraphNode<V> node);

    /**
     * Returns {@code true} if the edge exists in the graph.
     * Returns {@code false} if the edge does not exist in the graph.
     */
    public boolean containsEdge(GraphNode<V> start, GraphNode<V> end);

    /**
     * Returns {@code true} if the edge exists in the graph,
     * Returns {@code false} if the edge does not exist in the graph.
     */
    public boolean containsEdge(Edge<V> edge);


}