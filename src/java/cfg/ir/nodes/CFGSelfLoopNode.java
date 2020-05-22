package cfg.ir.nodes;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cyr7.cfg.ir.dfa.BackwardTransferFunction;
import cyr7.cfg.ir.dfa.ForwardTransferFunction;
import cyr7.cfg.ir.visitor.IrCFGVisitor;
import java_cup.runtime.ComplexSymbolFactory.Location;

public class CFGSelfLoopNode extends CFGNode {

    public CFGSelfLoopNode() {
        super(new Location(-1, -1));
    }

    @Override
    public <T> T accept(IrCFGVisitor<T> visitor) {
        return visitor.visit(this);
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

    @Override
    public Set<String> kills() {
        return Collections.emptySet();
    }

    @Override
    public void refreshDfaSets() {
        return;
    }

    @Override
    public String toString() {
        return "∞";
    }

}
