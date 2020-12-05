package cyr7.ir.nodes

import cyr7.ir.visit.*
import cyr7.x86.tiler.TilerData
import edu.cornell.cs.cs4120.util.CodeWriterSExpPrinter
import edu.cornell.cs.cs4120.util.SExpPrinter
import java_cup.runtime.ComplexSymbolFactory
import java.io.PrintWriter
import java.io.StringWriter
import java.util.*

/**
 * A node in an intermediate-representation abstract syntax tree.
 */
abstract class IRNode_c(private val location: ComplexSymbolFactory.Location) : IRNode {
    private var optimalTiling: Optional<TilerData> = Optional.empty()
    override fun setOptimalTilingOnce(tilerData: TilerData) {
        if (optimalTiling.isPresent) {
            throw UnsupportedOperationException()
        }
        optimalTiling = Optional.of(tilerData)
    }

    override fun getOptimalTiling() = optimalTiling.get()
    override fun hasOptimalTiling() = optimalTiling.isPresent

    override fun visitChildren(v: IRVisitor): IRNode = this
    override fun <T> aggregateChildren(v: AggregateVisitor<T>): T = v.unit()
    override fun buildInsnMapsEnter(v: InsnMapsBuilder) = v
    override fun buildInsnMaps(v: InsnMapsBuilder): IRNode = v.addInsn(this).let { this }
    override fun checkCanonicalEnter(v: CheckCanonicalIRVisitor) = v
    override fun isCanonical(v: CheckCanonicalIRVisitor) = true
    override fun isConstFolded(v: CheckConstFoldedIRVisitor) = true

    abstract override fun label(): String
    abstract override fun printSExp(p: SExpPrinter)
    override fun toString(): String {
        val sw = StringWriter()
        PrintWriter(sw).use { pw -> CodeWriterSExpPrinter(pw).use { sp -> printSExp(sp) } }
        return sw.toString()
    }

    override fun location(): ComplexSymbolFactory.Location = location
    abstract override fun hashCode(): Int
}
