package cfg.ir.dot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import cfg.ir.CFGGraph;
import cfg.ir.nodes.CFGNode;
import graph.Edge;
import graph.GraphNode;
import polyglot.util.Pair;

public class IrCFGDotUtil {

    private IrCFGDotUtil() {}

    public static DotData execute(CFGGraph cfg) {
        return new DotData(cfg.nodes(), cfg.edges());
    }

    public static class DotData {
        private List<String> nodes;
        private List<Pair<String, String>> edges;
        public DotData(Set<GraphNode<CFGNode>> nodes, Set<Edge<CFGNode, Boolean>> edges) {
            final Map<CFGNode, String> nodeToLabel = new HashMap<>();
            final AtomicInteger count = new AtomicInteger();
            this.nodes = nodes.stream().map(n -> {
                final String label = n.value().toString() + "[id=" + count.getAndIncrement() + "]";
                nodeToLabel.put(n.value(), label);
                return label;
            }).collect(Collectors.toList());

            this.edges = edges.stream().map(e ->
                new Pair<String, String>(nodeToLabel.get(e.start.value()),
                                         nodeToLabel.get(e.end.value()))
            ).collect(Collectors.toList());
        }

        public List<String> nodes() {
            return nodes;
        }

        public List<Pair<String, String>> edges() {
            return edges;
        }

    }
}