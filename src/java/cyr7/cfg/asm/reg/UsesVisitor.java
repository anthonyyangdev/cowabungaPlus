package cyr7.cfg.asm.reg;

import cyr7.cfg.asm.nodes.AsmCFGIfNode;
import cyr7.cfg.asm.nodes.AsmCFGOpNode;
import cyr7.cfg.asm.nodes.AsmCFGReturnNode;
import cyr7.cfg.asm.nodes.AsmCFGStartNode;
import cyr7.cfg.asm.visitor.AsmCFGVisitor;
import cyr7.util.Sets;
import cyr7.x86.ASMConstants;
import cyr7.x86.asm.ASMInstr;
import cyr7.x86.asm.ASMLabelArg;
import cyr7.x86.asm.ASMReg;
import cyr7.x86.asm.ASMTempRegArg;

import java.util.Collections;
import java.util.Set;

public final class UsesVisitor implements AsmCFGVisitor<Set<ASMTempRegArg>> {

    private final Set<ASMReg> returnRegisters;

    public UsesVisitor(Set<ASMReg> returnRegisters) {
        this.returnRegisters = returnRegisters;
    }

    public static Set<ASMTempRegArg> getAllArgs(ASMInstr instr) {
        return instr.args.stream()
            .map(arg -> arg.accept(ArgUsesVisitor.INSTANCE))
            .reduce(Sets::union)
            .orElse(Collections.emptySet());
    }

    @Override
    public Set<ASMTempRegArg> visit(AsmCFGOpNode n) {
        ASMInstr instr = n.sourceInstr();
        switch (instr.type) {
            case ADDQ:
            case SUBQ:
            case ANDQ:
            case ORQ:
            case XORQ:
            case SHLQ:
            case SHRQ:
            case SARQ:
            case PUSHQ:
            case POPQ:
            case CMPQ:
                return getAllArgs(instr);

            case MOVQ:
            case MOV:
            case MOVABSQ:
            case MOVZX:
            case LEAQ:
                if (instr.args.get(0) instanceof ASMTempRegArg) {
                    return instr.args.get(1).accept(ArgUsesVisitor.INSTANCE);
                } else {
                    return getAllArgs(instr);
                }

            case IMULQ:
                if (instr.args.size() == 2) {
                    return getAllArgs(instr);
                }
                // fall-through intentionally
            case IDIVQ:
                if (instr.args.get(0) instanceof ASMTempRegArg) {
                    return Set.of((ASMTempRegArg) instr.args.get(0), ASMReg.RAX);
                } else {
                    return Set.of();
                }

            case CALLQ:
                String mangledName = ((ASMLabelArg) instr.args.get(0)).label;
                // A call uses its argument registers
                return Collections.unmodifiableSet(
                    MangledNameParser.argRegisters(mangledName));

            case SETZ:
            case SETNE:
            case SETLE:
            case SETL:
            case SETG:
            case SETGE:
                return Collections.emptySet();

            case CQO:
                break;
            // control flow
            // these are represented as an inherent part of the CFG
            case RETQ:
            case JMP:
            case JE:
            case JNE:
            case JLE:
            case JGE:
            case JL:
            case JG:
                throw new IllegalArgumentException();
        }

        throw new AssertionError();
    }

    @Override
    public Set<ASMTempRegArg> visit(AsmCFGIfNode n) {
        return Set.of(ASMReg.FLAGS);
    }

    @Override
    public Set<ASMTempRegArg> visit(AsmCFGReturnNode n) {
        // why do this? Appel p. 228
        return Sets.union(returnRegisters,
            Set.of(ASMConstants.CALLEE_SAVED_REGISTERS));
    }

    @Override
    public Set<ASMTempRegArg> visit(AsmCFGStartNode n) {
        return Collections.emptySet();
    }

}