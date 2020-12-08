package cyr7.visitor

import cyr7.ir.AstToIrVisitor
import cyr7.ir.IAstToIrVisitor
import cyr7.ir.IdGenerator
import cyr7.semantics.types.ExpandedType
import cyr7.semantics.types.ResultType
import cyr7.typecheck.IPureCheckVisitor
import cyr7.typecheck.IxiFileOpener
import cyr7.typecheck.PureCheckVisitor
import cyr7.util.OneOfThree

class VisitorFactory {

    companion object {
        fun astToIrVisitor(generator: IdGenerator): IAstToIrVisitor {
            return AstToIrVisitor(generator)
        }
        fun typeCheckVisitor(fileOpener: IxiFileOpener): AstVisitor<OneOfThree<ExpandedType, ResultType, Void>> {
            TODO("Fix the interface used for the typecheck visitor")
        }
        fun pureCheckVisitor(): IPureCheckVisitor {
            return PureCheckVisitor()
        }
    }

}
