package cyr7.ast;

import cyr7.ast.expr.ExprNode;
import cyr7.ast.stmt.StmtNode;
import cyr7.ast.type.TypeExprNode;
import cyr7.visitor.AbstractVisitor;
import java_cup.runtime.ComplexSymbolFactory.Location;

/**
 * Represents a Variable Declaration, with a String [identifier] and a type [typeExpr] of the initialized
 * variable
 */
public final class VarDeclNode extends AbstractNode 
                                implements StmtNode, ExprNode {

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
