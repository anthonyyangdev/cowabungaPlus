package cyr7.ir;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

import cyr7.ast.stmt.VarDeclNode;
import cyr7.ast.expr.ExprNode;
import cyr7.ast.expr.FunctionCallExprNode;
import cyr7.ast.expr.access.ArrayAccessExprNode;
import cyr7.ast.expr.access.VariableAccessExprNode;
import cyr7.ast.expr.binexpr.*;
import cyr7.ast.expr.literalexpr.*;
import cyr7.ast.expr.unaryexpr.BoolNegExprNode;
import cyr7.ast.expr.unaryexpr.IntNegExprNode;
import cyr7.ast.expr.unaryexpr.LengthExprNode;
import cyr7.ast.stmt.*;
import cyr7.ast.toplevel.FunctionDeclNode;
import cyr7.ast.toplevel.FunctionHeaderDeclNode;
import cyr7.ast.toplevel.IxiProgramNode;
import cyr7.ast.toplevel.UseNode;
import cyr7.ast.toplevel.XiProgramNode;
import cyr7.ast.type.PrimitiveTypeNode;
import cyr7.ast.type.TypeExprArrayNode;
import cyr7.ir.interpret.Configuration;
import cyr7.ir.nodes.*;
import cyr7.ir.nodes.IRBinOp.OpType;
import cyr7.semantics.types.ExpandedType;
import cyr7.semantics.types.FunctionType;
import cyr7.semantics.types.PrimitiveType;
import cyr7.semantics.types.ResultType;
import cyr7.util.OneOfTwo;
import cyr7.visitor.AbstractVisitor;
import java_cup.runtime.ComplexSymbolFactory.Location;

/**
 * Assumption: All CJUMPs have both labels set
 */
public class ASTToIRVisitor extends AbstractVisitor<OneOfTwo<IRExpr, IRStmt>> {

    private final IdGenerator generator;

    public ASTToIRVisitor(IdGenerator generator) {
        this.generator = generator;
    }

    private String assemblyFunctionName(String name, FunctionType f) {
        return assemblyFunctionName(name, f.input, f.output);
    }

    private String assemblyFunctionName(String n, ExpandedType inputType,
            ExpandedType outputType) {
        String name = "_I" + n.replace("_", "__") + "_";
        return name + typeIdentifier(outputType, false) + typeIdentifier(
                inputType,
                true);
    }

    private String typeIdentifier(ExpandedType t, boolean isInput) {
        if (t.isSubtypeOfInt()) {
            return "i";
        } else if (t.isSubtypeOfBool()) {
            return "b";
        } else if (t.isSubtypeOfFloat()) {
            return "f";
        } else if (t.isUnit()) {
            return isInput ? "" : "p";
        } else if (t.isSubtypeOfArray()) {
            return "a" + typeIdentifier(new ExpandedType(t.getInnerArrayType()),
                    isInput);
        } else if (t.isTuple()) {
            StringBuffer types = new StringBuffer();
            t.getTypes().forEach(type ->
                    types.append(typeIdentifier(
                        new ExpandedType(type),
                        isInput)));
            if (isInput) {
                return types.toString();
            } else {
                return "t" + t.getTypes().size() + types.toString();
            }
        } else {
            throw new IllegalArgumentException("invalid type for function");
        }
    }

    // Top Level

    @Override
    public OneOfTwo<IRExpr, IRStmt> visit(FunctionDeclNode n) {
        IRNodeFactory make = new IRNodeFactory_c(n.getLocation());

        List<IRStmt> seq = new ArrayList<>();

        /* Adds move statements, moving each argument from argument temps
            to the appropriate temps corresponding to the variable identifiers.
        */
        for (int i = 0; i < n.header.args.size(); i++) {
            VarDeclNode node = n.header.args.get(i);
            seq.add(make.IRMove(
                    make.IRTemp(node.identifier),
                    make.IRTemp(generator.argTemp(i))));
        }

        // Add the body of the function.
        seq.add(n.block.accept(this)
                       .assertSecond());

        /* If the node had no return statements, insert a return statement
            at the end.
        */
        if (n.getResultType() == ResultType.UNIT) {
            seq.add(make.IRReturn());
        }
        return OneOfTwo.ofSecond(make.IRSeq(seq));
    }

    @Override
    public OneOfTwo<IRExpr, IRStmt> visit(FunctionHeaderDeclNode n) {
        throw new UnsupportedOperationException();
    }

    @Override
    public OneOfTwo<IRExpr, IRStmt> visit(IxiProgramNode n) {
        throw new UnsupportedOperationException();
    }

    @Override
    public OneOfTwo<IRExpr, IRStmt> visit(UseNode n) {
        throw new UnsupportedOperationException();
    }

    /**
     * Translates by instantiating the variable with a null value, i.e. 0.
     */
    @Override
    public OneOfTwo<IRExpr, IRStmt> visit(VarDeclNode n) {
        IRNodeFactory make = new IRNodeFactory_c(n.getLocation());

        return OneOfTwo.ofSecond(
            make.IRMove(make.IRTemp(n.identifier), make.IRInteger(0)));
    }

