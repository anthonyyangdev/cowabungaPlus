package cyr7.cfg.ir.visitor;

import cfg.ir.nodes.CFGBlockNode;
import cfg.ir.nodes.CFGCallNode;
import cfg.ir.nodes.CFGIfNode;
import cfg.ir.nodes.CFGMemAssignNode;
import cfg.ir.nodes.CFGReturnNode;
import cfg.ir.nodes.CFGSelfLoopNode;
import cfg.ir.nodes.CFGStartNode;
import cfg.ir.nodes.CFGVarAssignNode;

public interface IrCFGVisitor<R> {

    R visit(CFGCallNode n);

    R visit(CFGIfNode n);

    R visit(CFGVarAssignNode n);

    R visit(CFGMemAssignNode n);

    R visit(CFGReturnNode n);

    R visit(CFGStartNode n);

    R visit(CFGSelfLoopNode n);

    R visit(CFGBlockNode n);

}