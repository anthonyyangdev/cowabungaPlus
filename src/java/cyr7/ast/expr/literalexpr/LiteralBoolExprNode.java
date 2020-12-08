package cyr7.ast.expr.literalexpr;

import cyr7.ast.Node;
import cyr7.ast.expr.AbstractExprNode;
import cyr7.visitor.AstVisitor;
import java_cup.runtime.ComplexSymbolFactory.Location;

import java.util.List;

/**
 * Represents a boolean literal of either true or false
 */
public final class LiteralBoolExprNode extends AbstractExprNode {
    public final boolean contents;

    public LiteralBoolExprNode(Location location, boolean contents) {

        super(location);
        this.contents = contents;

    }

    @Override
    public List<Node> getChildren() {
        return List.of();
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof LiteralBoolExprNode) {
            LiteralBoolExprNode oNode = (LiteralBoolExprNode) o;
            return this.contents == oNode.contents;
        }
        return false;
    }

}
