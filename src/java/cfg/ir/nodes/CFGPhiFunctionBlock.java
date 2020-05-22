package cfg.ir.nodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cfg.ir.dfa.BackwardTransferFunction;
import cfg.ir.dfa.ForwardTransferFunction;
import cfg.ir.visitor.IrCFGVisitor;
import java_cup.runtime.ComplexSymbolFactory.Location;

public class CFGPhiFunctionBlock extends CFGNode {

    public final Map<String, List<String>> mappings;

    public CFGPhiFunctionBlock(Location location,
            int numOfIncoming, Set<String> variables) {
        super(location);

        this.mappings = new HashMap<>();
        for (String v: variables) {
            final List<String> arguments = new ArrayList<>();
            for (int i = 0; i < numOfIncoming; i++) {
                arguments.add(v);
            }
            this.mappings.put(v, arguments);
        }
    }

    @Override
    public <T> T accept(IrCFGVisitor<T> visitor) {
        throw new AssertionError("Do not use a visitor on phi-function block.");
    }

    @Override
    public <T> List<T> acceptForward(
            ForwardTransferFunction<T> transferFunction, T input) {
        throw new AssertionError("Do not use a transfer function on phi-function block.");
    }

    @Override
    public <T> T acceptBackward(BackwardTransferFunction<T> transferFunction,
            T input) {
        throw new AssertionError("Do not use a transfer function on phi-function block.");
    }

    @Override
    public Set<String> defs() {
        throw new UnsupportedOperationException("Do not use defs set of phi-function");
    }

    @Override
    public Set<String> uses() {
        throw new UnsupportedOperationException("Do not use uses set of phi-function");
    }

    @Override
    public Map<String, String> gens() {
        throw new UnsupportedOperationException("Do not use gen set of phi-function");
    }

    @Override
    public Set<String> kills() {
        throw new UnsupportedOperationException("Do not use kill set of phi-function");
    }

    @Override
    public void refreshDfaSets() {
        throw new UnsupportedOperationException("No data to refresh");
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        this.mappings.forEach((target, args) -> {
            buffer.append(target + " = φ(" + String.join(", ", args) + ")\n");
        });
        return buffer.toString();
    }

}
