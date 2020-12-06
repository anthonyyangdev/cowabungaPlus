package cyr7.ir.astToIr;

import static org.junit.jupiter.api.Assertions.assertEquals;

import cyr7.ast.ASTFactory;
import org.junit.jupiter.api.Test;

import cyr7.C;
import cyr7.ast.AbstractNode;
import cyr7.ir.nodes.IRStmt;
import cyr7.ir.util.IRFactory;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class TestStmt {

    private final ASTFactory make = new ASTFactory(C.LOC);

    @Test
    void testIfElseStmt() {
        AbstractNode astNode = make.ifElse(
                make.and(make.bool(false), make.bool(true)),
                make.returnStmt(make.integer("4")),
                make.returnStmt(make.integer("5"))
        );
        IRStmt result = (IRStmt) IRFactory.parseAstStmt(astNode);
        long expectedResult = 5;
        assertEquals(expectedResult, IRFactory.testStmts(result));
    }

}
