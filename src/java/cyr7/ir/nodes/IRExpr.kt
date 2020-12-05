package cyr7.ir.nodes

interface IRExpr: IRNode {
    fun isConstant(): Boolean
    fun constant(): Long
}
