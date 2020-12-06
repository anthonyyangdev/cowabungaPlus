package cyr7.typecheck

import cyr7.exceptions.parser.ParserException
import cyr7.exceptions.semantics.SemanticException
import cyr7.parser.ParserUtil
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.StringReader
import java.util.function.Function

internal class TestEdgeCases {
    private val funcs = """
        f(): int, int { return 1, 2} g() {} 
        h(i: int): int { return 0 } p(i: int, j: int) {} 
        empty(): int[] { return {}; }
    """.trimIndent()

    /**
     * Creates a program string with pre-defined functions.
     *
     *
     * f: () -> int, int
     *
     *
     * g: () -> ()
     *
     *
     * h: int -> int
     *
     *
     * p: int, int -> ()
     *
     *
     * empty: () -> int[]
     */
    var create = Function { s: String -> "$funcs\n $s" }
    @Throws(Exception::class)
    fun test(prgm: String) {
        ParserUtil.parseNode(StringReader(prgm), "test.xi", false)
                .accept(TypeCheckVisitor(null))
    }

    @Throws(Exception::class)
    fun testInterface(prgm: String) {
        ParserUtil.parseNode(StringReader(prgm), "test.ixi", true)
                .accept(TypeCheckVisitor(null))
    }

    @Test
    fun testArrayInit() {
        val p1 = create.apply("main() { i: int[true]; }")
        val p2 = create.apply("main() { i: int[f()]; }")
        val p3 = create.apply("main() { i: int[g()]; }")
        Assertions.assertThrows(SemanticException::class.java) { test(p1) }
        Assertions.assertThrows(SemanticException::class.java) { test(p2) }
        Assertions.assertThrows(SemanticException::class.java) { test(p3) }
    }

    @Test
    fun assignToWildcard() {
        val p1 = create.apply("main() { _ = 12; }")
        val p2 = create.apply("main() { _ = {2, 34, 5}; }")
        val p3 = create.apply("main() { _ = h(3); }")
        val p4 = create.apply("main() { _ = (h(3)); }")
        val p5 = create.apply("main() { _ = (h(3) + h(3)); }")
        val p6 = create.apply("main() { _ = f(); }")
        val p7 = create.apply("main() { _ = g(); }")
        Assertions.assertThrows(SemanticException::class.java) { test(p1) }
        Assertions.assertThrows(SemanticException::class.java) { test(p2) }
        Assertions.assertDoesNotThrow { test(p3) }
        Assertions.assertDoesNotThrow { test(p4) }

        // Because RHS is a sum, not a function call.
        Assertions.assertThrows(SemanticException::class.java) { test(p5) }
        Assertions.assertThrows(SemanticException::class.java) { test(p6) }
        Assertions.assertThrows(SemanticException::class.java) { test(p7) }
    }

    @Test
    fun tupleAndUnitTypesDoNotWrap() {

        // Nonordinary expressions in return types.
        val p1 = create.apply("main(): int, int { return f(); }")
        val p2 = create.apply("main(): int, int, int { return 2, f(); }")
        val p3 = create.apply("main() { return g(); }")
        Assertions.assertThrows(SemanticException::class.java) { test(p1) }
        Assertions.assertThrows(SemanticException::class.java) { test(p2) }
        Assertions.assertThrows(SemanticException::class.java) { test(p3) }

        // Nonordinary expressions as array elements
        val p4 = create.apply("main() { i: int[] = {f()}; }")
        val p5 = create.apply("main() { i: int[] = {g()}; }")
        val p6 = create.apply("main() { i: int[] = {h(1)}; }")
        Assertions.assertThrows(SemanticException::class.java) { test(p4) }
        Assertions.assertThrows(SemanticException::class.java) { test(p5) }
        Assertions.assertDoesNotThrow { test(p6) }

        // Nonordinary expressions in function calls
        val p7 = create.apply("main() { p(f()); }")
        val p8 = create.apply("main() { g(g()); }")
        val p9 = create.apply("main() { p(1, 3); }")
        Assertions.assertThrows(SemanticException::class.java) { test(p7) }
        Assertions.assertThrows(SemanticException::class.java) { test(p8) }
        Assertions.assertDoesNotThrow { test(p9) }
        val p10 = create.apply("main() {return main()}")
        Assertions.assertThrows(SemanticException::class.java) { test(p10) }
    }

