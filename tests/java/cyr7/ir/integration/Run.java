package cyr7.ir.integration;

import cyr7.ast.Node;
import cyr7.ast.expr.ExprNode;
import cyr7.ast.expr.FunctionCallExprNode;
import cyr7.ast.expr.literalexpr.LiteralArrayExprNode;
import cyr7.ast.expr.literalexpr.LiteralIntExprNode;
import cyr7.ast.stmt.BlockStmtNode;
import cyr7.ast.stmt.ProcedureStmtNode;
import cyr7.ast.toplevel.FunctionDeclNode;
import cyr7.ast.toplevel.FunctionHeaderDeclNode;
import cyr7.ast.toplevel.XiProgramNode;
import cyr7.ir.AstToIrVisitor;
import cyr7.ir.DefaultIdGenerator;
import cyr7.ir.IdGenerator;
import cyr7.ir.IrUtil;
import cyr7.ir.IrUtil.Configuration;
import cyr7.ir.fold.ConstFoldVisitor;
import cyr7.ir.interpret.IRSimulator;
import cyr7.ir.lowering.LoweringVisitor;
import cyr7.ir.nodes.IRCompUnit;
import cyr7.ir.nodes.IRFuncDecl;
import cyr7.ir.nodes.IRNode;
import cyr7.ir.visit.CheckCanonicalIRVisitor;
import cyr7.ir.visit.CheckConstFoldedIRVisitor;
import cyr7.parser.ParserUtil;
import cyr7.typecheck.IxiFileOpener;
import cyr7.typecheck.TypeCheckUtil;
import edu.cornell.cs.cs4120.util.CodeWriterSExpPrinter;
import edu.cornell.cs.cs4120.util.SExpPrinter;
import java_cup.runtime.ComplexSymbolFactory.Location;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

public final class Run {

    private static class Opener implements IxiFileOpener {

        @Override
        public Reader openIxiLibraryFile(String name) throws FileNotFoundException {
            if (name.equals("conv")) {
                return new StringReader(
                    "parseInt(str: int[]): int, bool\n"
                    + "unparseInt(n: int): int[]\n"
                );
            } else if (name.equals("io")) {
                return new StringReader(
                    "print(str: int[])\n"
                    + "println(str: int[])\n"
                    + "readln() : int[]\n"
                    + "getchar() : int\n"
                    + "eof() : bool\n"
                );
            } else {
                throw new FileNotFoundException(name);
            }
        }

    }

    public static String runFile(String filename) throws Exception {
        return runFile(filename, new int[][] {}, false);
    }

    public static String runFile(String filename, int[][] args, boolean bigHeap) throws Exception {
        InputStream filePath = Run.class
            .getClassLoader()
            .getResourceAsStream("irgen/"+ filename + ".xi");
        String program = new String(filePath.readAllBytes());
        return run(program, args, bigHeap);
    }

    public static String run(String program) throws Exception {
        return run(program, new int[][] {}, false);
    }

    public static String run(String program, int[][] args, boolean bigHeap) throws Exception {
        String mirResult = mirRun(program, args, bigHeap);

//        String lirResultNoOpts = lirRun(program, new Configuration(false, false));
//        assertEquals(mirResult, lirResultNoOpts);
//
//        String lirResultCFold = lirRun(program, new Configuration(true, false));
//        assertEquals(mirResult, lirResultCFold);
//
//        String lirResultCommute = lirRun(program, new Configuration(false, true));
//        assertEquals(mirResult, lirResultCommute);
//
//        String lirResultAll = lirRun(program, new Configuration(true, true));
//        assertEquals(mirResult, lirResultAll);

        return mirResult;
    }

    private static IRCompUnit lower(
        IRCompUnit compUnit,
        IdGenerator generator,
        Configuration configuration) {

        IRCompUnit lowered = IrUtil.lower(compUnit, generator, configuration);

        if (configuration.cFoldEnabled) {
            assertTrue(lowered.aggregateChildren(new CheckConstFoldedIRVisitor()));
        }

        CheckCanonicalIRVisitor visitor = new CheckCanonicalIRVisitor();
        assertTrue(lowered.aggregateChildren(visitor),
            "Program: "
                + sexp(lowered)
                + "\nOffending node: "
                + visitor.noncanonical());

        return lowered;
    }

    private static XiProgramNode addPremain(XiProgramNode toModify, int[][] args) {
        Location LOC = new Location(-1, -1);

        List<ExprNode> exprArgs = new ArrayList<>();
        for (int[] arg : args) {
            List<ExprNode> exprArg = new ArrayList<>();
            for (int val : arg) {
                exprArg.add(new LiteralIntExprNode(LOC, Integer.toString(val)));
            }
            exprArgs.add(new LiteralArrayExprNode(LOC, exprArg));
        }

        FunctionDeclNode premain = new FunctionDeclNode(LOC,
            new FunctionHeaderDeclNode(LOC,
                "premain",
                List.of(),
                List.of()
            ),
            new BlockStmtNode(LOC, List.of(
                new ProcedureStmtNode(LOC,
                    new FunctionCallExprNode(LOC, "main", List.of(
                        new LiteralArrayExprNode(LOC, exprArgs)
                    ))
                )
            ))
        );

        List<FunctionDeclNode> functionDecls = new ArrayList<>(toModify.functions);
        functionDecls.add(premain);

        return new XiProgramNode(toModify.getLocation(), toModify.uses, functionDecls);
    }

    private static String mirRun(String program, int[][] args, boolean bigHeap) throws Exception {
        Reader reader = new StringReader(program);

        XiProgramNode result = (XiProgramNode) ParserUtil.parseNode(reader, "Run", false);
        result = addPremain(result, args);
        TypeCheckUtil.typeCheck(result, new Opener());

        IdGenerator generator = new DefaultIdGenerator();

        IRCompUnit compUnit;
        {
            IRNode node = result.accept(new AstToIrVisitor(generator)).assertSecond();
            assert node instanceof IRCompUnit;
            compUnit = (IRCompUnit) node;
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        IRSimulator sim = new IRSimulator(
            compUnit,
            bigHeap ? 1_000_000 : IRSimulator.DEFAULT_HEAP_SIZE,
            new PrintStream(outputStream)
        );
        sim.call("_Ipremain_p", 0);
        return new String(outputStream.toByteArray(), Charset.defaultCharset());
    }

    private static String lirRun(String program, Configuration configuration, int[][] args) throws Exception {
        Reader reader = new StringReader(program);

        XiProgramNode result = (XiProgramNode) ParserUtil.parseNode(reader, "Run", false);
        addPremain(result, args);
        TypeCheckUtil.typeCheck(result, new Opener());

        IdGenerator generator = new DefaultIdGenerator();

        IRCompUnit compUnit;
        {
            IRNode node = result.accept(new AstToIrVisitor(generator)).assertSecond();
            assert node instanceof IRCompUnit;
            compUnit = (IRCompUnit) node;
        }

        IRCompUnit lowered = lower(compUnit, generator, configuration);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        IRSimulator sim = new IRSimulator(lowered, new PrintStream(outputStream));
        sim.call("_Imain_paai", 0);
        return new String(outputStream.toByteArray(), Charset.defaultCharset());
    }

    private static String sexp(IRNode n) {
        StringWriter writer = new StringWriter();
        SExpPrinter printer =
            new CodeWriterSExpPrinter(new PrintWriter(writer));
        n.printSExp(printer);
        printer.flush();
        return writer.toString();
    }

    private Run() { }

}
