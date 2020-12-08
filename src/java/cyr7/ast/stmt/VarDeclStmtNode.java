package cyr7.ast.stmt;

import cyr7.ast.AbstractNode;
import cyr7.ast.Node;
import cyr7.visitor.AstVisitor;
import java_cup.runtime.ComplexSymbolFactory.Location;

import java.util.List;
import java.util.Objects;

/**
 * A statement of the form
 * x:t
 */
public final class VarDeclStmtNode extends AbstractNode implements StmtNode {

    public final VarDeclNode varDecl;

    public VarDeclStmtNode(Location location, VarDeclNode varDecl) {
        super(location);

        this.varDecl = varDecl;
    }

    @Override
    public List<Node> getChildren() {
        return List.of(varDecl);
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VarDeclStmtNode that = (VarDeclStmtNode) o;
        return Objects.equals(varDecl, that.varDecl);
    }

}