    @Test
    fun badCompare() {
        val good1 = create.apply(
                "main(): bool, bool { return 1 == 1, 1 <= 1; }")
        val good2 = create.apply(
                "main(): bool, bool { return h(3) == 1, 1 <= h(1); }")
        val p1 = create.apply("main(): bool { return f() == f(); }")
        val p2 = create.apply("main(): bool { return g() == g(); }")
        val p3 = create.apply("main(): bool { return f() == g(); }")
        Assertions.assertDoesNotThrow { test(good1) }
        Assertions.assertDoesNotThrow { test(good2) }
        Assertions.assertThrows(SemanticException::class.java) { test(p1) }
        Assertions.assertThrows(SemanticException::class.java) { test(p2) }
        Assertions.assertThrows(SemanticException::class.java) { test(p3) }
    }

    @Test
    fun allowCertainTrailingCommas() {
        val good1 = create.apply("main (): int[] { return {1,2,4,5,}; }")
        val good2 = create.apply("main(): int[] {return {1,   }; }")
        val good3 = create.apply("main (): int[], int[] { return {}, {}; }")
        val bad1 = create.apply("main (): int[] { return {,}; }")
        val bad2 = create.apply("main (): int[], int[], { return {}, {}; }")
        val bad3 = create.apply("main (): int[] { return {{}}; }")
        Assertions.assertDoesNotThrow { test(good1) }
        Assertions.assertDoesNotThrow { test(good2) }
        Assertions.assertDoesNotThrow { test(good3) }
        Assertions.assertThrows(ParserException::class.java) { test(bad1) }
        Assertions.assertThrows(ParserException::class.java) { test(bad2) }
        Assertions.assertThrows(SemanticException::class.java) { test(bad3) }
    }

    @Test
    fun assignToFunction() {
        val good1 = create.apply("main() { empty()[0] = 12;}")
        val good2 = create.apply("main() { i: int = empty()[0];}")
        val parseBad1 = create.apply("main() { {}[0] = true;}")
        val parseBad2 = create.apply("main() { (empty())[0] = true;}")
        val semanticBad1 = create.apply("main() { empty()[0] = true;}")
        val semanticBad2 = create.apply("main() { i: bool = empty()[0];}")
        val semanticBad3 = create.apply("main() { empty()[0][0] = 2;}")
        Assertions.assertDoesNotThrow { test(good1) }
        Assertions.assertDoesNotThrow { test(good2) }
        Assertions.assertThrows(ParserException::class.java) { test(parseBad1) }
        Assertions.assertThrows(ParserException::class.java) { test(parseBad2) }
        Assertions.assertThrows(SemanticException::class.java) { test(semanticBad1) }
        Assertions.assertThrows(SemanticException::class.java) { test(semanticBad2) }
        Assertions.assertThrows(SemanticException::class.java) { test(semanticBad3) }
    }

    @Test
    fun multipleSameParamNames() {
        val good1 = create.apply("main1(i: int) { } main2(i: int) { }"
                + " main3(i: bool) { }")
        val good2 = create.apply("main1(i: int, j:bool) { } main2(i: int) { }"
                + " main3(i: bool) { }")
        val semanticBad1 = create.apply("main(i: int, i: bool) { }")
        val semanticBad2 = create.apply("main(i: int, i: int) { }")
        val semanticBad3 = create.apply("main1(i: int, j:bool, i:bool) { } "
                + "main2(i: int) { }"
                + " main3(i: bool) { }")
        Assertions.assertDoesNotThrow { test(good1) }
        Assertions.assertDoesNotThrow { test(good2) }
        Assertions.assertThrows(SemanticException::class.java) { test(semanticBad1) }
        Assertions.assertThrows(SemanticException::class.java) { test(semanticBad2) }
        Assertions.assertThrows(SemanticException::class.java) { test(semanticBad3) }
    }

