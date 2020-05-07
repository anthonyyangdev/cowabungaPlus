package cyr7.cfg.ir.nodes;

import java.util.List;

import cyr7.cfg.ir.dfa.BackwardTransferFunction;
import cyr7.cfg.ir.dfa.ForwardTransferFunction;
import cyr7.cfg.ir.visitor.IrCFGVisitor;
import java_cup.runtime.ComplexSymbolFactory.Location;

public class CFGStubNode extends CFGNode {

    public CFGStubNode() {
        super(new Location(-1, -1));
    }

    @Override
    public List<CFGNode> out() {
        return List.of();
    }

    @Override
    public <T> T accept(IrCFGVisitor<T> visitor) {
        throw new UnsupportedOperationException("Cannot visit stub node");
    }

    @Override
    public void replaceOutEdge(CFGNode stub, CFGNode n) {
        throw new UnsupportedOperationException("Cannot convert in stub node");
    }

    @Override
    public <T> List<T> acceptForward(
            ForwardTransferFunction<T> transferFunction, T input) {
        throw new UnsupportedOperationException("Cannot accept forward in stub node");
    }

    @Override
    public <T> T acceptBackward(BackwardTransferFunction<T> transferFunction,
            T input) {
        throw new UnsupportedOperationException("Cannot accept backward in stub node");
    }

    @Override
    public String toString() {
        return "Stub";
    }

}