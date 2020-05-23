package cfg.ir.nodes;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import cfg.ir.dfa.BackwardTransferFunction;
import cfg.ir.dfa.ForwardTransferFunction;
import cfg.ir.visitor.IrCFGVisitor;
import cyr7.ir.nodes.IRCallStmt;
import cyr7.ir.visit.IRExprVarsVisitor;
import java_cup.runtime.ComplexSymbolFactory.Location;

public class CFGCallNode extends CFGNode {

    // This includes both procedures and function calls
    public IRCallStmt call;

    private Set<String> useSet;
    private Set<String> defSet;
    private Set<String> killSet;

    public CFGCallNode(Location location, IRCallStmt call) {
        super(location);
        this.call = call;

        this.refreshDfaSets();
    }

    @Override
    public <T> T accept(IrCFGVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public <T> List<T> acceptForward(
            ForwardTransferFunction<T> transferFunction, T in) {
        return List.of(transferFunction.transfer(this, in));
    }

    @Override
    public <T> T acceptBackward(BackwardTransferFunction<T> transferFunction,
            T input) {
        return transferFunction.transfer(this, input);
    }

    @Override
    public String toString() {
        return call.toString().replaceAll("\n", "");
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
        return Collections.emptyMap();
    }

    @Override
    public Set<String> kills() {
        return Collections.unmodifiableSet(this.killSet);
    }

    @Override
    public void refreshDfaSets() {
        this.useSet = call.args().stream().flatMap(arg -> {
            return arg.accept(IRExprVarsVisitor.INSTANCE).stream();
        }).collect(Collectors.toSet());
        this.defSet = Set.copyOf(call.collectors());
        this.killSet = Set.copyOf(call.collectors());
    }
}
