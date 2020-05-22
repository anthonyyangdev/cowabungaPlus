package cfg.ir.dfa;

import cfg.ir.nodes.CFGBlockNode;
import cfg.ir.nodes.CFGCallNode;
import cfg.ir.nodes.CFGIfNode;
import cfg.ir.nodes.CFGMemAssignNode;
import cfg.ir.nodes.CFGPhiFunctionBlock;
import cfg.ir.nodes.CFGReturnNode;
import cfg.ir.nodes.CFGSelfLoopNode;
import cfg.ir.nodes.CFGVarAssignNode;

public interface BackwardTransferFunction<L> {

    L transfer(CFGCallNode n, L out);

    L transfer(CFGIfNode n, L out);

    L transfer(CFGMemAssignNode n, L out);

    L transfer(CFGReturnNode n, L out);

    L transfer(CFGVarAssignNode n, L out);

    L transfer(CFGSelfLoopNode n, L out);

    L transfer(CFGBlockNode n, L out);

    L transfer(CFGPhiFunctionBlock n, L in);

}
