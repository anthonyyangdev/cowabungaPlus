package cyr7.parser.xi;

import cyr7.C;
import cyr7.ast.ASTFactory;
import cyr7.ast.stmt.StmtNode;
import cyr7.exceptions.parser.UnexpectedTokenException;
import cyr7.parser.util.ParserFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestReturnStatement {

    private final ASTFactory ast = new ASTFactory(C.LOC);

    @Test
    void testForbidMultipleSemicolonsAfterReturn() throws Exception {
        try {
            ParserFactory.parseStatement("return ;;");
        } catch (UnexpectedTokenException e) {
            assertEquals(9, e.column);
        }

        try {
            ParserFactory.parseStatement("return 1 + 2;;");
        } catch (UnexpectedTokenException e) {
            assertEquals(14, e.column);
        }
    }

    @Test
    void testSemicolonReturnInteraction() throws Exception {
        StmtNode statement = ParserFactory.parseStatement("return x").get(0);
        assertEquals(ast.returnStmt(ast.variable("x")), statement);

        statement = ParserFactory.parseStatement("return x;").get(0);
        assertEquals(ast.returnStmt(ast.variable("x")), statement);

        statement = ParserFactory.parseStatement("return x, y;").get(0);
        assertEquals(ast.returnStmt(ast.variable("x"), ast.variable("y")), statement);
    }

}
