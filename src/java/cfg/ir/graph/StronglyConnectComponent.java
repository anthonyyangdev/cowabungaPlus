//package cfg.ir.graph;
//
//import java.util.Objects;
//
//import graph.GenericGraph;
//
//public class StronglyConnectComponent<V, E> {
//
//    public final GenericGraph<V, E> scc;
//
//    public StronglyConnectComponent(GenericGraph<V, E> scc) {
//        this.scc = scc;
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(scc);
//    }
//
//    @Override
//    public boolean equals(Object obj) {
//        if (this == obj) {
//            return true;
//        }
//        if (!(obj instanceof StronglyConnectComponent)) {
//            return false;
//        }
//        StronglyConnectComponent<?,?> other = (StronglyConnectComponent<?, ?>) obj;
//        return Objects.equals(scc, other.scc);
//    }
//
//}
