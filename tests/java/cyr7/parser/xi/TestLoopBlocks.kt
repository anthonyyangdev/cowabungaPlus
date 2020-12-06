package cyr7.parser.xi

import cyr7.C
import cyr7.ast.stmt.VarDeclNode
import cyr7.ast.expr.FunctionCallExprNode
import cyr7.ast.expr.access.VariableAccessExprNode
import cyr7.ast.expr.binexpr.AddExprNode
import cyr7.ast.expr.binexpr.LTExprNode
import cyr7.ast.expr.literalexpr.LiteralBoolExprNode
import cyr7.ast.expr.literalexpr.LiteralIntExprNode
import cyr7.ast.expr.literalexpr.LiteralStringExprNode
import cyr7.ast.expr.unaryexpr.LengthExprNode
import cyr7.ast.stmt.*
import cyr7.ast.type.PrimitiveEnum
import cyr7.ast.type.PrimitiveTypeNode
import cyr7.parser.util.ParserFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class TestLoopBlocks {

    private fun String.parse(): StmtNode {
        return ParserFactory.parseStatement(this)[0]
    }
    private val LOC = C.LOC

    @Test
    fun `test do-while loops`() {
        val statement = "do i = i + 1 while(true)".parse()
        assertEquals(
                DoWhileStmtNode(LOC,
                        AssignmentStmtNode(LOC,
                                VariableAccessExprNode(LOC, "i"),
                                AddExprNode(LOC,
                                        VariableAccessExprNode(LOC, "i"),
                                        LiteralIntExprNode(LOC, "1")
                        )
                ), LiteralBoolExprNode(LOC, true)
        ), statement)
    }

    @Test
    fun `test for-loop`() {
        val statement = """for (i: int = 1; i < length(x); i = i + 1) println("Hello") """.parse()
        assertEquals(
                ForLoopStmtNode(
                        LOC,
                        VarInitStmtNode(LOC, VarDeclNode(LOC, "i", PrimitiveTypeNode(LOC, PrimitiveEnum.INT)), LiteralIntExprNode(LOC, "1")),
                        LTExprNode(LOC, VariableAccessExprNode(LOC, "i"), LengthExprNode(LOC, VariableAccessExprNode(LOC, "x"))),
                        AssignmentStmtNode(LOC, VariableAccessExprNode(LOC, "i"), AddExprNode(LOC, VariableAccessExprNode(LOC, "i"), LiteralIntExprNode(LOC, "1"))),
                        ProcedureStmtNode(LOC, FunctionCallExprNode(LOC, "println", listOf(LiteralStringExprNode(LOC, "Hello"))))
                ),
                statement
        )
    }

}
