package cyr7.typecheck;

import cyr7.C;
import cyr7.ast.ASTFactory;
import cyr7.ast.Node;
import cyr7.ast.stmt.*;
import cyr7.exceptions.semantics.SemanticException;
import cyr7.semantics.context.Context;
import cyr7.semantics.types.ExpandedType;
import cyr7.semantics.types.FunctionType;
import cyr7.semantics.context.HashMapStackContext;
import cyr7.semantics.types.PrimitiveType;
import cyr7.semantics.types.ResultType;
import java_cup.runtime.ComplexSymbolFactory.Location;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TestTypeCheckVisitorStmt {

    Context context;
    TypeCheckVisitor visitor;
    Node node;
    TypeCheckVisitor.Result result;
    private final ASTFactory ast = new ASTFactory(C.LOC);
    Location loc = new Location(0, 0);

    @Test
    void testExprStmtNode() {
        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);

        visitor.context.addFn("bool_of_int", new FunctionType(ExpandedType.intType,
                                                ExpandedType.boolType));
        node = ast.exprStmt(ast.call("bool_of_int", ast.integer(1000)));
        result = node.accept(visitor);
        assertEquals(ResultType.UNIT, result.assertSecond());


        visitor.context.addFn("sleep", new FunctionType(ExpandedType.intType,
                ExpandedType.unitExpandedType));
        node = ast.exprStmt(ast.call("sleep", ast.integer(1000)));
        assertThrows(SemanticException.class, () -> node.accept(visitor));


        visitor.context.addFn("factorize", new FunctionType(ExpandedType.intType,
                new ExpandedType(
                        List.of(PrimitiveType.intDefault, PrimitiveType.intDefault))));
        node = ast.exprStmt(ast.call("factorize", ast.integer(1000)));
        assertThrows(SemanticException.class, () -> node.accept(visitor));

    }


    @Test
    void testVarInit() {
        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);

        node = ast.varInit(ast.varDecl("alias", ast.intType()), ast.integer(123));
        result = node.accept(visitor);
        assertEquals(ResultType.UNIT, result.assertSecond());


        // Assigning Mismatching types
        node = ast.varInit(ast.varDecl("alias2", ast.intType()), ast.bool(false));
        assertThrows(SemanticException.class, () -> node.accept(visitor));


        // Redeclaring variable names is bad
        node = ast.varInit(ast.varDecl("alias", ast.boolType()), ast.bool(false));
        assertThrows(SemanticException.class, () -> node.accept(visitor));

    }


    @Test
    void testAssignmentStmtNode() {
        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        visitor.context.addVar("cash", PrimitiveType.intDefault);

        node = ast.assign(ast.variable("cash"), ast.integer(123));
        result = node.accept(visitor);
        assertEquals(ResultType.UNIT, result.assertSecond());


        // Mismatched types
        node = ast.assign(ast.variable("cash"), ast.bool(false));
        assertThrows(SemanticException.class, () -> node.accept(visitor));



        // Attempting to assign to undeclared variable.
        node = ast.assign(ast.variable("undeclaredValue"), ast.string("Anything"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));
    }




    @Test
    void testMultiAssignmentStmtNode() {
        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        visitor.context.addFn("generate", new FunctionType(
                ExpandedType.unitExpandedType,
                new ExpandedType(
                        List.of(PrimitiveType.intDefault, PrimitiveType.boolDefault))));


        node = ast.multiAssign(List.of(
                Optional.of(ast.varDecl("apple", ast.intType())),
                Optional.of(ast.varDecl("oranges", ast.boolType()))
        ), ast.call("generate"));
        result = node.accept(visitor);
        assertEquals(ResultType.UNIT, result.assertSecond());


        node = ast.multiAssign(List.of(
                Optional.of(ast.varDecl("banana", ast.intType())),
                Optional.empty()
        ), ast.call("generate"));
        result = node.accept(visitor);
        assertEquals(ResultType.UNIT, result.assertSecond());


        node = ast.multiAssign(List.of(
                Optional.empty(),
                Optional.empty()
        ), ast.call("generate"));
        result = node.accept(visitor);
        assertEquals(ResultType.UNIT, result.assertSecond());


        // Duplicate Assignment
        node = ast.multiAssign(List.of(
                Optional.of(ast.varDecl("apple", ast.intType())),
                Optional.of(ast.varDecl("apple", ast.boolType()))
        ), ast.call("generate"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));



        // Mismatched types
        node = ast.multiAssign(List.of(
                Optional.of(ast.varDecl("gorilla", ast.intType())),
                Optional.empty(),
                Optional.empty()
        ), ast.call("generate"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));

    }


    @Test
    void testVarDeclStmtNode() {
        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);

        node = ast.varDeclStmt(ast.varDecl("testVar", ast.intType()));
        result = node.accept(visitor);
        assertEquals(ResultType.UNIT, result.assertSecond());



        // Redeclare Variables
        node = ast.varDeclStmt(ast.varDecl("testVar", ast.boolType()));
        assertThrows(SemanticException.class, () -> node.accept(visitor));
        node = ast.varDeclStmt(ast.varDecl("testVar", ast.intType()));
        assertThrows(SemanticException.class, () -> node.accept(visitor));

    }



    @Test
    void testArrayDeclStmtNode() {
        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);

        node = ast.arrayDecl("arr", ast.arrayType(ast.intType()));
        result = node.accept(visitor);
        assertEquals(ResultType.UNIT, result.assertSecond());

        node = ast.arrayDecl("arrSized", ast.arrayType(ast.intType(), ast.integer(12)));
        result = node.accept(visitor);
        assertEquals(ResultType.UNIT, result.assertSecond());

        // Using Strings as array size.
        node = ast.arrayDecl("arrSized", ast.arrayType(ast.intType(), ast.string("num")));
        assertThrows(SemanticException.class, () -> node.accept(visitor));


        // Redeclare Variables
        node = ast.arrayDecl("arr", ast.arrayType(ast.intType()));
        assertThrows(SemanticException.class, () -> node.accept(visitor));
        node = ast.varDeclStmt(ast.varDecl("arr", ast.boolType()));
        assertThrows(SemanticException.class, () -> node.accept(visitor));
    }


    @Test
    void testProcedureCall() {
        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        visitor.context.addFn("foo", new FunctionType(ExpandedType.unitExpandedType,
                ExpandedType.unitExpandedType));

        node = ast.procedure(ast.call("foo"));
        result = node.accept(visitor);
        assertEquals(ResultType.UNIT, result.assertSecond());



        visitor.context.addFn("bar", new FunctionType(ExpandedType.unitExpandedType,
                ExpandedType.intType));
        node = ast.procedure(ast.call("bar"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));


        visitor.context.addFn("python", new FunctionType(ExpandedType.unitExpandedType,
                new ExpandedType(List.of(PrimitiveType.intDefault,
                        PrimitiveType.boolDefault))));
        node = ast.procedure(ast.call("python"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));
    }


    @Test
    void testReturnStmt() {
        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        visitor.context.addRet(ExpandedType.unitExpandedType);
        node = ast.returnStmt();
        result = node.accept(visitor);
        assertEquals(ResultType.VOID, result.assertSecond());


        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        visitor.context.addRet(ExpandedType.intType);
        node = ast.returnStmt(ast.integer(0));
        result = node.accept(visitor);
        assertEquals(ResultType.VOID, result.assertSecond());


        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        visitor.context.addRet(new ExpandedType(List.of(
                PrimitiveType.intDefault, PrimitiveType.boolDefault
                )));
        node = ast.returnStmt(ast.integer(0), ast.bool(false));
        result = node.accept(visitor);
        assertEquals(ResultType.VOID, result.assertSecond());



        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        visitor.context.addRet(ExpandedType.unitExpandedType);
        node = ast.returnStmt(ast.integer(0));
        assertThrows(SemanticException.class, () -> node.accept(visitor));


        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        visitor.context.addRet(ExpandedType.intType);
        node = ast.returnStmt();
        assertThrows(SemanticException.class, () -> node.accept(visitor));


        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        visitor.context.addRet(new ExpandedType(List.of(
                PrimitiveType.intDefault, PrimitiveType.boolDefault
                )));
        node = ast.returnStmt(ast.integer(0));
        assertThrows(SemanticException.class, () -> node.accept(visitor));
    }


    @Test
    void testIfElseStmtNode() {
        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);


        /*
         * if (true)
         *    apple: int;
         * else
         *    apple: bool;
         */
        node = ast.ifElse(ast.bool(true),
                ast.varDeclStmt(ast.varDecl("apple", ast.intType())),
                ast.varDeclStmt(ast.varDecl("apple", ast.boolType())));
        result = node.accept(visitor);
        assertEquals(ResultType.UNIT, result.assertSecond());


        /*
         * if (true) {
         *    return;
         * } else
         *    apple: bool;
         */
        visitor.context.addRet(ExpandedType.unitExpandedType);
        node = ast.ifElse(ast.bool(true),
                ast.returnStmt(),
                ast.varDeclStmt(ast.varDecl("apple", ast.boolType())));
        result = node.accept(visitor);
        assertEquals(ResultType.UNIT, result.assertSecond());


        /*
         * if (true)
         *    apple: int;
         */
        visitor.context.addRet(ExpandedType.unitExpandedType);
        node = ast.ifElse(ast.bool(true),
                ast.varDeclStmt(ast.varDecl("apple", ast.intType())));
        result = node.accept(visitor);
        assertEquals(ResultType.UNIT, result.assertSecond());


        /*
         * if (true) {
         *    return;
         * } else {
         *    return;
         * }
         */
        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        visitor.context.addRet(ExpandedType.unitExpandedType);
        node = ast.ifElse(ast.bool(true),
                ast.block(ast.returnStmt()),
                ast.block(ast.returnStmt()));
        result = node.accept(visitor);
        assertEquals(ResultType.VOID, result.assertSecond());


        /*
         * if (true) {
         *    return;
         * }
         */
        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        visitor.context.addRet(ExpandedType.unitExpandedType);
        node = ast.ifElse(ast.bool(true), ast.returnStmt());
        result = node.accept(visitor);
        assertEquals(ResultType.UNIT, result.assertSecond());



        /*
         * if ({}) {
         *    return 123;
         * } else {
         *    return;
         * }
         */
        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        visitor.context.addRet(ExpandedType.unitExpandedType);
        node = ast.ifElse(ast.array(),
                ast.returnStmt(ast.integer(123)), ast.returnStmt());
        assertThrows(SemanticException.class, () -> node.accept(visitor));


        /*
         * if ({}[0]) {
         *    return;
         * } else {
         *    return;
         * }
         */
        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        visitor.context.addRet(ExpandedType.unitExpandedType);
        node = ast.ifElse(ast.arrayAccess(ast.array(), ast.integer(0)),
                ast.block(ast.returnStmt()),
                ast.block(ast.returnStmt()));
        result = node.accept(visitor);
        assertEquals(ResultType.VOID, result.assertSecond());






        /*
         * if ({}[0]) {
         *    return 123;
         * }
         */
        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        visitor.context.addRet(ExpandedType.intType);
        node = ast.ifElse(ast.arrayAccess(ast.array(), ast.integer(0)),
                ast.block(ast.returnStmt(ast.integer(123))));
        result = node.accept(visitor);
        assertEquals(ResultType.UNIT, result.assertSecond());





        /*
         * if (121)
         *    apple: int;
         * else
         *    apple: bool;
         */
        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        node = ast.ifElse(ast.integer(123),
                ast.varDeclStmt(ast.varDecl("apple", ast.intType())),
                ast.varDeclStmt(ast.varDecl("apple", ast.boolType())));
        assertThrows(SemanticException.class, () -> node.accept(visitor));


        /*
         * if (true) {
         *    return 123;
         * } else {
         *    return;
         * }
         */
        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        visitor.context.addRet(ExpandedType.unitExpandedType);
        node = ast.ifElse(ast.bool(true),
                ast.block(ast.returnStmt(ast.integer(123))),
                ast.block(ast.returnStmt()));
        assertThrows(SemanticException.class, () -> node.accept(visitor));




    }

    @Test
    void testWhileStmtNode() {
        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);


        /*
         * while (true)
         *    apple: int;
         */
        node = ast.whileLoop(ast.bool(true), ast.varDeclStmt(ast.varDecl("apple", ast.intType())));
        result = node.accept(visitor);
        assertEquals(ResultType.UNIT, result.assertSecond());



        /*
         * while (true) {
         *    return;
         * }
         */
        visitor.context.addRet(ExpandedType.unitExpandedType);
        node = ast.whileLoop(ast.bool(true), ast.block(ast.returnStmt()));
        result = node.accept(visitor);
        assertEquals(ResultType.UNIT, result.assertSecond());


        /*
         * while (true)
         *    apple: int;
         */
        visitor.context.addRet(ExpandedType.unitExpandedType);
        node = ast.whileLoop(ast.bool(true), ast.varDeclStmt(ast.varDecl("apple", ast.intType())));
        result = node.accept(visitor);
        assertEquals(ResultType.UNIT, result.assertSecond());


        /*
         * while ({}) {
         *    return 123;
         * }
         */
        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        visitor.context.addRet(ExpandedType.unitExpandedType);
        node = ast.whileLoop(ast.array(), ast.block(ast.returnStmt(ast.integer(123))));
        assertThrows(SemanticException.class, () -> node.accept(visitor));



        /*
         * while (121)
         *    apple: int;
         */
        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        node = ast.whileLoop(ast.integer(123), ast.varDeclStmt(ast.varDecl("apple", ast.intType())));
        assertThrows(SemanticException.class, () -> node.accept(visitor));


        /*
         * while (true) {
         *    return 123;
         * }
         */
        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        visitor.context.addRet(ExpandedType.unitExpandedType);
        node = ast.whileLoop(ast.bool(true), ast.block(ast.returnStmt(ast.integer(123))));
        assertThrows(SemanticException.class, () -> node.accept(visitor));


    }


    @Test
    void testBlockStmt() {
        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);

        node = new BlockStmtNode(loc, List.of());
        result = node.accept(visitor);
        assertEquals(ResultType.UNIT, result.assertSecond());


        node = ast.block(ast.varDeclStmt(ast.varDecl("ringo", ast.intType())));
        result = node.accept(visitor);
        assertEquals(ResultType.UNIT, result.assertSecond());


        visitor.context.addRet(ExpandedType.unitExpandedType);
        node = ast.block(ast.returnStmt());
        result = node.accept(visitor);
        assertEquals(ResultType.VOID, result.assertSecond());


        visitor.context.addRet(ExpandedType.unitExpandedType);
        node = ast.block(ast.returnStmt(), ast.varDeclStmt(ast.varDecl("ringo", ast.intType())));
        assertThrows(SemanticException.class, () -> node.accept(visitor));
    }


}
