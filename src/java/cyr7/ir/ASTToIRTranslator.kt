package cyr7.ir

import cyr7.ast.expr.ExprNode
import cyr7.ast.expr.FunctionCallExprNode
import cyr7.ast.expr.access.ArrayAccessExprNode
import cyr7.ast.expr.access.VariableAccessExprNode
import cyr7.ast.expr.binexpr.BinOpExprNode
import cyr7.ast.expr.literalexpr.*
import cyr7.ast.expr.unaryexpr.BoolNegExprNode
import cyr7.ast.expr.unaryexpr.IntNegExprNode
import cyr7.ast.expr.unaryexpr.LengthExprNode
import cyr7.ast.stmt.*
import cyr7.ast.toplevel.*
import cyr7.ast.type.PrimitiveTypeNode
import cyr7.ast.type.TypeExprArrayNode
import cyr7.ir.interpret.Configuration
import cyr7.ir.nodes.*
import cyr7.semantics.types.*
import cyr7.util.OneOfTwo
import cyr7.visitor.AbstractVisitor
import java_cup.runtime.ComplexSymbolFactory
import java.math.BigInteger
import java.util.*
import java.util.stream.Collectors
import kotlin.collections.MutableList
import kotlin.collections.emptyList
import kotlin.collections.forEach
import kotlin.collections.indices
import kotlin.collections.listOf
import kotlin.collections.map
import kotlin.collections.toList

