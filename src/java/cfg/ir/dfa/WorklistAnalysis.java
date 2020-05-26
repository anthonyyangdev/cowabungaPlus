package cfg.ir.dfa;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import cfg.ir.graph.CFGGraph;
import cfg.ir.graph.SCCGraph;
import cfg.ir.graph.SCCGraph.SCC;
import cfg.ir.nodes.CFGNode;

public final class WorklistAnalysis {

    public static <L> DfaResult<L> forward(CFGGraph graph,
            ForwardDataflowAnalysis<L> analysis) {

        final var sccGraph = SCCGraph.generate(graph, graph.startNode);

        final var worklist = new ArrayDeque<SCC>();
        worklist.add(sccGraph.startNode());

        Map<CFGNode, L> in = new HashMap<>();
        Map<CFGNode, Map<CFGNode, L>> out = new HashMap<>();

        for (CFGNode node: graph.nodes()) {
            HashMap<CFGNode, L> outEdges = new HashMap<>(1, 1);
            for (CFGNode outNode : graph.outgoingNodes(node)) {
                outEdges.put(outNode, analysis.topValue());
            }
            out.put(node, outEdges);
        }
        in.put(graph.startNode, analysis.topValue());

        while (!worklist.isEmpty()) {
            final var scc = worklist.remove();
            final var subWorklist = new ArrayDeque<>(scc.graph.nodes());
            while (!subWorklist.isEmpty()) {
                final var node = subWorklist.remove();
                L inValue = graph.incomingNodes(node)
                        .stream()
                        .map(n -> out.get(n).get(node))
                        .reduce(analysis::meet)
                        // the set of in-nodes to a node should never be empty
                        // unless it's the start node for a forward analysis or a
                        // return node for a backward analysis
                        .orElse(analysis.topValue());
                in.put(node, inValue);

                List<L> output = node.acceptForward(analysis.transfer(), inValue);

                final List<CFGNode> outNodes = graph.outgoingNodes(node);
                final int numOfOutNodes = outNodes.size();
                for (int i = 0; i < numOfOutNodes; i++) {
                    CFGNode outNode = outNodes.get(i);
                    L newOutValue = output.get(i);
                    L oldOutValue = out.get(node).get(outNode);

                    // If there is a change in out lattice.
                    if (!oldOutValue.equals(newOutValue)) {
                        out.get(node).put(outNode, newOutValue);
                        // If the out node is in the scc, add it back
                        // into the sub-worklist.
                        if (scc.graph.nodes().contains(outNode)) {
                            subWorklist.add(outNode);

                        // Otherwise, it is outside of the scc.
                        // Add the scc corresponding to this out node
                        // if the scc is not already in the queue.
                        } else if (!worklist.contains(sccGraph.sccOfNode(outNode))) {
                            final var nextScc = sccGraph.sccOfNode(outNode);
                            worklist.add(nextScc);
                        }
                    }
                }
            }
        }
        return new DfaResult<>(in, out);
    }

    public static <L> Map<CFGNode, L> backward(
            CFGGraph cfg,
            BackwardDataflowAnalysis<L> analysis) {

            // Ideally, worklist starts from a return node.
            Queue<CFGNode> worklist = new ArrayDeque<>(cfg.nodes());
            Map<CFGNode, L> in = new HashMap<>();
            Map<CFGNode, L> out = new HashMap<>();

            for (CFGNode node : cfg.nodes()) {
                out.put(node, analysis.topValue());
                in.put(node, analysis.topValue());
            }

            while (!worklist.isEmpty()) {
                CFGNode node = worklist.remove();

                L outValue = cfg.outgoingNodes(node)
                    .stream()
                    .map(in::get)
                    .reduce(analysis::meet)
                    // the set of in-nodes to a node should never be empty
                    // unless it's the start node for a forward analysis or a
                    // return node for a backward analysis
                    .orElse(analysis.topValue());
                out.put(node, outValue);

                L originalInValue = in.get(node);
                L inValue= node.acceptBackward(analysis.transfer(), outValue);
                if (!originalInValue.equals(inValue)) {
                    in.put(node, inValue);
                    worklist.addAll(cfg.incomingNodes(node));
                }
            }
            return out;
        }

    private WorklistAnalysis() { }

}
