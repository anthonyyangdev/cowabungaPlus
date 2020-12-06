package cyr7.typecheck;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import cyr7.C;
import cyr7.ast.ASTFactory;
import org.junit.jupiter.api.Test;

import cyr7.ast.Node;
import cyr7.ast.expr.FunctionCallExprNode;
import cyr7.ast.expr.access.ArrayAccessExprNode;
import cyr7.ast.expr.access.VariableAccessExprNode;
import cyr7.ast.expr.binexpr.GTEExprNode;
import cyr7.ast.expr.binexpr.GTExprNode;
import cyr7.ast.expr.binexpr.LTEExprNode;
import cyr7.ast.expr.binexpr.LTExprNode;
import cyr7.ast.expr.literalexpr.LiteralArrayExprNode;
import cyr7.ast.expr.literalexpr.LiteralBoolExprNode;
import cyr7.ast.expr.literalexpr.LiteralCharExprNode;
import cyr7.ast.expr.literalexpr.LiteralIntExprNode;
import cyr7.ast.expr.literalexpr.LiteralStringExprNode;
import cyr7.exceptions.semantics.SemanticException;
import cyr7.semantics.types.ArrayType;
import cyr7.semantics.context.Context;
import cyr7.semantics.types.ExpandedType;
import cyr7.semantics.types.FunctionType;
import cyr7.semantics.context.HashMapStackContext;
import cyr7.semantics.types.PrimitiveType;
import java_cup.runtime.ComplexSymbolFactory.Location;

class TestTypeCheckVisitorExpr {

    Context context;
    TypeCheckVisitor visitor;
    Node node;
    TypeCheckVisitor.Result result;
    Location loc = new Location(0, 0);
    IxiFileOpener opener = null;
    private ASTFactory ast = new ASTFactory(C.LOC);

    @Test
    void testIntNegExprNode() {
        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        node = ast.negateNumber(ast.integer("13243546"));
        result = node.accept(visitor);
        assertTrue(result.assertFirst().isSubtypeOfInt());

        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        node = ast.negateNumber(ast.string("Hello World"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));

        node = ast.negateNumber(ast.bool(false));
        assertThrows(SemanticException.class, () -> node.accept(visitor));
    }

    @Test
    void testBoolNegExprNode() {
        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        node = ast.negateBool(ast.bool(true));
        result = node.accept(visitor);
        assertTrue(result.assertFirst().isSubtypeOfBool());

        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        node = ast.negateBool(ast.string("Hello World"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));
        node = ast.negateBool(ast.integer("13243546"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));
    }


    @Test
    void testLiterals() {
        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        node = ast.bool(false);
        result = node.accept(visitor);
        assertTrue(result.assertFirst().isSubtypeOfBool());
        assertTrue(result.assertFirst().isOrdinary());

        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        node = ast.character("a");
        result = node.accept(visitor);
        assertTrue(result.assertFirst().isSubtypeOfInt());
        assertTrue(result.assertFirst().isOrdinary());

        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        node = ast.integer("1324");
        result = node.accept(visitor);
        assertTrue(result.assertFirst().isSubtypeOfInt());
        assertTrue(result.assertFirst().isOrdinary());

        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        node = ast.string("Hello World");
        result = node.accept(visitor);
        assertTrue(result.assertFirst().isArray());
        assertTrue(result.assertFirst().isASubtypeOf(
                new ExpandedType(new ArrayType(PrimitiveType.intDefault))));
        assertTrue(result.assertFirst().isOrdinary());
    }


    @Test
    void testAddExpr() {
        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        node = ast.add(ast.integer("1234"), ast.integer("1324"));
        result = node.accept(visitor);
        assertTrue(result.assertFirst().isSubtypeOfInt());
        assertTrue(result.assertFirst().isOrdinary());


        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        node = ast.add(ast.character("a"), ast.character("b"));
        result = node.accept(visitor);
        assertTrue(result.assertFirst().isSubtypeOfInt());
        assertTrue(result.assertFirst().isOrdinary());

        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        node = ast.add(ast.integer("134"), ast.character("b"));
        result = node.accept(visitor);
        assertTrue(result.assertFirst().isSubtypeOfInt());
        assertTrue(result.assertFirst().isOrdinary());

        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        node = ast.add(ast.string("1324"), ast.string("1324"));
        result = node.accept(visitor);
        assertTrue(result.assertFirst().isArray());
        assertTrue(result.assertFirst().isASubtypeOf(
                new ExpandedType(new ArrayType(PrimitiveType.intDefault))));
        assertTrue(result.assertFirst().isOrdinary());


        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        visitor.context.addVar("numbers", new ArrayType(PrimitiveType.intDefault));
        node = ast.add(ast.variable("numbers"), ast.string("1324"));
        result = node.accept(visitor);
        assertTrue(result.assertFirst().isArray());
        assertTrue(result.assertFirst().isASubtypeOf(
                new ExpandedType(new ArrayType(PrimitiveType.intDefault))));
        assertTrue(result.assertFirst().isOrdinary());


        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        node = ast.add(ast.bool(false), ast.bool(false));
        assertThrows(SemanticException.class, () -> node.accept(visitor));

        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        node = ast.add(ast.bool(false), ast.integer("124"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));

        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        node = ast.add(ast.bool(false), ast.string("This is a string"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));

        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        node = ast.add(ast.integer("65432"), ast.string("This is a string"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));


        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        visitor.context.addVar("bools", new ArrayType(PrimitiveType.boolDefault));
        node = ast.add(ast.variable("bools"), ast.string("1324"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));
    }

