package cyr7.ir.interpret;

import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;

import edu.cornell.cs.cs4120.util.CodeWriterSExpPrinter;
import edu.cornell.cs.cs4120.util.SExpPrinter;
import cyr7.ir.nodes.IRBinOp;
import cyr7.ir.nodes.IRBinOp.OpType;
import cyr7.ir.nodes.IRCallStmt;
import cyr7.ir.nodes.IRCompUnit;
import cyr7.ir.nodes.IRConst;
import cyr7.ir.nodes.IRFuncDecl;
import cyr7.ir.nodes.IRMove;
import cyr7.ir.nodes.IRName;
import cyr7.ir.nodes.IRNodeFactory_c;
import cyr7.ir.nodes.IRReturn;
import cyr7.ir.nodes.IRSeq;
import cyr7.ir.nodes.IRStmt;
import cyr7.ir.nodes.IRTemp;
import cyr7.ir.parse.IRLexer;
import cyr7.ir.parse.IRParser;
import cyr7.ir.visit.CheckCanonicalIRVisitor;
import cyr7.ir.visit.CheckConstFoldedIRVisitor;

public class Main {

    public static void main(String[] args) {
        // Runs a simple program in the interpreter

        // IR roughly corresponds to the following:
        //     a(i:int, j:int): int, int {
        //         return i, (2 * j);
        //     }
        //     b(i:int, j:int): int {
        //         x:int, y:int = a(i, j);
        //         return x + 5 * y;
        //     }

        String arg0 = Configuration.ABSTRACT_ARG_PREFIX + 0;
        String arg1 = Configuration.ABSTRACT_ARG_PREFIX + 1;
        String ret0 = Configuration.ABSTRACT_RET_PREFIX + 0;
        String ret1 = Configuration.ABSTRACT_RET_PREFIX + 1;

        IRStmt aBody = new IRSeq(new IRMove(new IRTemp("i"), new IRTemp(arg0)),
                                 new IRMove(new IRTemp("j"), new IRTemp(arg1)),
				 new IRMove(new IRTemp(ret0), new IRTemp("i")),
				 new IRMove(new IRTemp(ret1),
                                         new IRBinOp(OpType.MUL,
                                                 new IRConst(2),
                                                 new IRTemp("j"))),
				 new IRReturn());
        IRFuncDecl aFunc = new IRFuncDecl("a", aBody);

        IRStmt bBody =
                new IRSeq(new IRCallStmt(Arrays.asList("x", "y"), new IRName("a"),
					new IRTemp("i"),
					new IRTemp("j")),

                          new IRMove(new IRTemp(ret0), new IRBinOp(OpType.ADD,
                                  new IRTemp("x"),
                                  new IRBinOp(OpType.MUL,
                                          new IRConst(5),
                                          new IRTemp("y")))),
			  new IRReturn());
        IRFuncDecl bFunc = new IRFuncDecl("b", bBody);

        IRCompUnit compUnit = new IRCompUnit("test");
        compUnit.appendFunc(aFunc);
        compUnit.appendFunc(bFunc);

        // IR pretty-printer demo
        System.out.println("Code:");
        StringWriter sw = new StringWriter();
        try (PrintWriter pw = new PrintWriter(sw);
             SExpPrinter sp = new CodeWriterSExpPrinter(pw)) {
            compUnit.printSExp(sp);
        }
        System.out.println(sw);

        // IR interpreter demo
        {
            IRSimulator sim = new IRSimulator(compUnit);
            long result = sim.call("b", 2, 1);
            System.out.println("b(2,1) == " + result);
        }

        // IR canonical checker demo
        {
            CheckCanonicalIRVisitor cv = new CheckCanonicalIRVisitor();
            System.out.print("Canonical?: ");
            System.out.println(cv.visit(compUnit));
        }

        // IR constant-folding checker demo
        {
            CheckConstFoldedIRVisitor cv = new CheckConstFoldedIRVisitor();
            System.out.print("Constant-folded?: ");
            System.out.println(cv.visit(compUnit));
        }

        // IR parser demo
        String prog = sw.toString();
        try (StringReader r = new StringReader(prog)) {
            IRParser parser =
                    new IRParser(new IRLexer(r), new IRNodeFactory_c());
            IRCompUnit compUnit_ = null;
            try {
                compUnit_ = parser.parse().<IRCompUnit> value();
            }
            catch (RuntimeException e) {
                throw e;
            }
            catch (Exception e) {
                // Used by CUP to indicate an unrecoverable error.
                String msg = e.getMessage();
                if (msg != null) System.err.println("Syntax error: " + msg);
            }

            if (compUnit_ != null) {
                IRSimulator sim = new IRSimulator(compUnit_);
                long result = sim.call("b", 2, 1);
                System.out.println("b(2,1) == " + result);
            }
        }
    }
}
