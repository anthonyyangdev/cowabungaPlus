package cyr7.parser.xi;

import cyr7.C;
import cyr7.ast.ASTFactory;
import cyr7.ast.Node;
import cyr7.ast.stmt.*;
import cyr7.exceptions.parser.UnexpectedTokenException;
import cyr7.parser.util.ParserFactory;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static cyr7.parser.util.ParserFactory.LOC;

public class TestIfElseStatement {

    private final ASTFactory ast = new ASTFactory(C.LOC);

    @Test
    void testIfElseAndReturnInteraction() throws Exception {
        StmtNode statement = ParserFactory.parseStatement("return").get(0);
        assertEquals(statement, new ReturnStmtNode(LOC, new LinkedList<>()));

        statement = ParserFactory.parseStatement("if false { return false } else { " +
                "return true }").get(0);
        Node expected = ast.ifElse(
                ast.bool(false),
                ast.block(ast.returnStmt(ast.bool(false))),
                ast.block(ast.returnStmt(ast.bool(true)))
        );
        assertEquals(expected, statement);

        List<StmtNode> statements =
            ParserFactory.parseStatement(
                "if false { return false } return true"
            );
        Node expectedIf = ast.ifElse(ast.bool(false), ast.block(ast.returnStmt(ast.bool(false))));
        assertEquals(expectedIf, statements.get(0));

        Node expectedNext = ast.returnStmt(ast.bool(true));
        assertEquals(expectedNext, statements.get(1));

        assertThrows(UnexpectedTokenException.class, () ->
            ParserFactory.parseStatement(
                "if a < b { return false return true }"
            ));

        assertThrows(UnexpectedTokenException.class, () ->
            ParserFactory.parseStatement(
                "if a < b { return a } else return b"
            ));
    }

    @Test
    void testIfElseAndSemicolonInteraction() throws Exception {
        StmtNode statement = ParserFactory.parseStatement("if (true) { " +
            "return; };").get(0);
        Node expected = ast.ifElse(
                ast.bool(true),
                ast.block(ast.returnStmt())
        );
        assertEquals(expected, statement);

        statement = ParserFactory.parseStatement("if (a < b) b = b - a; else " +
            "a = a - b;").get(0);
        expected = ast.ifElse(
                ast.lt(ast.variable("a"), ast.variable("b")),
                ast.assign(ast.variable("b"), ast.sub(ast.variable("b"), ast.variable("a"))),
                ast.assign(ast.variable("a"), ast.sub(ast.variable("a"), ast.variable("b")))
        );
        assertEquals(expected, statement);
    }

}
