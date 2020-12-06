package cyr7.parser.xi;

import cyr7.C;
import cyr7.ast.ASTFactory;
import cyr7.ast.expr.ExprNode;
import cyr7.parser.util.ParserFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestArrayAccessExpr {

    private final ASTFactory ast = new ASTFactory(C.LOC);

    @Test
    void testFunctionCallArrayAccess() throws Exception {
        ExprNode parsed = ParserFactory.parseExpr("f()[0]");
        ExprNode expected = ast.arrayAccess(ast.call("f"), ast.integer(0));
        assertEquals(expected, parsed);
    }

    @Test
    void testArrayAccessPrecedence() throws Exception {
        ExprNode parsed = ParserFactory.parseExpr("5 + a[0]");
        ExprNode expected = ast.add(ast.integer(5), ast.arrayAccess(ast.variable("a"), ast.integer(0)));
        assertEquals(expected, parsed);

        parsed = ParserFactory.parseExpr("5 * a[0]");
        expected = ast.mul(ast.integer(5), ast.arrayAccess(ast.variable("a"), ast.integer(0)));
        assertEquals(expected, parsed);

        parsed = ParserFactory.parseExpr("5 + -a[0]");
        expected = ast.add(ast.integer(5),
                ast.negateNumber(ast.arrayAccess(ast.variable("a"), ast.integer(0))));
        assertEquals(expected, parsed);
    }

    @Test
    void testArrayLiteralAccess() throws Exception {
        ExprNode parsed = ParserFactory.parseExpr("{1, 3, 5,}[7]");
        ExprNode expected = ast.arrayAccess(
                ast.array(ast.integer(1), ast.integer(3), ast.integer(5)),
                ast.integer(7));
        assertEquals(expected, parsed);

        parsed = ParserFactory.parseExpr(
            "{ {f()}[h()], {1}[0], {1,2}[2]} [g()]");
        expected = ast.arrayAccess(
                ast.array(
                        ast.arrayAccess(ast.array(ast.call("f")), ast.call("h")),
                        ast.arrayAccess(ast.array(ast.integer(1)), ast.integer(0)),
                        ast.arrayAccess(ast.array(ast.integer(1), ast.integer(2)), ast.integer(2))
                ), ast.call("g"));
        assertEquals(expected, parsed);
    }

}