    @Override
    public OneOfTwo<IRExpr, IRStmt> visit(XiProgramNode n) {
        IRNodeFactory make = new IRNodeFactory_c(n.getLocation());

        String file = n.getLocation().getUnit();

        int dotIndex = file.lastIndexOf('.');
        if (dotIndex != -1) {
            file = file.substring(0, file.lastIndexOf('.'));
        }

        IRCompUnit program = make.IRCompUnit(file);
        for (FunctionDeclNode fun : n.functions) {
            IRStmt funStmts = fun.accept(this).assertSecond();
            String funcName = this.assemblyFunctionName(fun.header.identifier,
                    fun.header.getType());
            program.appendFunc(
                    make.IRFuncDecl(funcName, funStmts, fun.header.getType()));
        }
        return OneOfTwo.ofSecond(program);
    }

    @Override
    public OneOfTwo<IRExpr, IRStmt> visit(PrimitiveTypeNode n) {
        IRNodeFactory make = new IRNodeFactory_c(n.getLocation());
        return OneOfTwo.ofFirst(make.IRInteger(0));
    }

    /**
     * Allocates space in the heap for an n-dimensional array.
     */
    @Override
    public OneOfTwo<IRExpr, IRStmt> visit(TypeExprArrayNode n) {
        throw new UnsupportedOperationException("TypeExprArrayNode "
                                    + "visitor should not be used.");
    }

    private IRExpr allocateArray(TypeExprArrayNode n, Queue<IRTemp> arraySizes) {

        IRNodeFactory make = new IRNodeFactory_c(n.getLocation());
        if (arraySizes.isEmpty()) {
            return make.IRInteger(0);
        }

        List<IRStmt> commands = new ArrayList<>();
        IRTemp size = arraySizes.poll();

        String memBlockStart = generator.newTemp();
        String arrSize = generator.newTemp();
        String pointerStart = generator.newTemp();
        String lh = generator.newLabel();
        String lt = generator.newLabel();
        String lf = generator.newLabel();

        commands.add(make.IRMove(make.IRTemp(arrSize), size));

        IRExpr spaceNeeded = make.IRBinOp(OpType.MUL_INT,
                make.IRInteger(Configuration.WORD_SIZE),
                make.IRBinOp(OpType.ADD_INT,
                        make.IRTemp(arrSize),
                        make.IRInteger(1)));

        IRExpr memLoc = make.IRCall(make.IRName("_xi_alloc"),
                List.of(spaceNeeded), 1);
        commands.add(make.IRMove(make.IRTemp(memBlockStart), memLoc));

        commands.add(
                make.IRMove(
                        make.IRMem(make.IRTemp(memBlockStart)),
                        make.IRTemp(arrSize)));

        commands.add(make.IRMove(make.IRTemp(pointerStart),
                make.IRBinOp(OpType.ADD_INT,
                        make.IRTemp(memBlockStart),
                        make.IRInteger(Configuration.WORD_SIZE))));

        /* Iterate through all elements and recursively create child arrays
         * Each element in the array will be instantiated using the instructions
         * generated by createArray.
         * */
        IRExpr guard = make.IRBinOp(OpType.GT,
                make.IRTemp(arrSize),
                make.IRInteger(0));

        IRExpr createArray = (n.child instanceof TypeExprArrayNode)
                ? this.allocateArray((TypeExprArrayNode)(n.child), arraySizes)
                : make.IRInteger(0);

        IRExpr valueLoc = make.IRMem(make.IRBinOp(OpType.ADD_INT,
                make.IRTemp(pointerStart),
                make.IRBinOp(OpType.MUL_INT,
                        make.IRInteger(Configuration.WORD_SIZE),
                        make.IRTemp(arrSize))));

        IRStmt block = make.IRSeq(
                make.IRMove(make.IRTemp(arrSize),
                        make.IRBinOp(OpType.SUB_INT,
                                make.IRTemp(arrSize),
                                make.IRInteger(1))),
                make.IRMove(valueLoc, createArray));

        commands.add(make.IRSeq(make.IRLabel(lh),
                make.IRCJump(guard, lt, lf),
                make.IRLabel(lt),
                make.IRSeq(block, make.IRJump(make.IRName(lh))),
                make.IRLabel(lf)));

        return make.IRESeq(make.IRSeq(commands), make.IRTemp(pointerStart));
    }

    private List<IRExpr> getArraySizeExprs(TypeExprArrayNode n) {
        Optional<ExprNode> sizeExpr = n.size;
        List<IRExpr> arraySizes = new ArrayList<>();
        while (sizeExpr.isPresent()) {
            IRExpr size = sizeExpr.get().accept(this).assertFirst();
            arraySizes.add(size);
            if (n.child instanceof TypeExprArrayNode) {
                n = (TypeExprArrayNode)(n.child);
                sizeExpr = n.size;
            } else {
                return arraySizes;
            }
        }
        return arraySizes;
    }

    // Statements

