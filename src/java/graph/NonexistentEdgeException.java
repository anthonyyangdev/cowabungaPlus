package graph;

/**
 * Thrown if an expected edge does not exist.
 * @author ayang
 *
 */
public class NonexistentEdgeException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 890616958222602876L;

    public NonexistentEdgeException(Edge<?,?> edge) {
        super("The edge (" + edge.toString() + ") could not be found");
    }

}
