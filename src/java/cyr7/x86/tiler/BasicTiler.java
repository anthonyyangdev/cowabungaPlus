package cyr7.x86.tiler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import cyr7.ir.IdGenerator;
import cyr7.ir.interpret.Configuration;
import cyr7.ir.nodes.IRBinOp;
import cyr7.ir.nodes.IRCJump;
import cyr7.ir.nodes.IRCall;
import cyr7.ir.nodes.IRCallStmt;
import cyr7.ir.nodes.IRCompUnit;
import cyr7.ir.nodes.IRConst;
import cyr7.ir.nodes.IRESeq;
import cyr7.ir.nodes.IRExp;
import cyr7.ir.nodes.IRFuncDecl;
import cyr7.ir.nodes.IRJump;
import cyr7.ir.nodes.IRLabel;
import cyr7.ir.nodes.IRMem;
import cyr7.ir.nodes.IRMove;
import cyr7.ir.nodes.IRName;
import cyr7.ir.nodes.IRReturn;
import cyr7.ir.nodes.IRSeq;
import cyr7.ir.nodes.IRStmt;
import cyr7.ir.nodes.IRTemp;
import cyr7.semantics.types.FunctionType;
import cyr7.visitor.MyIRVisitor;
import cyr7.x86.asm.ASMAddrExpr.ScaleValues;
import cyr7.x86.asm.ASMArg;
import cyr7.x86.asm.ASMArgFactory;
import cyr7.x86.asm.ASMConstArg;
import cyr7.x86.asm.ASMLabel;
import cyr7.x86.asm.ASMLabelArg;
import cyr7.x86.asm.ASMLine;
import cyr7.x86.asm.ASMLineFactory;
import cyr7.x86.asm.ASMMemArg;
import cyr7.x86.asm.ASMReg;
import cyr7.x86.asm.ASMTempArg;
import cyr7.x86.asm.ASMTempArg.Size;
import cyr7.x86.asm.ASMTempRegArg;

public class BasicTiler implements MyIRVisitor<TilerData> {

    private final IdGenerator generator;
    private final Map<String, FunctionType> fMap;
    private final int numRetValues;
    private final String returnLbl;
    private final ASMArgFactory arg;

    private final ASMReg rbp = ASMReg.RBP;
    private final ASMReg rax = ASMReg.RAX;
    private final ASMReg rsp = ASMReg.RSP;
    private final ASMReg rdi = ASMReg.RDI;
    private final ASMReg rsi = ASMReg.RSI;
    private final ASMReg rdx = ASMReg.RDX;
    private final ASMReg rcx = ASMReg.RCX;
    private final ASMReg r8 = ASMReg.R8;
    private final ASMReg r9 = ASMReg.R9;

    public BasicTiler(IdGenerator generator, String tiledFunctionName,
            Map<String, FunctionType> fMap, String returnLbl) {
        this.generator = generator;
        this.fMap = Collections.unmodifiableMap(fMap);
        this.numRetValues = this.fMap.get(tiledFunctionName)
                                     .output
                                     .getTypes()
                                     .size();
        this.returnLbl = returnLbl;
        this.arg = ASMArgFactory.instance;
    }