    @Test
    fun lengthTest() {
        val parseBad1 = create.apply("main() { length({}); }")
        val semanticBad2 = create.apply("main() { return length({}); }")
        val parseBad3 = create.apply("main(): int { return length(); }")
        val semanticBad4 = create.apply("main(): int { return length(2); }")
        val semanticBad5 = create.apply(
                "main(): int { return length(true); }")
        val good1 = create.apply("main() { _ = length({1,4,6,7}); }")
        val good2 = create.apply(
                "main(): int { return length({1,4,6,7}); }")
        val good3 = create.apply(
                "main(): int { return length({}[0][0][0][0]); }")
        Assertions.assertThrows(ParserException::class.java) { test(parseBad1) }
        Assertions.assertThrows(SemanticException::class.java) { test(semanticBad2) }
        Assertions.assertThrows(ParserException::class.java) { test(parseBad3) }
        Assertions.assertThrows(SemanticException::class.java) { test(semanticBad4) }
        Assertions.assertThrows(SemanticException::class.java) { test(semanticBad5) }
        Assertions.assertDoesNotThrow { test(good1) }
        Assertions.assertDoesNotThrow { test(good2) }
        Assertions.assertDoesNotThrow { test(good3) }
    }

    /**
     * Test cases for when a void type value is used in addition.
     */
    @Test
    fun testAdding() {
        val good1 = create.apply("main() { "
                + "i: int = {}[0] + {}[0]; "
                + "r: bool = i < 3;}")
        val good2 = create.apply("main() { "
                + "i: int = (2 + {}[0]) + 5; }")
        val good3 = create.apply("main() { "
                + "i: int = ({}[0] + 2) + 5; }")
        val good4 = create.apply("main() { "
                + "i: int[] = ({}[0] + {2}) + {5}; }")
        val good5 = create.apply("main() { "
                + "i: int[] = ({}[0] + {}); }")
        val good6 = create.apply("main() { "
                + "i: int = ({}[0] + \"\"[0]); }")
        val good7 = create.apply("main() { "
                + "i: int = ({}[0] + {}[0]); }")
        val good8 = create.apply("main() { "
                + "i: bool = {}[0] + {}[0] == {}; }")
        val good9 = create.apply("main() { "
                + "i: bool = {}[0] + {}[0] == {1, 2, 4}; }")
        val good10 = create.apply("main() { "
                + "i: bool = {}[0] + {}[0] == 32; }")
        val good11 = create.apply("main() { "
                + "i: int[] = ({}[0] + {}[0]) + {32}; }")
        val good12 = create.apply("main() { "
                + "i: int = length({}[0] + {}[0]); }")
        val good13 = create.apply("main() { "
                + "i: int = ({}[0] + {}[0])[0]; }")
        val good14 = create.apply("main() { "
                + "i: int = (({}[0] + {}[0])[0][0] + {}[0])[0]; }")
        val good15 = create.apply("main() { "
                + "i: int = {}[0] + {}[0] + {}[0]; }")
        val good16 = create.apply("main() { "
                + "i: int[] = {}[0] + {}[0] + {}[0]; }")
        val good17 = create.apply("main() { "
                + "i: int[] = {{}[0] + {}[0] + {}[0]}; }")
        val good18 = create.apply("main() { "
                + "i: int[] = \"Hello\" + {}[0] + {}[0] + {}[0] + \"World\"; }")
        val good19 = create.apply("main() { "
                + "p({}[0] + {}[0], ({}[0] + {}[0])/2);}")
        val good20 = create.apply("main() { "
                + "a: bool = ({}[0] + {}[0]) == 12 }")
        val good21 = create.apply("main(i: int[]) { "
                + "main({}[0] + {}[0]) }")
        val good22 = create.apply("main(i: int[]) { "
                + "a: bool = ({}[0] + {}[0]) < 12; }")
        val good23 = create.apply("main(i: int[]) { "
                + "a: bool = length({}[0] + {}[0]) < 12; }")
        val good24 = create.apply("main(i: int[]): int[] { "
                + "return main({}[0] + {}[0]);}")
        val bad1 = create.apply("main() { i: int[] = {}[0] + {}[0]; "
                + "r: bool = i < 3; }")
        val bad2 = create.apply("main() { "
                + "i: bool = ({}[0] + {}[0]) & true; }")
        val bad3 = create.apply("main() { "
                + "i: int = ({}[0] + {}); }")
        val bad4 = create.apply("main() { "
                + "i: int = ({}[0] & {}[0]) + 3; }")
        val bad5 = create.apply("main() { "
                + "i: bool[] = {{}[0] + {}[0] + {}[0]}; }")
        Assertions.assertDoesNotThrow { test(good1) }
        Assertions.assertDoesNotThrow { test(good2) }
        Assertions.assertDoesNotThrow { test(good3) }
        Assertions.assertDoesNotThrow { test(good4) }
        Assertions.assertDoesNotThrow { test(good5) }
        Assertions.assertDoesNotThrow { test(good6) }
        Assertions.assertDoesNotThrow { test(good7) }
        Assertions.assertDoesNotThrow { test(good8) }
        Assertions.assertDoesNotThrow { test(good9) }
        Assertions.assertDoesNotThrow { test(good10) }
        Assertions.assertDoesNotThrow { test(good11) }
        Assertions.assertDoesNotThrow { test(good12) }
        Assertions.assertDoesNotThrow { test(good13) }
        Assertions.assertDoesNotThrow { test(good14) }
        Assertions.assertDoesNotThrow { test(good15) }
        Assertions.assertDoesNotThrow { test(good16) }
        Assertions.assertDoesNotThrow { test(good17) }
        Assertions.assertDoesNotThrow { test(good18) }
        Assertions.assertDoesNotThrow { test(good18) }
        Assertions.assertDoesNotThrow { test(good19) }
        Assertions.assertDoesNotThrow { test(good20) }
        Assertions.assertDoesNotThrow { test(good21) }
        Assertions.assertDoesNotThrow { test(good22) }
        Assertions.assertDoesNotThrow { test(good23) }
        Assertions.assertDoesNotThrow { test(good24) }
        Assertions.assertThrows(SemanticException::class.java) { test(bad1) }
        Assertions.assertThrows(SemanticException::class.java) { test(bad2) }
        Assertions.assertThrows(SemanticException::class.java) { test(bad3) }
        Assertions.assertThrows(SemanticException::class.java) { test(bad4) }
        Assertions.assertThrows(SemanticException::class.java) { test(bad5) }
    }

