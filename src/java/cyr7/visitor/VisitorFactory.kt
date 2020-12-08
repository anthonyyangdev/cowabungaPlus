package cyr7.visitor

import cyr7.ir.ASTToIRTranslator
import cyr7.ir.IdGenerator
import cyr7.ir.nodes.IRExpr
import cyr7.ir.nodes.IRStmt
import cyr7.semantics.types.ExpandedType
import cyr7.semantics.types.ResultType
import cyr7.typecheck.IxiFileOpener
import cyr7.typecheck.PureCheckVisitor
import cyr7.util.OneOfThree
import cyr7.util.OneOfTwo
import java_cup.runtime.ComplexSymbolFactory

class VisitorFactory {

    companion object {
        fun astToIrVisitor(generator: IdGenerator): AbstractVisitor<OneOfTwo<IRExpr, IRStmt>> {
            return ASTToIRTranslator(generator)
        }
        fun typeCheckVisitor(fileOpener: IxiFileOpener): AbstractVisitor<OneOfThree<ExpandedType, ResultType, Void>> {
            TODO("Fix the interface used for the typecheck visitor")
        }
        fun pureCheckVisitor(): AbstractVisitor<ComplexSymbolFactory.Location?> {
            return PureCheckVisitor()
        }
    }

}
