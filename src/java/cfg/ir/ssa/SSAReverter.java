package cfg.ir.ssa;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import cfg.ir.graph.CFGGraph;
import cfg.ir.nodes.CFGNode;
import cfg.ir.nodes.CFGPhiFunctionBlock;
import cfg.ir.nodes.CFGVarAssignNode;
import cyr7.ir.nodes.IRTemp;
import graph.GraphNode;

public class SSAReverter {

    private SSAReverter() {}

    /**
     * Transforms phi-functions into move statements that occur in the
     * incoming nodes.
     * @param start
     * @return
     */
    public static CFGGraph revert(CFGGraph cfg) {
        final Set<GraphNode<CFGNode>> visited = new HashSet<>();
        final Queue<GraphNode<CFGNode>> worklist = new ArrayDeque<>();

        worklist.add(cfg.startNode());

        while (!worklist.isEmpty()) {
            final var node = worklist.remove();

            if (visited.contains(node)) continue;

            if (node.value() instanceof CFGPhiFunctionBlock) {
                // Remove phi function and append to previous nodes.
                final var incoming = cfg.incomingNodes(node);
                final int numberOfInNodes = incoming.size();
                final var phi = (CFGPhiFunctionBlock)node.value();

                // Remove incoming edges coming from phi.
                cfg.remove(node);

                List<List<CFGNode>> newNodeList =
                                            new ArrayList<>(numberOfInNodes);
                for (int i = 0; i < numberOfInNodes; i++) {
                    newNodeList.add(new ArrayList<>());
                }

                phi.mappings.forEach((var, args) -> {
                    for (int j = 0; j < args.size(); j++) {
                        String a = args.get(j);
                        newNodeList.get(j).add(new CFGVarAssignNode(phi.location(),
                                             var, new IRTemp(phi.location(), a)));
                    }
                });
                for (int j = 0; j < numberOfInNodes; j++) {
                    // Remove incoming edges to phi.
                    final var listOfMoves = newNodeList.get(j);
                    final var previousNode = incoming.get(j);
                    cfg.innerInsert(previousNode, node, (CFGNode[])listOfMoves.toArray());
                }
            }
            visited.add(node);
            final var outgoing = cfg.outgoingNodes(node);
            for (GraphNode<CFGNode> out: outgoing) {
                if (!visited.contains(out)) {
                    worklist.add(out);
                }
            }
        }
        return cfg;
    }

}
