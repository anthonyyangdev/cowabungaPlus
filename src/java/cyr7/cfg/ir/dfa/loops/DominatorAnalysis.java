package cyr7.cfg.ir.dfa.loops;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import cyr7.cfg.ir.dfa.ForwardDataflowAnalysis;
import cyr7.cfg.ir.dfa.ForwardTransferFunction;
import cyr7.cfg.ir.nodes.CFGCallNode;
import cyr7.cfg.ir.nodes.CFGIfNode;
import cyr7.cfg.ir.nodes.CFGMemAssignNode;
import cyr7.cfg.ir.nodes.CFGNode;
import cyr7.cfg.ir.nodes.CFGSelfLoopNode;
import cyr7.cfg.ir.nodes.CFGStartNode;
import cyr7.cfg.ir.nodes.CFGVarAssignNode;
import cyr7.util.Sets;

public enum DominatorAnalysis implements ForwardDataflowAnalysis<Set<CFGNode>> {
    
    INSTANCE;

    public enum InfiniteSet implements Set<CFGNode> {
        INSTANCE;

        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean contains(Object o) {
            return false;
        }

        @Override
        public Iterator<CFGNode> iterator() {
            return null;
        }

        @Override
        public Object[] toArray() {
            return null;
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return null;
        }

        @Override
        public boolean add(CFGNode e) {
            return false;
        }

        @Override
        public boolean remove(Object o) {
            return false;
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return false;
        }

        @Override
        public boolean addAll(Collection<? extends CFGNode> c) {
            return false;
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            return false;
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            return false;
        }

        @Override
        public void clear() {
        }
        
        @Override
        public String toString() {
            return "[∞]";
        }
    }
    
    @Override
    public Set<CFGNode> topValue() {
        return InfiniteSet.INSTANCE;
    }

    @Override
    public ForwardTransferFunction<Set<CFGNode>> transfer() {
        return TransferFunction.INSTANCE;
    }

    @Override
    public Set<CFGNode> meet(Set<CFGNode> lhs, Set<CFGNode> rhs) {
        if (lhs == InfiniteSet.INSTANCE) {
            return rhs;
        } else if (rhs == InfiniteSet.INSTANCE) {
            return lhs;
        }
        return Sets.intersection(lhs, rhs);
    }
    
    private enum TransferFunction implements ForwardTransferFunction<Set<CFGNode>> {
        INSTANCE;

        @Override
        public Set<CFGNode> transfer(CFGCallNode n, Set<CFGNode> in) {
            if (in == InfiniteSet.INSTANCE) {
                return InfiniteSet.INSTANCE;
            }
            return Sets.union(in, Set.of(n));
        }
        
        @Override
        public Set<CFGNode> transfer(CFGMemAssignNode n, Set<CFGNode> in) {
            if (in == InfiniteSet.INSTANCE) {
                return InfiniteSet.INSTANCE;
            }
            return Sets.union(in, Set.of(n));
        }

        @Override
        public Set<CFGNode> transfer(CFGVarAssignNode n, Set<CFGNode> in) {
            if (in == InfiniteSet.INSTANCE) {
                return InfiniteSet.INSTANCE;
            }
            return Sets.union(in, Set.of(n));
        }

        @Override
        public Set<CFGNode> transferTrue(CFGIfNode n, Set<CFGNode> in) {
            if (in == InfiniteSet.INSTANCE) {
                return InfiniteSet.INSTANCE;
            }
            return Sets.union(in, Set.of(n));
        }

        @Override
        public Set<CFGNode> transferFalse(CFGIfNode n, Set<CFGNode> in) {
            if (in == InfiniteSet.INSTANCE) {
                return InfiniteSet.INSTANCE;
            }
            return Sets.union(in, Set.of(n));
        }

        @Override
        public Set<CFGNode> transfer(CFGStartNode n, Set<CFGNode> in) {
            return Sets.union(Set.of(), Set.of(n));
        }

        @Override
        public Set<CFGNode> transfer(CFGSelfLoopNode n, Set<CFGNode> in) {
            // TODO Auto-generated method stub
            return null;
        }
        
    }
    
}