    @Test
    fun returnBlock() {
        val bad1 = create.apply("main() { if (true) return else return}")
        Assertions.assertThrows(ParserException::class.java) { test(bad1) }
        val bad2 = create.apply(
                "main() { if (true) { return } else return}")
        Assertions.assertThrows(ParserException::class.java) { test(bad2) }
        val bad3 = create.apply(
                "main() { if (true) return else { return} }")
        Assertions.assertThrows(ParserException::class.java) { test(bad3) }
        val bad4 = create.apply("main() { if (true) return }")
        Assertions.assertThrows(ParserException::class.java) { test(bad4) }
        val bad5 = create.apply(
                "main() { if (true) i: int = 4 else return}")
        Assertions.assertThrows(ParserException::class.java) { test(bad5) }
        val good1 = create.apply(
                "main() { if (true) i: int = 4 else {return }}")
        Assertions.assertDoesNotThrow { test(good1) }
        val good2 = create.apply(
                "main() { if (true) {return} else {return }}")
        Assertions.assertDoesNotThrow { test(good2) }
        val good3 = create.apply("main() {"
                + "{{{return; }}}"
                + "}")
        Assertions.assertDoesNotThrow { test(good3) }
    }

    @Test
    fun blockTests() {
        val good1 = create.apply("main(): int[] {"
                + "{}{}{}{}{}{}{}{}{}{} return {1,2,3};"
                + "}")
        Assertions.assertDoesNotThrow { test(good1) }
        val good2 = create.apply("main(): int[] {"
                + "{}{}{}let: int = 12{}{}{}{}{}{return {let}}"
                + "}")
        Assertions.assertDoesNotThrow { test(good2) }
        val bad1 = create.apply("main(): int[] {"
                + "{}{}{}let: int = 12{}{}{}{}{return {let}}{}"
                + "}")
        Assertions.assertThrows(SemanticException::class.java) { test(bad1) }

        // i becomes out of scope.
        val bad2 = create.apply("main() : int {"
                + " {i: int = 4;}"
                + "i = 12; return i; }")
        Assertions.assertThrows(SemanticException::class.java) { test(bad2) }

        // i gets declared in an accessible scope.
        val bad3 = create.apply("main() : int {"
                + "i: int; {i: int = 4;}"
                + "i = 12; return i; }")
        Assertions.assertThrows(SemanticException::class.java) { test(bad3) }

        // Try not to confuse arrays with statement blocks
        val bad4 = create.apply("main() : int {"
                + "i: int[] = {return h(45)}}")
        Assertions.assertThrows(ParserException::class.java) { test(bad4) }
    }