    @Test
    void testEquality() {
        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        node = ast.eq(ast.integer("1324"), ast.integer("1324"));
        result = node.accept(visitor);
        assertTrue(result.assertFirst().isSubtypeOfBool());
        assertTrue(result.assertFirst().isOrdinary());


        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        node = ast.eq(ast.character("a"), ast.character("b"));
        result = node.accept(visitor);
        assertTrue(result.assertFirst().isSubtypeOfBool());
        assertTrue(result.assertFirst().isOrdinary());


        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        node = ast.eq(ast.integer("134"), ast.character("b"));
        result = node.accept(visitor);
        assertTrue(result.assertFirst().isSubtypeOfBool());
        assertTrue(result.assertFirst().isOrdinary());


        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        node = ast.eq(ast.string("1324"), ast.string("1324"));
        result = node.accept(visitor);
        assertTrue(result.assertFirst().isSubtypeOfBool());
        assertTrue(result.assertFirst().isOrdinary());


        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        visitor.context.addVar("numbers", new ArrayType(PrimitiveType.intDefault));
        node = ast.eq(ast.variable("numbers"), ast.string("1324"));
        result = node.accept(visitor);
        assertTrue(result.assertFirst().isSubtypeOfBool());
        assertTrue(result.assertFirst().isOrdinary());


        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        node = ast.eq(ast.bool(false), ast.bool(false));
        assertTrue(result.assertFirst().isSubtypeOfBool());
        assertTrue(result.assertFirst().isOrdinary());


        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        node = ast.eq(ast.bool(false), ast.integer("124"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));


        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        node = ast.eq(ast.bool(false), ast.string("This is a string"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));


        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        node = ast.eq(ast.integer("65432"), ast.string("This is a string"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));


        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        visitor.context.addVar("bools", new ArrayType(PrimitiveType.boolDefault));
        node = ast.eq(ast.variable("bools"), ast.string("1324"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));
    }


    @Test
    void testInequality() {
        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        node = ast.neq(ast.integer("1324"), ast.integer("1324"));
        result = node.accept(visitor);
        assertTrue(result.assertFirst().isSubtypeOfBool());
        assertTrue(result.assertFirst().isOrdinary());


        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        node = ast.neq(ast.character("a"), ast.character("b"));
        result = node.accept(visitor);
        assertTrue(result.assertFirst().isSubtypeOfBool());
        assertTrue(result.assertFirst().isOrdinary());


        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        node = ast.neq(ast.integer("134"), ast.character("b"));
        result = node.accept(visitor);
        assertTrue(result.assertFirst().isSubtypeOfBool());
        assertTrue(result.assertFirst().isOrdinary());


        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        node = ast.neq(ast.string("1324"), ast.string("1324"));
        result = node.accept(visitor);
        assertTrue(result.assertFirst().isSubtypeOfBool());
        assertTrue(result.assertFirst().isOrdinary());


        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        visitor.context.addVar("numbers", new ArrayType(PrimitiveType.intDefault));
        node = ast.neq(ast.variable("numbers"), ast.string("1324"));
        result = node.accept(visitor);
        assertTrue(result.assertFirst().isSubtypeOfBool());
        assertTrue(result.assertFirst().isOrdinary());


        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        node = ast.neq(ast.bool(false), ast.bool(false));
        assertTrue(result.assertFirst().isSubtypeOfBool());
        assertTrue(result.assertFirst().isOrdinary());


        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        node = ast.neq(ast.bool(false), ast.integer("124"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));


        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        node = ast.neq(ast.bool(false), ast.string("This is a string"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));


        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        node = ast.neq(ast.integer("65432"), ast.string("This is a string"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));


        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        visitor.context.addVar("bools", new ArrayType(PrimitiveType.boolDefault));
        node = ast.neq(ast.variable("bools"), ast.string("1324"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));
    }


