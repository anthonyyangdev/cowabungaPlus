package cyr7.x86.asm;

import java.util.List;
import java.util.stream.Collectors;

public class ASMInstr implements ASMLine {
    public final List<ASMArg> args;
    public final ASMInstrType type;

    protected ASMInstr(ASMInstrType type, List<ASMArg> args) {
        this.type = type;
        this.args = args;
    }

    public ASMInstr withArgsReplaced(List<ASMArg> newArgs) {
        return new ASMInstr(type, newArgs);
    }

    @Override
    public String getIntelAssembly() {
        String opCode = type.getIntelOpCode();
        String arguments = String.join(", ", args.stream().map(a -> a.getIntelArg()).collect(Collectors.toList()));
        return opCode + (arguments.length() != 0 ? " " + arguments : "");
    }

    @Override
    public String toString() {
        return "ASMInstr [args=" + args + ", type=" + type + "]";
    }
}