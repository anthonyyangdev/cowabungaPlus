package cyr7.cfg.nodes;

import java.util.List;

import cyr7.ir.cfg.CFGStubNode;
import cyr7.cfg.dfa.BackwardTransferFunction;
import cyr7.cfg.dfa.ForwardTransferFunction;
import cyr7.cfg.visitor.CFGVisitor;
import cyr7.ir.nodes.IRExpr;
import java_cup.runtime.ComplexSymbolFactory.Location;

public class CFGIfNode extends CFGNode {

    private CFGNode trueBranch;
    private CFGNode falseBranch;
    public final IRExpr cond;

    public CFGIfNode(Location location, CFGNode trueBranch,
            CFGNode falseBranch, IRExpr cond) {
        super(location);
        this.trueBranch = trueBranch;
        this.falseBranch = falseBranch;
        this.cond = cond;

        this.updateIns();
    }

    @Override
    public <T> T accept(CFGVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public CFGNode falseBranch() {
        return falseBranch;
    }

    public CFGNode trueBranch() {
        return trueBranch;
    }

    @Override
    public List<CFGNode> out() {
        return List.of(trueBranch, falseBranch);
    }

    @Override
    public void convertFromStub(CFGStubNode stub, CFGNode n) {
        if (trueBranch == stub) {
            this.trueBranch = n;
        } else if (falseBranch == stub) {
            this.falseBranch = n;
        } else {
            throw new UnsupportedOperationException(
                    "Cannot change out node unless it was originally a stub node.");
        }
        this.updateIns();
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
        return "(if " + cond.label() + ")";
    }
}