    @Test
    void testIntegerBinaryExpr() {
        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        node = ast.highMul(ast.integer("1324"), ast.integer("1324"));
        result = node.accept(visitor);
        assertTrue(result.assertFirst().isSubtypeOfInt());
        assertTrue(result.assertFirst().isOrdinary());
        node = ast.mul(ast.integer("1324"), ast.integer("1324"));
        result = node.accept(visitor);
        assertTrue(result.assertFirst().isSubtypeOfInt());
        assertTrue(result.assertFirst().isOrdinary());
        node = ast.rem(ast.integer("1324"), ast.integer("1324"));
        result = node.accept(visitor);
        assertTrue(result.assertFirst().isSubtypeOfInt());
        assertTrue(result.assertFirst().isOrdinary());
        node = ast.sub(ast.integer("1324"), ast.integer("1324"));
        result = node.accept(visitor);
        assertTrue(result.assertFirst().isSubtypeOfInt());
        assertTrue(result.assertFirst().isOrdinary());
        node = ast.div(ast.integer("1324"), ast.integer("1324"));
        result = node.accept(visitor);
        assertTrue(result.assertFirst().isSubtypeOfInt());
        assertTrue(result.assertFirst().isOrdinary());


        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        node = ast.highMul(ast.character("a"), ast.character("b"));
        result = node.accept(visitor);
        assertTrue(result.assertFirst().isSubtypeOfInt());
        assertTrue(result.assertFirst().isOrdinary());
        node = ast.mul(ast.character("a"), ast.character("b"));
        result = node.accept(visitor);
        assertTrue(result.assertFirst().isSubtypeOfInt());
        assertTrue(result.assertFirst().isOrdinary());
        node = ast.rem(ast.character("a"), ast.character("b"));
        result = node.accept(visitor);
        assertTrue(result.assertFirst().isSubtypeOfInt());
        assertTrue(result.assertFirst().isOrdinary());
        node = ast.sub(ast.character("a"), ast.character("b"));
        result = node.accept(visitor);
        assertTrue(result.assertFirst().isSubtypeOfInt());
        assertTrue(result.assertFirst().isOrdinary());
        node = ast.div(ast.character("a"), ast.character("b"));
        result = node.accept(visitor);
        assertTrue(result.assertFirst().isSubtypeOfInt());
        assertTrue(result.assertFirst().isOrdinary());


        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        node = ast.highMul(ast.integer("134"), ast.character("b"));
        result = node.accept(visitor);
        assertTrue(result.assertFirst().isSubtypeOfInt());
        assertTrue(result.assertFirst().isOrdinary());
        node = ast.mul(ast.integer("134"), ast.character("b"));
        result = node.accept(visitor);
        assertTrue(result.assertFirst().isSubtypeOfInt());
        assertTrue(result.assertFirst().isOrdinary());
        node = ast.rem(ast.integer("134"), ast.character("b"));
        result = node.accept(visitor);
        assertTrue(result.assertFirst().isSubtypeOfInt());
        assertTrue(result.assertFirst().isOrdinary());
        node = ast.sub(ast.integer("134"), ast.character("b"));
        result = node.accept(visitor);
        assertTrue(result.assertFirst().isSubtypeOfInt());
        assertTrue(result.assertFirst().isOrdinary());
        node = ast.div(ast.integer("134"), ast.character("b"));
        result = node.accept(visitor);
        assertTrue(result.assertFirst().isSubtypeOfInt());
        assertTrue(result.assertFirst().isOrdinary());



        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        node = ast.highMul(ast.string("str"), ast.string("world"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));
        node = ast.mul(ast.string("str"), ast.string("world"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));
        node = ast.rem(ast.string("str"), ast.string("world"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));
        node = ast.sub(ast.string("str"), ast.string("world"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));
        node = ast.div(ast.string("str"), ast.string("world"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));


        context = new HashMapStackContext();
        visitor.context.addVar("numbers", new ArrayType(PrimitiveType.intDefault));
        visitor = new TypeCheckVisitor(null);
        node = ast.highMul(ast.variable("numbers"), ast.string("1324"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));
        node = ast.mul(ast.variable("numbers"), ast.string("1324"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));
        node = ast.rem(ast.variable("numbers"), ast.string("1324"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));
        node = ast.sub(ast.variable("numbers"), ast.string("1324"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));
        node = ast.div(ast.variable("numbers"), ast.string("1324"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));


        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        node = ast.highMul(ast.bool(false), ast.bool(false));
        assertThrows(SemanticException.class, () -> node.accept(visitor));
        node = ast.mul(ast.bool(false), ast.bool(false));
        assertThrows(SemanticException.class, () -> node.accept(visitor));
        node = ast.rem(ast.bool(false), ast.bool(false));
        assertThrows(SemanticException.class, () -> node.accept(visitor));
        node = ast.sub(ast.bool(false), ast.bool(false));
        assertThrows(SemanticException.class, () -> node.accept(visitor));
        node = ast.div(ast.bool(false), ast.bool(false));
        assertThrows(SemanticException.class, () -> node.accept(visitor));



        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        node = ast.highMul(ast.bool(false), ast.integer("124"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));
        node = ast.mul(ast.bool(false), ast.integer("124"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));
        node = ast.rem(ast.bool(false), ast.integer("124"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));
        node = ast.sub(ast.bool(false), ast.integer("124"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));
        node = ast.div(ast.bool(false), ast.integer("124"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));



        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        node = ast.highMul(ast.bool(false), ast.string("This is a string"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));
        node = ast.mul(ast.bool(false), ast.string("This is a string"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));
        node = ast.rem(ast.bool(false), ast.string("This is a string"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));
        node = ast.sub(ast.bool(false), ast.string("This is a string"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));
        node = ast.div(ast.bool(false), ast.string("This is a string"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));



        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        node = ast.highMul(ast.integer("65432"), ast.string("This is a string"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));
        node = ast.mul(ast.integer("65432"), ast.string("This is a string"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));
        node = ast.rem(ast.integer("65432"), ast.string("This is a string"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));
        node = ast.sub(ast.integer("65432"), ast.string("This is a string"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));
        node = ast.div(ast.integer("65432"), ast.string("This is a string"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));



        context = new HashMapStackContext();
        visitor.context.addVar("bools", new ArrayType(PrimitiveType.boolDefault));
        visitor = new TypeCheckVisitor(null);
        node = ast.highMul(ast.variable("bools"), ast.string("1324"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));
        node = ast.mul(ast.variable("bools"), ast.string("1324"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));
        node = ast.rem(ast.variable("bools"), ast.string("1324"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));
        node = ast.sub(ast.variable("bools"), ast.string("1324"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));
        node = ast.div(ast.variable("bools"), ast.string("1324"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));
    }


