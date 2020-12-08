package cyr7.visitor

import cyr7.ir.ASTToIRTranslator
import cyr7.ir.IdGenerator
import cyr7.ir.nodes.IRExpr
import cyr7.ir.nodes.IRStmt
import cyr7.util.OneOfTwo

class VisitorFactory {

    companion object {
        fun astToIrVisitor(generator: IdGenerator): AbstractVisitor<OneOfTwo<IRExpr, IRStmt>> {
            return ASTToIRTranslator(generator)
        }
    }

}
