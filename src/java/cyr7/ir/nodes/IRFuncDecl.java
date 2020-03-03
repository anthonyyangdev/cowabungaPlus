package cyr7.ir.nodes;

import edu.cornell.cs.cs4120.util.SExpPrinter;
import cyr7.ir.visit.AggregateVisitor;
import cyr7.ir.visit.IRVisitor;
import cyr7.ir.visit.InsnMapsBuilder;

/** An IR function declaration */
public class IRFuncDecl extends IRNode_c {
    private String name;
    private IRStmt body;

    public IRFuncDecl(String name, IRStmt body) {
        this.name = name;
        this.body = body;
    }

    public String name() {
        return name;
    }

    public IRStmt body() {
        return body;
    }

    @Override
    public String label() {
        return "FUNC " + name;
    }

    @Override
    public IRNode visitChildren(IRVisitor v) {
        IRStmt stmt = (IRStmt) v.visit(this, body);

        if (stmt != body) return v.nodeFactory().IRFuncDecl(name, stmt);

        return this;
    }

    @Override
    public <T> T aggregateChildren(AggregateVisitor<T> v) {
        T result = v.unit();
        result = v.bind(result, v.visit(body));
        return result;
    }

    @Override
    public InsnMapsBuilder buildInsnMapsEnter(InsnMapsBuilder v) {
        v.addNameToCurrentIndex(name);
        v.addInsn(this);
        return v;
    }

    @Override
    public IRNode buildInsnMaps(InsnMapsBuilder v) {
        return this;
    }

    @Override
    public void printSExp(SExpPrinter p) {
        p.startList();
        p.printAtom("FUNC");
        p.printAtom(name);
        body.printSExp(p);
        p.endList();
    }
}