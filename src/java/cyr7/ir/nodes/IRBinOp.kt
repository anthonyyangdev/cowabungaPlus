package cyr7.ir.nodes

import cyr7.ir.visit.AggregateVisitor
import cyr7.ir.visit.CheckConstFoldedIRVisitor
import cyr7.ir.visit.IRVisitor
import cyr7.visitor.MyIRVisitor
import edu.cornell.cs.cs4120.util.SExpPrinter
import java_cup.runtime.ComplexSymbolFactory
import java.util.*

/**
 * An intermediate representation for a binary operation
 * OP(left, right)
 */
class IRBinOp(
        location: ComplexSymbolFactory.Location,
        private val type: OpType,
        private val left: IRExpr,
        private val right: IRExpr
) : IRExpr_c(location) {

    /**
     * Binary operators
     */
    enum class OpType {
        ADD_INT, SUB_INT, MUL_INT, HMUL_INT, DIV_INT, MOD_INT, AND, OR,
        XOR, LSHIFT, RSHIFT, ARSHIFT, EQ, NEQ, LT, GT, LEQ, GEQ,
        ADD_FLOAT, SUB_FLOAT, MUL_FLOAT, DIV_FLOAT, MOD_FLOAT;

        override fun toString(): String {
            return when (this) {
                ADD_INT -> "ADD_INT"
                SUB_INT -> "SUB_INT"
                MUL_INT -> "MUL_INT"
                HMUL_INT -> "HMUL_INT"
                DIV_INT -> "DIV_INT"
                MOD_INT -> "MOD_INT"
                AND -> "AND"
                OR -> "OR"
                XOR -> "XOR"
                LSHIFT -> "LSHIFT"
                RSHIFT -> "RSHIFT"
                ARSHIFT -> "ARSHIFT"
                EQ -> "EQ"
                NEQ -> "NEQ"
                LT -> "LT"
                GT -> "GT"
                LEQ -> "LEQ"
                GEQ -> "GEQ"
                ADD_FLOAT -> "ADD_FLOAT"
                SUB_FLOAT -> "SUB_FLOAT"
                MUL_FLOAT -> "MUL_FLOAT"
                DIV_FLOAT -> "DIV_FLOAT"
                MOD_FLOAT -> "MOD_FLOAT"
            }
        }

        val isCmpOp: Boolean
            get() = this == EQ || this == NEQ || this == LT || this == GT || this == LEQ || this == GEQ
    }

    fun opType(): OpType {
        return type
    }

    fun left(): IRExpr {
        return left
    }

    fun right(): IRExpr {
        return right
    }

    override fun label(): String {
        return type.toString()
    }

    override fun visitChildren(v: IRVisitor): IRNode {
        val left = v.visit(this, left) as IRExpr
        val right = v.visit(this, right) as IRExpr
        return if (left !== this.left || right !== this.right)
                v.nodeFactory().IRBinOp(type, left, right) else this
    }

    override fun <T> aggregateChildren(v: AggregateVisitor<T>): T {
        return v.unit().let {
            v.bind(it, v.visit(left)).let { r ->
                v.bind(r, v.visit(right))
            }
        }
    }

    override fun isConstFolded(v: CheckConstFoldedIRVisitor): Boolean {
        return if (isConstant()) {
            when (type) {
                OpType.DIV_INT, OpType.MOD_INT -> right.constant() == 0L
                else -> false
            }
        } else true
    }

    override fun isConstant(): Boolean {
        return left.isConstant() && right.isConstant()
    }

    override fun printSExp(p: SExpPrinter) {
        p.startList()
        p.printAtom(type.toString())
        left.printSExp(p)
        right.printSExp(p)
        p.endList()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val irBinOp = other as IRBinOp
        return type == irBinOp.type &&
                left == irBinOp.left &&
                right == irBinOp.right
    }

    override fun hashCode(): Int {
        return Objects.hash(type, left, right)
    }

    override fun <T> accept(v: MyIRVisitor<T>): T {
        return v.visit(this)
    }

    override fun userFriendlyString(): String {
        val lhs = left.userFriendlyString()
        val rhs = right.userFriendlyString()
        val operation: String = when (type) {
            OpType.ADD_INT, OpType.ADD_FLOAT -> " + "
            OpType.AND -> " & "
            OpType.ARSHIFT -> " >> "
            OpType.DIV_INT, OpType.DIV_FLOAT -> " / "
            OpType.EQ -> " == "
            OpType.GEQ -> " >= "
            OpType.GT -> " > "
            OpType.HMUL_INT -> " *>> "
            OpType.LEQ -> " <= "
            OpType.LSHIFT -> " << "
            OpType.LT -> " < "
            OpType.MOD_INT, OpType.MOD_FLOAT -> " % "
            OpType.MUL_INT, OpType.MUL_FLOAT -> " * "
            OpType.NEQ -> " != "
            OpType.OR -> " | "
            OpType.RSHIFT -> " >>> "
            OpType.SUB_INT, OpType.SUB_FLOAT -> " - "
            OpType.XOR -> " ^ "
        }
        return lhs + operation + rhs
    }
}
