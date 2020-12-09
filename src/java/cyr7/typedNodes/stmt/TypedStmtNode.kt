package cyr7.typedNodes.stmt

import cyr7.semantics.types.XiType
import cyr7.typedNodes.TypedNode

interface TypedStmtNode: TypedNode {
    val resultType: XiType.ResultType
}
