package cfg.ir.nodes;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cyr7.cfg.ir.dfa.BackwardTransferFunction;
import cyr7.cfg.ir.dfa.ForwardTransferFunction;
import cyr7.cfg.ir.visitor.IrCFGVisitor;
import java_cup.runtime.ComplexSymbolFactory.Location;

public class CFGStartNode extends CFGNode {

    public CFGStartNode(Location location) {
        super(location);
    }

    @Override
    public <T> T accept(IrCFGVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public <T> List<T> acceptForward(ForwardTransferFunction<T> transferFunction, T in) {
        return List.of(transferFunction.transfer(this, in));
    }

    @Override
    public <T> T acceptBackward(BackwardTransferFunction<T> transferFunction, T input) {
        return input;
    }

    @Override
    public String toString() {
        return "start";
    }

    @Override
    public Set<String> defs() {
        return Collections.emptySet();
    }

    @Override
    public Set<String> uses() {
        return Collections.emptySet();
    }

    @Override
    public Map<String, String> gens() {
        return Collections.emptyMap();
    }

    /**
     * Although the returned set is empty, this {@link CFGStartNode node}
     * actually kills everything.
     */
    @Override
    public Set<String> kills() {
        return Collections.emptySet();
    }

    @Override
    public void refreshDfaSets() {
        return;
    }
}
