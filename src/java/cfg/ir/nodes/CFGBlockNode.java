package cfg.ir.nodes;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import cfg.ir.dfa.BackwardTransferFunction;
import cfg.ir.dfa.ForwardTransferFunction;
import cfg.ir.visitor.IrCFGVisitor;
import java_cup.runtime.ComplexSymbolFactory.Location;

public class CFGBlockNode extends CFGNode {

    /**
     * A node in the block can only be a varAssignNode, memAssignNode, or
     * callNode.
     * <p>
     * The last node in the block is indicated by a CFGStubNode.
     */
    public List<CFGNode> block;

    private Set<String> useSet;
    private Set<String> defSet;
    private Set<String> killSet;
    private Map<String, String> genSet;

    /**
     * A block of CFG nodes, where for all nodes in {@code block}, all outgoing
     * and incoming point to nodes within {@code block}. Additionally,
     * {@code block} is a linear CFG tree, i.e. for each node, there is only
     * at most one incoming edge and one outgoing edge.
     */
    public CFGBlockNode(Location location, List<CFGNode> block) {
        super(location);
        this.block = block;

        this.refreshDfaSets();
    }

    @Override
    public <T> T accept(IrCFGVisitor<T> visitor) {
        throw new UnsupportedOperationException("Difficult");
    }

    @Override
    public <T> List<T> acceptForward(
            ForwardTransferFunction<T> transferFunction, T input) {
        return List.of(transferFunction.transfer(this, input));
    }

    @Override
    public <T> T acceptBackward(BackwardTransferFunction<T> transferFunction,
            T input) {
        return transferFunction.transfer(this, input);
    }

    @Override
    public String toString() {
        return String.join("\n", this.block.stream()
                                           .map(n -> n.toString())
                                           .collect(Collectors.toList()));
    }

    @Override
    public Set<String> defs() {
        return Collections.unmodifiableSet(this.defSet);
    }

    @Override
    public Set<String> uses() {
        return Collections.unmodifiableSet(this.useSet);
    }

    @Override
    public Map<String, String> gens() {
        return Collections.unmodifiableMap(this.genSet);
    }

    @Override
    public Set<String> kills() {
        return Collections.unmodifiableSet(this.killSet);
    }

    @Override
    public void refreshDfaSets() {
        this.killSet = new HashSet<>();
        this.defSet = new HashSet<>();
        this.useSet = new HashSet<>();
        this.genSet = new HashMap<>();
        this.initKillSet();
        this.initDefSet();
        this.initUseSet();
        this.initGenSet();
    }

    private void initUseSet() {
        for (int i = this.block.size() - 1; i >= 0; i--) {
            final var node = block.get(i);
            this.useSet.removeAll(node.defs());
            this.useSet.addAll(node.uses());
        }
        this.useSet = Collections.unmodifiableSet(this.useSet);
    }

    /**
     * All defined variables defined are killed.
     */
    private void initKillSet() {
        for (CFGNode n: this.block) {
            this.killSet.addAll(n.kills());
        }
        this.killSet = Collections.unmodifiableSet(this.killSet);
    }

    private void initDefSet() {
        for (CFGNode n: this.block) {
            this.defSet.addAll(n.defs());
        }
        this.defSet = Collections.unmodifiableSet(this.defSet);
    }

    private void initGenSet() {
        for (CFGNode n: this.block) {
            n.kills().forEach(this.genSet::remove);;
            this.genSet.values().removeAll(n.kills());
            n.gens().forEach(this.genSet::put);
            this.defSet.addAll(n.defs());
        }
        this.genSet = Collections.unmodifiableMap(this.genSet);
    }

}
