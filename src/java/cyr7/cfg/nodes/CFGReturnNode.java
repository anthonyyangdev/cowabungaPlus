package cyr7.cfg.nodes;

import java.util.List;

import cyr7.cfg.dfa.BackwardTransferFunction;
import cyr7.cfg.dfa.ForwardTransferFunction;
import cyr7.cfg.visitor.AbstractCFGVisitor;
import java_cup.runtime.ComplexSymbolFactory.Location;

public class CFGReturnNode extends CFGNode {

    public CFGReturnNode(Location location) {
        super(location);
        this.updateIns();
    }

    @Override
    public List<CFGNode> out() {
        return List.of();
    }

    @Override
    public <T> T accept(AbstractCFGVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public <T> List<T> acceptForward(ForwardTransferFunction<T> transferFunction, T in) {
        return List.of();
    }

    @Override
    public <T> T acceptBackward(BackwardTransferFunction<T> transferFunction, T input) {
        return transferFunction.transfer(this, input);
    }

    public String CFGLabel() {
        return "return";
    }


}
