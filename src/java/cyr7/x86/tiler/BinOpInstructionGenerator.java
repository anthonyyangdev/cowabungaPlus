package cyr7.x86.tiler;

import java.util.List;
import java.util.Optional;

import cyr7.ir.IdGenerator;
import cyr7.ir.nodes.IRBinOp;
import cyr7.x86.asm.ASMArg;
import cyr7.x86.asm.ASMArgFactory;
import cyr7.x86.asm.ASMLine;
import cyr7.x86.asm.ASMLineFactory;
import cyr7.x86.asm.ASMReg;
import cyr7.x86.asm.ASMRegSize;
import cyr7.x86.asm.ASMTempArg;

public class BinOpInstructionGenerator {

    protected static final ASMArgFactory arg = ASMArgFactory.instance;

    public static TilerData generateInstruction(
            IRBinOp n,
            final int cost,
            ASMArg leftArg,
            ASMArg rightArg,
            List<ASMLine> insns,
            IdGenerator generator) {

        ASMLineFactory make = new ASMLineFactory(n);
        ASMArg ret = arg.temp(generator.newTemp(), ASMRegSize.QWORD);

        switch (n.opType()) {
        case ADD_INT:
            insns.add(make.Mov(ret, leftArg));
            insns.add(make.Add(ret, rightArg));
            break;
        case AND:
            insns.add(make.Mov(ret, leftArg));
            insns.add(make.And(ret, rightArg));
            break;
        case DIV_INT: {
            String raxTemp = generator.newTemp();
            String rdxTemp = generator.newTemp();

            insns.add(make.Mov(arg.temp(raxTemp, ASMRegSize.QWORD), ASMReg.RAX));
            insns.add(make.Mov(arg.temp(rdxTemp, ASMRegSize.QWORD), ASMReg.RDX));

            insns.add(make.Mov(ASMReg.RAX, leftArg));
            insns.add(make.CQO());
            if (!(rightArg instanceof ASMTempArg)) {
                ASMTempArg temp = arg.temp(generator.newTemp(), ASMRegSize.QWORD);
                insns.add(make.Mov(temp, rightArg));
                rightArg = temp;
            }
            insns.add(make.Div(rightArg));
            insns.add(make.Mov(ret, ASMReg.RAX));

            insns.add(make.Mov(ASMReg.RAX, arg.temp(raxTemp, ASMRegSize.QWORD)));
            insns.add(make.Mov(ASMReg.RDX, arg.temp(rdxTemp, ASMRegSize.QWORD)));
            break;
        }
        case EQ: {
            ASMArg byteReg = new ASMTempArg(generator.newTemp(), ASMRegSize.BYTE);
            if (!(leftArg instanceof ASMTempArg)) {
                ASMTempArg temp = arg.temp(generator.newTemp(), ASMRegSize.QWORD);
                insns.add(make.Mov(temp, leftArg));
                leftArg = temp;
            }
            insns.add(make.Cmp(leftArg, rightArg));
            insns.add(make.SetZ(byteReg));
            insns.add(make.MovZX(ret, byteReg));
            break;
        }
        case GEQ: {
            ASMArg byteReg = new ASMTempArg(generator.newTemp(), ASMRegSize.BYTE);
            if (!(leftArg instanceof ASMTempArg)) {
                ASMTempArg temp = arg.temp(generator.newTemp(), ASMRegSize.QWORD);
                insns.add(make.Mov(temp, leftArg));
                leftArg = temp;
            }
            insns.add(make.Cmp(leftArg, rightArg));
            insns.add(make.SetGE(byteReg));
            insns.add(make.MovZX(ret, byteReg));
            break;
        }
        case GT: {
            ASMArg byteReg = new ASMTempArg(generator.newTemp(), ASMRegSize.BYTE);
            if (!(leftArg instanceof ASMTempArg)) {
                ASMTempArg temp = arg.temp(generator.newTemp(), ASMRegSize.QWORD);
                insns.add(make.Mov(temp, leftArg));
                leftArg = temp;
            }
            insns.add(make.Cmp(leftArg, rightArg));
            insns.add(make.SetG(byteReg));
            insns.add(make.MovZX(ret, byteReg));
            break;
        }
        case HMUL_INT: {
            String raxTemp = generator.newTemp();
            String rdxTemp = generator.newTemp();

            insns.add(make.Mov(arg.temp(raxTemp, ASMRegSize.QWORD), ASMReg.RAX));
            insns.add(make.Mov(arg.temp(rdxTemp, ASMRegSize.QWORD), ASMReg.RDX));

            insns.add(make.Mov(ASMReg.RAX, leftArg));
            if (!(rightArg instanceof ASMTempArg)) {
                ASMTempArg temp = arg.temp(generator.newTemp(), ASMRegSize.QWORD);
                insns.add(make.Mov(temp, rightArg));
                rightArg = temp;
            }
            insns.add(make.Mul(rightArg));
            insns.add(make.Mov(ret, ASMReg.RDX));

            insns.add(make.Mov(ASMReg.RAX, arg.temp(raxTemp, ASMRegSize.QWORD)));
            insns.add(make.Mov(ASMReg.RDX, arg.temp(rdxTemp, ASMRegSize.QWORD)));
            break;
        }
        case LEQ: {
            ASMArg byteReg = new ASMTempArg(generator.newTemp(), ASMRegSize.BYTE);
            if (!(leftArg instanceof ASMTempArg)) {
                ASMTempArg temp = arg.temp(generator.newTemp(), ASMRegSize.QWORD);
                insns.add(make.Mov(temp, leftArg));
                leftArg = temp;
            }
            insns.add(make.Cmp(leftArg, rightArg));
            insns.add(make.SetLE(byteReg));
            insns.add(make.MovZX(ret, byteReg));
            break;
        }
        case LSHIFT: {
            insns.add(make.Push(ASMReg.RCX));
            insns.add(make.Mov(ret, leftArg));
            insns.add(make.Mov(ASMReg.CL, rightArg.accept(new ReduceTo8BitVisitor())));
            insns.add(make.LShift(ret, ASMReg.CL));
            insns.add(make.Pop(ASMReg.RCX));
            break;
        }
        case RSHIFT: {
            insns.add(make.Push(ASMReg.RCX));
            insns.add(make.Mov(ret, leftArg));
            insns.add(make.Mov(ASMReg.CL, rightArg.accept(new ReduceTo8BitVisitor())));
            insns.add(make.RShift(ret, ASMReg.CL));
            insns.add(make.Pop(ASMReg.RCX));
            break;
        }
        case ARSHIFT: {
            insns.add(make.Push(ASMReg.RCX));
            insns.add(make.Mov(ret, leftArg));
            insns.add(make.Mov(ASMReg.CL, rightArg.accept(new ReduceTo8BitVisitor())));
            insns.add(make.ARShift(ret, ASMReg.CL));
            insns.add(make.Pop(ASMReg.RCX));
            break;
        }
        case LT: {
            ASMArg byteReg = new ASMTempArg(generator.newTemp(), ASMRegSize.BYTE);
            if (!(leftArg instanceof ASMTempArg)) {
                ASMTempArg temp = arg.temp(generator.newTemp(), ASMRegSize.QWORD);
                insns.add(make.Mov(temp, leftArg));
                leftArg = temp;
            }
            insns.add(make.Cmp(leftArg, rightArg));
            insns.add(make.SetL(byteReg));
            insns.add(make.MovZX(ret, byteReg));
            break;
        }
        case MOD_INT:
            String raxTemp = generator.newTemp();
            String rdxTemp = generator.newTemp();

            insns.add(make.Mov(arg.temp(raxTemp, ASMRegSize.QWORD), ASMReg.RAX));
            insns.add(make.Mov(arg.temp(rdxTemp, ASMRegSize.QWORD), ASMReg.RDX));

            insns.add(make.Mov(ASMReg.RAX, leftArg));
            insns.add(make.CQO());
            if (!(rightArg instanceof ASMTempArg)) {
                ASMTempArg temp = arg.temp(generator.newTemp(), ASMRegSize.QWORD);
                insns.add(make.Mov(temp, rightArg));
                rightArg = temp;
            }
            insns.add(make.Div(rightArg));
            insns.add(make.Mov(ret, ASMReg.RDX));

            insns.add(make.Mov(ASMReg.RAX, arg.temp(raxTemp, ASMRegSize.QWORD)));
            insns.add(make.Mov(ASMReg.RDX, arg.temp(rdxTemp, ASMRegSize.QWORD)));
            break;
        case MUL_INT:
            insns.add(make.Mov(ret, leftArg));
            insns.add(make.Mul(ret, rightArg));
            break;
        case NEQ: {
            ASMArg byteReg = new ASMTempArg(generator.newTemp(), ASMRegSize.BYTE);
            if (!(leftArg instanceof ASMTempArg)) {
                ASMTempArg temp = arg.temp(generator.newTemp(), ASMRegSize.QWORD);
                insns.add(make.Mov(temp, leftArg));
                leftArg = temp;
            }
            insns.add(make.Cmp(leftArg, rightArg));
            insns.add(make.SetNE(byteReg));
            insns.add(make.MovZX(ret, byteReg));
            break;
        }
        case OR:
            insns.add(make.Mov(ret, leftArg));
            insns.add(make.Or(ret, rightArg));
            break;
        case SUB_INT:
            insns.add(make.Mov(ret, leftArg));
            insns.add(make.Sub(ret, rightArg));
            break;
        case XOR:
            insns.add(make.Mov(ret, leftArg));
            insns.add(make.Xor(ret, rightArg));
            break;
        default:
            throw new UnsupportedOperationException("No case found.");
        }
        return new TilerData(cost, insns, Optional.of(ret));
    }

}