    @Override
    public TilerData visit(IRBinOp n) {
        ASMLineFactory make = new ASMLineFactory(n);
        if (n.hasOptimalTiling()) {
            return n.getOptimalTiling();
        }
        List<ASMLine> insns = new ArrayList<>();

        TilerData left = n.left()
                          .accept(this);
        TilerData right = n.right()
                           .accept(this);

        insns.addAll(left.optimalInstructions);
        insns.addAll(right.optimalInstructions);

        ASMArg ret = arg.temp(generator.newTemp(), Size.QWORD);
        switch (n.opType()) {
        case ADD:
            insns.add(make.Mov(ret, left.result.get()));
            insns.add(make.Add(ret, right.result.get()));
            break;
        case AND:
            insns.add(make.Mov(ret, left.result.get()));
            insns.add(make.And(ret, right.result.get()));
            break;
        case ARSHIFT:
            insns.add(make.Mov(ret, left.result.get()));
            insns.add(make.ARShift(ret, right.result.get()));
            break;
        case DIV:
            insns.add(make.Push(rax));
            insns.add(make.Push(rdx));
            insns.add(make.Mov(rax, left.result.get()));
            insns.add(make.CQO());
            insns.add(make.Div(right.result.get()));
            insns.add(make.Mov(ret, rax));
            insns.add(make.Pop(rdx));
            insns.add(make.Pop(rax));
            break;
        case EQ: {
            ASMArg byteReg = new ASMTempArg(generator.newTemp(), Size.BYTE);
            insns.add(make.Cmp(left.result.get(), right.result.get()));
            insns.add(make.SetZ(byteReg));
            insns.add(make.MovZX(ret, byteReg));
            break;
        }
        case GEQ: {
            ASMArg byteReg = new ASMTempArg(generator.newTemp(), Size.BYTE);
            insns.add(make.Cmp(left.result.get(), right.result.get()));
            insns.add(make.SetGE(byteReg));
            insns.add(make.MovZX(ret, byteReg));
            break;
        }
        case GT: {
            ASMArg byteReg = new ASMTempArg(generator.newTemp(), Size.BYTE);
            insns.add(make.Cmp(left.result.get(), right.result.get()));
            insns.add(make.SetG(byteReg));
            insns.add(make.MovZX(ret, byteReg));
            break;
        }
        case HMUL:
            insns.add(make.Push(rdx));
            insns.add(make.Mov(ret, left.result.get()));
            insns.add(make.Mul(ret, right.result.get()));
            insns.add(make.Mov(ret, rdx));
            insns.add(make.Pop(rdx));
            break;
        case LEQ: {
            ASMArg byteReg = new ASMTempArg(generator.newTemp(), Size.BYTE);
            insns.add(make.Cmp(left.result.get(), right.result.get()));
            insns.add(make.SetLE(byteReg));
            insns.add(make.MovZX(ret, byteReg));
            break;
        }
        case LSHIFT:
            insns.add(make.Mov(ret, left.result.get()));
            insns.add(make.LShift(ret, right.result.get()));
            break;
        case LT: {
            ASMArg byteReg = new ASMTempArg(generator.newTemp(), Size.BYTE);
            insns.add(make.Cmp(left.result.get(), right.result.get()));
            insns.add(make.SetL(byteReg));
            insns.add(make.MovZX(ret, byteReg));
            break;
        }
        case MOD:
            insns.add(make.Push(rax));
            insns.add(make.Push(rdx));
            insns.add(make.Mov(rax, left.result.get()));
            insns.add(make.CQO());
            insns.add(make.Div(right.result.get()));
            insns.add(make.Mov(ret, rdx));
            insns.add(make.Pop(rdx));
            insns.add(make.Pop(rax));
            break;
        case MUL:
            insns.add(make.Mov(ret, left.result.get()));
            insns.add(make.Mul(ret, right.result.get()));
            break;
        case NEQ: {
            ASMArg byteReg = new ASMTempArg(generator.newTemp(), Size.BYTE);
            insns.add(make.Cmp(left.result.get(), right.result.get()));
            insns.add(make.SetNE(byteReg));
            insns.add(make.MovZX(ret, byteReg));
            break;
        }
        case OR:
            insns.add(make.Mov(ret, left.result.get()));
            insns.add(make.Or(ret, right.result.get()));
            break;
        case RSHIFT:
            insns.add(make.Mov(ret, left.result.get()));
            insns.add(make.RShift(ret, right.result.get()));
            break;
        case SUB:
            insns.add(make.Mov(ret, left.result.get()));
            insns.add(make.Sub(ret, right.result.get()));
            break;
        case XOR:
            insns.add(make.Mov(ret, left.result.get()));
            insns.add(make.Xor(ret, right.result.get()));
            break;
        default:
            throw new UnsupportedOperationException("No case found.");
        }

        TilerData result = new TilerData(1 + left.tileCost + right.tileCost,
                insns, Optional.of(ret));
        n.setOptimalTilingOnce(result);
        return result;
    }

    @Override
    public TilerData visit(IRCall n) {
        throw new UnsupportedOperationException(
                "Call is not a valid node at this stage.");
    }

    @Override
    public TilerData visit(IRConst n) {
        ASMLineFactory make = new ASMLineFactory(n);
        if (n.hasOptimalTiling()) {
            return n.getOptimalTiling();
        }
        ASMArg ret = arg.temp(generator.newTemp(), Size.QWORD);
        List<ASMLine> insns = List.of(make.MovAbs(ret,
                new ASMConstArg(n.constant())));
        TilerData result = new TilerData(1, insns, Optional.of(ret));
        n.setOptimalTilingOnce(result);
        return result;
    }

    @Override
    public TilerData visit(IRESeq n) {
        throw new UnsupportedOperationException(
                "ESeq is not a valid node at this stage.");
    }

