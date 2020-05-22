package cfg.ir.constructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cfg.ir.CFGGraph;
import cfg.ir.nodes.CFGStartNode;
import cyr7.ir.block.BasicBlock;
import cyr7.ir.nodes.IRCompUnit;

public class CFGConstructor {

    private CFGConstructor() {}

    /**
     * Generates a CFG Tree for each function defined in the IRTree.
     */
    public static Map<String, CFGGraph> constructCFG(IRCompUnit c) {
        Map<String, CFGGraph> cfgCollection = new HashMap<>();
        c.functions().forEach((name, fn) -> {
            CFGGraph cfg = NormalCFGConstructor.construct(fn.body());
            cfgCollection.put(name, cfg);
         });
        return cfgCollection;
    }


    public static Map<String, CFGStartNode>
            constructBlockCFG(Map<String, List<List<BasicBlock>>> traces) {

        Map<String, CFGStartNode> cfgCollection = new HashMap<>();

        traces.forEach((name, blocks) -> {
            CFGStartNode fBody = BlockCfgConstructor.construct(blocks);
            cfgCollection.put(name, fBody);
        });
        return cfgCollection;
    }


}