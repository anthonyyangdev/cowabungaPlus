package cyr7.parser.xi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cyr7.C;
import cyr7.ast.ASTFactory;
import org.junit.jupiter.api.Test;

import cyr7.ast.expr.ExprNode;
import cyr7.exceptions.lexer.LexerIntegerOverflowException;
import cyr7.exceptions.parser.ParserIntegerOverflowException;
import cyr7.parser.ParserUtil;
import cyr7.parser.util.ParserFactory;

class TestExpr {

    private final ASTFactory ast = new ASTFactory(C.LOC);

    @Test
    void testIntOperations() throws Exception {
        String expr = "-1 + 1";
        ExprNode parsed = ParserFactory.parseExpr(expr);
        ExprNode expected = ast.add(ast.negateNumber(ast.integer("1")), ast.integer("1"));
        assertEquals(parsed, expected);

        expr = "1 + 2 + 3";
        parsed = ParserFactory.parseExpr(expr);
        expected = ast.add(ast.add(ast.integer("1"), ast.integer("2")), ast.integer("3"));
        assertEquals(parsed, expected);

        expr = "1 + (2 + 3)";
        parsed = ParserFactory.parseExpr(expr);
        expected = ast.add(ast.integer("1"), ast.add(ast.integer("2"), ast.integer("3")));
        assertEquals(parsed, expected);

        expr = "1 / (2 * 3 / 4)";
        parsed = ParserFactory.parseExpr(expr);
        expected = ast.div(
                ast.integer("1"),
                ast.div(ast.mul(ast.integer("2"), ast.integer("3")), ast.integer("4"))
        );
        assertEquals(parsed, expected);

        expr = "1 *>> 2 + 3 / 4";
        parsed = ParserFactory.parseExpr(expr);
        expected = ast.add(ast.highMul(ast.integer("1"), ast.integer("2")),
                ast.div(ast.integer("3"), ast.integer("4")));
        assertEquals(parsed, expected);

        expr = "0 - -3 / 4 % 5";
        parsed = ParserFactory.parseExpr(expr);
        expected = ast.sub(
                ast.integer("0"),
                ast.rem(
                        ast.div(ast.negateNumber(ast.integer("3")), ast.integer("4")),
                        ast.integer("5"))
        );
        assertEquals(parsed, expected);
    }


    @Test
    void testIntComparators() throws Exception {
        String expr = "-1 > 1";
        ExprNode parsed = ParserFactory.parseExpr(expr);
        ExprNode expected = ast.gt(ast.negateNumber(ast.integer("1")), ast.integer("1"));
        assertEquals(parsed, expected);


        expr = "-2 < 1 + 1";
        parsed = ParserFactory.parseExpr(expr);
        expected = ast.lt(ast.negateNumber(ast.integer("2")),
                ast.add(ast.integer("1"), ast.integer("1")));
        assertEquals(parsed, expected);

        expr = "2 + function() >= 1 % 1";
        parsed = ParserFactory.parseExpr(expr);
        expected = ast.gte(ast.add(ast.integer("2"), ast.call("function")),
                ast.rem(ast.integer("1"), ast.integer("1")));
        assertEquals(parsed, expected);

        expr = "(4 *>> 6) <= 1 + 1";
        parsed = ParserFactory.parseExpr(expr);
        expected = ast.lte(ast.highMul(ast.integer("4"), ast.integer("6")),
                ast.add(ast.integer("1"), ast.integer("1")));
        assertEquals(parsed, expected);

        expr = "3 != hello()";
        parsed = ParserFactory.parseExpr(expr);
        expected = ast.neq(ast.integer("3"), ast.call("hello"));
        assertEquals(parsed, expected);

        expr = "-4 == hello()";
        parsed = ParserFactory.parseExpr(expr);
        expected = ast.eq(ast.negateNumber(ast.integer("4")), ast.call("hello"));
        assertEquals(parsed, expected);
    }

    @Test
    void testBoolOperations() throws Exception {
        String expr = "true | false & true";
        ExprNode parsed = ParserFactory.parseExpr(expr);
        ExprNode expected = ast.or(
                ast.bool(true),
                ast.and(ast.bool(false), ast.bool(true)));
        assertEquals(parsed, expected);

        expr = "true | false | true";
        parsed = ParserFactory.parseExpr(expr);
        expected = ast.or(ast.or(ast.bool(true), ast.bool(false)), ast.bool(true));
        assertEquals(parsed, expected);

        expr = "(true | false) & true";
        parsed = ParserFactory.parseExpr(expr);
        expected = ast.and(ast.or(ast.bool(true), ast.bool(false)), ast.bool(true));
        assertEquals(parsed, expected);
    }

