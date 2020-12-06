package cyr7.parser.xi;

import cyr7.C;
import cyr7.ast.ASTFactory;
import cyr7.ast.Node;
import cyr7.ast.stmt.*;
import cyr7.exceptions.parser.ParserException;
import cyr7.exceptions.parser.UnexpectedTokenException;
import cyr7.parser.util.ParserFactory;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestVariableDeclarationStatement {

    private final ASTFactory ast = new ASTFactory(C.LOC);

    @Test
    void testVarInitStmtNode() throws Exception {
        StmtNode statement =
            ParserFactory.parseStatement("a: int = x").get(0);
        Node expected = ast.varInit(
                ast.varDecl("a", ast.intType()),
                ast.variable("x")
        );
        assertEquals(expected, statement);

        statement = ParserFactory.parseStatement("b: int[][] = x").get(0);
        expected = ast.varInit(
                ast.varDecl("b", ast.arrayType(ast.arrayType(ast.intType()))),
                ast.variable("x")
        );
        assertEquals(expected, statement);

        statement = ParserFactory.parseStatement("i: int = c[3];").get(0);
        expected = ast.varInit(
                ast.varDecl("i", ast.intType()),
                ast.arrayAccess(ast.variable("c"), ast.integer(3))
        );
        assertEquals(expected, statement);
    }

    @Test
    void testVarDeclStmtNode() throws Exception {
        StmtNode statement = ParserFactory.parseStatement("z: int").get(0);
        Node expected = ast.varDecl("z", ast.intType());
        assertEquals(expected, statement);

        statement = ParserFactory.parseStatement("c: bool[][]").get(0);
        expected = ast.varDecl("c", ast.arrayType(ast.arrayType(ast.boolType())));
        assertEquals(expected, statement);
    }

    @Test
    void testSingleVariableDeclaration() throws Exception {
    }

    @Test
    void testSingleVarDeclarationsInvalidExamples() {
        assertThrows(UnexpectedTokenException.class,
            () -> ParserFactory.parseStatement("_"),
            "Single var decl statements cannot be empty"
        );

        assertThrows(UnexpectedTokenException.class,
            () -> ParserFactory.parseStatement("a: (int[][], bool)"),
            "Invalid type declaration"
        );
    }

    @Test
    void testMultiAssignDeclarations() throws Exception {
        StmtNode statement = ParserFactory.parseStatement("a: int, b: bool = function()").get(0);
        Node expected = ast.multiAssign(
                List.of(
                        Optional.of(ast.varDecl("a", ast.intType())),
                        Optional.of(ast.varDecl("b", ast.boolType()))
                ),
                ast.call("function")
        );
        assertEquals(expected, statement);

        statement = ParserFactory.parseStatement("a: int, _ = f()").get(0);
        expected = ast.multiAssign(
                List.of(
                        Optional.of(ast.varDecl("a", ast.intType())),
                        Optional.empty()
                ),
                ast.call("f")
        );
        assertEquals(expected, statement);

        statement =
            ParserFactory.parseStatement("_, _ = f()").get(0);
        expected = ast.multiAssign(
                List.of(
                        Optional.empty(),
                        Optional.empty()
                ),
                ast.call("f")
        );
        assertEquals(expected, statement);
    }

    @Test
    void testMultiVarDeclarationsInvalidExamples() {
        assertThrows(UnexpectedTokenException.class,
            () -> ParserFactory.parseStatement("_, _"),
            "Multi var decl statements must have a initialization expr"
        );

        assertThrows(UnexpectedTokenException.class,
            () -> ParserFactory.parseStatement("_, _ = x"),
            "Multi var decl statements must have a function call expr"
        );

        assertThrows(UnexpectedTokenException.class,
            () -> ParserFactory.parseStatement("a: int[5][6], b: bool[5][7]"),
            "Multi var decl statements cannot declare arrays"
        );
    }

    @Test
    void testArrayVarDeclarations() throws Exception {
        StmtNode statement =
            ParserFactory.parseStatement("a: int[5]").get(0);
        Node expected = ast.arrayDecl("a", ast.arrayType(ast.intType(), ast.integer(5)));
        assertEquals(expected, statement);

        statement = ParserFactory.parseStatement("a: int[5][]").get(0);
        expected = ast.arrayDecl("a",
                ast.arrayType(
                        ast.arrayType(ast.intType()),
                        ast.integer(5)
                )
        );
        assertEquals(expected, statement);

        statement = ParserFactory.parseStatement("a: int[5 + i][4 + j]").get(0);
        expected = ast.arrayDecl("a",
                ast.arrayType(
                        ast.arrayType(
                                ast.intType(),
                                ast.add(ast.integer(4), ast.variable("j"))
                        ),
                        ast.add(ast.integer(5), ast.variable("i"))
                )
        );
        assertEquals(expected, statement);
    }

    @Test
    void testArrayVarDeclarationsInvalidExamples() {
        assertThrows(ParserException.class,
            () -> ParserFactory.parseStatement("a: bool[][5]"));

        assertThrows(ParserException.class,
            () -> ParserFactory.parseStatement("a: int[][5][]"));

        assertThrows(ParserException.class,
            () -> ParserFactory.parseStatement("a: int[x][][5]"));

        assertThrows(ParserException.class,
            () -> ParserFactory.parseStatement("a: [x][][5]"));

        assertThrows(ParserException.class,
            () -> ParserFactory.parseStatement("a: [x][][] = j"));

        assertThrows(ParserException.class,
            () -> ParserFactory.parseStatement("a: [x][][5] = j"));
    }

}
