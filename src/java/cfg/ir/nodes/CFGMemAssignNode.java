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
import cyr7.util.Sets;
import java_cup.runtime.ComplexSymbolFactory.Location;

public class CFGMemAssignNode extends CFGNode {

    public IRExpr target;
    public IRExpr value;

    private Set<String> useSet;

    public CFGMemAssignNode(
        Location location,
        IRExpr target,
        IRExpr value) {

        super(location);
        this.target = target;
        this.value = value;

        this.refreshDfaSets();
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
        return transferFunction.transfer(this, input);
    }

    @Override
    public String toString() {
        String targetString = target.userFriendlyString().replaceAll("\n", "");
        String valueString = target.userFriendlyString().replaceAll("\n", "");
        return targetString + " = " + valueString;
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
        this.useSet = Sets.union(
                value.accept(IRExprVarsVisitor.INSTANCE),
                target.accept(IRExprVarsVisitor.INSTANCE));
    }
}