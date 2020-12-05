package cyr7.ir.interpret

import cyr7.cli.CLI
import cyr7.ir.interpret.exception.Trap
import cyr7.ir.interpret.heap.XiHeapFactory
import cyr7.ir.nodes.*
import cyr7.ir.visit.InsnMapsBuilder
import edu.cornell.cs.cs4120.util.InternalCompilerError
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintStream
import java.math.BigInteger
import java.util.*
import kotlin.random.Random
import kotlin.random.asJavaRandom

class MyIRSimulator(private val cu: IRCompUnit, val heapSize: Int, private val stdout: PrintStream) {

    companion object {
        @JvmStatic
        val DEFAULT_HEAP_SIZE = 128 * 10240
        @JvmStatic
        val BIG_HEAP_SIZE = 512 * 10240

        const val debugLevel = 0
    }

    private val heap = XiHeapFactory.createInstance(heapSize)
    private val inReader = BufferedReader(InputStreamReader(System.`in`))
    private val simulatorSettings = SimulatorSettings(heap, inReader, stdout)
    private val libraryFunctions: BuiltInLibrary = BuiltInLibrary(simulatorSettings)
    private val imb = InsnMapsBuilder()
    private val exprStack = ExprStack(debugLevel)
    private val compUnit = imb.visit(cu) as IRCompUnit
    private val indexToInsn: Map<Long, IRNode> = imb.indexToInsn()
    private val nameToIndex: Map<String, Long> = imb.nameToIndex()

    constructor(cu: IRCompUnit, stdout: PrintStream) : this(cu, DEFAULT_HEAP_SIZE, stdout)
    constructor(cu: IRCompUnit): this(cu, System.out)

    /**
     * Simulate a function call, throwing away all returned values past the first
     * All arguments to the function call are passed via registers with prefix
     * [Configuration.ABSTRACT_ARG_PREFIX] and indices starting from 0.
     * @param name name of the function call
     * @param args arguments to the function call
     * @return the value that would be in register
     * [Configuration.ABSTRACT_RET_PREFIX] index 0
     */
    fun call(name: String, vararg args: Long): Long {
        return call(ExecutionFrame(this, -1), name, *args)
    }

    /**
     * Simulate a function call.
     * All arguments to the function call are passed via registers with prefix
     * [Configuration.ABSTRACT_ARG_PREFIX] and indices starting from 0.
     * The function call should return the results via registers with prefix
     * [Configuration.ABSTRACT_RET_PREFIX] and indices starting from 0.
     * @param parent parent call frame to write _RET values to
     * @param name name of the function call
     * @param args arguments to the function call
     * @return the value of register
     * [Configuration.ABSTRACT_RET_PREFIX] index 0
     */
    fun call(parent: ExecutionFrame, name: String, vararg args: Long): Long {
        // Catch standard library calls.
        val fDecl = compUnit.getFunction(name)
        val libFun = libraryFunctions.get(name)
        if (fDecl == null && libFun != null) {
            val ret = libFun.execute(args)
            for (i in ret.indices) {
                parent.put(Configuration.ABSTRACT_RET_PREFIX + i, ret[i])
            }
            if (ret.isNotEmpty()) {
                return ret[0]
            }
        } else {
            if (fDecl == null) throw InternalCompilerError("Tried to call an unknown function: '$name'")
            // Create a new stack frame.
            val ip: Long = findLabel(name)
            val frame = ExecutionFrame(this, ip)

            // Pass the remaining arguments into registers.
            for (i in args.indices) frame.put(Configuration.ABSTRACT_ARG_PREFIX + i, args[i])

            // Simulate!
            do {
                val ongoing = frame.advance()
            } while (ongoing)
            val typeInName = name.substring(name.lastIndexOf("_") + 1)
            val numReturnVals = when (typeInName[0]) {
                't' -> {
                    val number = StringBuilder()
                    var i = 1
                    while (i < typeInName.length && Character.isDigit(typeInName[i])) {
                        number.append(typeInName[i])
                        i++
                    }
                    number.toString().toInt()
                }
                'p' -> 0
                else -> 1
            }

            // Transfer child's return temps to the parent frame, because the child frame is going away
            for (i in 0 until numReturnVals) {
                val currRetTmp = Configuration.ABSTRACT_RET_PREFIX + i
                CLI.debugPrint("Returning: $currRetTmp, ${frame[currRetTmp]}")
                parent.put(currRetTmp, frame[currRetTmp])
            }
            if (numReturnVals > 0) {
                return frame[Configuration.ABSTRACT_RET_PREFIX + 0]
            }
        }
        return 0
    }

