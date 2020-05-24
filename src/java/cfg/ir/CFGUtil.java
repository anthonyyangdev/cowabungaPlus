package cfg.ir;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import cfg.ir.constructor.CFGConstructor;
import cfg.ir.graph.CFGGraph;
import cfg.ir.nodes.CFGNode;
import cyr7.cli.OptConfig;
import cyr7.ir.DefaultIdGenerator;
import cyr7.ir.IRUtil;
import cyr7.ir.nodes.IRCompUnit;
import cyr7.typecheck.IxiFileOpener;
import graph.Edge;
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

    public static void outputDotForFunctionIR(CFGGraph cfg, Writer writer) {
        final PrintWriter printer = new PrintWriter(writer);
        final Map<CFGNode, Integer> nodeToLabel = new HashMap<>();
        final AtomicInteger count = new AtomicInteger();
        final List<String> nodes = cfg.nodes().stream().map(n -> {
                final int id = count.getAndIncrement();
                final String label = "    " + id + " [label =\"" + n.value().toString() + "\"]";
                nodeToLabel.put(n.value(), id);
                return label;
            }).collect(Collectors.toList());

        final List<Edge<CFGNode, Boolean>> edges = new ArrayList<>(cfg.edges());

        printer.println("digraph nfa {");
        printer.println("    node [shape=rectangle]");
        for(String label: nodes) {
            printer.println("    "+ label + ";");
        }
        printer.println();
        for(Edge<CFGNode, Boolean> e: edges) {
            final int startId = nodeToLabel.get(e.start.value());
            final int endId = nodeToLabel.get(e.end.value());
            String edgeName = "    " + startId + " -> " + endId +
                            e.value.map(
                                v -> " [label =\"  " + String.valueOf(v) + "\"]")
                            .orElse("");
            printer.println(edgeName);
        }
        printer.println("}");
        printer.flush();
    }

    private CFGUtil() { }

}