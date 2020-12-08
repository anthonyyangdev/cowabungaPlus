package cyr7.parser.xi;

import cyr7.C;
import cyr7.ast.ASTFactory;
import cyr7.ast.Node;
import cyr7.ast.stmt.StmtNode;
import cyr7.parser.util.ParserFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestProcedureStatememt {

    private final ASTFactory ast = new ASTFactory(C.LOC);

    @Test
    void testProcedureStatement() throws Exception {
        StmtNode statement = ParserFactory.parseStatement("f()").get(0);
        Node expected = ast.procedure(ast.call("f"));
        assertEquals(expected, statement);

        statement = ParserFactory.parseStatement("lengthy({5, 10, 15})").get(0);
        expected = ast.procedure(ast.call("f",
                ast.array(ast.integer(5), ast.integer(10), ast.integer(15))));
        assertEquals(expected, statement);
    }

}
