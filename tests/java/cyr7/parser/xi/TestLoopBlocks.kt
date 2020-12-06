package cyr7.parser.xi

import cyr7.C
import cyr7.ast.ASTFactory
import cyr7.ast.stmt.*
import cyr7.parser.util.ParserFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class TestLoopBlocks {

    private fun String.parse(): StmtNode {
        return ParserFactory.parseStatement(this)[0]
    }
    val ast = ASTFactory(C.LOC);

    @Test
    fun `test do-while loops`() {
        val statement = "do i = i + 1 while(true)".parse()
        assertEquals(ast.doWhile(
                ast.assign(ast.variable("i"), ast.add(ast.variable("i"), ast.integer(1))),
                ast.bool(true)
        ), statement)
    }

    @Test
    fun `test for-loop`() {
        val statement = """for (i: int = 1; i < length(x); i = i + 1) println("Hello") """.parse()
        val init = ast.varInit(ast.varDecl("i", ast.intType()), ast.integer(1))
        val cond = ast.lt(ast.variable("i"), ast.length(ast.variable("x")))
        val epilogue = ast.assign(ast.variable("i"), ast.add(ast.variable("i"), ast.integer(1)))
        val body = ast.procedure(ast.call("println", ast.string("Hello")))
        assertEquals(ast.forLoop(init, cond, epilogue, body), statement)
    }

}
