package cyr7.lexer

import cyr7.parser.sym
import cyr7.util.Util
import java_cup.runtime.ComplexSymbolFactory.ComplexSymbol
import java.io.BufferedReader
import java.io.IOException
import java.io.Reader
import java.io.Writer

object LexerUtil {
    /**
     * Read contents and write a line by line description of the lexed output.
     *
     * @param reader the contents to read
     * @param writer the place to write
     * @throws IOException if the reader throws an `IOException`
     */
    @JvmStatic
    @Throws(IOException::class)
    fun lex(reader: Reader, writer: Writer, filename: String): Boolean {
        val lexer = MyLexer(BufferedReader(reader), filename)
        var token: ComplexSymbol
        return try {
            while (lexer.next_token().also { token = it }.sym != sym.EOF) {
                writer.append(fullDescription(token))
                        .append(System.lineSeparator())
            }
            true
        } catch (e: Exception) {
            writer.append(e.message)
                    .append(System.lineSeparator())
            false
        }
    }

    /**
     *
     * A description of a token including line and column number in the format:
     *
     * `$line:$column $desc`
     *
     * @param token the symbol to describe
     * @return a description of the token
     */
    @JvmStatic
    fun fullDescription(token: ComplexSymbol): String {
        val line = token.xleft.line
        val column = token.xleft.column
        val location = "$line:$column "
        return location + symbolDescription(token.sym, token.value)
    }

    /**
     * A readable description for tokens based on their Symbol ID.
     *
     * @param symId the ID of the symbol
     * @param value the value of a given symbol
     * @return a string containing a description of the symbol
     */
    @JvmStatic
    fun symbolDescription(symId: Int, value: Any?): String {
        return when (symId) {
            sym.IXI_FILE -> "Ixi File"
            sym.XI_FILE -> "Xi File"
            sym.USE -> "use"
            sym.IF -> "if"
            sym.WHILE -> "while"
            sym.ELSE -> "else"
            sym.RETURN -> "return"
            sym.LENGTH -> "length"
            sym.FREE -> "free"
            sym.TYPE_INT -> "int"
            sym.TYPE_BOOL -> "bool"
            sym.TYPE_FLOAT -> "float"
            sym.BOOL_LITERAL -> value.toString()
            sym.INT_LITERAL -> "integer $value"
            sym.FLOAT_LITERAL -> "float $value"
            sym.INT_MAX -> "integer ${MyLexer.maxIntegerString}"
            sym.CHAR_LITERAL -> "character ${Util.unescapeCharacterString(value.toString())}"
            sym.STRING_LITERAL -> "string ${Util.unescapeString(value.toString())}"
            sym.ID -> "id $value"
            sym.L_PAREN -> "("
            sym.R_PAREN -> ")"
            sym.L_SQ_BRKT -> "["
            sym.R_SQ_BRKT -> "]"
            sym.L_BRACE -> "{"
            sym.R_BRACE -> "}"
            sym.COLON -> ":"
            sym.SEMICOLON -> ";"
            sym.COMMA -> ","
            sym.UNDERSCORE -> "_"
            sym.ASSIGN -> "="
            sym.PLUS -> "+"
            sym.MINUS -> "-"
            sym.MULT -> "*"
            sym.HIGH_MULT -> "*>>"
            sym.DIVIDE -> "/"
            sym.REMAINDER -> "%"
            sym.NEG_BOOL -> "!"
            sym.LT -> "<"
            sym.LTE -> "<="
            sym.GT -> ">"
            sym.GTE -> ">="
            sym.EQUALS -> "=="
            sym.NOT_EQUALS -> "!="
            sym.LOGICAL_AND -> "&"
            sym.LOGICAL_OR -> "|"
            sym.EOF -> "EOF"
            sym.FOR -> "for"
            sym.DO -> "do"
            sym.PURE -> "pure"
            sym.error -> throw RuntimeException("Lexer should not output the parser error symbol: $value")
            else -> throw RuntimeException("Token $symId is missing a description.")
        }
    }
}
