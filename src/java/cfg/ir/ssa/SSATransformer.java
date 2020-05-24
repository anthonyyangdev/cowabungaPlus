package cfg.ir.ssa;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import cfg.ir.graph.CFGGraph;
import cfg.ir.nodes.CFGCallNode;
import cfg.ir.nodes.CFGIfNode;
import cfg.ir.nodes.CFGMemAssignNode;
import cfg.ir.nodes.CFGNode;
import cfg.ir.nodes.CFGPhiFunctionBlock;
import cfg.ir.nodes.CFGReturnNode;
import cfg.ir.nodes.CFGSelfLoopNode;
import cfg.ir.nodes.CFGStartNode;
import cfg.ir.nodes.CFGVarAssignNode;
import cfg.ir.opt.IRTempReplacer;
import cfg.ir.visitor.IrCFGVisitor;
import cyr7.ir.nodes.IRCallStmt;
import graph.GraphNode;

public class SSATransformer {

    private SSATransformer() {}

    /**
     * Returns a CFG that uses phi-function blocks.
     * @param cfg
     * @return
     */
    public static CFGGraph convert(CFGGraph cfg) {
        // Create the dominance frontier.
        var domTreeComp = new DominatorTreeComputer(cfg);
        var domFrontier = domTreeComp.computeDFMap();
        var childrenNodes = domTreeComp.getChildren();

        final var defsites = getDefsites(cfg);
        final var phiLocations = computePhiFuncLoc(defsites, domFrontier);

        final var phiFuncsInserted = insertPhiFunctions(phiLocations, cfg);

        domTreeComp = new DominatorTreeComputer(phiFuncsInserted);
        domFrontier = domTreeComp.computeDFMap();
        childrenNodes = domTreeComp.getChildren();

        return RenameVariable
                        .execute(phiFuncsInserted, childrenNodes, defsites);
    }

    private static class RenameVariable {

        private RenameVariable() {}

        public static CFGGraph execute(final CFGGraph cfg,
                Map<CFGNode, Set<CFGNode>> domTree,
                Map<String, Set<CFGNode>> defsites) {

            // Initialization stage
            final Map<String, Integer> count = new HashMap<>();
            final Map<String, Deque<Integer>> stack = new HashMap<>();
            defsites.keySet().forEach(a -> {
                count.put(a, 0);
                stack.put(a, new ArrayDeque<>());
                stack.get(a).push(0);
            });

            final var visitor = new Visitor(count, stack);
            nameVariables(count, stack, domTree, cfg.startNode(), cfg, visitor);
            return cfg;
        }


        private static void nameVariables(
                final Map<String, Integer> count,
                final Map<String, Deque<Integer>> stack,
                final Map<CFGNode, Set<CFGNode>> domTree,
                final GraphNode<CFGNode> node,
                final CFGGraph cfg,
                final Visitor visitor) {

            List<String> definitions = new ArrayList<>();

            // First part of rename(n)
            if (node.value() instanceof CFGPhiFunctionBlock) {
                // We'll write the case for phi functions here, so
                // that we don't need to add a node to the visitor file.
                // If needed, we can add it to the visitor and copy the
                // following into the visitor.
                // stack and count values are the same as the ones in the
                // visitor b/c of referencing.
                final var phi = (CFGPhiFunctionBlock)node.value();
                final var defs = Set.copyOf(phi.mappings.keySet());
                defs.stream().forEach(def -> {
                    final int i = count.get(def) + 1;
                    definitions.add(def);
                    count.put(def, i);
                    stack.get(def).push(i);
                    List<String> args = phi.mappings.remove(def);
                    phi.mappings.put(def + "_" + i, args);
                });
            } else {
                definitions.addAll(node.value().accept(visitor));
                node.value().refreshDfaSets();
            }

            // for each successor y of node, check for phi functions
            final var outgoing = cfg.outgoingNodes(node);
            for (GraphNode<CFGNode> outNodes: outgoing) {
                final var y = outNodes.value();
                if (y instanceof CFGPhiFunctionBlock) {
                    final var phi = (CFGPhiFunctionBlock)y;
                    final int j = cfg.incomingNodes(new GraphNode<>(phi))
                                     .indexOf(node);
                    final var defs = phi.mappings.keySet();
                    for (String def: defs) {
                        final var args = phi.mappings.get(def);
                        final var arg = args.get(j);
                        final int i = stack.get(arg).peek();
                        args.set(j, arg + "_" + i);
                    }
                }
            }
            for (CFGNode out: domTree.get(node.value())) {
                nameVariables(count, stack, domTree, new GraphNode<>(out), cfg, visitor);
            }
            for (String def: definitions) {
                stack.get(def).pop();
            }
        }