    @Override
    public OneOfTwo<IRExpr, IRStmt> visit(ArrayDeclStmtNode n) {
        IRNodeFactory make = new IRNodeFactory_c(n.getLocation());

        // Sizes of each index
        List<IRExpr> arraySizes = this.getArraySizeExprs(n.type);

        List<IRStmt> stmts = new ArrayList<>();
        Queue<IRTemp> sizeTemps = new ArrayDeque<>();

        for (IRExpr arraySize : arraySizes) {
            IRTemp sizeTemp = make.IRTemp(generator.newTemp());
            stmts.add(make.IRMove(sizeTemp, arraySize));
            sizeTemps.add(sizeTemp);
        }
        IRExpr val = this.allocateArray(n.type, sizeTemps);

        stmts.add(make.IRMove(make.IRTemp(n.identifier), val));
        return OneOfTwo.ofSecond(make.IRSeq(stmts));
    }

    @Override
    public OneOfTwo<IRExpr, IRStmt> visit(AssignmentStmtNode n) {
        IRNodeFactory make = new IRNodeFactory_c(n.getLocation());

        IRExpr lhs = n.lhs.accept(this)
                          .assertFirst();
        IRExpr rhs = n.rhs.accept(this)
                          .assertFirst();
        return OneOfTwo.ofSecond(make.IRMove(lhs, rhs));
    }

    @Override
    public OneOfTwo<IRExpr, IRStmt> visit(BlockStmtNode n) {
        IRNodeFactory make = new IRNodeFactory_c(n.getLocation());

        List<IRStmt> stmts = n.statements.stream()
                                         .map(stmt -> stmt.accept(this)
                                                          .assertSecond())
                                         .collect(Collectors.toList());
        return OneOfTwo.ofSecond(make.IRSeq(stmts));
    }

    @Override
    public OneOfTwo<IRExpr, IRStmt> visit(ExprStmtNode n) {
        IRNodeFactory make = new IRNodeFactory_c(n.getLocation());

        IRExpr e = n.expr.accept(this)
                         .assertFirst();
        return OneOfTwo.ofSecond(make.IRExp(e));
    }

    /**
     * Translates if (e) s1 else s2
     */
    @Override
    public OneOfTwo<IRExpr, IRStmt> visit(IfElseStmtNode n) {
        IRNodeFactory make = new IRNodeFactory_c(n.getLocation());

        String lt = generator.newLabel();
        String lf = generator.newLabel();
        String end = generator.newLabel();
        List<IRStmt> commands = new ArrayList<>();

        commands.add(n.guard.accept(new CTranslationVisitor(generator, lt,
                lf)));
        commands.add(make.IRLabel(lf));
        n.elseBlock.ifPresent(stmtNode -> commands.add(stmtNode
                .accept(this)
                .assertSecond()));
        commands.add(make.IRJump(make.IRName(end)));
        commands.add(make.IRLabel(lt));
        commands.add(n.ifBlock.accept(this)
                              .assertSecond());
        commands.add(make.IRLabel(end));
        return OneOfTwo.ofSecond(make.IRSeq(commands));
    }

    @Override
    public OneOfTwo<IRExpr, IRStmt> visit(DoWhileStmtNode n) {
        IRNodeFactory make = new IRNodeFactory_c(n.getLocation());

        String failsCheck = generator.newLabel();
        String passesCheck = generator.newLabel();
        List<IRStmt> stmts = new ArrayList<>();

        stmts.add(make.IRLabel(passesCheck));
        stmts.add(n.getBody().accept(this).assertSecond());
        stmts.add(n.getCondition().accept(new CTranslationVisitor(generator, passesCheck, failsCheck)));
        stmts.add(make.IRLabel(failsCheck));
        return OneOfTwo.ofSecond(make.IRSeq(stmts));
    }

    @Override
    public OneOfTwo<IRExpr, IRStmt> visit(ForLoopStmtNode n) {
        IRNodeFactory make = new IRNodeFactory_c(n.getLocation());

        String end = generator.newLabel();
        String loopGuard = generator.newLabel();
        String loopBody = generator.newLabel();

        List<IRStmt> stmts = new ArrayList<>();
        stmts.add(n.getVarDecl().accept(this).assertSecond());
        stmts.add(make.IRLabel(loopGuard));
        stmts.add(n.getCondition().accept(new CTranslationVisitor(generator, loopBody, end)));
        stmts.add(make.IRLabel(loopBody));
        stmts.add(n.getBody().accept(this).assertSecond());
        stmts.add(n.getEpilogue().accept(this).assertSecond());
        stmts.add(make.IRJump(make.IRName(loopGuard)));
        stmts.add(make.IRLabel(end));

        return OneOfTwo.ofSecond(make.IRSeq(stmts));
    }

    @Override
    public OneOfTwo<IRExpr, IRStmt> visit(MultiAssignStmtNode n) {
        IRNodeFactory make = new IRNodeFactory_c(n.getLocation());

        List<IRExpr> params = n.initializer.parameters.stream()
            .map(stmt -> stmt.accept(this).assertFirst())
            .collect(Collectors.toList());

        var fType = n.initializer.getFunctionType().get();

        String encodedName = assemblyFunctionName(
            n.initializer.identifier,
            fType);

        List<String> collectors = new ArrayList<>(n.varDecls.size());
        for (Optional<VarDeclNode> var : n.varDecls) {
            var.ifPresentOrElse(
                varDecl -> collectors.add(varDecl.identifier),
                () -> collectors.add("_"));
        }

        return OneOfTwo.ofSecond(
            make.IRCallStmt(
                collectors,
                make.IRName(encodedName),
                params));
    }

