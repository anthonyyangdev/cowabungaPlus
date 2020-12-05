package cyr7.ir.nodes;

import cyr7.ir.visit.CheckCanonicalIRVisitor
import java_cup.runtime.ComplexSymbolFactory.Location

/**
 * An intermediate representation for expressions
 */
abstract class IRExpr_c(val location: Location): IRNode_c(location), IRExpr {

    override fun checkCanonicalEnter(v: CheckCanonicalIRVisitor): CheckCanonicalIRVisitor {
        return v.enterExpr()
    }

    override fun isCanonical(v: CheckCanonicalIRVisitor): Boolean {
        return v.inExpr() || !v.inExp()
    }

    override fun isConstant() = false

    override fun constant(): Long = throw UnsupportedOperationException()
}
