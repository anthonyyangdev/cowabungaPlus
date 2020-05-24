//package cfg.ir.graph;
//
//import java.util.ArrayDeque;
//import java.util.Deque;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Map;
//import java.util.Objects;
//import java.util.Queue;
//import java.util.Set;
//
//import graph.GenericGraph;
//import graph.GraphNode;
//
//public class SCCGraph<V, E>
//    extends GenericGraph<GenericGraph<V, E>, E> {
//
//    private GraphNode<GenericGraph<V, E>> startNode;
//
//    private SCCGraph() {}
//
//    public void setStart(GraphNode<GenericGraph<V, E>> start) {
//        this.startNode = start;
//    }
//
//    public GraphNode<GenericGraph<V, E>> startNode() {
//        return startNode;
//    }
//
//    private static <V, E> GraphNode<GenericGraph<V, E>>
//        scc(GenericGraph<V, E> graph) {
//        return new GraphNode<>(graph);
//    }
//
//
//    /**
//     * Generates a graph with strongly-connected components, using a
//     * generic {@code graph} and a designated {@code start} node.
//     * @param <V>
//     * @param <E>
//     * @param graph
//     * @param start
//     */
//    private static <V, E> SCCGraph<V, E>
//        generateSccGraph(GenericGraph<V, E> graph, GraphNode<V> start) {
//        SCCGraph<V, E> sccGraph = new SCCGraph<>();
//
//        Set<GraphNode<V>> visited = new HashSet<>();
//        Queue<GraphNode<V>> postOrderTraversal = new ArrayDeque<>();
//        Deque<GraphNode<V>> nextNodeToVisit = new ArrayDeque<>();
//        nextNodeToVisit.add(start);
//
//        // Get postOrderTraversal via DFS
//        while (!nextNodeToVisit.isEmpty()) {
//            final var nextNode = nextNodeToVisit.pop();
//            postOrderTraversal.add(nextNode);
//            visited.add(nextNode);
//            final var outgoing = graph.outgoingNodes(nextNode);
//            for (GraphNode<V> out: outgoing) {
//                if (!visited.contains(out)) {
//                    nextNodeToVisit.push(out);
//                }
//            }
//        }
//
//        // Generate a SCC graph by reverse-traversal.
//        Set<GraphNode<V>> component = new HashSet<>();
//        Map<GraphNode<V>, GraphNode<GenericGraph<V, E>>>
//            nodeToSccNode = new HashMap<>();
//        while (!postOrderTraversal.isEmpty()) {
//            final var nextNode = postOrderTraversal.remove();
//            component.add(nextNode);
//            if (postOrderTraversal.isEmpty()
//                    || !graph.outgoingNodes(postOrderTraversal.peek()).contains(nextNode)) {
//                // Create an SCC here.
//                final var sccComponent = scc(graph.subGraph(component));
//                component.forEach(n -> nodeToSccNode.put(n, sccComponent));
//                sccGraph.insert(sccComponent);
//
//                component.stream().map(graph::incomingNodes)
//                    .flatMap(in -> in.stream()
//                                 .map(nodeToSccNode::get)
//                                 .filter(Objects::nonNull)
//                                 .distinct())
//                    .forEach(inScc -> sccGraph.join(inScc, sccComponent));
//
//                component.stream().map(graph::outgoingNodes)
//                    .flatMap(out -> out.stream()
//                             .map(nodeToSccNode::get)
//                             .filter(Objects::nonNull)
//                             .distinct())
//                    .forEach(outScc -> sccGraph.join(sccComponent, outScc));
//
//                if (component.contains(start)) {
//                    sccGraph.setStart(sccComponent);
//                }
//                component.clear();
//            }
//        }
//        return sccGraph;
//    }
//
//
//}
