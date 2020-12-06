package cyr7.ast.expr;

import cyr7.ast.Node;
import cyr7.semantics.types.ExpandedType;

/**
 * Represents a generic expression: Ex: 1+1, true, arr[2][3]
 */
public interface ExprNode extends Node {

    ExpandedType getType();

    ExprNode setType(ExpandedType t);

}
