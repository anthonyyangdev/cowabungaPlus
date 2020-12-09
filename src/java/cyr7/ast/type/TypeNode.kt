package cyr7.ast.type

import cyr7.ast.Node
import cyr7.ast.expr.ExprNode
import cyr7.visitor.AstVisitor
import java_cup.runtime.ComplexSymbolFactory.Location

sealed class TypeNode(location: Location): TypeExprNode(location) {
    class UnionTypeNode(loc: Location, vararg val types: TypeNode): TypeNode(loc) {
        override fun equals(other: Any?): Boolean {
            return other is UnionTypeNode && other.types.contentEquals(types)
        }
        override fun <T> accept(visitor: AstVisitor<T>): T {
            TODO("Not yet implemented")
        }
        override fun hashCode(): Int {
            return types.contentHashCode()
        }
        override val children: List<Node>
            get() = listOf(*types)
    }
    class TupleTypeNode(loc: Location, vararg val types: TypeNode): TypeNode(loc) {
        override fun equals(other: Any?): Boolean {
            return other is TupleTypeNode && other.types.contentEquals(types)
        }
        override fun <T> accept(visitor: AstVisitor<T>): T {
            TODO("Not yet implemented")
        }
        override fun hashCode(): Int {
            return types.contentHashCode()
        }
        override val children: List<Node>
            get() = listOf(*types)
    }
    class PrimitiveTypeNode(override val location: Location, val type: PrimitiveEnum): TypeNode(location) {
        override fun equals(other: Any?): Boolean {
            return other is PrimitiveTypeNode && other.type == type
        }
        override fun <T> accept(visitor: AstVisitor<T>): T {
            TODO("Not yet implemented")
        }
        override fun hashCode(): Int {
            var result = location.hashCode()
            result = 31 * result + type.hashCode()
            return result
        }
        override val children: List<Node>
            get() = emptyList()
    }
    class TypeExprArrayNode(val loc: Location, val child: TypeNode, val size: ExprNode?): TypeNode(loc) {
        override fun equals(other: Any?): Boolean {
            return other is TypeExprArrayNode && other.child == child && other.size == size
        }
        override fun <T> accept(visitor: AstVisitor<T>): T {
            TODO("Not yet implemented")
        }
        override fun hashCode(): Int {
            var result = loc.hashCode()
            result = 31 * result + child.hashCode()
            result = 31 * result + (size?.hashCode() ?: 0)
            return result
        }
        override val children: List<Node>
            get() = listOfNotNull(child, size)
    }
}


/**
 * @param - primitive type of the larger type
 * @param - dimensionList - the list of array dimensions associated with
 *          the type, with Optional.empty()
 *          representing no size was given for that dimension.
 *          dimensionList must be passed in order, i.e. to create
 *          the type for int[4][3][], we pass in: {Optional.of(4),
 *          Optional.of(3), Optional.empty()}
 * @return [TypeNode.TypeExprArrayNode] - an ITypeExprNode representing a recursive definition
 * of the type of the object
 */
fun fromDimensionList(
        primitive: TypeNode.PrimitiveTypeNode,
        dimensionList: List<ExprNode?>
): TypeNode.TypeExprArrayNode {
    assert(dimensionList.isNotEmpty())
    return dimensionList.reversed().fold<ExprNode?, TypeNode>(primitive) {acc, expr ->
        TypeNode.TypeExprArrayNode(acc.location, acc, expr)
    } as TypeNode.TypeExprArrayNode
}
