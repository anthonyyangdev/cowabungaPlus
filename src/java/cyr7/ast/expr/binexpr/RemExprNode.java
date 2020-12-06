package cyr7.ast.expr.binexpr;

import cyr7.ast.expr.ExprNode;
import cyr7.visitor.AbstractVisitor;
import java_cup.runtime.ComplexSymbolFactory.Location;

/**
 * Node that represents the expression [ExprNode left] % [ExprNode right], or
 * the remainder when [ExprNode left] is divided by [ExprNode right]
 */
public final class RemExprNode extends BinOpExprNode {

    public RemExprNode(Location location, ExprNode left, ExprNode right) {
        super(location, OpType.REM, left, right);
    }

}
