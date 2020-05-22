package cfg.ir.nodes;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cyr7.cfg.ir.dfa.BackwardTransferFunction;
import cyr7.cfg.ir.dfa.ForwardTransferFunction;
import cyr7.cfg.ir.visitor.IrCFGVisitor;
import cyr7.ir.interpret.Configuration;
import cyr7.ir.nodes.IRExpr;
import cyr7.ir.nodes.IRTemp;
import cyr7.ir.visit.IRExprVarsVisitor;
import java_cup.runtime.ComplexSymbolFactory.Location;

public class CFGVarAssignNode extends CFGNode {

    public String variable;
    public IRExpr value;
    private Set<String> useSet;
    private Set<String> defSet;
    private Set<String> killSet;
    private Map<String, String> genSet;

    public CFGVarAssignNode(Location location, String variable, IRExpr value) {
        super(location);
        this.variable = variable;
        this.value = value;

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
        String valueString = value.toString()
                                  .replaceAll("\n", "");
        return String.format("%s=%s", variable, valueString);
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
        if (this.value instanceof IRTemp
            && ((IRTemp)this.value).name().startsWith(Configuration.ABSTRACT_ARG_PREFIX)) {
            this.useSet = Collections.emptySet();
        } else {
            this.useSet = value.accept(IRExprVarsVisitor.INSTANCE);
        }

        if (!this.variable.startsWith(Configuration.ABSTRACT_RET_PREFIX)) {
            this.defSet = Collections.singleton(variable);
        } else {
            this.defSet = Collections.emptySet();
        }

        this.killSet = Collections.singleton(variable);
        this.genSet = new HashMap<>();
        if (value instanceof IRTemp) {
            String source = ((IRTemp)value).name();
            if (!source.startsWith(Configuration.ABSTRACT_ARG_PREFIX)
                    && !source.equals(this.variable)) {
                genSet.put(variable, source);
            }
        }
    }
}