    @Override
    public OneOfTwo<IRExpr, IRStmt> visit(ProcedureStmtNode n) {
        IRNodeFactory make = new IRNodeFactory_c(n.getLocation());
        List<IRExpr> params = n.procedureCall.parameters.stream()
            .map(stmt -> stmt.accept(this).assertFirst())
            .collect(Collectors.toList());
        var fType = n.procedureCall.getFunctionType().get();
        String encodedName = assemblyFunctionName(n.procedureCall.identifier, fType);
        return OneOfTwo.ofSecond(make.IRCallStmt(List.of(), make.IRName(encodedName), params));
    }

    @Override
    public OneOfTwo<IRExpr, IRStmt> visit(FreeStmtNode n) {
        IRNodeFactory make = new IRNodeFactory_c(n.getLocation());
        IRExpr e = n.getExpr().accept(this)
                .assertFirst();
        return OneOfTwo.ofSecond(
                make.IRExp(make.IRCall(make.IRName("_free_memory_"), List.of(e), 0)));
    }

    @Override
    public OneOfTwo<IRExpr, IRStmt> visit(ReturnStmtNode n) {
        IRNodeFactory make = new IRNodeFactory_c(n.getLocation());

        List<IRStmt> stmts = new ArrayList<>();
        List<IRTemp> returnValTemps = new ArrayList<>();

        // Move each return arg into a temp representing its value
        for (ExprNode expr : n.exprs) {
            IRTemp valTemp = make.IRTemp(generator.newTemp());
            stmts.add(make.IRMove(valTemp,
                    expr.accept(this)
                        .assertFirst()));
            returnValTemps.add(valTemp);
        }

        // After calculation, move each of these return values into RET_0, RET_1
        // Need to do this because otherwise "return 1, fun(0)" would overwrite
        // RET_0
        for (int i = 0; i < returnValTemps.size(); i++) {
            stmts.add(make.IRMove(make.IRTemp(generator.retTemp(i)),
                    returnValTemps.get(i)));
        }
        stmts.add(make.IRReturn());
        return OneOfTwo.ofSecond(make.IRSeq(stmts));
    }

    @Override
    public OneOfTwo<IRExpr, IRStmt> visit(VarDeclStmtNode n) {
        IRNodeFactory make = new IRNodeFactory_c(n.getLocation());

        // Initialize it to 0
        return OneOfTwo.ofSecond(make.IRMove(make.IRTemp(n.varDecl.identifier),
                make.IRInteger(0)));
    }

    @Override
    public OneOfTwo<IRExpr, IRStmt> visit(VarInitStmtNode n) {
        IRNodeFactory make = new IRNodeFactory_c(n.getLocation());

        String name = n.varDecl.identifier;
        IRExpr expr = n.initializer.accept(this)
                                   .assertFirst();

        return OneOfTwo.ofSecond(make.IRMove(make.IRTemp(name), expr));
    }

    @Override
    public OneOfTwo<IRExpr, IRStmt> visit(WhileStmtNode n) {
        IRNodeFactory make = new IRNodeFactory_c(n.getLocation());

        String lh = generator.newLabel();
        String lt = generator.newLabel();
        String lf = generator.newLabel();

        IRStmt guard = n.guard.accept(new CTranslationVisitor(generator, lt,
                lf));
        IRStmt block = n.block.accept(this)
                              .assertSecond();

        return OneOfTwo.ofSecond(make.IRSeq(make.IRLabel(lh),
                guard,
                make.IRLabel(lt),
                make.IRSeq(block, make.IRJump(make.IRName(lh))),
                make.IRLabel(lf)));
    }

    // Expressions

    /**
     * Translates f(e1, e2, e3, ...)
     */
    @Override
    public OneOfTwo<IRExpr, IRStmt> visit(FunctionCallExprNode n) {
        IRNodeFactory make = new IRNodeFactory_c(n.getLocation());

        List<IRExpr> params = n.parameters.stream()
                .map(stmt -> stmt.accept(this).assertFirst())
                .collect(Collectors.toList());
        var fType = n.getFunctionType().get();
        String encodedName = assemblyFunctionName(n.identifier, fType);
        return OneOfTwo.ofFirst(make.IRCall(make.IRName(encodedName), params,
                fType.output.getTypes().size()));
    }

