package cyr7.ir

import cyr7.ir.nodes.IRExpr
import cyr7.ir.nodes.IRStmt
import cyr7.util.OneOfTwo
import cyr7.visitor.AstVisitor

interface IAstToIrVisitor: AstVisitor<OneOfTwo<IRExpr, IRStmt>>
