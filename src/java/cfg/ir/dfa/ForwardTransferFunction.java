package cfg.ir.dfa;

import cfg.ir.nodes.CFGBlockNode;
import cfg.ir.nodes.CFGCallNode;
import cfg.ir.nodes.CFGIfNode;
import cfg.ir.nodes.CFGMemAssignNode;
import cfg.ir.nodes.CFGPhiFunctionBlock;
import cfg.ir.nodes.CFGSelfLoopNode;
import cfg.ir.nodes.CFGStartNode;
import cfg.ir.nodes.CFGVarAssignNode;

public interface ForwardTransferFunction<L> {

    L transfer(CFGCallNode n, L in);

    L transferTrue(CFGIfNode n, L in);

    L transferFalse(CFGIfNode n, L in);

    L transfer(CFGMemAssignNode n, L in);

    L transfer(CFGStartNode n, L in);

    L transfer(CFGVarAssignNode n, L in);

    L transfer(CFGSelfLoopNode n, L in);

    L transfer(CFGBlockNode n, L in);

    L transfer(CFGPhiFunctionBlock n, L in);

}