    @Test
    void testBoolComparators() throws Exception {
        String expr = "true == false & true";
        ExprNode parsed = ParserFactory.parseExpr(expr);
        ExprNode expected = ast.and(ast.eq(ast.bool(true), ast.bool(false)), ast.bool(true));
        assertEquals(parsed, expected);

        expr = "true | false != true";
        parsed = ParserFactory.parseExpr(expr);
        expected = ast.or(ast.bool(true), ast.neq(ast.bool(false), ast.bool(true)));
        assertEquals(parsed, expected);

        expr = "(true | false) != !true";
        parsed = ParserFactory.parseExpr(expr);
        expected = ast.neq(ast.or(ast.bool(true), ast.bool(false)), ast.negateBool(ast.bool(true)));
        assertEquals(parsed, expected);

        expr = "!!!true";
        parsed = ParserFactory.parseExpr(expr);
        expected = ast.negateBool(ast.negateBool(ast.negateBool(ast.bool(true))));
        assertEquals(parsed, expected);
    }

    @Test
    void testVariables() throws Exception {
        String expr = "a + b";
        ExprNode parsed = ParserFactory.parseExpr(expr);
        ExprNode expected = ast.add(ast.variable("a"), ast.variable("b"));
        assertEquals(parsed, expected);

        expr = "a[2][3][4]";
        parsed = ParserFactory.parseExpr(expr);
        expected = ast.arrayAccess(
                        ast.arrayAccess(
                                ast.arrayAccess(
                                        ast.variable("a"),
                                        ast.integer("2")
                                ),
                                ast.integer("3")
                        ),
                        ast.integer("4")
                );
        assertEquals(parsed, expected);

        expr = "abcdefghij[2+2]";
        parsed = ParserFactory.parseExpr(expr);
        expected = ast.arrayAccess(
                ast.variable("abcdefghij"),
                ast.add(ast.integer("2"), ast.integer("2"))
        );
        assertEquals(parsed, expected);

        expr = "length(a[2+b])";
        parsed = ParserFactory.parseExpr(expr);
        ExprNode param = ast.arrayAccess(
                ast.variable("a"),
                ast.add(ast.integer("2"), ast.variable("b"))
        );
        expected = ast.length(param);
        assertEquals(parsed, expected);
    }

    @Test
    void testFunctionCalls() throws Exception {
        String expr = "hello('a', b, 3, a[4], \"hello\")";
        ExprNode parsed = ParserFactory.parseExpr(expr);
        ExprNode expected = ast.call("hello",
                ast.character("a"),
                ast.variable("b"),
                ast.integer("3"),
                ast.arrayAccess(ast.variable("a"), ast.integer("4")),
                ast.string("hello")
        );
        assertEquals(parsed, expected);

        expr = "a(b + 4) + b[a] *>> a[b+a]";
        parsed = ParserFactory.parseExpr(expr);

        var left = ast.call("a", ast.add(ast.variable("b"), ast.integer("4")));
        var right = ast.highMul(
                ast.arrayAccess(ast.variable("b"), ast.variable("a")),
                ast.arrayAccess(ast.variable("a"), ast.add(ast.variable("b"), ast.variable("a"))));
        expected = ast.add(left, right);
        ParserUtil.printSExpr(parsed);
        ParserUtil.printSExpr(expected);
        assertEquals(parsed, expected);
    }


    @Test
    void testMisc() throws Exception {
        String expr = "{1,2,3,4,5}";
        ExprNode parsed = ParserFactory.parseExpr(expr);
        ExprNode expected = ast.array(
                ast.integer("1"),
                ast.integer("2"),
                ast.integer("3"),
                ast.integer("4"),
                ast.integer("5")
        );
        assertEquals(parsed, expected);
    }

    @Test
    void testMaxInt() throws Exception {
        String expr = "9223372036854775807";
        ExprNode parsed = ParserFactory.parseExpr(expr);
        ExprNode expected = ast.integer("9223372036854775807");
        assertEquals(parsed, expected);

        expr = "-9223372036854775808";
        parsed = ParserFactory.parseExpr(expr);
        expected = ast.negateNumber(ast.integer("9223372036854775808"));
        assertEquals(parsed, expected);

        final String largeExpr = "9223372036854775808";
        assertThrows(ParserIntegerOverflowException.class, () ->
            ParserFactory.parseExpr(largeExpr)
        );

        final String largeNegInt = "-9223372036854775809";
        assertThrows(LexerIntegerOverflowException.class, () ->
            ParserFactory.parseExpr(largeNegInt)
        );

        final String veryLargeInt = "99129428931919223372036854775809";
        assertThrows(LexerIntegerOverflowException.class, () ->
            ParserFactory.parseExpr(veryLargeInt)
        );

        final String veryLargeNegInt = "-99129428931919223372036854775809";
        assertThrows(LexerIntegerOverflowException.class, () ->
            ParserFactory.parseExpr(veryLargeNegInt)
        );
    }
}
