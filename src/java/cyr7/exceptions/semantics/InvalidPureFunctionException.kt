package cyr7.exceptions.semantics

import java_cup.runtime.ComplexSymbolFactory

class InvalidPureFunctionException(
        functionName: String,
        location: ComplexSymbolFactory.Location
) : SemanticException("$functionName is declared to be pure, but it contains an impure statement.", location)
