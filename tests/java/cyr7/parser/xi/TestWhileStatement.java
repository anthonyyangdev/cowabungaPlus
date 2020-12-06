package cyr7.parser.xi;

import cyr7.C;
import cyr7.ast.ASTFactory;
import cyr7.ast.stmt.*;
import cyr7.exceptions.parser.UnexpectedTokenException;
import cyr7.parser.util.ParserFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestWhileStatement {

    private final ASTFactory ast = new ASTFactory(C.LOC);

    @Test
    void testSingleLineWhileStatement() throws Exception {
        StmtNode expected = ast.whileLoop(
                ast.lt(ast.variable("a"), ast.variable("b")),
                ast.assign(ast.variable("a"), ast.add(ast.variable("a"), ast.integer(1)))
        );
        StmtNode statement = ParserFactory
            .parseStatement("while (a < b) a = " + "a + 1; ").get(0);
        assertEquals(expected, statement);
        statement = ParserFactory
            .parseStatement("while (a < b) a = a + 1 \n return 0 ").get(0);
        assertEquals(expected, statement);
    }

    @Test
    void testWhileAndSemicolonInteraction() throws Exception {
        StmtNode statement = ParserFactory
                .parseStatement("while (a < b) { a = a + 1; }; ").get(0);
        StmtNode expected = ast.whileLoop(
                ast.lt(ast.variable("a"), ast.variable("b")),
                ast.block(ast.assign(ast.variable("a"), ast.add(ast.variable("a"), ast.integer(1))))
        );
        assertEquals(expected, statement);
    }

    @Test
    void testWhileAndReturnInteraction() throws Exception {
        try {
            ParserFactory.parseStatement("while i < j return");
        } catch (UnexpectedTokenException e) {
            assertEquals(13, e.column);
        }

        try {
            ParserFactory.parseStatement("while (i < j) return 0;");
        } catch (UnexpectedTokenException e) {
            assertEquals(15, e.column);
        }

        StmtNode statement =
            ParserFactory.parseStatement("while b { return 0; }").get(0);
        assertEquals(ast.whileLoop(
                ast.variable("b"),
                ast.block(ast.returnStmt(ast.integer(0)))
        ), statement);

        statement =
            ParserFactory.parseStatement(
                "while c { b = true return 0 }"
            ).get(0);
        assertEquals(ast.whileLoop(
                ast.variable("c"),
                ast.block(
                        ast.assign(ast.variable("b"), ast.bool(true)),
                        ast.returnStmt(ast.integer(0))
                )
        ), statement);
    }

}
