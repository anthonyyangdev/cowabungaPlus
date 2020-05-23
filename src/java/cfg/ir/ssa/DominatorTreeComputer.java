package cfg.ir.ssa;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import cfg.ir.CFGGraph;
import cfg.ir.nodes.CFGNode;
import graph.GraphNode;
import polyglot.util.Pair;

/**
 * Computes the dominator tree of a CFG using Algorithm 19.10b by Appel.
 *
 */
public class DominatorTreeComputer {

    private final CFGNode start;
    private final Map<CFGNode, Integer> dfnum;
    private final Map<CFGNode, CFGNode> ancestors;
    private final Map<CFGNode, CFGNode> semi;
    private final Map<CFGNode, Set<CFGNode>> bucket;
    private final Map<CFGNode, CFGNode> idom;
    private final Map<CFGNode, Set<CFGNode>> idomChildren;
    private final Map<CFGNode, Set<CFGNode>> domFrontier;

    private final Map<CFGNode, CFGNode> sameDom;

    private final Map<CFGNode, CFGNode> best;

    private final Map<Integer, CFGNode> vertex;

    private boolean computedDomTree;
    private boolean computedDomFrontier;
    private boolean computedChildren;

    /**
     * The parent of the key in the spanning-tree generated by DFS.
     */
    private final Map<CFGNode, Optional<CFGNode>> parent;

    private int N;

    private final Map<CFGNode, List<GraphNode<CFGNode>>> outgoing;
    private final Map<CFGNode, List<GraphNode<CFGNode>>> incoming;


    public DominatorTreeComputer(CFGGraph cfg) {
        this.start = cfg.startNode().value();
        this.outgoing = new HashMap<>();
        this.incoming = new HashMap<>();

        cfg.nodes().forEach(n -> {
            outgoing.put(n.value(), cfg.outgoingNodes(n));
            incoming.put(n.value(), cfg.incomingNodes(n));
        });

        this.dfnum = new HashMap<>();
        this.ancestors = new HashMap<>();
        this.semi = new HashMap<>();
        this.bucket = new HashMap<>();
        this.idom = new HashMap<>();
        this.vertex = new HashMap<>();
        this.parent = new HashMap<>();
        this.best = new HashMap<>();
        this.sameDom = new HashMap<>();
        this.idomChildren = new HashMap<>();
        this.domFrontier = new HashMap<>();
        this.computedDomTree = false;
        this.computedDomFrontier = false;
        this.computedChildren = false;

        N = 0;
    }


    /**
     * Numbers the nodes in the CFG via DFS-order to produce a spanning-tree.
     * This numbering is stored in {@code this.dfnum}.
     * <p>
     * Computes also {@code vertex} and {@code parent}.
     * <p>
     * Maps all nodes to the empty set in {@code this.bucket}.
     */
    private void numberNodesDFS() {
        this.dfnum.clear();
        final Deque<Pair<Optional<CFGNode>, CFGNode>> stack = new ArrayDeque<>();
        stack.push(new Pair<>(Optional.empty(), start));
        while (!stack.isEmpty()) {
            final var element = stack.pop();

            final var immediateParent = element.part1();
            final var node = element.part2();

            if (dfnum.containsKey(node)) {
                continue; // Goto next. Dfnum already computed
            }

            this.bucket.put(node, new HashSet<>());
            this.idomChildren.put(node, new HashSet<>());
            this.bucket.put(node, new HashSet<>());

            this.dfnum.put(node, N);
            this.vertex.put(N, node);
            this.parent.put(node, immediateParent);

            N++;
            final var outgoing = this.outgoing.get(node);
            for (GraphNode<CFGNode> outNode: outgoing) {
                final var out = outNode.value();
                if (!this.dfnum.containsKey(out)) {
                    stack.push(new Pair<>(Optional.of(node), out));
                }
            }
        }
    }

    /**
     * Returns an ancestor of node {@code v} that has the lowest semi-dominator,
     * where lowest, refers to its {@code dfnum} numbering.
     * <p>
     * Amortized log(N) performance.
     * @param v
     * @return
     */
    private CFGNode ancestorWithTheLowestSemi(CFGNode v) {
        final var a = ancestors.get(v);
        if (ancestors.containsKey(a)) {
            final var b = ancestorWithTheLowestSemi(a);
            ancestors.put(v, ancestors.get(a));
            if (dfnum.get(semi.get(b))
                    < dfnum.get(semi.get(best.get(a)))) {
                best.put(v, b);
            }
        }
        return best.get(v);
    }

    /**
     * Adds the edge p → n to spanning forest implied by ancestor array.
     * @param p The ancestor node.
     * @param n The child node.
     */
    private void link(CFGNode p, CFGNode n) {
        this.ancestors.put(n, p);
        this.best.put(n, n);
    }

