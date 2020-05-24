package cfg.ir;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import cfg.ir.nodes.CFGNode;
import cfg.ir.nodes.CFGStartNode;
import graph.Edge;
import graph.GenericGraph;
import graph.GraphNode;
import graph.NonexistentEdgeException;
import graph.NonexistentNodeException;
import java_cup.runtime.ComplexSymbolFactory.Location;

/**
 * Graph structure for control-flow graphs. Edges optionally have a value for
 * if nodes to indicate which branch is the true branch and which is the false
 * branch.
 * @author ayang
 *
 */
public class CFGGraph extends GenericGraph<CFGNode, Boolean> {

    private final GraphNode<CFGNode> startNode;

    public CFGGraph(Location n) {
        super();
        this.startNode = new GraphNode<>(new CFGStartNode(n));
        this.insert(startNode);
    }

    public GraphNode<CFGNode> startNode() {
        return startNode;
    }

    /**
     * Removes all nodes from the graph that are unreachable from the start
     * node.
     */
    public void clean() {
        Set<GraphNode<CFGNode>> reachable = new HashSet<>();
        Queue<GraphNode<CFGNode>> worklist = new ArrayDeque<>();
        worklist.add(startNode);
        while (!worklist.isEmpty()) {
            final var node = worklist.remove();
            reachable.add(node);
            for (Edge<CFGNode, Boolean> e: this.outgoingEdges.get(node)) {
                final var out = e.end;
                if (!reachable.contains(out)) {
                    worklist.add(out);
                }
            }
        }
        final var unreachable = nodes();
        unreachable.removeAll(reachable);
        for (GraphNode<CFGNode> node: unreachable) {
            this.remove(node);
        }
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
    public boolean join(Edge<CFGNode, Boolean> edge)
            throws NonexistentNodeException {
        final var start = edge.start;
        final var end = edge.end;
        if (!this.outgoingEdges.containsKey(start)
                || !this.incomingEdges.containsKey(end)) {
            throw new NonexistentNodeException(start);
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
    public Edge<CFGNode, Boolean> unlink(GraphNode<CFGNode> start,
            GraphNode<CFGNode> end) throws NonexistentEdgeException {
        final var removedEdge = new Edge<CFGNode, Boolean>(start, end);
        final var removedEdgeTrue = new Edge<CFGNode, Boolean>(start, end, true);
        final var removedEdgeFalse = new Edge<CFGNode, Boolean>(start, end, false);
        if (!this.outgoingEdges.containsKey(start)
                || !this.incomingEdges.containsKey(end)) {
            throw new NonexistentEdgeException(removedEdge);
        }

        final var removedEdgeSet = new HashSet<Edge<CFGNode, Boolean>>();
        removedEdgeSet.add(removedEdge);
        removedEdgeSet.add(removedEdgeTrue);
        removedEdgeSet.add(removedEdgeFalse);
        this.outgoingEdges.get(start).removeAll(removedEdgeSet);
        this.incomingEdges.get(end).removeAll(removedEdgeSet);

        return removedEdge;
    }

    /**
     * Inserts a new node with value {@code prev} before {@code node}.
     * This means all edges that went into {@code node} now point to
     * {@code prev}. Outgoing edges from {@code node} remain the same. Lastly,
     * an edge from {@code prev} to {@code node} is created.
     * @param prev
     * @param node
     */
    public void prependNode(GraphNode<CFGNode> node, CFGNode... newNode) {
        if (newNode.length == 0) return;

        GraphNode<CFGNode> firstInChain = new GraphNode<>(newNode[0]);
        this.insert(firstInChain);
        GraphNode<CFGNode> lastInChain = firstInChain;
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
    public void postpendNode(GraphNode<CFGNode> node, CFGNode... newNode) {
        if (newNode.length == 0) return;

        GraphNode<CFGNode> firstInChain = new GraphNode<>(newNode[0]);
        this.insert(firstInChain);
        GraphNode<CFGNode> lastInChain = firstInChain;
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
    public void innerInsert(GraphNode<CFGNode> start, GraphNode<CFGNode> end, CFGNode... newNode) {
        if (newNode.length == 0) return;

        GraphNode<CFGNode> firstInChain = new GraphNode<>(newNode[0]);
        this.insert(firstInChain);
        GraphNode<CFGNode> lastInChain = firstInChain;
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
    public void replaceNode(GraphNode<CFGNode> prev, GraphNode<CFGNode> now) {
        final var incomingEdgesToPrev = this.incomingEdges.get(prev);
        final var outgoingEdgesFromPrev = this.outgoingEdges.get(prev);

        final var incomingToNow = incomingEdgesToPrev.stream().map(e -> {
            final var start = e.start.equals(prev) ? now : e.start;
            if (e.value.isPresent()) {
                return new Edge<CFGNode, Boolean>(start, now, e.value.get());
            } else {
                return new Edge<CFGNode, Boolean>(start, now);
            }
        }).collect(Collectors.toList());

        final var outgoingFromNow = outgoingEdgesFromPrev.stream().map(e -> {
            final var end = e.end.equals(prev) ? now : e.end;
            if (e.value.isPresent()) {
                return new Edge<CFGNode, Boolean>(now, end, e.value.get());
            } else {
                return new Edge<CFGNode, Boolean>(now, end);
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