    protected fun leave(frame: ExecutionFrame) {
        interpret(frame, frame.currentInsn)
    }

    protected fun interpret(frame: ExecutionFrame, insn: IRNode) {
        if (insn is IRInteger) exprStack.pushValue(insn.value()) else if (insn is IRTemp) {
            val tempName = insn.name()
            exprStack.pushTemp(frame[tempName], tempName)
        } else if (insn is IRBinOp) {
            val r: Long = exprStack.popValue()
            val l: Long = exprStack.popValue()
            val result: Long
            result = when (insn.opType()) {
                IRBinOp.OpType.ADD_INT -> l + r
                IRBinOp.OpType.SUB_INT -> l - r
                IRBinOp.OpType.MUL_INT -> l * r
                IRBinOp.OpType.HMUL_INT -> BigInteger.valueOf(l)
                        .multiply(BigInteger.valueOf(r))
                        .shiftRight(64)
                        .toLong()
                IRBinOp.OpType.DIV_INT -> {
                    if (r == 0L) throw Trap("Division by zero!")
                    l / r
                }
                IRBinOp.OpType.MOD_INT -> {
                    if (r == 0L) throw Trap("Division by zero!")
                    l % r
                }
                IRBinOp.OpType.AND -> l and r
                IRBinOp.OpType.OR -> l or r
                IRBinOp.OpType.XOR -> l xor r
                IRBinOp.OpType.LSHIFT -> l shl r.toInt()
                IRBinOp.OpType.RSHIFT -> l ushr r.toInt()
                IRBinOp.OpType.ARSHIFT -> l shr r.toInt()
                IRBinOp.OpType.EQ -> if (l == r) 1 else 0.toLong()
                IRBinOp.OpType.NEQ -> if (l != r) 1 else 0.toLong()
                IRBinOp.OpType.LT -> if (l < r) 1 else 0.toLong()
                IRBinOp.OpType.GT -> if (l > r) 1 else 0.toLong()
                IRBinOp.OpType.LEQ -> if (l <= r) 1 else 0.toLong()
                IRBinOp.OpType.GEQ -> if (l >= r) 1 else 0.toLong()
            }
            exprStack.pushValue(result)
        } else if (insn is IRMem) {
            val addr: Long = exprStack.popValue()
            exprStack.pushAddr(heap.read(addr), addr)
        } else if (insn is IRCall) {
            val argsCount = insn.args().size
            val args = LongArray(argsCount)
            for (i in argsCount - 1 downTo 0) args[i] = exprStack.popValue()
            if (debugLevel > 2) {
                println("Arguments: " + args.contentToString())
            }
            val target: ExprStack.StackItem = exprStack.pop()
            val targetName: String
            targetName = if (target.type == ExprStack.StackItem.Kind.NAME) target.name else if (indexToInsn.containsKey(target.value)) {
                val node = indexToInsn[target.value]
                if (node is IRFuncDecl) node.name() else throw InternalCompilerError("Call to a non-function instruction!")
            } else throw InternalCompilerError("Invalid function call '$insn' (target '${target.value}' is unknown!)")
            val retVal = call(frame, targetName, *args)
            if (debugLevel > 2) {
                System.err.println("Return value: $retVal")
            }
            exprStack.pushValue(retVal)
        } else if (insn is IRName) {
            val name = insn.name()
            exprStack.pushName(if (libraryFunctions.contains(name)) -1 else findLabel(name), name)
        } else if (insn is IRMove) {
            val r: Long = exprStack.popValue()
            val stackItem: ExprStack.StackItem = exprStack.pop()
            when (stackItem.type) {
                ExprStack.StackItem.Kind.MEM -> {
                    if (debugLevel > 0) println("mem[${stackItem.addr}]=$r")
                    heap.store(stackItem.addr, r)
                }
                ExprStack.StackItem.Kind.TEMP -> {
                    if (debugLevel > 0) println("temp[${stackItem.temp}]=$r")
                    frame.put(stackItem.temp, r)
                }
                else -> throw InternalCompilerError("Invalid MOVE!")
            }
        } else if (insn is IRCallStmt) {
            val make: IRNodeFactory = IRNodeFactory_c(insn.location())
            val syntheticCall = make.IRCall(insn.target(),
                    insn.args())
            interpret(frame, syntheticCall)
            exprStack.popValue()
            val collectors = insn.collectors()
            val len = collectors.size
            for (i in 0 until len) {
                val syntheticCollectorTemp = make.IRTemp(collectors[i])
                val syntheticReturnTemp = make
                        .IRTemp(Configuration.ABSTRACT_RET_PREFIX + i)
                interpret(frame, syntheticCollectorTemp)
                interpret(frame, syntheticReturnTemp)
                val syntheticMove = make.IRMove(syntheticCollectorTemp,
                        syntheticReturnTemp)
                interpret(frame, syntheticMove)
            }
        } else if (insn is IRExp) {
            // Discard result.
            exprStack.pop()
        } else if (insn is IRJump) frame.setIP(exprStack.popValue()) else if (insn is IRCJump) {
            val label = when (val top: Long = exprStack.popValue()) {
                0L -> insn.falseLabel().orElse(null)
                1L -> insn.trueLabel()
                else -> throw InternalCompilerError("Invalid value in CJUMP - expected 0/1, got $top")
            }
            if (label != null) frame.setIP(findLabel(label))
        } else if (insn is IRReturn) {
            frame.setIP(-1)
        }
    }

