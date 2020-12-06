package cyr7.ir.astToIr

import cyr7.C
import cyr7.ast.ASTFactory
import cyr7.ast.Node
import cyr7.ir.ASTToIRVisitor
import cyr7.ir.DefaultIdGenerator
import cyr7.ir.IdGenerator
import cyr7.ir.nodes.*
import cyr7.semantics.types.PrimitiveType
import cyr7.typecheck.TypeCheckUtil
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class TestExpr {
    private val make = IRNodeFactory_c(C.LOC)
    private val ast = ASTFactory(C.LOC)

    /*
    @Test
    void testLiteral() {
        AbstractNode astNode = new LiteralBoolExprNode(C.LOC, false);
        IRNode result = IRFactory.parseAstExpr(astNode);
        long expectedResult = 0;
        assertEquals(expectedResult, IRFactory.testExpr(result));

        astNode = new LiteralBoolExprNode(C.LOC, true);
        result = IRFactory.parseAstExpr(astNode);
        expectedResult = 1;
        assertEquals(expectedResult, IRFactory.testExpr(result));

        astNode = new LiteralIntExprNode(C.LOC, "9223372036854775807");
        result = IRFactory.parseAstExpr(astNode);
        expectedResult = Long.parseLong("9223372036854775807");
        assertEquals(expectedResult, IRFactory.testExpr(result));
    }

    @Test
    void testUnaryOps() {
        AbstractNode astNode = new BoolNegExprNode(C.LOC,
                new LiteralBoolExprNode(C.LOC, true));
        IRNode result = IRFactory.parseAstExpr(astNode);
        long expectedResult = 0;
        assertEquals(expectedResult, IRFactory.testExpr(result));

        astNode = new IntNegExprNode(C.LOC,
                new LiteralIntExprNode(C.LOC, "9223372036854775807"));
        result = IRFactory.parseAstExpr(astNode);
        expectedResult = Long.parseLong("-9223372036854775807");
        assertEquals(expectedResult, IRFactory.testExpr(result));
    }

    @Test
    void testBinOps() {
        AbstractNode astNode = new AddExprNode(C.LOC,
                new LiteralIntExprNode(C.LOC, "5"),
                new LiteralIntExprNode(C.LOC, "5"));

        IRNode result = IRFactory.parseAstExpr(astNode);
        long expectedResult = 10;
        assertEquals(expectedResult, IRFactory.testExpr(result));

        astNode = new AddExprNode(C.LOC, new LiteralIntExprNode(C.LOC, "5"),
                new LiteralIntExprNode(C.LOC, "5"));

        result = IRFactory.parseAstExpr(astNode);
        expectedResult = 10;
        assertEquals(expectedResult, IRFactory.testExpr(result));
    }

    @Test
    void testOr() {
        Node node = new OrExprNode(C.LOC, new LiteralBoolExprNode(C.LOC, true),
                new LiteralBoolExprNode(C.LOC, false));

        LookaheadIdGenerator generator = new LookaheadIdGenerator();
        String x = generator.peekTemp(0);
        String lt = generator.peekLabel(0);
        String lf = generator.peekLabel(1);
        String lf_ = generator.peekLabel(2);

        IRStmt left = make.IRJump(make.IRName(lt));
        IRStmt right = make.IRJump(make.IRName(lf_));

        IRExpr expected = make
                .IRESeq(make.IRSeq(make.IRMove(make.IRTemp(x), make.IRConst(1)),
                        left, make.IRLabel(lf), right, make.IRLabel(lf_),
                        make.IRMove(make.IRTemp(x), make.IRConst(0)),
                        make.IRLabel(lt)), make.IRTemp(x));
        assertEq(expected, node, generator);
    }
    */
    @Test
    fun testRem() {
        val node: Node = ast.rem(ast.integer(5), ast.integer(10))
        val expected: IRExpr = make.IRBinOp(IRBinOp.OpType.MOD_INT, make.IRInteger(5),
                make.IRInteger(10))
        assertEq(expected, node)
    }

    @Test
    fun testSub() {
        val node: Node = ast.sub(ast.integer(5), ast.integer(10))
        val expected: IRExpr = make.IRBinOp(IRBinOp.OpType.SUB_INT, make.IRInteger(5),
                make.IRInteger(10))
        assertEq(expected, node)
    }

    @Test
    fun testBoolNegExpr() {
        val node: Node = ast.negateBool(ast.bool(false))
        val expected: IRExpr = make.IRBinOp(IRBinOp.OpType.XOR, make.IRInteger(1),
                make.IRInteger(0))
        assertEq(expected, node)
    }

    @Test
    fun testIntNegExpr() {
        val node = ast.negateNumber(ast.integer(5))
        val expected: IRExpr = make.IRBinOp(IRBinOp.OpType.SUB_INT, make.IRInteger(0),
                make.IRInteger(5))
        assertEq(expected, node)
    }

    @Test
    fun testLiteralInt() {
        val node = ast.integer(5)
        val expected: IRExpr = make.IRInteger(5)
        assertEq(expected, node)
    }

    @Test
    fun testLiteralBool() {
        val node = ast.bool(true)
        val expected: IRExpr = make.IRInteger(1)
        assertEq(expected, node)
    }

    @Test
    fun testLiteralChar() {
        val node = ast.character('x')
        val expected: IRExpr = make.IRInteger('x'.toLong())
        assertEq(expected, node)
    }

    @Test
    fun `test floating-point values`() {
        val node = ast.floating(1e21)
        val expected = make.IRFloat(1e21)
        assertEq(expected, node)
    }

    @Test
    fun `test casting integers to floats`() {
        val node = ast.add(ast.integer(14), ast.floating(1.5))
        val expected = make.IRBinOp(IRBinOp.OpType.ADD_FLOAT,
                make.IRCast(make.IRInteger(14), PrimitiveType.intDefault, PrimitiveType.floatDefault),
                make.IRFloat(1.5)
        )
        assertEq(expected, node)
    }

    companion object {
        private fun assertEq(expected: IRNode, toTransform: Node) {
            TypeCheckUtil.typeCheckNoIxiFiles(toTransform)
            Assertions.assertEquals(expected,
                    toTransform.accept(ASTToIRVisitor(DefaultIdGenerator())).assertFirst())
        }

        private fun assertEq(expected: IRNode, toTransform: Node,
                             generator: IdGenerator) {
            Assertions.assertEquals(expected, toTransform.accept(ASTToIRVisitor(generator))
                    .assertFirst())
        }
    }
}
