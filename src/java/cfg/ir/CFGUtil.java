package cfg.ir;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;

import cfg.ir.constructor.CFGConstructor;
import cfg.ir.dot.IrCFGDotUtil;
import cfg.ir.dot.IrCFGDotUtil.DotData;
import cyr7.cli.OptConfig;
import cyr7.ir.DefaultIdGenerator;
import cyr7.ir.IRUtil;
import cyr7.ir.nodes.IRCompUnit;
import cyr7.typecheck.IxiFileOpener;
import polyglot.util.Pair;
public final class CFGUtil {

    public static void generateDot(
            Reader reader,
            Writer writer,
            String filename,
            IxiFileOpener fileOpener,
            OptConfig optConfig,
            String functionName) throws Exception {

        IRCompUnit lowered = IRUtil.generateIR(
                reader,
                filename,
                fileOpener,
                optConfig,
                new DefaultIdGenerator());
        Map<String, CFGGraph> cfg = CFGConstructor.constructCFG(lowered);
        outputDotForFunctionIR(cfg.get(functionName), writer);
    }

    public static Map<String, CFGGraph> generateAllInitialDot(
            Reader reader,
            String filename,
            IxiFileOpener fileOpener) throws Exception {
        IRCompUnit lowered = IRUtil.generateInitialIR(
                reader,
                filename,
                fileOpener,
                new DefaultIdGenerator());
        return CFGConstructor.constructCFG(lowered);
    }

    public static Map<String, CFGGraph> generateAllFinalDot(
            Reader reader,
            String filename,
            IxiFileOpener fileOpener,
            OptConfig optConfig) throws Exception {
        IRCompUnit lowered = IRUtil.generateIR(
                reader,
                filename,
                fileOpener,
                optConfig,
                new DefaultIdGenerator());
        return CFGConstructor.constructCFG(lowered);
    }

    public static void testGenerateDotAsm() throws Exception {
        File f = new File("tests/resources/testJunk.xi");
        FileReader fr = new FileReader(f);
        BufferedReader br  = new BufferedReader(fr);
        Reader reader = new BufferedReader(br);
        IRCompUnit lowered = IRUtil.generateIR(
                reader,
                "testJunk.xi",
                null,
                OptConfig.none(), new DefaultIdGenerator());
        CFGGraph cfg = CFGConstructor.constructCFG(lowered).get("_Imain_paai");
        Writer writer = new PrintWriter(System.out);
        outputDotForFunctionIR(cfg, writer);
    }

    public static void outputDotForFunctionIR(CFGGraph node, Writer writer) {
        PrintWriter printer = new PrintWriter(writer);
        DotData data = IrCFGDotUtil.execute(node);
        printer.println("digraph nfa {");
        printer.println("    node [shape=rectangle]");
        for(String label: data.nodes()) {
            printer.println("    \""+label+"\"");
        }
        printer.println();
        for(Pair<String, String> edge: data.edges()) {
            String edgeName = "    \""+edge.part1()+"\" -> \""+edge.part2()+"\"";
            printer.println(edgeName);
        }
        printer.println("}");
        printer.flush();
    }

    private CFGUtil() { }

}