    /**
     * @param name name of the label
     * @return the IR node at the named label
     */
    protected fun findLabel(name: String): Long {
        return nameToIndex[name] ?: throw Trap("Could not find label '$name'!")
    }

    /**
     * Holds the instruction pointer and temporary registers
     * within an execution frame.
     */
    open class ExecutionFrame(
            /** instruction pointer  */
            private val simulator: MyIRSimulator,
            private var ip: Long) {
        /** local registers (register name -> value)  */
        private val regs: MutableMap<String, Long>
        private val r = Random.asJavaRandom()

        /**
         * Fetch the value at the given register
         * @param tempName name of the register
         * @return the value at the given register
         */
        operator fun get(tempName: String): Long {
            if (!regs.containsKey(tempName)) {
                /* Referencing a temp before having written to it - initialize
                   with garbage */
                put(tempName, r.nextLong())
            }
            return regs[tempName]!!
        }

        /**
         * Store a value into the given register
         * @param tempName name of the register
         * @param value value to be stored
         */
        fun put(tempName: String, value: Long) {
            regs[tempName] = value
        }

        /**
         * Advance the instruction pointer. Since we're dealing with a tree,
         * this is postorder traversal, one step at a time, modulo jumps.
         */
        fun advance(): Boolean {
            // Time out if necessary.
            if (Thread.currentThread().isInterrupted) return false
            if (debugLevel > 1) println("Evaluating ${currentInsn.label()} at ${currentInsn.location()}")
            val backupIP = ip
            simulator.leave(this)
            if (ip == -1L) return false /* RETURN */
            if (ip != backupIP) /* A jump was performed */ return true
            ip++
            return true
        }

        fun setIP(ip: Long) {
            this.ip = ip
            if (debugLevel > 1) {
                if (ip == -1L) println("Returning") else println("Jumping to "
                        + currentInsn.label())
            }
        }

        val currentInsn: IRNode
            get() = simulator.indexToInsn[ip] ?: throw Trap("No next instruction.  Forgot RETURN?")

        fun regs(): Map<String, Long> {
            return Collections.unmodifiableMap(regs)
        }

        init {
            regs = HashMap()
        }
    }
}
