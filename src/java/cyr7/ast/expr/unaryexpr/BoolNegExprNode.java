package cyr7.ast.expr.unaryexpr;

import cyr7.ast.expr.ExprNode;
import cyr7.exceptions.SemanticException;
import cyr7.semantics.Context;
import cyr7.semantics.PrimitiveType;
import cyr7.semantics.TypeCheckUtil;
import cyr7.semantics.ExpandedType;
import cyr7.visitor.AbstractVisitor;
import edu.cornell.cs.cs4120.util.SExpPrinter;
import java_cup.runtime.ComplexSymbolFactory;

/**
 * Represents the logical negation expression: ![ExprNode expr]
 */
public class BoolNegExprNode extends UnaryExprNode {

    public BoolNegExprNode(ComplexSymbolFactory.Location location,
            ExprNode expr) {
        super(location, expr);
    }

    @Override
    public <T> T accept(AbstractVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public void prettyPrint(SExpPrinter printer) {
        printer.startList();

        printer.printAtom("!");

        expr.prettyPrint(printer);

        printer.endList();
    }

    public boolean equals(Object o) {
        if (o instanceof BoolNegExprNode) {
            return super.equals(o);
        }
        return false;
    }

    @Override
    public ExpandedType typeCheck(Context c) throws SemanticException {
        if (TypeCheckUtil.checkTypeEquality(expr.typeCheck(c),
                PrimitiveType.BOOL))
            return PrimitiveType.BOOL;
        throw new SemanticException("Failed at BOOL NEG");
    }
}