        /**
         * This visitor will rename variables based on the algorithm described
         * by Appel.
         * <p>
         * This use to run the procedure for the first loop of the
         * Rename(n) function in Algorithm 19.7.
         *
         */
        private static class Visitor implements IrCFGVisitor<List<String>> {

            private final Map<String, Integer> count;
            private final Map<String, Deque<Integer>> stack;

            public Visitor(final Map<String, Integer> count,
                    final Map<String, Deque<Integer>> stack) {
                this.count = count;
                this.stack = stack;
            }

            @Override
            public List<String> visit(CFGCallNode n) {
                Map<String, String> tempReplaceMapping = new HashMap<>();
                for (String use: n.uses()) {
                    final int i = stack.get(use).peek();
                    tempReplaceMapping.put(use, use + "_" + i);
                }
                final var newArgs = n.call.args()
                                    .stream().map(arg -> IRTempReplacer
                                    .replace(arg, tempReplaceMapping))
                                    .collect(Collectors.toList());

                List<String> originalCollectors = n.call.collectors();

                List<String> collectors = new ArrayList<>();
                for (String def: n.call.collectors()) {
                    final int i = count.get(def) + 1;
                    count.put(def, i);
                    stack.get(def).push(i);
                    collectors.add(def + "_" + i);
                }
                n.call = new IRCallStmt(n.call.location(), collectors,
                                        n.call.target(), newArgs);
                return originalCollectors;
            }

            @Override
            public List<String> visit(CFGIfNode n) {
                Map<String, String> tempReplaceMapping = new HashMap<>();
                for (String use: n.uses()) {
                    final int i = stack.get(use).peek();
                    tempReplaceMapping.put(use, use + "_" + i);
                }
                n.cond = IRTempReplacer.replace(n.cond, tempReplaceMapping);
                return Collections.emptyList();
            }

            @Override
            public List<String> visit(CFGVarAssignNode n) {
                Map<String, String> tempReplaceMapping = new HashMap<>();
                for (String use: n.uses()) {
                    final int i = stack.get(use).peek();
                    tempReplaceMapping.put(use, use + "_" + i);
                }
                n.value = IRTempReplacer.replace(n.value, tempReplaceMapping);

                // To ignore var assignments to returns.
                if (n.defs().isEmpty()) {
                    return List.of();
                }

                final int i = count.get(n.variable) + 1;
                final String originalVar = n.variable;
                count.put(n.variable, i);
                stack.get(n.variable).push(i);
                n.variable = n.variable + "_" + i;

                return List.of(originalVar);

            }

            @Override
            public List<String> visit(CFGMemAssignNode n) {
                Map<String, String> tempReplaceMapping = new HashMap<>();
                for (String use: n.uses()) {
                    final int i = stack.get(use).peek();
                    tempReplaceMapping.put(use, use + "_" + i);
                }

                n.target = IRTempReplacer.replace(n.target, tempReplaceMapping);
                n.value = IRTempReplacer.replace(n.value, tempReplaceMapping);
                return Collections.emptyList();
            }

            @Override
            public List<String> visit(CFGReturnNode n) {
                return Collections.emptyList();
            }

