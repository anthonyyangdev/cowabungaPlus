package cfg.ir.graph;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.Graph;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.Graphs;
import com.google.common.graph.MutableGraph;

import polyglot.util.Pair;

public class GraphColoring<V> {


    public static void main(String[] args) {

        MutableGraph<Integer> graph = GraphBuilder.undirected().build();
        graph.addNode(1);
        graph.addNode(2);
        graph.addNode(3);
        graph.addNode(4);
        graph.addNode(5);

        graph.putEdge(1, 2);
        graph.putEdge(1, 3);
        graph.putEdge(1, 5);

        graph.putEdge(3, 5);
        graph.putEdge(3, 4);

        graph.putEdge(5, 2);
        graph.putEdge(5, 4);

        graph.putEdge(2, 4);

        final var colorer = new GraphColoring<>(graph);
        final var result = colorer.colorGraph(4);

        System.out.println(result.part1());
        System.out.println(result.part2());

    }

    private MutableGraph<V> graph;

    public GraphColoring(Graph<V> graph) {
        this.graph = Graphs.copyOf(graph);
    }

    /**
     * Returns a colored graph, with a set of spilled nodes.
     * @param numOfColors
     * @return
     */
    public Pair<Map<V, Integer>, Set<V>> colorGraph(final int numOfColors) {
        MutableGraph<V> graphCopy = Graphs.copyOf(this.graph);

        // Step 1: Simplification until no nodes.
        Deque<Pair<V, Set<EndpointPair<V>>>> removedNodes = new ArrayDeque<>();
        Set<V> spilledNodes = new HashSet<>();
        while (!graphCopy.nodes().isEmpty()) {
            removedNodes.addAll(this.simplify(graphCopy, numOfColors));

            // Spill a node;
            if (!graphCopy.nodes().isEmpty()) {
                spilledNodes.add(this.chooseSpilledNode(graphCopy));
            }
        }

        // Step 2: Simplify graph until it contains no nodes if possible.
        // Add back the removed nodes and assign it a color different from its
        // neighbors.
        final var allColors = IntStream.range(0, numOfColors)
                                       .boxed()
                                       .collect(Collectors.toSet());

        Map<V, Integer> mapping = new HashMap<>();
        while (!removedNodes.isEmpty()) {
            final var nodePair = removedNodes.pop();
            final var node = nodePair.part1();
            final var edges = nodePair.part2();

            graphCopy.addNode(node);
            for (EndpointPair<V> e: edges) {
                if (graphCopy.nodes().containsAll(Set.of(e.nodeU(), e.nodeV()))) {
                    graphCopy.putEdge(e);
                }
            }
            final var adjacent = graphCopy.adjacentNodes(node);
            final var neighbors = adjacent.stream().map(n->mapping.get(n))
                                          .collect(Collectors.toSet());
            mapping.put(node,
                        allColors.stream().filter(c -> !neighbors.contains(c))
                                          .collect(Collectors.toList()).get(0));
        }

        return new Pair<>(mapping, spilledNodes);
    }


    /**
     * Mutates {@code graph} by removing all nodes with less than
     * {@code numOfColors - 1} of edges. Returns a stack to add nodes back.
     * @param graph
     * @return
     */
    private Deque<Pair<V, Set<EndpointPair<V>>>>
        simplify(final MutableGraph<V> graph, final int numOfColors) {
        Deque<Pair<V, Set<EndpointPair<V>>>> map = new ArrayDeque<>();

        int numOfChanges;
        do {
            numOfChanges = 0;
            final var nodes = Set.copyOf(graph.nodes());
            for (V n: nodes) {
                if (graph.inDegree(n) < numOfColors) {
                    map.push(new Pair<>(n, Set.copyOf(graph.incidentEdges(n))));
                    graph.removeNode(n);
                    numOfChanges++;
                }
            }
        } while (numOfChanges != 0);
        return map;
    }


    private V chooseSpilledNode(final MutableGraph<V> graph) {
        V maximized = null;
        for (V n: graph.nodes()) {
            if (maximized == null || graph.inDegree(n) > graph.inDegree(maximized)) {
                maximized = n;
            }
        }
        graph.removeNode(maximized);
        return maximized;
    }

}