    @Test
    fun sameLineAssign() {
        val bad1 = create.apply("main() { i: int = i; }")
        Assertions.assertThrows(SemanticException::class.java) { test(bad1) }
        val bad2 = create.apply("main() : int {"
                + "return i: int;"
                + "}")
        Assertions.assertThrows(ParserException::class.java) { test(bad2) }
    }

    /**
     * These are test cases created based on posts/responses made by other
     * students/course staff on the CS4120 Piazza.
     *
     *
     * Each test program is named in the form p[num], where [num] is the
     * post number on Piazza.
     */
    @Test
    fun piazzaExamplePrograms() {
        val p194 = """
             main() { f(g()) }
             f(a:int, b: int) { }
             g(): int, int { return 1, 1 }
             """.trimIndent()
        Assertions.assertThrows(SemanticException::class.java) { test(p194) }
        val p190 = """
             foo() { }
             bar() { x:int = foo() }
             """.trimIndent()
        Assertions.assertThrows(SemanticException::class.java) { test(p190) }
        val p186 = """main() {
  if (true) return
  a:int = 2
}"""
        // Because return statement is not the last statement in a block.
        Assertions.assertThrows(ParserException::class.java) { test(p186) }
        val p187 = """f(a: int[]) : int[] {
  b: int[] = a
  return b
}
main() {
   x: int[] = { 1, 2, 3, 4 }
   f(x)[0] = 42
   println(unparseInt(x[0]))
}

unparseInt(i: int): int[] { return "" }println(s: int[]) { return; }"""
        Assertions.assertDoesNotThrow { test(p187) }
        val p185 = """
             foo1 (a: int) {}
             foo2 (a: int) {}
             """.trimIndent()
        Assertions.assertDoesNotThrow { test(p185) }
        val p179 = """foo(){
            a:int[][] = {}[0][0][0][0][0][0][0][0][0][0][0]
        }""".trimIndent()
        Assertions.assertDoesNotThrow { test(p179) }
        val p176 = """
             main() { x: int[]; x = {1,}
             x = {1,2,3,} }
             """.trimIndent()
        Assertions.assertDoesNotThrow { test(p176) }
        val p175 = """f() : int {
            { return 5 }
            c: int = 5
        }"""
        Assertions.assertThrows(SemanticException::class.java) { test(p175) }
    }

    @Test
    fun useFunction() {
        val p1 = "main(i: int) { i: int = 12;}"
        Assertions.assertThrows(SemanticException::class.java) { test(p1) }
        val p2 = "i(i: int) { i = 12;}"
        Assertions.assertThrows(SemanticException::class.java) { test(p2) }
    }

    @Test
    fun interfaceFileTest() {
        // As stated in the documentation, and based on type-checking rules
        // it is fine to have multiple identifiers with the same name in the
        // arguments.
        val i1 = "main(i: int, i: int)"
        Assertions.assertDoesNotThrow { testInterface(i1) }
        val i2 = "main(i: int, i: int, i: int)"
        Assertions.assertDoesNotThrow { testInterface(i2) }
    }
}
