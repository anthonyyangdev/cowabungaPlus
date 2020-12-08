package cyr7.ast.toplevel;

import java.util.Collections;
import java.util.List;

import cyr7.ast.AbstractNode;
import cyr7.ast.Node;
import cyr7.util.Util;
import cyr7.visitor.AstVisitor;
import java_cup.runtime.ComplexSymbolFactory;

public final class IxiProgramNode extends AbstractNode {

    public final List<FunctionHeaderDeclNode> functionDeclarations;

    public IxiProgramNode(ComplexSymbolFactory.Location location,
                          List<FunctionHeaderDeclNode> lst) {
        super(location);

        assert lst != null;

        this.functionDeclarations = Util.immutableCopy(lst);
    }

    @Override
    public List<Node> getChildren() {
        return Collections.unmodifiableList(functionDeclarations);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof IxiProgramNode) {
            IxiProgramNode oNode = (IxiProgramNode) o;
            return this.functionDeclarations.equals(oNode.functionDeclarations);
        }
        return false;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }

}