    /**
     * Translates arr[index].
     */
    @Override
    public OneOfTwo<IRExpr, IRStmt> visit(ArrayAccessExprNode n) {
        IRNodeFactory make = new IRNodeFactory_c(n.getLocation());

        String arrTemp = generator.newTemp();
        String indexTemp = generator.newTemp();
        String lengthTemp = generator.newTemp();
        String lt = generator.newLabel();
        String lf = generator.newLabel();
        List<IRStmt> commands = new ArrayList<IRStmt>();

        IRExpr index = n.index.accept(this)
                              .assertFirst();
        IRExpr arr = n.child.accept(this)
                            .assertFirst();

        commands.add(make.IRMove(make.IRTemp(arrTemp), arr));
        commands.add(make.IRMove(make.IRTemp(indexTemp), index));

        IRExpr length = make.IRMem(make.IRBinOp(OpType.SUB_INT,
                make.IRTemp(arrTemp),
                make.IRInteger(Configuration.WORD_SIZE)));
        commands.add(make.IRMove(make.IRTemp(lengthTemp), length));

        // Check for out of bounds
        commands.add(make.IRCJump(make.IRBinOp(OpType.AND,
                make.IRBinOp(OpType.LEQ,
                        make.IRInteger(0),
                        make.IRTemp(indexTemp)),
                make.IRBinOp(OpType.LT,
                        make.IRTemp(indexTemp),
                        make.IRTemp(lengthTemp))), lt, lf));
        commands.add(make.IRLabel(lf));
        commands.add(make.IRExp(
                make.IRCall(make.IRName("_xi_out_of_bounds"), List.of(), 1)));
        commands.add(make.IRLabel(lt));

        IRExpr val = make.IRMem(make.IRBinOp(OpType.ADD_INT,
                make.IRTemp(arrTemp),
                make.IRBinOp(OpType.MUL_INT,
                        make.IRTemp(indexTemp),
                        make.IRInteger(8))));
        return OneOfTwo.ofFirst(make.IRESeq(make.IRSeq(commands), val));
    }

    @Override
    public OneOfTwo<IRExpr, IRStmt> visit(VariableAccessExprNode n) {
        IRNodeFactory make = new IRNodeFactory_c(n.getLocation());
        return OneOfTwo.ofFirst(make.IRTemp(n.identifier));
    }

    @Override
    public OneOfTwo<IRExpr, IRStmt> visit(BinOpExprNode n) {
        switch(n.getOp()) {
            case ADD: return add(n);
            case SUB: return sub(n);
            case MUL: return mul(n);
            case DIV: return div(n);
            case REM: return rem(n);
            case HIGH_MUL: return highMul(n);
            case LTE: return lte(n);
            case LT: return lt(n);
            case GTE: return gte(n);
            case GT: return gt(n);
            case NEQ: return neq(n);
            case EQ: return eq(n);
            case OR: return or(n);
            case AND: return and(n);
            default:
                throw new UnsupportedOperationException("Unimplemented");
        }
    }

