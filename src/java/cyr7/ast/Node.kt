package cyr7.ast

import cyr7.visitor.AbstractVisitor
import java_cup.runtime.ComplexSymbolFactory

interface Node {
    val location: ComplexSymbolFactory.Location
    override fun equals(other: Any?): Boolean
    fun <T> accept(visitor: AbstractVisitor<T>): T
    val children: List<Node>
}
