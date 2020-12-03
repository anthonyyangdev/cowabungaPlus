package cyr7.ir.interpret;

import cyr7.cli.CLI;
import cyr7.ir.interpret.builtin.LibraryFunction;
import cyr7.ir.interpret.heap.XiHeap;
import cyr7.ir.nodes.IRBinOp;
import cyr7.ir.nodes.IRCJump;
import cyr7.ir.nodes.IRCall;
import cyr7.ir.nodes.IRCallStmt;
import cyr7.ir.nodes.IRCompUnit;
import cyr7.ir.nodes.IRConst;
import cyr7.ir.nodes.IRExp;
import cyr7.ir.nodes.IRFuncDecl;
import cyr7.ir.nodes.IRJump;
import cyr7.ir.nodes.IRMem;
import cyr7.ir.nodes.IRMove;
import cyr7.ir.nodes.IRName;
import cyr7.ir.nodes.IRNode;
import cyr7.ir.nodes.IRNodeFactory;
import cyr7.ir.nodes.IRNodeFactory_c;
import cyr7.ir.nodes.IRReturn;
import cyr7.ir.nodes.IRTemp;
import cyr7.ir.visit.InsnMapsBuilder;
import edu.cornell.cs.cs4120.util.InternalCompilerError;
import polyglot.util.SerialVersionUID;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Stack;

/**
 * A simple IR interpreter
 */
public class IRSimulator {

    /** compilation unit to be interpreted */
    private final IRCompUnit compUnit;

    /** map from address to instruction */
    protected Map<Long, IRNode> indexToInsn;

    /** map from labeled name to address */
    private final Map<String, Long> nameToIndex;

    /** a random number generator for initializing garbage */
    protected Random r = new Random();

    protected ExprStack exprStack;
    protected XiHeap heap;

    protected BuiltInLibrary libraryFunctions;

    protected static int debugLevel = 0;

    public static final int DEFAULT_HEAP_SIZE = 128 * 10240;
    public static final int BIG_HEAP_SIZE = 512 * 10240;

    /**
     * Construct an IR interpreter with a default heap size
     * @param compUnit the compilation unit to be interpreted
     */
    public IRSimulator(IRCompUnit compUnit) {
        this(compUnit, DEFAULT_HEAP_SIZE, System.out);
    }

    /**
     * Construct an IR interpreter with a default heap size
     * @param compUnit the compilation unit to be interpreted
     */
    public IRSimulator(IRCompUnit compUnit, PrintStream stdout) {
        this(compUnit, DEFAULT_HEAP_SIZE, stdout);
    }

    /**
     * Construct an IR interpreter
     * @param compUnit the compilation unit to be interpreted
     * @param heapSize the heap size
     */
    public IRSimulator(IRCompUnit compUnit, int heapSize, PrintStream stdout) {
        this.exprStack = new ExprStack();
        this.compUnit = compUnit;
        this.heap = new XiHeap(heapSize);
        var inReader = new BufferedReader(new InputStreamReader(System.in));
        var simulatorSettings = new SimulatorSettings(heap, inReader, stdout);
        libraryFunctions = new BuiltInLibrary(simulatorSettings);

        InsnMapsBuilder imb = new InsnMapsBuilder();
        compUnit = (IRCompUnit) imb.visit(compUnit);
        indexToInsn = imb.indexToInsn();
        nameToIndex = imb.nameToIndex();
    }

    /**
     * Simulate a function call, throwing away all returned values past the first
     * All arguments to the function call are passed via registers with prefix
     * {@link Configuration#ABSTRACT_ARG_PREFIX} and indices starting from 0.
     * @param name name of the function call
     * @param args arguments to the function call
     * @return the value that would be in register
     *          {@link Configuration#ABSTRACT_RET_PREFIX} index 0
     */
    public long call(String name, long... args) {
        return call(new ExecutionFrame(-1), name, args);
    }