    @Override
    public TilerData visit(IRMem n) {
        if (n.hasOptimalTiling()) {
            return n.getOptimalTiling();
        }
        TilerData expr = n.expr()
                          .accept(this);

        List<ASMLine> insns = new ArrayList<ASMLine>();
        insns.addAll(expr.optimalInstructions);

        if (!(expr.result.get() instanceof ASMTempRegArg)) {
            throw new RuntimeException("Something bad happened...");
        } else {
            ASMArg ret = new ASMMemArg((ASMTempRegArg) expr.result.get());
            TilerData result = new TilerData(1 + expr.tileCost, insns, Optional
                                                                               .of(ret));
            n.setOptimalTilingOnce(result);
            return result;
        }
    }

    @Override
    public TilerData visit(IRName n) {
        if (n.hasOptimalTiling()) {
            return n.getOptimalTiling();
        }
        Optional<ASMArg> lbl = Optional.of(new ASMLabelArg(n.name()));
        TilerData result = new TilerData(1, List.of(), lbl);
        n.setOptimalTilingOnce(result);
        return result;
    }

    @Override
    public TilerData visit(IRTemp n) {
        if (n.hasOptimalTiling()) {
            return n.getOptimalTiling();
        }
        ASMArg ret = new ASMTempArg(n.name(), Size.QWORD);
        TilerData result = new TilerData(0, new ArrayList<>(), Optional.of(
                ret));
        n.setOptimalTilingOnce(result);
        return result;
    }

    @Override
    public TilerData visit(IRCallStmt n) {
        ASMLineFactory make = new ASMLineFactory(n);
        if (n.hasOptimalTiling()) {
            return n.getOptimalTiling();
        }

        List<ASMLine> insn = new ArrayList<>();
        TilerData targetTile = n.target()
                                .accept(this);
        List<TilerData> argTiles = n.args()
                                    .stream()
                                    .map(a -> a.accept(this))
                                    .collect(Collectors.toList());

        int lastRegisterArg;
        int tileCost = 0;
        if (n.numOfReturnValues > 2) {
            lastRegisterArg = Math.min(5, argTiles.size());
            long size = (n.numOfReturnValues - 2) * 8;
            insn.add(make.MovAbs(rdi, arg.constant(size)));
            insn.add(make.Sub(rsp, rdi));
            insn.add(make.Mov(rdi, rsp));

            for (int i = 0; i < lastRegisterArg; i++) {
                TilerData argTile = argTiles.get(i);
                tileCost += argTile.tileCost;
                insn.addAll(argTile.optimalInstructions);
                switch (i) {
                case 0:
                    insn.add(make.Mov(rsi, argTile.result.get()));
                    break;
                case 1:
                    insn.add(make.Mov(rdx, argTile.result.get()));
                    break;
                case 2:
                    insn.add(make.Mov(rcx, argTile.result.get()));
                    break;
                case 3:
                    insn.add(make.Mov(r8, argTile.result.get()));
                    break;
                case 4:
                    insn.add(make.Mov(r9, argTile.result.get()));
                    break;
                default:
                    throw new RuntimeException("Unexpected Error with index");
                }
            }
        } else {
            lastRegisterArg = Math.min(6, argTiles.size());
            for (int i = 0; i < lastRegisterArg; i++) {
                TilerData argTile = argTiles.get(i);
                tileCost += argTile.tileCost;
                insn.addAll(argTile.optimalInstructions);
                switch (i) {
                case 0:
                    insn.add(make.Mov(rdi, argTile.result.get()));
                    break;
                case 1:
                    insn.add(make.Mov(rsi, argTile.result.get()));
                    break;
                case 2:
                    insn.add(make.Mov(rdx, argTile.result.get()));
                    break;
                case 3:
                    insn.add(make.Mov(rcx, argTile.result.get()));
                    break;
                case 4:
                    insn.add(make.Mov(r8, argTile.result.get()));
                    break;
                case 5:
                    insn.add(make.Mov(r9, argTile.result.get()));
                    break;
                default:
                    throw new RuntimeException("Unexpected Error with index");
                }
            }
        }
        for (int i = argTiles.size() - 1; i >= lastRegisterArg; i--) {
            TilerData argTile = argTiles.get(i);
            tileCost += argTile.tileCost;
            insn.addAll(argTile.optimalInstructions);
            insn.add(make.Push(argTile.result.get()));
        }

        tileCost += targetTile.tileCost;
        insn.addAll(targetTile.optimalInstructions);
        insn.add(make.Call(targetTile.result.get()));

        if (n.collectors()
             .size() == 1) {
            String resultTemp = n.collectors()
                                 .get(0);
            insn.add(make.Mov(arg.temp(resultTemp, Size.QWORD), ASMReg.RAX));
        } else if (n.collectors()
                    .size() > 1) {
            // TODO: FIX ME AHAAAAAHAHAHAHA
            throw new RuntimeException("bleh");
        }

        TilerData result = new TilerData(tileCost, insn, Optional.empty());
        n.setOptimalTilingOnce(result);
        return result;
    }

