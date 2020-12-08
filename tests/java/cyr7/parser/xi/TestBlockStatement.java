package cyr7.parser.xi;

import cyr7.C;
import cyr7.ast.ASTFactory;
import cyr7.ast.stmt.StmtNode;
import cyr7.parser.util.ParserFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestBlockStatement {

    private final ASTFactory ast = new ASTFactory(C.LOC);

    @Test
    void testNestedBlockStatement() throws Exception {
        StmtNode statement =
            ParserFactory
                .parseStatement("{ { }; { return }; return };")
                .get(0);
        StmtNode expected = ast.block(
                ast.block(),
                ast.block(ast.returnStmt()),
                ast.returnStmt()
        );
        assertEquals(expected, statement);

        statement = ParserFactory.parseStatement("{ while x { }; };").get(0);
        expected = ast.block(
                ast.whileLoop(ast.variable("x"), ast.block())
        );
        assertEquals(expected, statement);
    }

}
