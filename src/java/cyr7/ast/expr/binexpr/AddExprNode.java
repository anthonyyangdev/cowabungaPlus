package cyr7.ast.expr.binexpr;

import cyr7.ast.expr.ExprNode;
import cyr7.visitor.AbstractVisitor;
import java_cup.runtime.ComplexSymbolFactory.Location;

/**
 * Node that represents the expression [ExprNode left] + [ExprNode right]
 */
public final class AddExprNode extends BinOpExprNode {

    public AddExprNode(Location location, ExprNode left,
                       ExprNode right) {
        super(location, OpType.ADD, left, right);
    }
}
