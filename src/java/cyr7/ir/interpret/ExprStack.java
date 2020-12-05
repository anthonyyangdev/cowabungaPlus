package cyr7.ir.interpret;

import java.util.Stack;

/**
 * While traversing the IR tree, we require a stack in order to hold
 * a number of single-word values (e.g., to evaluate binary expressions).
 * This also keeps track of whether a value was created by a TEMP
 * or MEM, or NAME reference, which is useful when executing moves.
 */
public class ExprStack {

    private final Stack<StackItem> stack;
    private final int debugLevel;

    public ExprStack(int debugLevel) {
        stack = new Stack<>();
        this.debugLevel = debugLevel;
    }

    public long popValue() {
        long value = stack.pop().value;
        if (debugLevel > 1) System.out.println("Popping value " + value);
        return value;
    }

    public StackItem pop() {
        return stack.pop();
    }

    public void pushAddr(long value, long addr) {
        if (debugLevel > 1)
            System.out.println("Pushing MEM " + value + " (" + addr + ")");
        stack.push(new StackItem(value, addr));
    }

    public void pushTemp(long value, String temp) {
        if (debugLevel > 1)
            System.out.println("Pushing TEMP " + value + " (" + temp + ")");
        stack.push(new StackItem(StackItem.Kind.TEMP, value, temp));
    }

    public void pushName(long value, String name) {
        if (debugLevel > 1)
            System.out.println("Pushing NAME " + value + " (" + name + ")");
        stack.push(new StackItem(StackItem.Kind.NAME, value, name));
    }

    public void pushValue(long value) {
        if (debugLevel > 1) System.out.println("Pushing value " + value);
        stack.push(new StackItem(value));
    }

    static class StackItem {
        public enum Kind {
            COMPUTED, MEM, TEMP, NAME
        }

        public Kind type;
        public long value;
        public long addr;
        public String temp;
        public String name;

        public StackItem(long value) {
            type = Kind.COMPUTED;
            this.value = value;
        }

        public StackItem(long value, long addr) {
            type = Kind.MEM;
            this.value = value;
            this.addr = addr;
        }

        public StackItem(Kind type, long value, String string) {
            this.type = type;
            this.value = value;
            if (type == Kind.TEMP)
                temp = string;
            else name = string;
        }
    }
}