    public OneOfTwo<IRExpr, IRStmt> add(BinOpExprNode n) {
        if (n.getType().isSubtypeOfInt()) {
            return binOp(IRBinOp.OpType.ADD_INT, n);
        } else if (n.getType().isSubtypeOfFloat()) {
            return binOp(OpType.ADD_FLOAT, n);
        }
        assert n.getType().isSubtypeOfArray();

        IRNodeFactory make = new IRNodeFactory_c(n.getLocation());
        List<IRStmt> seq = new ArrayList<>();

        String leftArrAddr = generator.newTemp();
        String leftArrSize = generator.newTemp();

        String rightArrAddr = generator.newTemp();
        String rightArrSize = generator.newTemp();

        String summedArrSize = generator.newTemp();
        String summedArrAddr = generator.newTemp();

        seq.add(make.IRMove(make.IRTemp(leftArrAddr),
                n.getLeft().accept(this)
                      .assertFirst()));

        seq.add(make.IRMove(make.IRTemp(rightArrAddr),
                n.getRight().accept(this)
                       .assertFirst()));

        seq.add(make.IRMove(make.IRTemp(leftArrSize),
                make.IRMem(make.IRBinOp(OpType.SUB_INT,
                        make.IRTemp(leftArrAddr),
                        make.IRInteger(8)))));
        seq.add(make.IRMove(make.IRTemp(rightArrSize),
                make.IRMem(make.IRBinOp(OpType.SUB_INT,
                        make.IRTemp(rightArrAddr),
                        make.IRInteger(8)))));
        seq.add(make.IRMove(make.IRTemp(summedArrSize),
                make.IRBinOp(OpType.ADD_INT,
                        make.IRTemp(leftArrSize),
                        make.IRTemp(rightArrSize))));

        // Space for concatenated array
        seq.add(make.IRMove(make.IRTemp(summedArrAddr),
                make.IRCall(make.IRName("_xi_alloc"),
                        List.of(make.IRBinOp(OpType.MUL_INT,
                                make.IRInteger(8),
                                make.IRBinOp(OpType.ADD_INT,
                                        make.IRTemp(summedArrSize),
                                        make.IRInteger(1)))),
                        1)));

        seq.add(make.IRMove(make.IRMem(make.IRTemp(summedArrAddr)),
                make.IRTemp(summedArrSize)));
        // Move array address pointer to actual start of array
        seq.add(make.IRMove(make.IRTemp(summedArrAddr),
                make.IRBinOp(OpType.ADD_INT,
                        make.IRTemp(summedArrAddr),
                        make.IRInteger(8))));

        /*
         * i = 0; while (i < leftArr.size) { summed[i] = leftArr[i]; i++; } j =
         * 0; while (j < rightArr.size) { summed[leftArr.size + j] =
         * rightArr[j]; j++; }
         */

        String leftSumming = generator.newLabel();
        String leftBody = generator.newLabel();
        String i = generator.newTemp();
        String rightSumming = generator.newLabel();
        String rightBody = generator.newLabel();
        String j = generator.newTemp();
        String exit = generator.newLabel();

        seq.add(make.IRMove(make.IRTemp(i), make.IRInteger(0)));
        seq.add(make.IRMove(make.IRTemp(j), make.IRInteger(0)));

        seq.add(make.IRLabel(leftSumming));
        // While i < leftArrSize
        seq.add(make.IRCJump(make.IRBinOp(OpType.LT,
                make.IRTemp(i),
                make.IRTemp(leftArrSize)), leftBody, rightSumming));
        seq.add(make.IRLabel(leftBody));
        // Move value at [leftArrAddr + 8 * i] into [summedArrAddr + 8 * i]
        seq.add(make.IRMove(make.IRMem(make.IRBinOp(OpType.ADD_INT,
                make.IRTemp(summedArrAddr),
                make.IRBinOp(OpType.MUL_INT, make.IRTemp(i), make.IRInteger(8)))),
                make.IRMem(make.IRBinOp(OpType.ADD_INT,
                        make.IRTemp(leftArrAddr),
                        make.IRBinOp(OpType.MUL_INT,
                                make.IRTemp(i),
                                make.IRInteger(8))))));
        // i++
        seq.add(make.IRMove(make.IRTemp(i),
                make.IRBinOp(OpType.ADD_INT, make.IRTemp(i), make.IRInteger(1))));

        seq.add(make.IRJump(make.IRName(leftSumming)));

        seq.add(make.IRLabel(rightSumming));
        seq.add(make.IRCJump(make.IRBinOp(OpType.LT,
                make.IRTemp(j),
                make.IRTemp(rightArrSize)), rightBody, exit));
        seq.add(make.IRLabel(rightBody));
        seq.add(make.IRMove(make.IRMem(make.IRBinOp(OpType.ADD_INT,
                make.IRTemp(summedArrAddr),
                make.IRBinOp(OpType.MUL_INT,
                        make.IRBinOp(OpType.ADD_INT,
                                make.IRTemp(j),
                                make.IRTemp(leftArrSize)),
                        make.IRInteger(8)))),
                make.IRMem(make.IRBinOp(OpType.ADD_INT,
                        make.IRTemp(rightArrAddr),
                        make.IRBinOp(OpType.MUL_INT,
                                make.IRTemp(j),
                                make.IRInteger(8))))));
        seq.add(make.IRMove(make.IRTemp(j),
                make.IRBinOp(OpType.ADD_INT, make.IRTemp(j), make.IRInteger(1))));
        seq.add(make.IRJump(make.IRName(leftSumming)));

        seq.add(make.IRLabel(exit));
        return OneOfTwo.ofFirst(make.IRESeq(make.IRSeq(seq),
                make.IRTemp(summedArrAddr)));
    }

    public OneOfTwo<IRExpr, IRStmt> and(BinOpExprNode n) {
        IRNodeFactory make = new IRNodeFactory_c(n.getLocation());

        String t = generator.newTemp();
        String lt = generator.newLabel();
        String lf = generator.newLabel();

        IRStmt e1CTranslated = n.getLeft().accept(new CTranslationVisitor(generator, lt, lf));
        IRExpr e2 = n.getRight().accept(this).assertFirst();

        return OneOfTwo.ofFirst(make.IRESeq(make.IRSeq(make.IRMove(make.IRTemp(
                t), make.IRInteger(0)),
                e1CTranslated,
                make.IRLabel(lt),
                make.IRMove(make.IRTemp(t), e2),
                make.IRLabel(lf)), make.IRTemp(t)));
    }

    public OneOfTwo<IRExpr, IRStmt> eq(BinOpExprNode n) {
        return binOp(IRBinOp.OpType.EQ, n);
    }

    public OneOfTwo<IRExpr, IRStmt> gte(BinOpExprNode n) {
        return binOp(IRBinOp.OpType.GEQ, n);
    }

    public OneOfTwo<IRExpr, IRStmt> gt(BinOpExprNode n) {
        return binOp(IRBinOp.OpType.GT, n);
    }

    public OneOfTwo<IRExpr, IRStmt> highMul(BinOpExprNode n) {
        return binOp(IRBinOp.OpType.HMUL_INT, n);
    }

    public OneOfTwo<IRExpr, IRStmt> lte(BinOpExprNode n) {
        return binOp(IRBinOp.OpType.LEQ, n);
    }

    public OneOfTwo<IRExpr, IRStmt> lt(BinOpExprNode n) {
        return binOp(IRBinOp.OpType.LT, n);
    }

    public OneOfTwo<IRExpr, IRStmt> neq(BinOpExprNode n) {
        return binOp(IRBinOp.OpType.NEQ, n);
    }