    @Test
    void testComparisonExpr() {
        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        node = ast.lte(ast.integer(1324), ast.integer(1324));
        result = node.accept(visitor);
        assertTrue(result.assertFirst().isSubtypeOfBool());
        assertTrue(result.assertFirst().isOrdinary());
        node = ast.lt(ast.integer(1324), ast.integer(1324));
        result = node.accept(visitor);
        assertTrue(result.assertFirst().isSubtypeOfBool());
        assertTrue(result.assertFirst().isOrdinary());
        node = ast.gte(ast.integer(1324), ast.integer(1324));
        result = node.accept(visitor);
        assertTrue(result.assertFirst().isSubtypeOfBool());
        assertTrue(result.assertFirst().isOrdinary());
        node = ast.gt(ast.integer(1324), ast.integer(1324));
        result = node.accept(visitor);
        assertTrue(result.assertFirst().isSubtypeOfBool());
        assertTrue(result.assertFirst().isOrdinary());



        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        node = ast.gte(ast.character('a'), ast.character('b'));
        result = node.accept(visitor);
        assertTrue(result.assertFirst().isSubtypeOfBool());
        assertTrue(result.assertFirst().isOrdinary());
        node = ast.gt(ast.character('a'), ast.character('b'));
        result = node.accept(visitor);
        assertTrue(result.assertFirst().isSubtypeOfBool());
        assertTrue(result.assertFirst().isOrdinary());
        node = ast.lte(ast.character('a'), ast.character('b'));
        result = node.accept(visitor);
        assertTrue(result.assertFirst().isSubtypeOfBool());
        assertTrue(result.assertFirst().isOrdinary());
        node = ast.lt(ast.character('a'), ast.character('b'));
        result = node.accept(visitor);
        assertTrue(result.assertFirst().isSubtypeOfBool());
        assertTrue(result.assertFirst().isOrdinary());



        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        node = ast.lte(ast.integer(134), ast.character("b"));
        result = node.accept(visitor);
        assertTrue(result.assertFirst().isSubtypeOfBool());
        assertTrue(result.assertFirst().isOrdinary());
        node = ast.lt(ast.integer(134), ast.character("b"));
        result = node.accept(visitor);
        assertTrue(result.assertFirst().isSubtypeOfBool());
        assertTrue(result.assertFirst().isOrdinary());
        node = ast.gte(ast.integer(134), ast.character("b"));
        result = node.accept(visitor);
        assertTrue(result.assertFirst().isSubtypeOfBool());
        assertTrue(result.assertFirst().isOrdinary());
        node = ast.gt(ast.integer(134), ast.character("b"));
        result = node.accept(visitor);
        assertTrue(result.assertFirst().isSubtypeOfBool());
        assertTrue(result.assertFirst().isOrdinary());



        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        node = ast.lte(ast.string("str"), ast.string("world"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));
        node = ast.lt(ast.string("str"), ast.string("world"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));
        node = ast.gte(ast.string("str"), ast.string("world"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));
        node = ast.gt(ast.string("str"), ast.string("world"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));



        context = new HashMapStackContext();
        visitor.context.addVar("numbers", new ArrayType(PrimitiveType.intDefault));
        visitor = new TypeCheckVisitor(null);
        node = ast.lte(ast.variable("numbers"), ast.string("1234"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));
        node = ast.lt(ast.variable("numbers"), ast.string("1234"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));
        node = ast.gte(ast.variable("numbers"), ast.string("1234"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));
        node = ast.gt(ast.variable("numbers"), ast.string("1234"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));




        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        node = ast.lte(ast.bool(false), ast.bool(false));
        assertThrows(SemanticException.class, () -> node.accept(visitor));
        node = ast.lt(ast.bool(false), ast.bool(false));
        assertThrows(SemanticException.class, () -> node.accept(visitor));
        node = ast.gte(ast.bool(false), ast.bool(false));
        assertThrows(SemanticException.class, () -> node.accept(visitor));
        node = ast.gt(ast.bool(false), ast.bool(false));
        assertThrows(SemanticException.class, () -> node.accept(visitor));




        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        node = ast.lte(ast.bool(false), ast.integer(124));
        assertThrows(SemanticException.class, () -> node.accept(visitor));
        node = ast.lt(ast.bool(false), ast.integer(124));
        assertThrows(SemanticException.class, () -> node.accept(visitor));
        node = ast.gte(ast.bool(false), ast.integer(124));
        assertThrows(SemanticException.class, () -> node.accept(visitor));
        node = ast.gt(ast.bool(false), ast.integer(124));
        assertThrows(SemanticException.class, () -> node.accept(visitor));




        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        node = ast.lte(ast.bool(false), ast.string("This is a string"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));
        node = ast.lt(ast.bool(false), ast.string("This is a string"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));
        node = ast.gte(ast.bool(false), ast.string("This is a string"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));
        node = ast.gt(ast.bool(false), ast.string("This is a string"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));




        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        node = ast.lte(ast.integer(65432), ast.string("This is a string"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));
        node = ast.lt(ast.integer(65432), ast.string("This is a string"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));
        node = ast.gte(ast.integer(65432), ast.string("This is a string"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));
        node = ast.gt(ast.integer(65432), ast.string("This is a string"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));




        context = new HashMapStackContext();
        visitor.context.addVar("bools", new ArrayType(PrimitiveType.boolDefault));
        visitor = new TypeCheckVisitor(null);
        node = ast.lte(ast.variable("bools"), ast.string("1324"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));
        node = ast.lt(ast.variable("bools"), ast.string("1324"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));
        node = ast.gte(ast.variable("bools"), ast.string("1324"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));
        node = ast.gt(ast.variable("bools"), ast.string("1324"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));
    }


