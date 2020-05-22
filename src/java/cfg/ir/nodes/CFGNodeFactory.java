package cfg.ir.nodes;

import cyr7.ir.nodes.IRCallStmt;
import cyr7.ir.nodes.IRExpr;
import java_cup.runtime.ComplexSymbolFactory.Location;

public final class CFGNodeFactory {

    private final Location location;

    public CFGNodeFactory(Location location) {
        this.location = location;
    }

    public CFGCallNode Call(IRCallStmt callStmt) {
        return new CFGCallNode(location, callStmt);
    }

    public CFGIfNode If(IRExpr cond) {
        return new CFGIfNode(location, cond);
    }

    public CFGMemAssignNode MemAssign(IRExpr target, IRExpr value) {
        return new CFGMemAssignNode(location, target, value);
    }

    public CFGReturnNode Return() {
        return new CFGReturnNode(location);
    }

    public CFGStartNode Start() {
        return new CFGStartNode(location);
    }

    public CFGVarAssignNode VarAssign(String variable, IRExpr value) {
        return new CFGVarAssignNode(location, variable, value);
    }

    public CFGSelfLoopNode SelfLoop() {
        return new CFGSelfLoopNode();
    }

}
