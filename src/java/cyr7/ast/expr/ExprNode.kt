package cyr7.ast.expr

import cyr7.ast.Node
import cyr7.semantics.types.ExpandedType

/**
 * Represents a generic expression: Ex: 1+1, true, arr[2][3]
 */
interface ExprNode : Node {
    val type: ExpandedType?
    fun setType(t: ExpandedType): ExprNode
}
