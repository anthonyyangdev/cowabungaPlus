package cfg.ir.graph;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;

import com.google.common.graph.Graph;
import com.google.common.graph.Graphs;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;

import cfg.ir.nodes.CFGNode;
import polyglot.util.Pair;

public class SCCGraph {

    public static class SCC {
        public final Graph<CFGNode> graph;

        public SCC(Graph<CFGNode> graph) {
            this.graph = graph;
        }
    }

    private final MutableValueGraph<SCC, Optional<Boolean>> graph;
    private final Map<CFGNode, SCC> nodeToScc;
    private SCC startNode;

    private SCCGraph() {
        this.graph = ValueGraphBuilder.directed().allowsSelfLoops(true).build();
        this.nodeToScc = new HashMap<>();
    }

    public SCC sccOfNode(CFGNode node) {
        return nodeToScc.get(node);
    }

    public void setStart(SCC start) {
        this.startNode = start;
    }

    public SCC startNode() {
        return startNode;
    }

    private static SCC scc(Graph<CFGNode> graph) {
        return new SCC(graph);
    }


    /**
     * Generates a graph with strongly-connected components, using a
     * a graph {@code graph} and a designated {@code start} node.
     * @param graph
     * @param start
     */
    public static SCCGraph generate(CFGGraph graph, CFGNode start) {
        SCCGraph sccGraph = new SCCGraph();

        Set<CFGNode> visited = new HashSet<>();
        Queue<CFGNode> postOrderTraversal = new ArrayDeque<>();
        Deque<CFGNode> nextNodeToVisit = new ArrayDeque<>();
        nextNodeToVisit.add(start);

        // Get postOrderTraversal via DFS
        while (!nextNodeToVisit.isEmpty()) {
            final var nextNode = nextNodeToVisit.pop();
            postOrderTraversal.add(nextNode);
            visited.add(nextNode);
            final var outgoing = graph.outgoingNodes(nextNode);
            for (CFGNode out: outgoing) {
                if (!visited.contains(out)) {
                    nextNodeToVisit.push(out);
                }
            }
        }

        // Generate a SCC graph by reverse-traversal.
        Set<CFGNode> component = new HashSet<>();
        Map<CFGNode, SCC> nodeToSccNode = new HashMap<>();
        while (!postOrderTraversal.isEmpty()) {
            final var nextNode = postOrderTraversal.remove();
            component.add(nextNode);
            if (postOrderTraversal.isEmpty()
                    || !graph.outgoingNodes(postOrderTraversal.peek()).contains(nextNode)) {
                // Create an SCC here.
                final var sccComponent =
                        scc(Graphs.inducedSubgraph(graph.graph(), component));
                component.forEach(n -> nodeToSccNode.put(n, sccComponent));
                sccGraph.graph.addNode(sccComponent);


                component.stream().map(n->new Pair<>(graph.incomingNodes(n), n))
                      .flatMap(inPair -> {
                          final var incoming = inPair.part1();
                          final var target = inPair.part2();
                          return incoming.stream()
                                  .map(i -> new Pair<>(nodeToSccNode.get(i),
                                                graph.edgeValue(i, target)))
                                  .filter(o -> Objects.nonNull(o.part1()))
                                  .distinct();
                      })
                      .forEach(inScc -> {
                          final var scc = inScc.part1();
                          final var value = inScc.part2();
                          sccGraph.graph.putEdgeValue(scc, sccComponent, value);
                      });

                component.stream().map(n->new Pair<>(graph.outgoingNodes(n), n))
                    .flatMap(outPair -> {
                        final var outgoing = outPair.part1();
                        final var begin = outPair.part2();
                        return outgoing.stream()
                                .map(out -> new Pair<>(nodeToSccNode.get(out),
                                        graph.edgeValue(begin, out)))
                                .filter(o -> Objects.nonNull(o.part1()))
                                .distinct();
                    })
                    .forEach(outScc -> {
                        final var scc = outScc.part1();
                        final var value = outScc.part2();
                        sccGraph.graph.putEdgeValue(sccComponent, scc, value);
                    });


                if (component.contains(start)) {
                    sccGraph.setStart(sccComponent);
                }
                component.clear();
            }
        }

        sccGraph.nodeToScc.putAll(nodeToSccNode);
        return sccGraph;
    }


}
