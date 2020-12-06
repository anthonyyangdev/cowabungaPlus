package cyr7.parser.xi;

import cyr7.ast.ASTFactory;
import cyr7.ast.expr.ExprNode;
import cyr7.parser.util.ParserFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static cyr7.parser.util.ParserFactory.LOC;

public class TestOperators {

    private final ASTFactory ast = new ASTFactory(LOC);

    @Test
    void testAddMul() throws Exception {
        ExprNode expr = ParserFactory.parseExpr("1 + 2 * 3");
        ExprNode expected = ast.add(ast.integer(1), ast.mul(ast.integer(2), ast.integer(3)));
        assertEquals(expected, expr);
    }

    @Test
    void testAddLeftAssociative() throws Exception {
        ExprNode expr = ParserFactory.parseExpr("1 + 2 + 3");
        ExprNode expected = ast.add(ast.add(ast.integer(1), ast.integer(2)), ast.integer(3));
        assertEquals(expected, expr);
    }

    @Test
    void testAddMulParenthesis() throws Exception {
        ExprNode expr = ParserFactory.parseExpr("(1 + 2) * 3");
        ExprNode expected = ast.mul(ast.integer(1), ast.mul(ast.integer(2), ast.integer(3)));
        assertEquals(expected, expr);
    }

}
