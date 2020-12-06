package cyr7.ast.expr.binexpr;

import cyr7.ast.expr.ExprNode;
import cyr7.visitor.AbstractVisitor;
import java_cup.runtime.ComplexSymbolFactory.Location;

/**
 * Node that represents taking the equality operator on [ExprNode left] &
 * [ExprNode right]. Evaluates to true or false
 */
public final class EqualsExprNode extends BinOpExprNode {

    public EqualsExprNode(Location location, ExprNode left, ExprNode right) {
        super(location, OpType.EQ, left, right);
    }

}
