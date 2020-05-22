package cyr7.graph;

/**
 * Thrown if an expected node does not exist.
 * @author ayang
 *
 */
public class NonexistantNodeException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = -7685364721770342822L;

    public NonexistantNodeException(GraphNode<?> node) {
        super("The node (" + node.toString() + ") could not be found.");
    }

}
