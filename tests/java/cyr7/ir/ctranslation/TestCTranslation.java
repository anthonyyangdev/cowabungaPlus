package cyr7.ir.ctranslation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import cyr7.ast.ASTFactory;
import cyr7.semantics.types.FunctionType;
import org.junit.jupiter.api.Test;

import cyr7.C;
import cyr7.ast.Node;
import cyr7.ast.expr.FunctionCallExprNode;
import cyr7.ir.CTranslationVisitor;
import cyr7.ir.nodes.IRNode;
import cyr7.ir.nodes.IRNodeFactory;
import cyr7.ir.nodes.IRNodeFactory_c;
import cyr7.ir.nodes.IRStmt;
import cyr7.semantics.types.ExpandedType;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class TestCTranslation {

    private final IRNodeFactory make = new IRNodeFactory_c(C.LOC);
    private final ASTFactory ast = new ASTFactory(C.LOC);

    @Test
    void testTrue() {
        LookaheadIdGenerator generator = new LookaheadIdGenerator();
        String t = generator.newLabel();
        String f = generator.newLabel();

        Node node = ast.bool(true);
        IRStmt expected = make.IRJump(make.IRName(t));
        assertEquals(expected, node.accept(new CTranslationVisitor(generator, t, f)));
    }

    @Test
    void testFalse() {
        LookaheadIdGenerator generator = new LookaheadIdGenerator();
        String t = generator.newLabel();
        String f = generator.newLabel();

        Node node = ast.bool(false);
        IRStmt expected = make.IRJump(make.IRName(f));
        assertEquals(expected, node.accept(new CTranslationVisitor(generator, t, f)));
    }

    @Test
    void testAndLiterals() {
        LookaheadIdGenerator generator = new LookaheadIdGenerator();
        String t = generator.newLabel();
        String f = generator.newLabel();
        String l1 = generator.peekLabel(0);

        Node node = ast.and(ast.bool(true), ast.bool(false));
        IRStmt expected = make.IRSeq(make.IRJump(make.IRName(l1)),
                make.IRLabel(l1), make.IRJump(make.IRName(f))
        );
        assertEquals(
            expected,
            node.accept(new CTranslationVisitor(generator, t, f))
        );
    }

    @Test
    void testNestedAnd() {
        LookaheadIdGenerator generator = new LookaheadIdGenerator();
        String t = generator.newLabel();
        String f = generator.newLabel();
        String l2 = generator.peekLabel(0);
        String l3 = generator.peekLabel(1);
        String l4 = generator.peekLabel(2);

        // (true & false) & (true & true)
        Node node = ast.and(
                ast.and(ast.bool(true), ast.bool(false)),
                ast.and(ast.bool(true), ast.bool(true))
        );
        IRStmt expected = make.IRSeq(make.IRSeq(make.IRJump(make.IRName(l3)),
                make.IRLabel(l3), make.IRJump(make.IRName(f))
            ),
                make.IRLabel(l2), make.IRSeq(make.IRJump(make.IRName(l4)),
                        make.IRLabel(l4), make.IRJump(make.IRName(t))
            )
        );
        assertEquals(
            expected,
            node.accept(new CTranslationVisitor(generator, t, f))
        );
    }

    @Test
    void testOrLiteral() {
        LookaheadIdGenerator generator = new LookaheadIdGenerator();
        String t = generator.newLabel();
        String f = generator.newLabel();
        String l1 = generator.peekLabel(0);

        Node node = ast.or(ast.bool(true), ast.bool(false));
        IRStmt expected = make.IRSeq(make.IRJump(make.IRName(t)),
                make.IRLabel(l1), make.IRJump(make.IRName(f))
        );
        assertEquals(
            expected,
            node.accept(new CTranslationVisitor(generator, t, f))
        );
    }

    @Test
    void testNestedOr() {
        LookaheadIdGenerator generator = new LookaheadIdGenerator();
        String t = generator.newLabel();
        String f = generator.newLabel();
        String l2 = generator.peekLabel(0);
        String l3 = generator.peekLabel(1);
        String l4 = generator.peekLabel(2);

        // (false | true) | (true | false)
        Node node = ast.or(
                ast.or(ast.bool(false), ast.bool(true)),
                ast.or(ast.bool(true), ast.bool(false))
        );
        IRStmt expected = make.IRSeq(make.IRSeq(make.IRJump(make.IRName(l3)),
                make.IRLabel(l3), make.IRJump(make.IRName(t))
            ),
                make.IRLabel(l2), make.IRSeq(make.IRJump(make.IRName(t)),
                        make.IRLabel(l4), make.IRJump(make.IRName(f))
            )
        );
        assertEquals(
            expected,
            node.accept(new CTranslationVisitor(generator, t, f))
        );
    }

    @Test
    void testOrNestingAnd() {
        LookaheadIdGenerator generator = new LookaheadIdGenerator();
        String t = generator.newLabel();
        String f = generator.newLabel();
        String l2 = generator.peekLabel(0);
        String l3 = generator.peekLabel(1);
        String l4 = generator.peekLabel(2);

        FunctionCallExprNode fcall = ast.call("f");
        fcall.setType(ExpandedType.boolType);
        fcall.setFunctionType(new FunctionType(new ExpandedType(List.of()), ExpandedType.boolType));
        FunctionCallExprNode gcall = ast.call("g");
        gcall.setType(ExpandedType.boolType);
        gcall.setFunctionType(new FunctionType(new ExpandedType(List.of()), ExpandedType.boolType));
        FunctionCallExprNode hcall = ast.call("h");
        hcall.setType(ExpandedType.boolType);
        hcall.setFunctionType(new FunctionType(new ExpandedType(List.of()), ExpandedType.boolType));
        FunctionCallExprNode icall = ast.call("i");
        icall.setType(ExpandedType.boolType);
        icall.setFunctionType(new FunctionType(new ExpandedType(List.of()), ExpandedType.boolType));

        // (f() & g()) | (h() & i())
        Node node = ast.or(
                ast.and(fcall, gcall).setType(ExpandedType.boolType),
                ast.and(hcall, icall).setType(ExpandedType.boolType)
        ).setType(ExpandedType.boolType);
        IRNode expected = make.IRSeq(make.IRSeq(
                make.IRCJump(make.IRCall(make.IRName("_If_b")), l3, l2),
                make.IRLabel(l3),
                make.IRCJump(make.IRCall(make.IRName("_Ig_b")), t, l2)
            ),
                make.IRLabel(l2),
                make.IRSeq(
                        make.IRCJump(make.IRCall(make.IRName("_Ih_b")), l4, f),
                        make.IRLabel(l4),
                        make.IRCJump(make.IRCall(make.IRName("_Ii_b")), t, f)
            )
        );
        assertEquals(
            expected,
            node.accept(new CTranslationVisitor(generator, t, f))
        );
    }

    @Test
    void testFunctionCall() {
        LookaheadIdGenerator generator = new LookaheadIdGenerator();
        String t = generator.newLabel();
        String f = generator.newLabel();

        FunctionCallExprNode fcall = ast.call("f");
        fcall.setType(ExpandedType.boolType);
        fcall.setFunctionType(new FunctionType(new ExpandedType(List.of()), ExpandedType.boolType));
        IRNode expected = make.IRCJump(make.IRCall(make.IRName("_If_b")), t, f);
        assertEquals(
            expected,
            fcall.accept(new CTranslationVisitor(generator, t, f))
        );
    }

}
