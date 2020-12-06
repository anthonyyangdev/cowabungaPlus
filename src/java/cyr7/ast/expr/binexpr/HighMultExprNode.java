package cyr7.ast.expr.binexpr;

import cyr7.ast.expr.ExprNode;
import cyr7.visitor.AbstractVisitor;
import java_cup.runtime.ComplexSymbolFactory.Location;

/**
 * Node that represents the expr: [ExprNode left] *>> [ExprNode right] where the
 * high multiplication operator returns the highest 64 bit of the 128 bit
 * multiplication operation
 */
public final class HighMultExprNode extends BinOpExprNode {

    public HighMultExprNode(Location location, ExprNode left, ExprNode right) {
        super(location, BinOpExprNode.OpType.HIGH_MUL, left, right);
    }

}