    @Test
    void testBooleanOperators() {
        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        node = ast.and(ast.integer("1324"), ast.integer("1324"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));
        node = ast.or(ast.integer("1324"), ast.integer("1324"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));


        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        node = ast.and(ast.character("a"), ast.character("b"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));
        node = ast.or(ast.character("a"), ast.character("b"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));


        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        node = ast.and(ast.integer("14"), ast.character("b"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));
        node = ast.or(ast.integer("134"), ast.character("b"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));



        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        node = ast.and(ast.string("1324"), ast.string("1324"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));
        node = ast.or(ast.string("1324"), ast.string("1324"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));



        context = new HashMapStackContext();
        visitor.context.addVar("numbers", new ArrayType(PrimitiveType.intDefault));
        visitor = new TypeCheckVisitor(null);
        node = ast.and(ast.variable("numbers"), ast.string("1324"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));
        node = ast.or(ast.variable("numbers"), ast.string("1324"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));


        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        node = ast.and(ast.bool(false), ast.bool(false));
        result = node.accept(visitor);
        assertTrue(result.assertFirst().isSubtypeOfBool());
        assertTrue(result.assertFirst().isOrdinary());
        node = ast.or(ast.bool(false), ast.bool(false));
        result = node.accept(visitor);
        assertTrue(result.assertFirst().isSubtypeOfBool());
        assertTrue(result.assertFirst().isOrdinary());


        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        node = ast.and(ast.bool(false), ast.integer("124"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));
        node = ast.or(ast.bool(false), ast.integer("124"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));


        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        node = ast.and(ast.bool(false), ast.string("This is a string"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));
        node = ast.or(ast.bool(false), ast.string("This is a string"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));


        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        node = ast.and(ast.integer("65432"), ast.string("This is a string"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));
        node = ast.or(ast.integer("65432"), ast.string("This is a string"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));



        context = new HashMapStackContext();
        visitor.context.addVar("bools", new ArrayType(PrimitiveType.boolDefault));
        visitor = new TypeCheckVisitor(null);
        node = ast.and(ast.variable("bools"), ast.string("1324"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));
        node = ast.or(ast.variable("bools"), ast.string("1324"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));

    }


