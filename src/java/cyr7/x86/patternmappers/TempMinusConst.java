package cyr7.x86.patternmappers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import cyr7.ir.nodes.IRBinOp;
import cyr7.ir.nodes.IRConst;
import cyr7.ir.nodes.IRExpr;
import cyr7.x86.asm.ASMAddrExpr;
import cyr7.x86.asm.ASMAddrExpr.ScaleValues;
import cyr7.x86.asm.ASMArg;
import cyr7.x86.asm.ASMLine;
import cyr7.x86.asm.ASMLineFactory;
import cyr7.x86.asm.ASMMemArg;
import cyr7.x86.asm.ASMTempArg;
import cyr7.x86.asm.ASMTempArg.Size;
import cyr7.x86.pattern.BiPatternBuilder;
import cyr7.x86.tiler.ComplexTiler;
import cyr7.x86.tiler.TilerData;

public class TempMinusConst extends MemoryAddrPattern {

    public TempMinusConst(boolean isMemPattern) {
        super(isMemPattern);
    }

    @Override
    protected Optional<ASMAddrExpr> matchAddress(
        IRBinOp n,
        ComplexTiler tiler,
        ASMLineFactory make,
        List<ASMLine> insns) {
        var tempMinusConst = BiPatternBuilder.left()
                                             .instOf(ASMTempArg.class)
                                             .right()
                                             .instOf(IRConst.class)
                                             .finish()
                                             .mappingLeft(IRExpr.class,
                                                     (Function<IRExpr, ASMArg>) node -> node.accept(
                                                             tiler).result.get());

        if (tempMinusConst.matches(new Object[]
            { n.left(), n.right() })) {
            ASMTempArg lhs = tempMinusConst.leftObj();
            IRConst rhs = tempMinusConst.rightObj();

            insns.addAll(tempMinusConst.preMapLeft()
                                       .getOptimalTiling().optimalInstructions);

            ASMAddrExpr addrExpr = arg.addr(Optional.of(arg.temp(lhs.name,
                    Size.QWORD)),
                    ScaleValues.ONE,
                    Optional.empty(),
                    -rhs.constant());
            return Optional.of(addrExpr);
        } else {
            return Optional.empty();
        }
    }

}