class ASTToIRTranslator(
        val generator: IdGenerator,
): AbstractVisitor<OneOfTwo<IRExpr, IRStmt>>() {

    private fun assemblyFunctionName(name: String, f: FunctionType): String {
        return assemblyFunctionName(name, f.input, f.output)
    }

    private fun assemblyFunctionName(n: String, inputType: ExpandedType,
                                     outputType: ExpandedType): String {
        val name = "_I" + n.replace("_", "__") + "_"
        return name + typeIdentifier(outputType, false) + typeIdentifier(inputType, true)
    }

    private fun typeIdentifier(t: ExpandedType, isInput: Boolean): String {
        return if (t.isSubtypeOfInt) {
            "i"
        } else if (t.isSubtypeOfBool) {
            "b"
        } else if (t.isSubtypeOfFloat) {
            "f"
        } else if (t.isUnit) {
            if (isInput) "" else "p"
        } else if (t.isSubtypeOfArray) {
            "a" + typeIdentifier(ExpandedType(t.innerArrayType), isInput)
        } else if (t.isTuple) {
            val types = StringBuffer()
            t.types.forEach { type: OrdinaryType ->
                types.append(typeIdentifier(ExpandedType(type), isInput))
            }
            if (isInput) {
                types.toString()
            } else {
                "t" + t.types.size + types.toString()
            }
        } else {
            throw IllegalArgumentException("invalid type for function")
        }
    }

    override fun visit(n: FunctionDeclNode): OneOfTwo<IRExpr, IRStmt> {
        val make = IRNodeFactory_c(n.location)
        val seq: MutableList<IRStmt> = ArrayList()

        /* Adds move statements, moving each argument from argument temps
            to the appropriate temps corresponding to the variable identifiers. */
        for (i in n.header.args.indices) {
            val node = n.header.args[i]
            seq.add(make.IRMove(
                    make.IRTemp(node.identifier),
                    make.IRTemp(generator.argTemp(i))))
        }
        // Add the body of the function.
        seq.add(n.block.accept(this).assertSecond())

        /* If the node had no return statements, insert a return statement
            at the end.
        */
        if (n.resultType == ResultType.UNIT) {
            seq.add(make.IRReturn())
        }
        return OneOfTwo.ofSecond(make.IRSeq(seq))
    }

    override fun visit(n: FunctionHeaderDeclNode): OneOfTwo<IRExpr, IRStmt> {
        throw UnsupportedOperationException()
    }

    override fun visit(n: IxiProgramNode): OneOfTwo<IRExpr, IRStmt> {
        throw UnsupportedOperationException()
    }

    override fun visit(n: UseNode): OneOfTwo<IRExpr, IRStmt> {
        throw UnsupportedOperationException()
    }

    override fun visit(n: VarDeclNode): OneOfTwo<IRExpr, IRStmt> {
        val make: IRNodeFactory = IRNodeFactory_c(n.location)
        return OneOfTwo.ofSecond(
                make.IRMove(make.IRTemp(n.identifier), make.IRInteger(0)))
    }

    override fun visit(n: XiProgramNode): OneOfTwo<IRExpr, IRStmt> {
        val make: IRNodeFactory = IRNodeFactory_c(n.location)

        val file: String = n.location.unit.let {
            val dotIndex = it.lastIndexOf('.')
            if (dotIndex != -1) { it.substring(0, dotIndex) } else { it }
        }
        val program = make.IRCompUnit(file)
        for (function in n.functions) {
            val funStmts = function.accept(this).assertSecond()
            val funcName = this.assemblyFunctionName(function.header.identifier,
                    function.header.type)
            program.appendFunc(
                    make.IRFuncDecl(funcName, funStmts, function.header.type))
        }
        return OneOfTwo.ofSecond(program)
    }

    override fun visit(n: PrimitiveTypeNode): OneOfTwo<IRExpr, IRStmt> {
        val make: IRNodeFactory = IRNodeFactory_c(n.location)
        return OneOfTwo.ofFirst(make.IRInteger(0))
    }

    override fun visit(n: TypeExprArrayNode): OneOfTwo<IRExpr, IRStmt> {
        throw UnsupportedOperationException("TypeExprArrayNode visitor should not be used.")
    }

    private fun allocateArray(n: TypeExprArrayNode, arraySizes: Queue<IRTemp>): IRExpr {
        val make: IRNodeFactory = IRNodeFactory_c(n.location)
        if (arraySizes.isEmpty()) {
            return make.IRInteger(0)
        }
        val commands: MutableList<IRStmt> = ArrayList()
        val size = arraySizes.poll()
        val memBlockStart = generator.newTemp()
        val arrSize = generator.newTemp()
        val pointerStart = generator.newTemp()
        val lh = generator.newLabel()
        val lt = generator.newLabel()
        val lf = generator.newLabel()
        commands.add(make.IRMove(make.IRTemp(arrSize), size))
        val spaceNeeded: IRExpr = make.IRBinOp(IRBinOp.OpType.MUL_INT,
                make.IRInteger(Configuration.WORD_SIZE.toLong()),
                make.IRBinOp(IRBinOp.OpType.ADD_INT,
                        make.IRTemp(arrSize),
                        make.IRInteger(1)))
        val memLoc: IRExpr = make.IRCall(make.IRName("_xi_alloc"),
                listOf(spaceNeeded), 1)
        commands.add(make.IRMove(make.IRTemp(memBlockStart), memLoc))
        commands.add(
                make.IRMove(
                        make.IRMem(make.IRTemp(memBlockStart)),
                        make.IRTemp(arrSize)))
        commands.add(make.IRMove(make.IRTemp(pointerStart),
                make.IRBinOp(IRBinOp.OpType.ADD_INT,
                        make.IRTemp(memBlockStart),
                        make.IRInteger(Configuration.WORD_SIZE.toLong()))))

        /* Iterate through all elements and recursively create child arrays
         * Each element in the array will be instantiated using the instructions
         * generated by createArray.
         * */
        val guard: IRExpr = make.IRBinOp(IRBinOp.OpType.GT,
                make.IRTemp(arrSize),
                make.IRInteger(0))
        val createArray = if (n.child is TypeExprArrayNode) allocateArray(n.child, arraySizes)
                          else make.IRInteger(0)
        val valueLoc: IRExpr = make.IRMem(make.IRBinOp(IRBinOp.OpType.ADD_INT,
                make.IRTemp(pointerStart),
                make.IRBinOp(IRBinOp.OpType.MUL_INT,
                        make.IRInteger(Configuration.WORD_SIZE.toLong()),
                        make.IRTemp(arrSize))))
        val block: IRStmt = make.IRSeq(
                make.IRMove(make.IRTemp(arrSize),
                        make.IRBinOp(IRBinOp.OpType.SUB_INT,
                                make.IRTemp(arrSize),
                                make.IRInteger(1))),
                make.IRMove(valueLoc, createArray))
        commands.add(make.IRSeq(make.IRLabel(lh),
                make.IRCJump(guard, lt, lf),
                make.IRLabel(lt),
                make.IRSeq(block, make.IRJump(make.IRName(lh))),
                make.IRLabel(lf)))
        return make.IRESeq(make.IRSeq(commands), make.IRTemp(pointerStart))
    }

    private fun getArraySizeExprs(n: TypeExprArrayNode): List<IRExpr> {
        var n = n
        var sizeExpr = n.size
        val arraySizes: MutableList<IRExpr> = ArrayList()
        while (sizeExpr.isPresent) {
            val size = sizeExpr.get().accept(this).assertFirst()
            arraySizes.add(size)
            val child = n.child
            if (child is TypeExprArrayNode) {
                n = child
                sizeExpr = n.size
            } else {
                return arraySizes
            }
        }
        return arraySizes
    }


    override fun visit(n: ArrayDeclStmtNode): OneOfTwo<IRExpr, IRStmt> {
        val make: IRNodeFactory = IRNodeFactory_c(n.location)
        // Sizes of each index
        val arraySizes = getArraySizeExprs(n.type)
        val stmts: MutableList<IRStmt> = ArrayList()
        val sizeTemps: Queue<IRTemp> = ArrayDeque()
        for (arraySize in arraySizes) {
            val sizeTemp = make.IRTemp(generator.newTemp())
            stmts.add(make.IRMove(sizeTemp, arraySize))
            sizeTemps.add(sizeTemp)
        }
        val value = allocateArray(n.type, sizeTemps)
        stmts.add(make.IRMove(make.IRTemp(n.identifier), value))
        return OneOfTwo.ofSecond(make.IRSeq(stmts))
    }

    override fun visit(n: AssignmentStmtNode): OneOfTwo<IRExpr, IRStmt> {
        val make: IRNodeFactory = IRNodeFactory_c(n.location)
        val lhs = n.lhs.accept(this).assertFirst()
        val rhs = n.rhs.accept(this).assertFirst()
        return OneOfTwo.ofSecond(make.IRMove(lhs, rhs))
    }

    override fun visit(n: BlockStmtNode): OneOfTwo<IRExpr, IRStmt> {
        val make: IRNodeFactory = IRNodeFactory_c(n.location)
        val stmts = n.statements.map { it.accept(this).assertSecond() }.toList()
        return OneOfTwo.ofSecond(make.IRSeq(stmts))
    }

    override fun visit(n: ExprStmtNode): OneOfTwo<IRExpr, IRStmt> {
        val make: IRNodeFactory = IRNodeFactory_c(n.location)
        val e = n.expr.accept(this).assertFirst()
        return OneOfTwo.ofSecond(make.IRExp(e))
    }

    override fun visit(n: IfElseStmtNode): OneOfTwo<IRExpr, IRStmt> {
        val make: IRNodeFactory = IRNodeFactory_c(n.location)
        val lt = generator.newLabel()
        val lf = generator.newLabel()
        val end = generator.newLabel()
        val commands: MutableList<IRStmt> = ArrayList()
        commands.add(n.guard.accept(CTranslationVisitor(generator, lt, lf)))
        commands.add(make.IRLabel(lf))
        n.elseBlock.ifPresent { commands.add(it.accept(this).assertSecond()) }
        commands.add(make.IRJump(make.IRName(end)))
        commands.add(make.IRLabel(lt))
        commands.add(n.ifBlock.accept(this).assertSecond())
        commands.add(make.IRLabel(end))
        return OneOfTwo.ofSecond(make.IRSeq(commands))
    }

    override fun visit(n: DoWhileStmtNode): OneOfTwo<IRExpr, IRStmt> {
        val make: IRNodeFactory = IRNodeFactory_c(n.location)

        val failsCheck = generator.newLabel()
        val passesCheck = generator.newLabel()
        val stmts: MutableList<IRStmt> = ArrayList()
        stmts.add(make.IRLabel(passesCheck))
        stmts.add(n.body.accept(this).assertSecond())
        stmts.add(n.condition.accept(CTranslationVisitor(generator, passesCheck, failsCheck)))
        stmts.add(make.IRLabel(failsCheck))
        return OneOfTwo.ofSecond(make.IRSeq(stmts))
    }

    override fun visit(n: ForLoopStmtNode): OneOfTwo<IRExpr, IRStmt> {
        val make: IRNodeFactory = IRNodeFactory_c(n.location)

        val end = generator.newLabel()
        val loopGuard = generator.newLabel()
        val loopBody = generator.newLabel()

        val stmts: MutableList<IRStmt> = ArrayList()
        stmts.add(n.varDecl.accept(this).assertSecond())
        stmts.add(make.IRLabel(loopGuard))
        stmts.add(n.condition.accept(CTranslationVisitor(generator, loopBody, end)))
        stmts.add(make.IRLabel(loopBody))
        stmts.add(n.body.accept(this).assertSecond())
        stmts.add(n.epilogue.accept(this).assertSecond())
        stmts.add(make.IRJump(make.IRName(loopGuard)))
        stmts.add(make.IRLabel(end))

        return OneOfTwo.ofSecond(make.IRSeq(stmts))
    }

    override fun visit(n: MultiAssignStmtNode): OneOfTwo<IRExpr, IRStmt> {
        val make: IRNodeFactory = IRNodeFactory_c(n.location)

        val params = n.initializer.parameters.stream()
                .map { stmt: ExprNode -> stmt.accept(this).assertFirst() }
                .collect(Collectors.toList())

        val fType = n.initializer.functionType.get()
        val encodedName = assemblyFunctionName(n.initializer.identifier, fType)

        val collectors: MutableList<String> = ArrayList(n.varDecls.size)
        for (varDeclOpt in n.varDecls) {
            varDeclOpt.ifPresentOrElse({ collectors.add(it.identifier) }) { collectors.add("_") }
        }

        return OneOfTwo.ofSecond(
                make.IRCallStmt(
                        collectors,
                        make.IRName(encodedName),
                        params))
    }

    override fun visit(n: ProcedureStmtNode): OneOfTwo<IRExpr, IRStmt> {
        val make: IRNodeFactory = IRNodeFactory_c(n.location)
        val params = n.procedureCall.parameters.stream()
                .map { it.accept(this).assertFirst() }
                .collect(Collectors.toList())
        val fType = n.procedureCall.functionType.get()
        val encodedName = assemblyFunctionName(n.procedureCall.identifier, fType)
        return OneOfTwo.ofSecond(make.IRCallStmt(emptyList(), make.IRName(encodedName), params))
    }

    override fun visit(n: FreeStmtNode): OneOfTwo<IRExpr, IRStmt> {
        val make: IRNodeFactory = IRNodeFactory_c(n.location)
        val e = n.expr.accept(this).assertFirst()
        return OneOfTwo.ofSecond(
                make.IRExp(make.IRCall(make.IRName("_free_memory_"), listOf(e), 0)))
    }

    override fun visit(n: ReturnStmtNode): OneOfTwo<IRExpr, IRStmt> {
        val make: IRNodeFactory = IRNodeFactory_c(n.location)
        val stmts: MutableList<IRStmt> = ArrayList()
        val returnValTemps: MutableList<IRTemp> = ArrayList()

        // Move each return arg into a temp representing its value
        for (expr in n.exprs) {
            val valTemp = make.IRTemp(generator.newTemp())
            stmts.add(make.IRMove(valTemp, expr.accept(this).assertFirst()))
            returnValTemps.add(valTemp)
        }

        // After calculation, move each of these return values into RET_0, RET_1
        // Need to do this because otherwise "return 1, fun(0)" would overwrite
        // RET_0
        for (i in returnValTemps.indices) {
            stmts.add(make.IRMove(make.IRTemp(generator.retTemp(i)), returnValTemps[i]))
        }
        stmts.add(make.IRReturn())
        return OneOfTwo.ofSecond(make.IRSeq(stmts))
    }

    override fun visit(n: VarDeclStmtNode): OneOfTwo<IRExpr, IRStmt> {
        val make: IRNodeFactory = IRNodeFactory_c(n.location)
        // Initialize it to 0
        return OneOfTwo.ofSecond(
                make.IRMove(make.IRTemp(n.varDecl.identifier), make.IRInteger(0)))
    }

    override fun visit(n: VarInitStmtNode): OneOfTwo<IRExpr, IRStmt> {
        val make: IRNodeFactory = IRNodeFactory_c(n.location)

        val name = n.varDecl.identifier
        val expr = n.initializer.accept(this).assertFirst()
        return OneOfTwo.ofSecond(make.IRMove(make.IRTemp(name), expr))
    }

    override fun visit(n: WhileStmtNode): OneOfTwo<IRExpr, IRStmt> {
        val make: IRNodeFactory = IRNodeFactory_c(n.location)

        val lh = generator.newLabel()
        val lt = generator.newLabel()
        val lf = generator.newLabel()

        val guard = n.guard.accept(CTranslationVisitor(generator, lt, lf))
        val block = n.block.accept(this).assertSecond()

        return OneOfTwo.ofSecond(make.IRSeq(make.IRLabel(lh),
                guard,
                make.IRLabel(lt),
                make.IRSeq(block, make.IRJump(make.IRName(lh))),
                make.IRLabel(lf)))
    }

    override fun visit(n: FunctionCallExprNode): OneOfTwo<IRExpr, IRStmt> {
        val make: IRNodeFactory = IRNodeFactory_c(n.location)

        val params = n.parameters.map { it.accept(this).assertFirst() }.toList()
        val fType = n.functionType.get()
        val encodedName = assemblyFunctionName(n.identifier, fType)
        return OneOfTwo.ofFirst(make.IRCall(make.IRName(encodedName), params,
                fType.output.types.size))
    }

    override fun visit(n: ArrayAccessExprNode): OneOfTwo<IRExpr, IRStmt> {
        val make: IRNodeFactory = IRNodeFactory_c(n.location)

        val arrTemp = generator.newTemp()
        val indexTemp = generator.newTemp()
        val lengthTemp = generator.newTemp()
        val lt = generator.newLabel()
        val lf = generator.newLabel()
        val commands: MutableList<IRStmt> = ArrayList()

        val index = n.index.accept(this).assertFirst()
        val arr = n.child.accept(this).assertFirst()

        commands.add(make.IRMove(make.IRTemp(arrTemp), arr))
        commands.add(make.IRMove(make.IRTemp(indexTemp), index))

        val length: IRExpr = make.IRMem(make.IRBinOp(IRBinOp.OpType.SUB_INT,
                make.IRTemp(arrTemp),
                make.IRInteger(Configuration.WORD_SIZE.toLong())))
        commands.add(make.IRMove(make.IRTemp(lengthTemp), length))

        // Check for out of bounds

        // Check for out of bounds
        commands.add(make.IRCJump(make.IRBinOp(IRBinOp.OpType.AND,
                make.IRBinOp(IRBinOp.OpType.LEQ,
                        make.IRInteger(0),
                        make.IRTemp(indexTemp)),
                make.IRBinOp(IRBinOp.OpType.LT,
                        make.IRTemp(indexTemp),
                        make.IRTemp(lengthTemp))), lt, lf))
        commands.add(make.IRLabel(lf))
        commands.add(make.IRExp(
                make.IRCall(make.IRName("_xi_out_of_bounds"), emptyList(), 1)))
        commands.add(make.IRLabel(lt))

        val value: IRExpr = make.IRMem(make.IRBinOp(IRBinOp.OpType.ADD_INT,
                make.IRTemp(arrTemp),
                make.IRBinOp(IRBinOp.OpType.MUL_INT,
                        make.IRTemp(indexTemp),
                        make.IRInteger(8))))
        return OneOfTwo.ofFirst(make.IRESeq(make.IRSeq(commands), value))
    }

    override fun visit(n: VariableAccessExprNode): OneOfTwo<IRExpr, IRStmt> {
        val make: IRNodeFactory = IRNodeFactory_c(n.location)
        return OneOfTwo.ofFirst(make.IRTemp(n.identifier))
    }

    override fun visit(n: BinOpExprNode): OneOfTwo<IRExpr, IRStmt> {
        return when (n.op) {
            BinOpExprNode.OpType.ADD -> add(n)
            BinOpExprNode.OpType.SUB -> sub(n)
            BinOpExprNode.OpType.MUL -> mul(n)
            BinOpExprNode.OpType.DIV -> div(n)
            BinOpExprNode.OpType.REM -> rem(n)
            BinOpExprNode.OpType.HIGH_MUL -> highMul(n)
            BinOpExprNode.OpType.LTE -> lte(n)
            BinOpExprNode.OpType.LT -> lt(n)
            BinOpExprNode.OpType.GTE -> gte(n)
            BinOpExprNode.OpType.GT -> gt(n)
            BinOpExprNode.OpType.NEQ -> neq(n)
            BinOpExprNode.OpType.EQ -> eq(n)
            BinOpExprNode.OpType.OR -> or(n)
            BinOpExprNode.OpType.AND -> and(n)
        }
    }

    private fun add(n: BinOpExprNode): OneOfTwo<IRExpr, IRStmt> {
        if (n.type!!.isSubtypeOfInt) {
            return binOp(IRBinOp.OpType.ADD_INT, n)
        } else if (n.type!!.isSubtypeOfFloat) {
            return binOp(IRBinOp.OpType.ADD_FLOAT, n)
        }
        assert(n.type!!.isSubtypeOfArray)
        val make: IRNodeFactory = IRNodeFactory_c(n.location)
        val seq: MutableList<IRStmt> = ArrayList()
        val leftArrAddr = generator.newTemp()
        val leftArrSize = generator.newTemp()
        val rightArrAddr = generator.newTemp()
        val rightArrSize = generator.newTemp()
        val summedArrSize = generator.newTemp()
        val summedArrAddr = generator.newTemp()
        seq.add(make.IRMove(make.IRTemp(leftArrAddr),
                n.left.accept(this).assertFirst()))
        seq.add(make.IRMove(make.IRTemp(rightArrAddr),
                n.right.accept(this).assertFirst()))
        seq.add(make.IRMove(make.IRTemp(leftArrSize),
                make.IRMem(make.IRBinOp(IRBinOp.OpType.SUB_INT,
                        make.IRTemp(leftArrAddr),
                        make.IRInteger(8)))))
        seq.add(make.IRMove(make.IRTemp(rightArrSize),
                make.IRMem(make.IRBinOp(IRBinOp.OpType.SUB_INT,
                        make.IRTemp(rightArrAddr),
                        make.IRInteger(8)))))
        seq.add(make.IRMove(make.IRTemp(summedArrSize),
                make.IRBinOp(IRBinOp.OpType.ADD_INT,
                        make.IRTemp(leftArrSize),
                        make.IRTemp(rightArrSize))))

        // Space for concatenated array
        seq.add(make.IRMove(make.IRTemp(summedArrAddr),
                make.IRCall(make.IRName("_xi_alloc"),
                        listOf(make.IRBinOp(IRBinOp.OpType.MUL_INT,
                                make.IRInteger(8),
                                make.IRBinOp(IRBinOp.OpType.ADD_INT,
                                        make.IRTemp(summedArrSize),
                                        make.IRInteger(1)))),
                        1)))
        seq.add(make.IRMove(make.IRMem(make.IRTemp(summedArrAddr)),
                make.IRTemp(summedArrSize)))
        // Move array address pointer to actual start of array
        seq.add(make.IRMove(make.IRTemp(summedArrAddr),
                make.IRBinOp(IRBinOp.OpType.ADD_INT,
                        make.IRTemp(summedArrAddr),
                        make.IRInteger(8))))

        /*
         * i = 0; while (i < leftArr.size) { summed[i] = leftArr[i]; i++; } j =
         * 0; while (j < rightArr.size) { summed[leftArr.size + j] =
         * rightArr[j]; j++; }
         */
        val leftSumming = generator.newLabel()
        val leftBody = generator.newLabel()
        val i = generator.newTemp()
        val rightSumming = generator.newLabel()
        val rightBody = generator.newLabel()
        val j = generator.newTemp()
        val exit = generator.newLabel()
        seq.add(make.IRMove(make.IRTemp(i), make.IRInteger(0)))
        seq.add(make.IRMove(make.IRTemp(j), make.IRInteger(0)))
        seq.add(make.IRLabel(leftSumming))
        // While i < leftArrSize
        seq.add(make.IRCJump(make.IRBinOp(IRBinOp.OpType.LT,
                make.IRTemp(i),
                make.IRTemp(leftArrSize)), leftBody, rightSumming))
        seq.add(make.IRLabel(leftBody))
        // Move value at [leftArrAddr + 8 * i] into [summedArrAddr + 8 * i]
        seq.add(make.IRMove(make.IRMem(make.IRBinOp(IRBinOp.OpType.ADD_INT,
                make.IRTemp(summedArrAddr),
                make.IRBinOp(IRBinOp.OpType.MUL_INT, make.IRTemp(i), make.IRInteger(8)))),
                make.IRMem(make.IRBinOp(IRBinOp.OpType.ADD_INT,
                        make.IRTemp(leftArrAddr),
                        make.IRBinOp(IRBinOp.OpType.MUL_INT,
                                make.IRTemp(i),
                                make.IRInteger(8))))))
        // i++
        seq.add(make.IRMove(make.IRTemp(i),
                make.IRBinOp(IRBinOp.OpType.ADD_INT, make.IRTemp(i), make.IRInteger(1))))
        seq.add(make.IRJump(make.IRName(leftSumming)))
        seq.add(make.IRLabel(rightSumming))
        seq.add(make.IRCJump(make.IRBinOp(IRBinOp.OpType.LT,
                make.IRTemp(j),
                make.IRTemp(rightArrSize)), rightBody, exit))
        seq.add(make.IRLabel(rightBody))
        seq.add(make.IRMove(make.IRMem(make.IRBinOp(IRBinOp.OpType.ADD_INT,
                make.IRTemp(summedArrAddr),
                make.IRBinOp(IRBinOp.OpType.MUL_INT,
                        make.IRBinOp(IRBinOp.OpType.ADD_INT,
                                make.IRTemp(j),
                                make.IRTemp(leftArrSize)),
                        make.IRInteger(8)))),
                make.IRMem(make.IRBinOp(IRBinOp.OpType.ADD_INT,
                        make.IRTemp(rightArrAddr),
                        make.IRBinOp(IRBinOp.OpType.MUL_INT,
                                make.IRTemp(j),
                                make.IRInteger(8))))))
        seq.add(make.IRMove(make.IRTemp(j),
                make.IRBinOp(IRBinOp.OpType.ADD_INT, make.IRTemp(j), make.IRInteger(1))))
        seq.add(make.IRJump(make.IRName(leftSumming)))
        seq.add(make.IRLabel(exit))
        return OneOfTwo.ofFirst(make.IRESeq(make.IRSeq(seq),
                make.IRTemp(summedArrAddr)))
    }

    private fun and(n: BinOpExprNode): OneOfTwo<IRExpr, IRStmt> {
        val make: IRNodeFactory = IRNodeFactory_c(n.location)
        val t = generator.newTemp()
        val lt = generator.newLabel()
        val lf = generator.newLabel()
        val e1CTranslated = n.left.accept(CTranslationVisitor(generator, lt, lf))
        val e2 = n.right.accept(this).assertFirst()
        return OneOfTwo.ofFirst(make.IRESeq(make.IRSeq(make.IRMove(make.IRTemp(
                t), make.IRInteger(0)),
                e1CTranslated,
                make.IRLabel(lt),
                make.IRMove(make.IRTemp(t), e2),
                make.IRLabel(lf)), make.IRTemp(t)))
    }

    private fun eq(n: BinOpExprNode): OneOfTwo<IRExpr, IRStmt> {
        return binOp(IRBinOp.OpType.EQ, n)
    }

    private fun gte(n: BinOpExprNode): OneOfTwo<IRExpr, IRStmt> {
        return binOp(IRBinOp.OpType.GEQ, n)
    }

    private fun gt(n: BinOpExprNode): OneOfTwo<IRExpr, IRStmt> {
        return binOp(IRBinOp.OpType.GT, n)
    }

    private fun highMul(n: BinOpExprNode): OneOfTwo<IRExpr, IRStmt> {
        return binOp(IRBinOp.OpType.HMUL_INT, n)
    }

    private fun lte(n: BinOpExprNode): OneOfTwo<IRExpr, IRStmt> {
        return binOp(IRBinOp.OpType.LEQ, n)
    }

    private fun lt(n: BinOpExprNode): OneOfTwo<IRExpr, IRStmt> {
        return binOp(IRBinOp.OpType.LT, n)
    }

    private fun neq(n: BinOpExprNode): OneOfTwo<IRExpr, IRStmt> {
        return binOp(IRBinOp.OpType.NEQ, n)
    }

    private fun or(n: BinOpExprNode): OneOfTwo<IRExpr, IRStmt> {
        val make: IRNodeFactory = IRNodeFactory_c(n.location)
        val t = generator.newTemp()
        val lt = generator.newLabel()
        val lf = generator.newLabel()
        val e1CTranslated = n.left.accept(CTranslationVisitor(generator,
                lt, lf))
        val e2 = n.right.accept(this)
                .assertFirst()
        return OneOfTwo.ofFirst(make.IRESeq(make.IRSeq(make.IRMove(make.IRTemp(
                t), make.IRInteger(1)),
                e1CTranslated,
                make.IRLabel(lf),
                make.IRMove(make.IRTemp(t), e2),
                make.IRLabel(lt)), make.IRTemp(t)))
    }

    private fun mul(n: BinOpExprNode): OneOfTwo<IRExpr, IRStmt> {
        if (n.left.type!!.isSubtypeOfFloat || n.right.type!!.isSubtypeOfFloat) {
            return binOp(IRBinOp.OpType.MUL_FLOAT, n)
        } else if (n.left.type!!.isSubtypeOfInt && n.right.type!!.isSubtypeOfInt) {
            return binOp(IRBinOp.OpType.MUL_INT, n)
        }
        throw UnsupportedOperationException("Cannot type check")
    }

    private fun rem(n: BinOpExprNode): OneOfTwo<IRExpr, IRStmt> {
        if (n.left.type!!.isSubtypeOfFloat || n.right.type!!.isSubtypeOfFloat) {
            return binOp(IRBinOp.OpType.MOD_FLOAT, n)
        } else if (n.left.type!!.isSubtypeOfInt && n.right.type!!.isSubtypeOfInt) {
            return binOp(IRBinOp.OpType.MOD_INT, n)
        }
        throw UnsupportedOperationException("Cannot type check")
    }

    private fun sub(n: BinOpExprNode): OneOfTwo<IRExpr, IRStmt> {
        if (n.left.type!!.isSubtypeOfFloat || n.right.type!!.isSubtypeOfFloat) {
            return binOp(IRBinOp.OpType.SUB_FLOAT, n)
        } else if (n.left.type!!.isSubtypeOfInt && n.right.type!!.isSubtypeOfInt) {
            return binOp(IRBinOp.OpType.SUB_INT, n)
        }
        throw UnsupportedOperationException("Cannot type check")
    }

    private fun div(n: BinOpExprNode): OneOfTwo<IRExpr, IRStmt> {
        if (n.left.type!!.isSubtypeOfFloat || n.right.type!!.isSubtypeOfFloat) {
            return binOp(IRBinOp.OpType.DIV_FLOAT, n)
        } else if (n.left.type!!.isSubtypeOfInt && n.right.type!!.isSubtypeOfInt) {
            return binOp(IRBinOp.OpType.DIV_INT, n)
        }
        throw UnsupportedOperationException("Cannot type check")
    }

    private fun binOp(left: IRExpr, right: IRExpr, opType: IRBinOp.OpType, n: BinOpExprNode): OneOfTwo<IRExpr, IRStmt> {
        var left: IRExpr = left
        var right: IRExpr = right
        val make: IRNodeFactory = IRNodeFactory_c(n.location)
        if (n.left.type!!.isSubtypeOfInt && n.right.type!!.isSubtypeOfInt) {
            return OneOfTwo.ofFirst(make.IRBinOp(opType, left, right))
        }
        if (n.left.type!!.isSubtypeOfInt) {
            left = make.IRCast(left, PrimitiveType.intDefault, PrimitiveType.floatDefault)
        }
        if (n.right.type!!.isSubtypeOfInt) {
            right = make.IRCast(right, PrimitiveType.intDefault, PrimitiveType.floatDefault)
        }
        return OneOfTwo.ofFirst(make.IRBinOp(opType, left, right))
    }

    private fun binOp(opType: IRBinOp.OpType, n: BinOpExprNode): OneOfTwo<IRExpr, IRStmt> {
        val left = n.left.accept(this).assertFirst()
        val right = n.right.accept(this).assertFirst()
        return binOp(left, right, opType, n)
    }

    /**
     * Translates an array literal with known IRExpr values.
     *
     * @param vals
     * @param location
     * @return
     */
    private fun visitArr(vals: kotlin.collections.List<IRExpr>,
                         location: ComplexSymbolFactory.Location): OneOfTwo<IRExpr, IRStmt> {
        val make: IRNodeFactory = IRNodeFactory_c(location)
        val memBlockStart = generator.newTemp()
        val pointerStart = generator.newTemp()
        val size = vals.size
        val spaceNeeded: IRExpr = make.IRBinOp(IRBinOp.OpType.MUL_INT,
                make.IRInteger(Configuration.WORD_SIZE.toLong()),
                make.IRBinOp(IRBinOp.OpType.ADD_INT, make.IRInteger(size.toLong()), make.IRInteger(1)))
        val commands: MutableList<IRStmt> = ArrayList()
        val memLoc: IRExpr = make.IRCall(make.IRName("_xi_alloc"),
                listOf(spaceNeeded), 1)
        commands.add(make.IRMove(make.IRTemp(memBlockStart), memLoc))
        commands.add(make.IRMove(make.IRMem(make.IRTemp(memBlockStart)),
                make.IRInteger(size.toLong())))
        // Move size into the start of requested memory block.
        commands.add(make.IRMove(make.IRTemp(pointerStart),
                make.IRBinOp(IRBinOp.OpType.ADD_INT,
                        make.IRTemp(memBlockStart),
                        make.IRInteger(Configuration.WORD_SIZE.toLong()))))

        // Setting the values of the indices in memory
        for (i in 0 until size) {
            val valueLoc: IRExpr = make.IRMem(make.IRBinOp(IRBinOp.OpType.ADD_INT,
                    make.IRTemp(pointerStart),
                    make.IRBinOp(IRBinOp.OpType.MUL_INT,
                            make.IRInteger(Configuration.WORD_SIZE.toLong()),
                            make.IRInteger(i.toLong()))))
            commands.add(make.IRMove(valueLoc, vals[i]))
        }
        return OneOfTwo.ofFirst(make.IRESeq(make.IRSeq(commands),
                make.IRTemp(pointerStart)))
    }

    override fun visit(n: LiteralArrayExprNode): OneOfTwo<IRExpr, IRStmt> {
        val values = n.arrayVals.map { it.accept(this).assertFirst() }.toList()
        return visitArr(values, n.location)
    }

    override fun visit(n: LiteralBoolExprNode): OneOfTwo<IRExpr, IRStmt> {
        val make: IRNodeFactory = IRNodeFactory_c(n.location)
        return OneOfTwo.ofFirst(make.IRInteger(if (n.contents) 1 else 0.toLong()))
    }

    override fun visit(n: LiteralCharExprNode): OneOfTwo<IRExpr, IRStmt> {
        val make: IRNodeFactory = IRNodeFactory_c(n.location)
        assert(n.contents.length == 1)
        return OneOfTwo.ofFirst(make.IRInteger(n.contents[0].toLong()))
    }

    override fun visit(n: LiteralIntExprNode): OneOfTwo<IRExpr, IRStmt> {
        val make: IRNodeFactory = IRNodeFactory_c(n.location)
        return OneOfTwo.ofFirst(make.IRInteger(n.contents.toLong()))
    }

    override fun visit(n: LiteralFloatExprNode): OneOfTwo<IRExpr, IRStmt> {
        val make: IRNodeFactory = IRNodeFactory_c(n.location)
        return OneOfTwo.ofFirst(make.IRFloat(n.value))
    }

    override fun visit(n: LiteralStringExprNode): OneOfTwo<IRExpr, IRStmt> {
        val make: IRNodeFactory = IRNodeFactory_c(n.location)

        val vals: MutableList<IRExpr> = ArrayList()
        for (c in n.contents.toCharArray()) {
            vals.add(make.IRInteger(c.toLong()))
        }
        return visitArr(vals, n.location)
    }

    override fun visit(n: BoolNegExprNode): OneOfTwo<IRExpr, IRStmt> {
        val make: IRNodeFactory = IRNodeFactory_c(n.location)
        val e = n.expr.accept(this).assertFirst()
        return OneOfTwo.ofFirst(make.IRBinOp(IRBinOp.OpType.XOR, make.IRInteger(1), e))
    }

    override fun visit(n: IntNegExprNode): OneOfTwo<IRExpr, IRStmt> {
        val make: IRNodeFactory = IRNodeFactory_c(n.location)

        // intercept -2^64 as literal and make it 0-(e-1)-1
        if (n.expr is LiteralIntExprNode) {
            val literal = BigInteger(n.expr.contents)
            var max = BigInteger(Long.MIN_VALUE.toString())
            max = max.negate()
            if (literal == max) {
                return OneOfTwo.ofFirst(make.IRInteger(Long.MIN_VALUE))
            }
        }
        val e = n.expr.accept(this).assertFirst()
        if (Objects.requireNonNull(n.expr.type)!!.isSubtypeOfInt) {
            return OneOfTwo.ofFirst(make.IRBinOp(IRBinOp.OpType.SUB_INT,
                    make.IRInteger(0), e))
        } else if (n.expr.type!!.isSubtypeOfFloat) {
            return OneOfTwo.ofFirst(make.IRBinOp(IRBinOp.OpType.SUB_FLOAT,
                    make.IRFloat(0.0), e))
        }
        throw UnsupportedOperationException("Cannot negate value")
    }

    override fun visit(n: LengthExprNode): OneOfTwo<IRExpr, IRStmt> {
        val make: IRNodeFactory = IRNodeFactory_c(n.location)
        val e = n.expr.accept(this).assertFirst()
        return OneOfTwo.ofFirst(make.IRMem(make.IRBinOp(IRBinOp.OpType.SUB_INT,
                e, make.IRInteger(Configuration.WORD_SIZE.toLong()))))
    }
}
