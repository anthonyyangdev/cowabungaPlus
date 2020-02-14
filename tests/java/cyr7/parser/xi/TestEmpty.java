package cyr7.parser.xi;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.StringReader;
import java.util.Collections;
import java.util.LinkedList;

import cyr7.parser.ParserFactory;
import org.junit.jupiter.api.Test;

import cyr7.ast.FunctionHeaderDeclNode;
import cyr7.ast.XiProgramNode;
import cyr7.ast.stmt.VarDeclNode;
import cyr7.ast.type.ITypeExprNode;
import cyr7.parser.XiParser;

public class TestEmpty {

    LinkedList<VarDeclNode> args;
    LinkedList<ITypeExprNode> returnTypes;
    FunctionHeaderDeclNode function;
    LinkedList<FunctionHeaderDeclNode> functions;
    XiProgramNode expected;
    StringReader prgm;
    XiParser parser;
    Object tree;

    @Test
    void test() throws Exception {
        String program = "";
        expected = new XiProgramNode(null, new LinkedList<>(), new LinkedList<>());
        parser = ParserFactory.make(program, false);
        tree = parser.parse().value;
        assertEquals(expected, tree);

        program = "\n\n\n\n";
        expected = new XiProgramNode(null, new LinkedList<>(), new LinkedList<>());
        parser = ParserFactory.make(program, false);
        tree = parser.parse().value;
        assertEquals(expected, tree);

    }

}