    @Override
    public TilerData visit(IRCJump n) {
        ASMLineFactory make = new ASMLineFactory(n);
        if (n.hasOptimalTiling()) {
            return n.getOptimalTiling();
        }

        List<ASMLine> insn = new ArrayList<>();
        TilerData cond = n.cond()
                          .accept(this);
        ASMArg temp = cond.result.map(r -> r)
                                 .orElseThrow(() -> new RuntimeException(
                                         "Expected a temporary but was not found."));
        insn.addAll(cond.optimalInstructions);
        insn.add(make.Cmp(temp, new ASMConstArg(1)));
        insn.add(make.JumpE(new ASMLabelArg(n.trueLabel())));

        TilerData result = new TilerData(1 + cond.tileCost, insn, Optional
                                                                          .empty());
        n.setOptimalTilingOnce(result);
        return result;
    }

    @Override
    public TilerData visit(IRCompUnit n) {
        throw new UnsupportedOperationException(
                "CompUnit is not translated by the BasicTiler.");
    }

    @Override
    public TilerData visit(IRExp n) {
        throw new UnsupportedOperationException(
                "Exp is not a valid node at this stage.");
    }

    @Override
    public TilerData visit(IRFuncDecl n) {
        throw new UnsupportedOperationException(
                "FuncDecl is not translated by the BasicTiler.");
    }

    @Override
    public TilerData visit(IRJump n) {
        ASMLineFactory make = new ASMLineFactory(n);
        if (n.hasOptimalTiling()) {
            return n.getOptimalTiling();
        }
        List<ASMLine> instructions = new ArrayList<>();
        TilerData target = n.target()
                            .accept(this);
        instructions.addAll(target.optimalInstructions);

        TilerData result;
        if (target.result.isPresent()) {
            instructions.add(make.Jump(target.result.get()));
            result = new TilerData(1 + target.tileCost, instructions, Optional
                                                                              .empty());
        } else {
            throw new RuntimeException("Missing target result");
        }
        n.setOptimalTilingOnce(result);
        return result;
    }

    @Override
    public TilerData visit(IRLabel n) {
        ASMLineFactory make = new ASMLineFactory(n);
        if (n.hasOptimalTiling()) {
            return n.getOptimalTiling();
        }
        List<ASMLine> instructions = List.of(new ASMLabel(n.name()));

        TilerData result = new TilerData(instructions.size(), instructions,
                Optional.empty());
        n.setOptimalTilingOnce(result);
        return result;
    }

