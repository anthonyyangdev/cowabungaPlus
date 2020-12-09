package cyr7.typecheck

import cyr7.visitor.AstVisitor
import java_cup.runtime.ComplexSymbolFactory.Location

interface IPureCheckVisitor: AstVisitor<Location?>
