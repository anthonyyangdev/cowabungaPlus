package cyr7.lexer

import cyr7.parser.sym
import java_cup.runtime.Symbol
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class LexerFloatTest {

    @Test
    fun `basic floating-point value lexing`() {
        val lexer = LexerFactory.make("100.123 123456.  0.0  0.001 .1232")
        var token: Symbol = lexer.next_token()
        assertEquals(sym.FLOAT_LITERAL, token.sym)
        assertEquals(100.123, token.value)

        val k = 1E3
        token = lexer.next_token()
        assertEquals(sym.FLOAT_LITERAL, token.sym)
        assertEquals(123456.0, token.value)

        token = lexer.next_token()
        assertEquals(sym.FLOAT_LITERAL, token.sym)
        assertEquals(0.0, token.value)

        token = lexer.next_token()
        assertEquals(sym.FLOAT_LITERAL, token.sym)
        assertEquals(0.001, token.value)

        token = lexer.next_token()
        assertEquals(sym.FLOAT_LITERAL, token.sym)
        assertEquals(0.1232, token.value)

    }

    @Test
    fun `test scientific notation`() {
        val inputs = arrayOf("1e43", "1e-13", "1E43", "1e-43", "00.3e1", ".03e21", "001e3", "01e-3")
        val lexer = LexerFactory.make(inputs.joinToString(" "))
        for (input in inputs) {
            val token = lexer.next_token()
            assertEquals(sym.FLOAT_LITERAL, token.sym)
            assertEquals(input.toDouble(), token.value)
        }
    }

    @Test
    fun `infinite value float`() {
        val lexer = LexerFactory.make("  Infinity  -Infinity")
        var token: Symbol = lexer.next_token()
        assertEquals(sym.FLOAT_LITERAL, token.sym)
        assertEquals(Double.POSITIVE_INFINITY, token.value)

        token = lexer.next_token()
        assertEquals(sym.MINUS, token.sym)

        token = lexer.next_token()
        assertEquals(sym.FLOAT_LITERAL, token.sym)
        assertEquals(Double.POSITIVE_INFINITY, token.value)
    }

    @Test
    fun `not a number`() {
        val lexer = LexerFactory.make("  NaN ")
        val token: Symbol = lexer.next_token()
        assertEquals(sym.FLOAT_LITERAL, token.sym)
        assertTrue(token.value is Double && (token.value as Double).isNaN())
    }

}
