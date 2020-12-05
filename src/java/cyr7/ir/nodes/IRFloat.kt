package cyr7.ir.nodes

import cyr7.visitor.MyIRVisitor
import edu.cornell.cs.cs4120.util.SExpPrinter
import java_cup.runtime.ComplexSymbolFactory.Location

/**
 * Intermediate representation of a floating-point value.
 */
class IRFloat(val loc: Location, val value: Double): IRExpr_c(loc) {
    override fun hashCode(): Int = value.hashCode()
    override fun equals(other: Any?) = other is IRFloat && other.value == this.value

    override fun label(): String = "Float($value)"
    override fun constant(): Long = this.value.toRawBits()

    override fun <T : Any?> accept(v: MyIRVisitor<T>): T {
        return v.visit(this)
    }

    override fun printSExp(p: SExpPrinter) {
        p.startList()
        p.printAtom("FLOAT")
        p.printAtom(value.toString())
        p.endList()
    }

    override fun userFriendlyString() = value.toString()
}