            @Override
            public List<String> visit(CFGStartNode n) {
                return Collections.emptyList();
            }

            @Override
            public List<String> visit(CFGSelfLoopNode n) {
                return Collections.emptyList();
            }
        }

    }


    private static CFGGraph insertPhiFunctions(
            final Map<String, Set<CFGNode>> phiLocations,
            final CFGGraph cfg) {

        // Invert the phiLocation mapping
        final Map<CFGNode, Set<String>> requiredPhiFuncMap = new HashMap<>();
        phiLocations.forEach((var, nodeSet) ->
            nodeSet.forEach(node -> {
                if (requiredPhiFuncMap.containsKey(node)) {
                    requiredPhiFuncMap.get(node).add(var);
                } else {
                    requiredPhiFuncMap.put(node, new HashSet<>());
                    requiredPhiFuncMap.get(node).add(var);
                }
            })
        );

        requiredPhiFuncMap.keySet().forEach(n -> {
            final var setOfVars = requiredPhiFuncMap.get(n);
            final var paramSize = cfg.inDegree(new GraphNode<>(n));

            final var phiBlock =
                    new CFGPhiFunctionBlock(n.location(), paramSize, setOfVars);

            // Need to insert phiBlock before n, but still keep n
            cfg.prependNode(new GraphNode<>(phiBlock), n);
        });

        return cfg;
    }


    /**
     * Returns a map of defined variables to the set of nodes that those
     * variables are defined in.
     *
     * <p>
     * See page 407 (Algorithm 19.6) of Appel for algorithm.
     *
     * @param start
     * @return
     */
    private static Map<String, Set<CFGNode>> getDefsites(CFGGraph cfg) {
        final var start = cfg.startNode();
        Set<GraphNode<CFGNode>> visited = new HashSet<>();
        Deque<GraphNode<CFGNode>> worklist = new ArrayDeque<>();
        Map<String, Set<CFGNode>> defsites = new HashMap<>();
        worklist.add(start);

        while (!worklist.isEmpty()) {
            final var node = worklist.remove();
            node.value().defs().forEach(def -> {
                if (!defsites.containsKey(def)) {
                    defsites.put(def, new HashSet<>());
                }
                defsites.get(def).add(node.value());
            });
            visited.add(node);
            final var outgoing = cfg.outgoingNodes(node);
            for (GraphNode<CFGNode> out: outgoing) {
                if (!visited.contains(out)) {
                    worklist.add(out);
                }
            }
        }
        return defsites;
    }


    /**
     * Computes the set of nodes a variable needs to have a phi-function.
     * <p>
     * See part 2 of algorithm 19.6 by Appel.
     * @param defsites Variable v -> Set of CFGNodes where v is defined.
     * @param domFrontier CFGNode n -> Set of CFGNodes that dominate n.
     * @param start The start node a function.
     * @return
     */
    private static Map<String, Set<CFGNode>> computePhiFuncLoc(
            final Map<String, Set<CFGNode>> defsites,
            final Map<CFGNode, Set<CFGNode>> domFrontier) {
        final Set<String> variables = Set.copyOf(defsites.keySet());
        final Deque<CFGNode> worklist = new ArrayDeque<>();
        final Map<String, Set<CFGNode>> nodesRequiringPhiForVar = new HashMap<>();
        for (String a: variables) {
            worklist.addAll(defsites.get(a));
            nodesRequiringPhiForVar.put(a, new HashSet<>());
            while (!worklist.isEmpty()) {
                final var node = worklist.remove();
                final var nodesInFrontier = domFrontier.get(node);
                for (CFGNode y: nodesInFrontier) {
                    if (!nodesRequiringPhiForVar.get(a).contains(y)) {
                        nodesRequiringPhiForVar.get(a).add(y);
                        if (!y.defs().contains(a)) {
                            worklist.add(y);
                        }
                    }
                }
            }
        }
        return nodesRequiringPhiForVar;
    }


}
