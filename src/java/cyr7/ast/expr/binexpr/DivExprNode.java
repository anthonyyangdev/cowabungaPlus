package cyr7.ast.expr.binexpr;

import cyr7.ast.expr.ExprNode;
import cyr7.visitor.AbstractVisitor;
import java_cup.runtime.ComplexSymbolFactory.Location;

/**
 * Node that represents taking [ExprNode] and dividing (integer division) by
 * [ExprNode right]
 */
public final class DivExprNode extends BinOpExprNode {

    public DivExprNode(Location location, ExprNode left,
                       ExprNode right) {
        super(location, OpType.DIV, left, right);
    }
}
