package cyr7.ir.nodes

import cyr7.ir.visit.IRVisitor
import cyr7.semantics.types.PrimitiveType
import cyr7.visitor.MyIRVisitor
import edu.cornell.cs.cs4120.util.SExpPrinter
import java_cup.runtime.ComplexSymbolFactory
import java.util.*

class IRCast(
        location: ComplexSymbolFactory.Location,
        val value: IRExpr,
        val fromType: PrimitiveType,
        val targetType: PrimitiveType
): IRExpr_c(location) {
    override fun label() = "$targetType"

    override fun printSExp(p: SExpPrinter) {
        p.startList()
        p.printAtom("Cast from $fromType to $targetType")
        value.printSExp(p)
        p.endList()
    }

    override fun hashCode(): Int {
        return Objects.hash(value, fromType, targetType)
    }

    override fun <T> accept(v: MyIRVisitor<T>): T {
        return v.visit(this)
    }

    override fun userFriendlyString(): String {
        return "($targetType from $fromType) ${value.userFriendlyString()}"
    }

    override fun visitChildren(v: IRVisitor): IRNode {
        val value = v.visit(this, value) as IRExpr
        return if (value !== this.value)
            v.nodeFactory().IRCast(value, fromType, targetType) else this
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return true
    }
}
