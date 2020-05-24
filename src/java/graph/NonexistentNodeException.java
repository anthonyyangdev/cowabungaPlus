package graph;

import cfg.ir.nodes.CFGNode;

/**
 * Thrown if an expected node does not exist.
 * @author ayang
 *
 */
public class NonexistentNodeException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = -7685364721770342822L;

    public NonexistentNodeException(CFGNode node) {
        super("The node (" + node.toString() + ") could not be found.");
    }

}