    @Override
    public TilerData visit(IRMove n) {
        ASMLineFactory make = new ASMLineFactory(n);
        if (n.hasOptimalTiling()) {
            return n.getOptimalTiling();
        }

        TilerData target = n.target()
                            .accept(this);
        TilerData source = n.source()
                            .accept(this);
        List<ASMLine> instrs = new ArrayList<>();
        instrs.addAll(source.optimalInstructions);
        instrs.addAll(target.optimalInstructions);

        final String ARG_PREFIX = Configuration.ABSTRACT_ARG_PREFIX;
        final String RET_PREFIX = Configuration.ABSTRACT_RET_PREFIX;

        TilerData result;
        if (n.target() instanceof IRTemp
            && ((IRTemp) n.target()).name().startsWith(ARG_PREFIX)) {
            // Handle in CallStmt
            result = new TilerData(0, List.of(), Optional.empty());
        } else if (n.source() instanceof IRTemp
            && ((IRTemp) n.source()).name().startsWith(ARG_PREFIX)) {

            String index = ((IRTemp) n.source()).name()
                                                .substring(ARG_PREFIX.length());
            int i = Integer.parseInt(index);
            ASMArg targetTemp = target.result.get();
            if (this.numRetValues > 2) {
                switch (i) {
                case 0:
                    instrs.add(make.Mov(targetTemp, rsi));
                    break;
                case 1:
                    instrs.add(make.Mov(targetTemp, rdx));
                    break;
                case 2:
                    instrs.add(make.Mov(targetTemp, rcx));
                    break;
                case 3:
                    instrs.add(make.Mov(targetTemp, r8));
                    break;
                case 4:
                    instrs.add(make.Mov(targetTemp, r9));
                    break;
                default:
                    int offset = 8 * (i - 6);
                    var addr = arg.addr(Optional.of(rbp),
                            ScaleValues.ONE,
                            Optional.empty(),
                            offset);
                    var mem = arg.mem(addr);
                    instrs.add(make.Mov(targetTemp, mem));
                    break;
                }
            } else {
                switch (i) {
                case 0:
                    instrs.add(make.Mov(targetTemp, rdi));
                    break;
                case 1:
                    instrs.add(make.Mov(targetTemp, rsi));
                    break;
                case 2:
                    instrs.add(make.Mov(targetTemp, rdx));
                    break;
                case 3:
                    instrs.add(make.Mov(targetTemp, rcx));
                    break;
                case 4:
                    instrs.add(make.Mov(targetTemp, r8));
                    break;
                case 5:
                    instrs.add(make.Mov(targetTemp, r9));
                    break;
                default:
                    int offset = 8 * (i - 5);
                    var addr = arg.addr(Optional.of(ASMReg.RBP),
                            ScaleValues.ONE,
                            Optional.empty(),
                            offset);
                    var mem = arg.mem(addr);
                    instrs.add(make.Mov(targetTemp, mem));
                    break;
                }
            }
            result = new TilerData(1 + target.tileCost, instrs, Optional
                                                                        .empty());
        } else if (n.target() instanceof IRTemp
            && ((IRTemp) n.target()).name().startsWith(RET_PREFIX)) {
            String index = ((IRTemp) n.target()).name()
                                                .substring(RET_PREFIX.length());
            int i = Integer.parseInt(index);
            switch (i) {
            case 0:
                instrs.add(make.Mov(rax, source.result.get()));
                break;
            case 1:
                instrs.add(make.Mov(rdx, source.result.get()));
                break;
            default:
                int offset = 8 * (i - 1);
                var addr = arg.addr(Optional.of(ASMReg.RDI),
                        ScaleValues.ONE,
                        Optional.empty(),
                        offset);
                var mem = arg.mem(addr);
                instrs.add(make.Mov(mem, source.result.get()));
                break;
            }
            result = new TilerData(1 + source.tileCost, instrs, Optional
                                                                        .empty());
        } else if (n.source() instanceof IRTemp
            && ((IRTemp) n.source()).name().startsWith(RET_PREFIX)) {

            String index = ((IRTemp) n.source()).name()
                                                .substring(RET_PREFIX.length());
            int i = Integer.parseInt(index);
            switch (i) {
            case 0:
                instrs.add(make.Mov(target.result.get(), rax));
                break;
            case 1:
                instrs.add(make.Mov(target.result.get(), rdx));
                break;
            default:
                int offset = 8 * (i - 1);
                var addr = arg.addr(Optional.of(ASMReg.RDI),
                        ScaleValues.ONE,
                        Optional.empty(),
                        offset);
                var mem = arg.mem(addr);
                instrs.add(make.Mov(target.result.get(), mem));
                break;
            }
            result = new TilerData(1 + target.tileCost, instrs, Optional
                                                                        .empty());
        } else {
            instrs.add(make.Mov(target.result.get(), source.result.get()));
            result = new TilerData(1 + target.tileCost + source.tileCost,
                    instrs, Optional.empty());
        }
        n.setOptimalTilingOnce(result);
        return result;
    }

    @Override
    public TilerData visit(IRReturn n) {
        ASMLineFactory make = new ASMLineFactory(n);
        if (n.hasOptimalTiling()) {
            return n.getOptimalTiling();
        }
        List<ASMLine> instrs = new ArrayList<>();
        instrs.add(make.Jump(new ASMLabelArg(this.returnLbl)));
        TilerData result = new TilerData(instrs.size(), instrs, Optional
                                                                        .empty());
        n.setOptimalTilingOnce(result);
        return result;
    }

    @Override
    public TilerData visit(IRSeq n) {
        ASMLineFactory make = new ASMLineFactory(n);
        if (n.hasOptimalTiling()) {
            return n.getOptimalTiling();
        }

        List<ASMLine> instructions = new ArrayList<>();
        int sumCosts = 0;
        for (IRStmt s : n.stmts()) {
            TilerData data = s.accept(this);
            assert data.result.isEmpty();

            instructions.addAll(data.optimalInstructions);
            sumCosts += data.tileCost;
        }
        TilerData result = new TilerData(1 + sumCosts, instructions, Optional
                                                                             .empty());
        n.setOptimalTilingOnce(result);
        return result;
    }
}