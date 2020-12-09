package cyr7.typedNodes.expr

import cyr7.semantics.types.XiType
import cyr7.typedNodes.TypedNode

interface TypedExprNode: TypedNode {
    val expandedType: XiType.ExpandedType
}
