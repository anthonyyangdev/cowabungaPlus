package cyr7.exceptions.semantics

import java_cup.runtime.ComplexSymbolFactory

class InvalidPureFunctionException(
        functionName: String,
        location: ComplexSymbolFactory.Location
) : SemanticException("$functionName is declared to be pure contains a possibly impure statement/expression.", location)