    public OneOfTwo<IRExpr, IRStmt> or(BinOpExprNode n) {
        IRNodeFactory make = new IRNodeFactory_c(n.getLocation());

        String t = generator.newTemp();
        String lt = generator.newLabel();
        String lf = generator.newLabel();

        IRStmt e1CTranslated = n.getLeft().accept(new CTranslationVisitor(generator,
                lt, lf));
        IRExpr e2 = n.getRight().accept(this)
                           .assertFirst();

        return OneOfTwo.ofFirst(make.IRESeq(make.IRSeq(make.IRMove(make.IRTemp(
                t), make.IRInteger(1)),
                e1CTranslated,
                make.IRLabel(lf),
                make.IRMove(make.IRTemp(t), e2),
                make.IRLabel(lt)), make.IRTemp(t)));
    }

    public OneOfTwo<IRExpr, IRStmt> mul(BinOpExprNode n) {
        if (n.getLeft().getType().isSubtypeOfFloat() || n.getRight().getType().isSubtypeOfFloat()) {
            return binOp(OpType.MUL_FLOAT, n);
        } else if (n.getLeft().getType().isSubtypeOfInt() && n.getRight().getType().isSubtypeOfInt()) {
            return binOp(OpType.MUL_INT, n);
        }
        throw new UnsupportedOperationException("Cannot type check");
    }

    public OneOfTwo<IRExpr, IRStmt> rem(BinOpExprNode n) {
        if (n.getLeft().getType().isSubtypeOfFloat() || n.getRight().getType().isSubtypeOfFloat()) {
            return binOp(OpType.MOD_FLOAT, n);
        } else if (n.getLeft().getType().isSubtypeOfInt() && n.getRight().getType().isSubtypeOfInt()) {
            return binOp(OpType.MOD_INT, n);
        }
        throw new UnsupportedOperationException("Cannot type check");
    }

    public OneOfTwo<IRExpr, IRStmt> sub(BinOpExprNode n) {
        if (n.getLeft().getType().isSubtypeOfFloat() || n.getRight().getType().isSubtypeOfFloat()) {
            return binOp(OpType.SUB_FLOAT, n);
        } else if (n.getLeft().getType().isSubtypeOfInt() && n.getRight().getType().isSubtypeOfInt()) {
            return binOp(OpType.SUB_INT, n);
        }
        throw new UnsupportedOperationException("Cannot type check");
    }

    public OneOfTwo<IRExpr, IRStmt> div(BinOpExprNode n) {
        if (n.getLeft().getType().isSubtypeOfFloat() || n.getRight().getType().isSubtypeOfFloat()) {
            return binOp(OpType.DIV_FLOAT, n);
        } else if (n.getLeft().getType().isSubtypeOfInt() && n.getRight().getType().isSubtypeOfInt()) {
            return binOp(OpType.DIV_INT, n);
        }
        throw new UnsupportedOperationException("Cannot type check");
    }

    private OneOfTwo<IRExpr, IRStmt> binOp(IRExpr left, IRExpr right, OpType opType, BinOpExprNode n) {
        IRNodeFactory make = new IRNodeFactory_c(n.getLocation());

        if (n.getLeft().getType().isSubtypeOfInt() && n.getRight().getType().isSubtypeOfInt()) {
            return OneOfTwo.ofFirst(make.IRBinOp(opType, left, right));
        }
        if (n.getLeft().getType().isSubtypeOfInt()) {
            left = make.IRCast(left, PrimitiveType.intDefault, PrimitiveType.floatDefault);
        }
        if (n.getRight().getType().isSubtypeOfInt()) {
            right = make.IRCast(right, PrimitiveType.intDefault, PrimitiveType.floatDefault);
        }
        return OneOfTwo.ofFirst(make.IRBinOp(opType, left, right));
    }

    private OneOfTwo<IRExpr, IRStmt> binOp(OpType opType, BinOpExprNode n) {
        IRExpr left = n.getLeft().accept(this).assertFirst();
        IRExpr right = n.getRight().accept(this).assertFirst();
        return binOp(left, right, opType, n);
    }

    /**
     * Translates an array literal with known IRExpr values.
     *
     * @param vals
     * @param location
     * @return
     */
    private OneOfTwo<IRExpr, IRStmt> visitArr(List<IRExpr> vals,
            Location location) {
        IRNodeFactory make = new IRNodeFactory_c(location);

        String memBlockStart = generator.newTemp();
        String pointerStart = generator.newTemp();
        int size = vals.size();

        IRExpr spaceNeeded = make.IRBinOp(OpType.MUL_INT,
                make.IRInteger(Configuration.WORD_SIZE),
                make.IRBinOp(OpType.ADD_INT, make.IRInteger(size), make.IRInteger(1)));

        List<IRStmt> commands = new ArrayList<IRStmt>();

        IRExpr memLoc = make.IRCall(make.IRName("_xi_alloc"),
                List.of(spaceNeeded), 1);

        commands.add(make.IRMove(make.IRTemp(memBlockStart), memLoc));
        commands.add(make.IRMove(make.IRMem(make.IRTemp(memBlockStart)),
                make.IRInteger(size)));
        // Move size into the start of requested memory block.

        commands.add(make.IRMove(make.IRTemp(pointerStart),
                make.IRBinOp(OpType.ADD_INT,
                        make.IRTemp(memBlockStart),
                        make.IRInteger(Configuration.WORD_SIZE))));

        // Setting the values of the indices in memory
        for (int i = 0; i < size; i++) {
            IRExpr valueLoc = make.IRMem(make.IRBinOp(OpType.ADD_INT,
                    make.IRTemp(pointerStart),
                    make.IRBinOp(OpType.MUL_INT,
                            make.IRInteger(Configuration.WORD_SIZE),
                            make.IRInteger(i))));
            commands.add(make.IRMove(valueLoc, vals.get(i)));
        }
        return OneOfTwo.ofFirst(make.IRESeq(make.IRSeq(commands),
                make.IRTemp(pointerStart)));
    }