    /**
     * Simulate a function call.
     * All arguments to the function call are passed via registers with prefix
     * {@link Configuration#ABSTRACT_ARG_PREFIX} and indices starting from 0.
     * The function call should return the results via registers with prefix
     * {@link Configuration#ABSTRACT_RET_PREFIX} and indices starting from 0.
     * @param parent parent call frame to write _RET values to
     * @param name name of the function call
     * @param args arguments to the function call
     * @return the value of register
     *          {@link Configuration#ABSTRACT_RET_PREFIX} index 0
     */
    public long call(ExecutionFrame parent, String name, long... args) {
        // Catch standard library calls.
        IRFuncDecl fDecl = compUnit.getFunction(name);
        LibraryFunction libFun = libraryFunctions.get(name);
        if (fDecl == null && libFun != null) {
            final List<Long> ret = libFun.execute(args);
            for (int i = 0; i < ret.size(); i++) {
                parent.put(Configuration.ABSTRACT_RET_PREFIX + i, ret.get(i));
            }
            if (ret.size() > 0) {
                return ret.get(0);
            }
        } else {
            if (fDecl == null)
                throw new InternalCompilerError("Tried to call an unknown function: '"
                        + name + "'");
            // Create a new stack frame.
            long ip = findLabel(name);
            ExecutionFrame frame = new ExecutionFrame(ip);

            // Pass the remaining arguments into registers.
            for (int i = 0; i < args.length; ++i)
                frame.put(Configuration.ABSTRACT_ARG_PREFIX + i, args[i]);

            // Simulate!
            while (frame.advance());

            String typeInName = name.substring(name.lastIndexOf("_") + 1);
            int numReturnVals = 1;
            if (typeInName.charAt(0) == 'p') {
                numReturnVals = 0;
            } else if (typeInName.charAt(0) == 't') {
                StringBuilder number = new StringBuilder();
                for (int i = 1;
                        i < typeInName.length() && Character.isDigit(typeInName.charAt(i));
                        i++) {
                    number.append(typeInName.charAt(i));
                }
                numReturnVals = Integer.parseInt(number.toString());
            }

            // Transfer child's return temps to the parent frame, because the child frame is going away
            for (int i = 0; i < numReturnVals; i++) {
                String currRetTmp = Configuration.ABSTRACT_RET_PREFIX + i;
                CLI.debugPrint("Returning: " + currRetTmp + ", " + frame.get(currRetTmp));
                parent.put(currRetTmp, frame.get(currRetTmp));
            }
            if (numReturnVals > 0) {
                return frame.get(Configuration.ABSTRACT_RET_PREFIX + 0);
            }
        }
        return 0;
    }

    protected void leave(ExecutionFrame frame) {
        interpret(frame, frame.getCurrentInsn());
    }

    protected void interpret(ExecutionFrame frame, IRNode insn) {
        if (insn instanceof IRConst)
            exprStack.pushValue(((IRConst) insn).value());
        else if (insn instanceof IRTemp) {
            String tempName = ((IRTemp) insn).name();
            exprStack.pushTemp(frame.get(tempName), tempName);
        }
        else if (insn instanceof IRBinOp) {
            long r = exprStack.popValue();
            long l = exprStack.popValue();
            long result;
            switch (((IRBinOp) insn).opType()) {
            case ADD:
                result = l + r;
                break;
            case SUB:
                result = l - r;
                break;
            case MUL:
                result = l * r;
                break;
            case HMUL:
                result = BigInteger.valueOf(l)
                                   .multiply(BigInteger.valueOf(r))
                                   .shiftRight(64)
                                   .longValue();
                break;
            case DIV:
                if (r == 0) throw new Trap("Division by zero!");
                result = l / r;
                break;
            case MOD:
                if (r == 0) throw new Trap("Division by zero!");
                result = l % r;
                break;
            case AND:
                result = l & r;
                break;
            case OR:
                result = l | r;
                break;
            case XOR:
                result = l ^ r;
                break;
            case LSHIFT:
                result = l << r;
                break;
            case RSHIFT:
                result = l >>> r;
                break;
            case ARSHIFT:
                result = l >> r;
                break;
            case EQ:
                result = l == r ? 1 : 0;
                break;
            case NEQ:
                result = l != r ? 1 : 0;
                break;
            case LT:
                result = l < r ? 1 : 0;
                break;
            case GT:
                result = l > r ? 1 : 0;
                break;
            case LEQ:
                result = l <= r ? 1 : 0;
                break;
            case GEQ:
                result = l >= r ? 1 : 0;
                break;
            default:
                throw new InternalCompilerError("Invalid binary operation");
            }
            exprStack.pushValue(result);
        }
        else if (insn instanceof IRMem) {
            long addr = exprStack.popValue();
            exprStack.pushAddr(heap.read(addr), addr);
        }
        else if (insn instanceof IRCall) {
            int argsCount = ((IRCall) insn).args().size();
            long[] args = new long[argsCount];
            for (int i = argsCount - 1; i >= 0; --i)
                args[i] = exprStack.popValue();
            if (debugLevel > 2) {
                System.out.println("Arguments: " + Arrays.toString(args));
            }
            StackItem target = exprStack.pop();
            String targetName;
            if (target.type == StackItem.Kind.NAME)
                targetName = target.name;
            else if (indexToInsn.containsKey(target.value)) {
                IRNode node = indexToInsn.get(target.value);
                if (node instanceof IRFuncDecl)
                    targetName = ((IRFuncDecl) node).name();
                else throw new InternalCompilerError("Call to a non-function instruction!");
            }
            else throw new InternalCompilerError("Invalid function call '"
                    + insn + "' (target '" + target.value + "' is unknown)!");

            long retVal = call(frame, targetName, args);
            if (debugLevel > 2) {
                System.err.println("Return value: " + retVal);
            }
            exprStack.pushValue(retVal);
        }
        else if (insn instanceof IRName) {
            String name = ((IRName) insn).name();
            exprStack.pushName(libraryFunctions.contains(name)
                    ? -1 : findLabel(name), name);
        }
        else if (insn instanceof IRMove) {
            long r = exprStack.popValue();
            StackItem stackItem = exprStack.pop();
            switch (stackItem.type) {
            case MEM:
                if (debugLevel > 0)
                    System.out.println("mem[" + stackItem.addr + "]=" + r);
                heap.store(stackItem.addr, r);
                break;
            case TEMP:
                if (debugLevel > 0)
                    System.out.println("temp[" + stackItem.temp + "]=" + r);
                frame.put(stackItem.temp, r);
                break;
            default:
                throw new InternalCompilerError("Invalid MOVE!");
            }
        }
        else if (insn instanceof IRCallStmt) {
            IRNodeFactory make = new IRNodeFactory_c(insn.location());

            IRCallStmt callStmt = (IRCallStmt) insn;
            IRCall syntheticCall = make.IRCall(callStmt.target(),
                    callStmt.args());
            interpret(frame, syntheticCall);
            exprStack.popValue();
            List<String> collectors = callStmt.collectors();
            int len = collectors.size();
            for (int i = 0; i < len; i++) {
                IRTemp syntheticCollectorTemp = make.IRTemp(collectors.get(i));
                IRTemp syntheticReturnTemp = make
                        .IRTemp(Configuration.ABSTRACT_RET_PREFIX + i);
                interpret(frame, syntheticCollectorTemp);
                interpret(frame, syntheticReturnTemp);
                IRMove syntheticMove = make.IRMove(syntheticCollectorTemp,
                        syntheticReturnTemp);
                interpret(frame, syntheticMove);
            }
        }
        else if (insn instanceof IRExp) {
            // Discard result.
            exprStack.pop();
        }
        else if (insn instanceof IRJump)
            frame.setIP(exprStack.popValue());
        else if (insn instanceof IRCJump) {
            IRCJump irCJump = (IRCJump) insn;
            long top = exprStack.popValue();
            String label;
            if (top == 0)
                label = irCJump.falseLabel().orElse(null);
            else if (top == 1)
                label = irCJump.trueLabel();
            else throw new InternalCompilerError("Invalid value in CJUMP - expected 0/1, got "
                    + top);
            if (label != null) frame.setIP(findLabel(label));
        }
        else if (insn instanceof IRReturn) {
            frame.setIP(-1);
        }
    }

