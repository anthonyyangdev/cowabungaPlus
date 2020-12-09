package cyr7.typedNodes

import cyr7.semantics.types.XiType
import cyr7.typedNodes.visitor.ITypedAstVisitor
import java_cup.runtime.ComplexSymbolFactory

interface TypedNode {
    val location: ComplexSymbolFactory.Location
    val type: XiType
    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    fun <T> accept(visitor: ITypedAstVisitor<T>): T
    fun children(): List<TypedNode>
}
