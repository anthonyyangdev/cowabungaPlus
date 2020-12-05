package cyr7.ir.nodes

import cyr7.ir.visit.AggregateVisitor
import cyr7.ir.visit.CheckCanonicalIRVisitor
import cyr7.ir.visit.CheckConstFoldedIRVisitor
import cyr7.ir.visit.IRVisitor
import cyr7.ir.visit.InsnMapsBuilder
import cyr7.visitor.MyIRVisitor
import cyr7.x86.tiler.TilerData
import edu.cornell.cs.cs4120.util.SExpPrinter
import java_cup.runtime.ComplexSymbolFactory.Location

/**
 * A node in an intermediate-representation abstract syntax tree.
 */
interface IRNode {

    /**
     * Visit the children of this IR node.
     * @param v the visitor
     * @return the result of visiting children of this node
     */
    fun visitChildren(v: IRVisitor): IRNode

    fun <T>aggregateChildren(v: AggregateVisitor<T>): T

    fun buildInsnMapsEnter(v: InsnMapsBuilder): InsnMapsBuilder

    fun buildInsnMaps(v: InsnMapsBuilder): IRNode

    fun checkCanonicalEnter(v: CheckCanonicalIRVisitor): CheckCanonicalIRVisitor

    fun isCanonical(v: CheckCanonicalIRVisitor): Boolean

    fun isConstFolded(v: CheckConstFoldedIRVisitor): Boolean

    /**
     * String label that represents the node value/structure.
     */
    fun label(): String

    fun <T> accept(v: MyIRVisitor<T>): T

    fun hasOptimalTiling(): Boolean

    fun getOptimalTiling(): TilerData

    fun setOptimalTilingOnce(tilerData: TilerData)

    /**
     * Print an S-expression representation of this IR node.
     * @param p the S-expression printer
     */
    fun printSExp(p: SExpPrinter)

    fun location(): Location

    fun userFriendlyString(): String

}