    /**
     *
     * @param name name of the label
     * @return the IR node at the named label
     */
    protected long findLabel(String name) {
        if (!nameToIndex.containsKey(name))
            throw new Trap("Could not find label '" + name + "'!");
        return nameToIndex.get(name);
    }

    /**
     * Holds the instruction pointer and temporary registers
     * within an execution frame.
     */
    protected class ExecutionFrame {
        /** instruction pointer */
        protected long ip;

        /** local registers (register name -> value) */
        private final Map<String, Long> regs;

        public ExecutionFrame(long ip) {
            this.ip = ip;
            regs = new HashMap<>();
        }

        /**
         * Fetch the value at the given register
         * @param tempName name of the register
         * @return the value at the given register
         */
        public long get(String tempName) {
            if (!regs.containsKey(tempName)) {
                /* Referencing a temp before having written to it - initialize
                   with garbage */
                put(tempName, r.nextLong());
            }
            return regs.get(tempName);
        }

        /**
         * Store a value into the given register
         * @param tempName name of the register
         * @param value value to be stored
         */
        public void put(String tempName, long value) {
            regs.put(tempName, value);
        }

        /**
         * Advance the instruction pointer. Since we're dealing with a tree,
         * this is postorder traversal, one step at a time, modulo jumps.
         */
        public boolean advance() {
            // Time out if necessary.
            if (Thread.currentThread().isInterrupted()) return false;

            if (debugLevel > 1)
                System.out.println("Evaluating " + getCurrentInsn().label() + " at " + getCurrentInsn().location());
            long backupIP = ip;
            leave(this);

            if (ip == -1) return false; /* RETURN */

            if (ip != backupIP) /* A jump was performed */
                return true;

            ip++;
            return true;
        }

        public void setIP(long ip) {
            this.ip = ip;
            if (debugLevel > 1) {
                if (ip == -1)
                    System.out.println("Returning");
                else System.out.println("Jumping to "
                        + getCurrentInsn().label());
            }
        }

        public IRNode getCurrentInsn() {
            IRNode insn = indexToInsn.get(ip);
            if (insn == null)
                throw new Trap("No next instruction.  Forgot RETURN?");
            return insn;
        }

        public Map<String, Long> regs() {
            return Collections.unmodifiableMap(this.regs);
        }

    };

    /**
     * While traversing the IR tree, we require a stack in order to hold
     * a number of single-word values (e.g., to evaluate binary expressions).
     * This also keeps track of whether a value was created by a TEMP
     * or MEM, or NAME reference, which is useful when executing moves.
     */
    protected static class ExprStack {

        private final Stack<StackItem> stack;

        public ExprStack() {
            stack = new Stack<>();
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
    }

    public static class StackItem {
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

    public static class Trap extends RuntimeException {
        private static final long serialVersionUID =
                SerialVersionUID.generate();

        public Trap(String message) {
            super(message);
        }
    }

    public static class OutOfBoundTrap extends Trap {
        private static final long serialVersionUID =
                SerialVersionUID.generate();

        public OutOfBoundTrap(String message) {
            super(message);
        }
    }
}
