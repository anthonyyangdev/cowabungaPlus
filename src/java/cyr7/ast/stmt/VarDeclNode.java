package cyr7.ast.stmt;

import cyr7.ast.Node;
import cyr7.ast.expr.AbstractExprNode;
import cyr7.ast.type.TypeExprNode;
import cyr7.visitor.AbstractVisitor;
import java_cup.runtime.ComplexSymbolFactory.Location;

import java.util.List;

/**
 * Represents a Variable Declaration, with a String [identifier] and a type [typeExpr] of the initialized
 * variable
 */
public final class VarDeclNode extends AbstractExprNode implements StmtNode {

    public final String identifier;
    public final TypeExprNode typeExpr;

    public VarDeclNode(Location location, String identifier,
                                TypeExprNode typeExpr) {
        super(location);

        assert identifier != null;
        assert typeExpr != null;

        this.identifier = identifier;
        this.typeExpr = typeExpr;
    }

    @Override
    public List<Node> getChildren() {
        return List.of(typeExpr);
    }

    @Override
    public <T> T accept(AbstractVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VarDeclNode that = (VarDeclNode) o;
        return identifier.equals(that.identifier) &&
            typeExpr.equals(that.typeExpr);
    }

}