    @Override
    public OneOfTwo<IRExpr, IRStmt> visit(LiteralArrayExprNode n) {
        List<IRExpr> values = n.arrayVals.stream()
                                         .map(stmt -> stmt.accept(this)
                                                          .assertFirst())
                                         .collect(Collectors.toList());
        return visitArr(values, n.getLocation());
    }

    @Override
    public OneOfTwo<IRExpr, IRStmt> visit(LiteralBoolExprNode n) {
        IRNodeFactory make = new IRNodeFactory_c(n.getLocation());
        return OneOfTwo.ofFirst(make.IRInteger(n.contents ? 1 : 0));
    }

    @Override
    public OneOfTwo<IRExpr, IRStmt> visit(LiteralCharExprNode n) {
        IRNodeFactory make = new IRNodeFactory_c(n.getLocation());
        assert n.contents.length() == 1;
        return OneOfTwo.ofFirst(make.IRInteger(n.contents.charAt(0)));
    }

    /**
     * Turns the number into a contant literal.
     */
    @Override
    public OneOfTwo<IRExpr, IRStmt> visit(LiteralIntExprNode n) {
        IRNodeFactory make = new IRNodeFactory_c(n.getLocation());
        return OneOfTwo.ofFirst(make.IRInteger(Long.parseLong(n.contents)));
    }

    @Override
    public OneOfTwo<IRExpr, IRStmt> visit(LiteralFloatExprNode n) {
        IRNodeFactory make = new IRNodeFactory_c(n.getLocation());
        return OneOfTwo.ofFirst(make.IRFloat(n.getValue()));
    }

    /**
     * TODO: Check if it is better to transform to a direct array literal.
     */
    @Override
    public OneOfTwo<IRExpr, IRStmt> visit(LiteralStringExprNode n) {
        IRNodeFactory make = new IRNodeFactory_c(n.getLocation());

        List<IRExpr> vals = new ArrayList<>();
        for (char c : n.contents.toCharArray()) {
            vals.add(make.IRInteger(c));
        }
        return visitArr(vals, n.getLocation());
    }

    /**
     * Converts to true ^ b, where b is any boolean.
     */
    @Override
    public OneOfTwo<IRExpr, IRStmt> visit(BoolNegExprNode n) {
        IRNodeFactory make = new IRNodeFactory_c(n.getLocation());

        IRExpr e = n.expr.accept(this)
                         .assertFirst();
        return OneOfTwo.ofFirst(make.IRBinOp(IRBinOp.OpType.XOR,
                make.IRInteger(1),
                e));
    }

    /**
     * Transforms to 0-e.
     */
    @Override
    public OneOfTwo<IRExpr, IRStmt> visit(IntNegExprNode n) {
        IRNodeFactory make = new IRNodeFactory_c(n.getLocation());

        // intercept -2^64 as literal and make it 0-(e-1)-1
        if (n.expr instanceof LiteralIntExprNode) {
            BigInteger literal = new BigInteger(((LiteralIntExprNode) n.expr).contents);
            BigInteger max = new BigInteger(String.valueOf(Long.MIN_VALUE));
            max = max.negate();
            if (literal.equals(max)) {
                return OneOfTwo.ofFirst(make.IRInteger(Long.MIN_VALUE));
            }
        }
        IRExpr e = n.expr.accept(this).assertFirst();
        if (Objects.requireNonNull(n.expr.getType()).isSubtypeOfInt()) {
            return OneOfTwo.ofFirst(make.IRBinOp(OpType.SUB_INT,
                    make.IRInteger(0), e));
        } else if (n.expr.getType().isSubtypeOfFloat()) {
            return OneOfTwo.ofFirst(make.IRBinOp(OpType.SUB_FLOAT,
                    make.IRFloat(0), e));
        }
        throw new UnsupportedOperationException("Cannot negate value");
    }

    @Override
    public OneOfTwo<IRExpr, IRStmt> visit(LengthExprNode n) {
        IRNodeFactory make = new IRNodeFactory_c(n.getLocation());
        IRExpr e = n.expr.accept(this)
                         .assertFirst();
        return OneOfTwo.ofFirst(make.IRMem(make.IRBinOp(OpType.SUB_INT,
                e,
                make.IRInteger(Configuration.WORD_SIZE))));
    }

}
