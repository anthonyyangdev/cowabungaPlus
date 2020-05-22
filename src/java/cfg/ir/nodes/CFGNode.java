package cfg.ir.nodes;

import java.util.List;
import java.util.Map;
import java.util.Set;

import cyr7.cfg.ir.dfa.BackwardTransferFunction;
import cyr7.cfg.ir.dfa.ForwardTransferFunction;
import cyr7.cfg.ir.visitor.IrCFGVisitor;
import java_cup.runtime.ComplexSymbolFactory.Location;

public abstract class CFGNode {

    private final Location location;

    protected CFGNode(Location location) {
        this.location = location;
    }

    public Location location() {
        return location;
    }

    public abstract <T> T accept(IrCFGVisitor<T> visitor);

    /**
     * Invariant: the number of elements in the output of this list must
     * correspond one-to-one with the elements of out().
     */
    public abstract <T> List<T> acceptForward(ForwardTransferFunction<T> transferFunction, T input);

    public abstract <T> T acceptBackward(BackwardTransferFunction<T> transferFunction, T input);

    public abstract Set<String> defs();
    public abstract Set<String> uses();

    public abstract Map<String, String> gens();
    public abstract Set<String> kills();

    public abstract void refreshDfaSets();

}