    @Test
    void testArrayExprNode() {
        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);
        node = ast.array(ast.integer(9), ast.integer(10), ast.integer(21));
        result = node.accept(visitor);
        assertTrue(result.assertFirst().isArray());
        assertTrue(result.assertFirst().getInnerArrayType().isInt());


        node = ast.array(
                ast.array(ast.integer(9)),
                ast.array(ast.integer(10)),
                ast.array(ast.integer(21))
        );
        result = node.accept(visitor);
        assertTrue(result.assertFirst().isArray());
        assertTrue(result.assertFirst().getInnerArrayType().isArray());
        assertTrue(result.assertFirst().isASubtypeOf(
                new ExpandedType(
                        new ArrayType(new ArrayType(PrimitiveType.intDefault)))));


        node = new LiteralArrayExprNode(loc, List.of());
        result = node.accept(visitor);
        assertTrue(result.assertFirst().isArray());
        assertTrue(result.assertFirst().getInnerArrayType().isVoid());

        node = ast.array(ast.bool(true), ast.integer(10), ast.bool(false));
        assertThrows(SemanticException.class, () -> node.accept(visitor));


        node = ast.array(ast.array(ast.integer(9)), ast.integer(10), ast.array(ast.integer(21)));
        assertThrows(SemanticException.class, () -> node.accept(visitor));
    }


    @Test
    void testFunctionCall() {
        context = new HashMapStackContext();


        visitor = new TypeCheckVisitor(null);
        visitor.context.addFn("print", new FunctionType(
                new ExpandedType(new ArrayType(PrimitiveType.intDefault)),
                ExpandedType.unitExpandedType));
        node = new FunctionCallExprNode(loc, "print",
                List.of(new LiteralStringExprNode(loc, "Hello World")));
        result = node.accept(visitor);
        assertTrue(result.assertFirst().isUnit());





        visitor = new TypeCheckVisitor(null);
        visitor.context.addFn("duplicate", new FunctionType(
                new ExpandedType(new ArrayType(PrimitiveType.intDefault)),
                new ExpandedType(new ArrayType(PrimitiveType.intDefault))));
        node = new FunctionCallExprNode(loc, "duplicate",
                List.of(new LiteralStringExprNode(loc, "Hello World")));
        result = node.accept(visitor);
        assertTrue(result.assertFirst().isArray());
        assertTrue(result.assertFirst().getInnerArrayType().isInt());




        visitor = new TypeCheckVisitor(null);
        visitor.context.addFn("exit", new FunctionType(
                ExpandedType.unitExpandedType,
                ExpandedType.unitExpandedType));
        node = new FunctionCallExprNode(loc, "exit", List.of());
        result = node.accept(visitor);
        assertTrue(result.assertFirst().isUnit());




        visitor = new TypeCheckVisitor(null);
        visitor.context.addFn("random", new FunctionType(
                ExpandedType.unitExpandedType, ExpandedType.intType));
        node = new FunctionCallExprNode(loc, "random", List.of());
        result = node.accept(visitor);
        assertTrue(result.assertFirst().isSubtypeOfInt());
        assertTrue(result.assertFirst().getOrdinaryType().isInt());



        ExpandedType tuple = new ExpandedType(
                List.of(PrimitiveType.intDefault, PrimitiveType.boolDefault,
                        PrimitiveType.boolDefault)
                );
        ExpandedType oppositeTuple = new ExpandedType(
                List.of(PrimitiveType.boolDefault, PrimitiveType.intDefault,
                        PrimitiveType.intDefault)
                );

        visitor = new TypeCheckVisitor(null);
        visitor.context.addFn("genMany",
                new FunctionType(ExpandedType.unitExpandedType, tuple));
        visitor.context.addFn("singleToMany",
                new FunctionType(ExpandedType.intType, tuple));
        visitor.context.addFn("transform", new FunctionType(tuple, oppositeTuple));
        visitor.context.addFn("consume", new FunctionType(
                tuple, ExpandedType.unitExpandedType));
        visitor.context.addFn("manyToSingle",
                new FunctionType(tuple, ExpandedType.intType));

        node = new FunctionCallExprNode(loc, "genMany", List.of());
        result = node.accept(visitor);
        assertTrue(result.assertFirst().isTuple());
        assertTrue(result.assertFirst().isASubtypeOf(tuple));

        node = new FunctionCallExprNode(loc, "singleToMany",
                List.of(new LiteralIntExprNode(loc, "0")));
        result = node.accept(visitor);
        assertTrue(result.assertFirst().isTuple());
        assertTrue(result.assertFirst().isASubtypeOf(tuple));


        node = new FunctionCallExprNode(loc, "transform",
                List.of(new LiteralIntExprNode(loc, "0"),
                        new LiteralBoolExprNode(loc, false),
                        new LiteralBoolExprNode(loc, true)));
        result = node.accept(visitor);
        assertTrue(result.assertFirst().isTuple());
        assertTrue(result.assertFirst().isASubtypeOf(oppositeTuple));


        node = new FunctionCallExprNode(loc, "consume",
                List.of(new LiteralIntExprNode(loc, "0"),
                        new LiteralBoolExprNode(loc, false),
                        new LiteralBoolExprNode(loc, true)));
        result = node.accept(visitor);
        assertTrue(result.assertFirst().isUnit());


        node = new FunctionCallExprNode(loc, "manyToSingle",
                List.of(new LiteralIntExprNode(loc, "0"),
                        new LiteralBoolExprNode(loc, false),
                        new LiteralBoolExprNode(loc, true)));
        result = node.accept(visitor);
        assertTrue(result.assertFirst().isSubtypeOfInt());
        assertTrue(result.assertFirst().getOrdinaryType().isInt());


        node = new FunctionCallExprNode(loc, "nonexistantFunction", List.of());
        assertThrows(SemanticException.class, () -> node.accept(visitor));

        node = new FunctionCallExprNode(loc, "manyToSingle", List.of());
        assertThrows(SemanticException.class, () -> node.accept(visitor));

        node = new FunctionCallExprNode(loc, "consume", List.of());
        assertThrows(SemanticException.class, () -> node.accept(visitor));

        node = new FunctionCallExprNode(loc, "genMany",
                List.of(new LiteralStringExprNode(loc, "Bad Input")));
        assertThrows(SemanticException.class, () -> node.accept(visitor));

    }


    @Test
    void testAccessNodes() {

        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);

        visitor.context.addVar("cash", PrimitiveType.intDefault);
        node = new VariableAccessExprNode(loc, "cash");
        result = node.accept(visitor);
        assertTrue(result.assertFirst().isSubtypeOfInt());

        // empty[0]
        visitor.context.addVar("empty", new ArrayType(PrimitiveType.intDefault));
        node = new ArrayAccessExprNode(loc,
                new VariableAccessExprNode(loc, "empty"),
                new LiteralIntExprNode(loc, "0"));
        result = node.accept(visitor);
        assertTrue(result.assertFirst().isSubtypeOfInt());


        // empty
        node = new VariableAccessExprNode(loc, "empty");
        result = node.accept(visitor);
        assertTrue(result.assertFirst().isArray());


        // empty[true]
        node = new ArrayAccessExprNode(loc,
                new VariableAccessExprNode(loc, "empty"),
                new LiteralBoolExprNode(loc, true));
        assertThrows(SemanticException.class, () -> node.accept(visitor));


        // empty["this is not a number"]
        node = new ArrayAccessExprNode(loc,
                new VariableAccessExprNode(loc, "empty"),
                new LiteralStringExprNode(loc, "this is not a number"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));


        // empty[0][0]
        node = new ArrayAccessExprNode(loc,
                new ArrayAccessExprNode(
                        loc,
                        new VariableAccessExprNode(loc, "empty"),
                        new LiteralIntExprNode(loc, "0")),
                new LiteralIntExprNode(loc, "0"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));


        visitor.context.addVar("twoDimensionMap", new ArrayType(
                new ArrayType(PrimitiveType.intDefault)));
        node = new ArrayAccessExprNode(loc,
                        new VariableAccessExprNode(loc, "twoDimensionMap"),
                new LiteralIntExprNode(loc, "0"));
        result = node.accept(visitor);
        assertTrue(result.assertFirst().isArray());
        assertTrue(result.assertFirst().getInnerArrayType().isInt());


        node = new ArrayAccessExprNode(loc,
                new ArrayAccessExprNode(
                        loc,
                        new VariableAccessExprNode(loc, "twoDimensionMap"),
                        new LiteralIntExprNode(loc, "0")),
                new LiteralIntExprNode(loc, "0"));
        result = node.accept(visitor);
        assertTrue(result.assertFirst().isSubtypeOfInt());


        node = new ArrayAccessExprNode(loc,
                new ArrayAccessExprNode(
                        loc,
                        new VariableAccessExprNode(loc, "twoDimensionMap"),
                        new LiteralIntExprNode(loc, "0")),
                new LiteralStringExprNode(loc, "NaN"));
        assertThrows(SemanticException.class, () -> node.accept(visitor));

    }


    @Test
    void testArrayLiterals() {
        context = new HashMapStackContext();
        visitor = new TypeCheckVisitor(null);

        node = new ArrayAccessExprNode(loc,
                new LiteralArrayExprNode(loc,
                        List.of(new LiteralIntExprNode(loc, "0"))),
                new LiteralIntExprNode(loc, "0"));
        result = node.accept(visitor);
        assertTrue(result.assertFirst().isSubtypeOfInt());


        // Access to an empty array can be potentially any type.
        node = new ArrayAccessExprNode(loc,
                new LiteralArrayExprNode(loc, List.of()),
                new LiteralIntExprNode(loc, "0"));
        result = node.accept(visitor);
        assertTrue(result.assertFirst().isSubtypeOfInt());
        assertTrue(result.assertFirst().isSubtypeOfBool());
        assertTrue(result.assertFirst().isSubtypeOfArray());




        // The array literal's type becomes the supertype of among the elements.
        // Example: {{}, {1,2,3}}
        node = new LiteralArrayExprNode(loc,
                List.of(
                        new LiteralArrayExprNode(loc, List.of()),
                        new LiteralArrayExprNode(loc, List.of(
                                new LiteralIntExprNode(loc, "1"),
                                new LiteralIntExprNode(loc, "2"),
                                new LiteralIntExprNode(loc, "3")
                                ))
                        ));
        result = node.accept(visitor);
        assertTrue(result.assertFirst().isArray());
        assertTrue(result.assertFirst().isASubtypeOf(
                new ExpandedType(
                        new ArrayType(new ArrayType(PrimitiveType.intDefault)))));



        // The array literal's type becomes the supertype of among the elements.
        // {{}, {1,2,3}}[0][0]
        node = new ArrayAccessExprNode(loc,
                new ArrayAccessExprNode(loc,
                        new LiteralArrayExprNode(loc,
                          List.of(
                               new LiteralArrayExprNode(loc, List.of()),
                               new LiteralArrayExprNode(loc, List.of(
                                       new LiteralIntExprNode(loc, "1"),
                                       new LiteralIntExprNode(loc, "2"),
                                       new LiteralIntExprNode(loc, "3")))
                               )), new LiteralIntExprNode(loc, "0")),
                new LiteralIntExprNode(loc, "0"));
        result = node.accept(visitor);
        assertTrue(result.assertFirst().isSubtypeOfInt());
        assertTrue(result.assertFirst().getOrdinaryType().isInt());





        // {{}, {}}[0][0]
        node = new ArrayAccessExprNode(loc,
                new ArrayAccessExprNode(loc,
                        new LiteralArrayExprNode(loc,
                          List.of(
                               new LiteralArrayExprNode(loc, List.of()),
                               new LiteralArrayExprNode(loc, List.of())
                               )), new LiteralIntExprNode(loc, "0")),
                new LiteralIntExprNode(loc, "0"));
        result = node.accept(visitor);
        assertTrue(result.assertFirst().isSubtypeOfInt());
        assertTrue(result.assertFirst().isSubtypeOfBool());
        assertTrue(result.assertFirst().isSubtypeOfArray());
        assertTrue(result.assertFirst().isVoid());






        // Attempt to add an access to an empty array to an array
        node = ast.add(
                ast.arrayAccess(ast.array(), ast.integer("0")),
                ast.string("A string")
        );
        result = node.accept(visitor);
        assertTrue(result.assertFirst().isArray());
        assertFalse(result.assertFirst().isVoid());


        node = ast.add(
                ast.arrayAccess(ast.array(), ast.integer("0")),
                ast.integer("132")
        );
        result = node.accept(visitor);
        assertTrue(result.assertFirst().isSubtypeOfInt());
        assertFalse(result.assertFirst().isVoid());




        node = ast.div(
                ast.arrayAccess(ast.array(), ast.integer("0")),
                ast.arrayAccess(ast.array(), ast.integer("0"))
        );
        result = node.accept(visitor);
        assertTrue(result.assertFirst().isSubtypeOfFloat());
        assertFalse(result.assertFirst().isVoid());



        node = ast.add(
                ast.arrayAccess(ast.array(), ast.integer("0")),
                ast.arrayAccess(ast.array(), ast.integer("0"))
        );
        result = node.accept(visitor);
        assertTrue(result.assertFirst().isGenericAdd());


        node = ast.and(
                ast.arrayAccess(ast.array(), ast.integer("0")),
                ast.arrayAccess(ast.array(), ast.integer("0"))
        );
        result = node.accept(visitor);
        assertTrue(result.assertFirst().isSubtypeOfBool());
        assertFalse(result.assertFirst().isVoid());

    }
}