    /**
     * Computes the immediate dominators mapping, i.e.
     * {@code dominators().get(n)} is the immediate dominator of {@code n}.
     */
    public Map<CFGNode, CFGNode> dominators() {
        if (this.computedDomTree) {
            return Map.copyOf(this.idom);
        }
        N = 0;
        this.bucket.clear();
        this.dfnum.clear();
        this.semi.clear();
        this.ancestors.clear();
        this.idom.clear();
        this.idomChildren.clear();
        this.sameDom.clear();

        this.numberNodesDFS();
        for (int i = N - 1; i >= 1; i--) {  // Skip the root node
            final var n = this.vertex.get(i);
            final var p = this.parent.get(n).get();
            // Nodes have nonzero dfnum, so no parents are empty.
            CFGNode s = p;  // May be reassigned.

            /**
             * These lines calculate the semidominator of n, based on the
             * Semidominator Theorem.
             */
            final var incoming = this.incoming.get(n);
            for (GraphNode<CFGNode> node: incoming) {
                final var v = node.value();
                CFGNode sPrime;
                if (dfnum.get(v) <= dfnum.get(n)) {
                    sPrime = v;
                } else {
                    sPrime = semi.get(ancestorWithTheLowestSemi(v));
                }
                if (dfnum.get(sPrime) < dfnum.get(s)) {
                    s = sPrime;
                }
            }

            /**
             * Calculation of n’s dominator is deferred until the path from
             * s to n has been linked into the forest.
             */
            semi.put(n, s);
            bucket.get(s).add(n);
            this.link(p, n);

            /**
             * Now that the path from p to v has been linked into the spanning
             * forest, these lines calculate the dominator of v, based on the
             * first clause of the Dominator Theorem, or else defer the
             * calculation until y’s dominator is known.
             */
            for (CFGNode v: bucket.get(p)) {
                final var y = ancestorWithTheLowestSemi(v);
                if (semi.get(y) == semi.get(v)) {
                    idom.put(v, p);
                } else {
                    sameDom.put(v, y);
                }
            }
            bucket.get(p).clear();
        }

        /**
         * Now all the deferred dominator calculations, based on the second
         * clause of the Dominator Theorem, are performed.
         */
        for (int i = 0; i < N; i++) {
            final var n = vertex.get(i);
            if (sameDom.containsKey(n)) {
                idom.put(n, idom.get(sameDom.get(n)));
            }
        }
        this.computeChildren();
        this.computedDomTree = true;
        return Map.copyOf(this.idom);
    }

    /**
     * Computes the immediate children of nodes in the dominator tree.
     * @return
     */
    private void computeChildren() {
        if (this.computedChildren) {
            return;
        }
        final var allNodes = this.idom.keySet();
        final Map<CFGNode, Set<CFGNode>> children = new HashMap<>();
        allNodes.forEach(node -> {
            final var dominator = this.idom.get(node);
            if (!children.containsKey(dominator)) {
                children.put(dominator, new HashSet<>());
            }
            children.get(dominator).add(node);
        });
        this.computedChildren = true;
        this.idomChildren.putAll(children);
    }

    /**
     * See page 406 of Appel.
     */
    private void computeDF(CFGNode n) {
        if (this.domFrontier.containsKey(n))
            return;

        final var s = new HashSet<CFGNode>();
        final var outgoing = this.outgoing.get(n);
        for (GraphNode<CFGNode> node: outgoing) {
            final var y = node.value();
            if (idom.get(y) != n) {
                s.add(y);
            }
        }
        final var children = this.idomChildren.get(n);
        for (CFGNode c: children) {
            computeDF(c);
            final var frontierOfC = this.domFrontier.get(c);
            for (CFGNode w: frontierOfC) {
                if (!this.idomChildren.get(n).contains(w)
                        || n == w) {
                    s.add(w);
                }
            }
        }
        this.domFrontier.put(n, s);
    }


    public Map<CFGNode, Set<CFGNode>> getChildren() {
        if (!this.computedChildren)
            this.dominators();

        for (CFGNode c: this.idomChildren.keySet()) {
            for (CFGNode other: this.idomChildren.keySet()) {
                if (c != other) {
                    if (this.idomChildren.get(other).stream().anyMatch(v -> {
                        return this.idomChildren.get(c).contains(v);
                    })) {
                        throw new AssertionError("Dom Trees should have unique children");
                    }
                }
            }
        }

        return Map.copyOf(this.idomChildren);
    }

    /**
     * Computes the successors of n that are not strictly dominated by n.
     */
    public Map<CFGNode, Set<CFGNode>> computeDFMap() {
        if (this.computedDomFrontier)
            return Map.copyOf(this.domFrontier);
        if (!this.computedDomTree)
            this.dominators();

        final var allNodes = this.idom.keySet();
        allNodes.forEach(this::computeDF);
        this.computedDomFrontier = true;
        return Map.copyOf(this.domFrontier);
    }


}
