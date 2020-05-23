package cfg.ir.nodes;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cfg.ir.dfa.BackwardTransferFunction;
import cfg.ir.dfa.ForwardTransferFunction;
import cfg.ir.visitor.IrCFGVisitor;
import cyr7.ir.nodes.IRExpr;
import cyr7.ir.visit.IRExprVarsVisitor;
import java_cup.runtime.ComplexSymbolFactory.Location;

public class CFGIfNode extends CFGNode {

    public IRExpr cond;

    private Set<String> useSet;

    public CFGIfNode(Location location, IRExpr cond) {
        super(location);
        this.cond = cond;

        this.refreshDfaSets();
    }

    @Override
    public <T> T accept(IrCFGVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public <T> List<T> acceptForward(ForwardTransferFunction<T> transferFunction, T in) {
        return List.of(
            transferFunction.transferTrue(this, in),
            transferFunction.transferFalse(this, in));
    }

    @Override
    public <T> T acceptBackward(BackwardTransferFunction<T> transferFunction, T input) {
        return transferFunction.transfer(this, input);
    }

    @Override
    public String toString() {
        String condString = cond.userFriendlyString().replaceAll("\n", "");
        return String.format("if (%s)", condString);
    }

    @Override
    public Set<String> defs() {
        return Collections.emptySet();
    }

    @Override
    public Set<String> uses() {
        return Collections.unmodifiableSet(this.useSet);
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
        this.useSet = cond.accept(IRExprVarsVisitor.INSTANCE);
    }
}