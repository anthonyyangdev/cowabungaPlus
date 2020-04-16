package cyr7.x86.patternmappers;

import cyr7.ir.nodes.IRBinOp;
import cyr7.ir.nodes.IRBinOp.OpType;
import cyr7.ir.nodes.IRConst;
import cyr7.ir.nodes.IRExpr;
import cyr7.x86.asm.ASMAddrExpr.ScaleValues;
import cyr7.x86.asm.ASMAddrExpr;
import cyr7.x86.asm.ASMArg;
import cyr7.x86.asm.ASMLine;
import cyr7.x86.asm.ASMLineFactory;
import cyr7.x86.asm.ASMTempArg;
import cyr7.x86.asm.ASMTempArg.Size;
import cyr7.x86.pattern.BiPatternBuilder;
import cyr7.x86.tiler.ComplexTiler;
import cyr7.x86.tiler.TilerData;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

// This matches c*t + n
public class _ConstTimesTemp_PlusOffset extends MemoryAddrPattern {

    public _ConstTimesTemp_PlusOffset(boolean isMemPattern) {
        super(isMemPattern);
    }
    
    @Override
    protected Optional<ASMAddrExpr> matchAddress(
        IRBinOp n,
        ComplexTiler tiler,
        ASMLineFactory make,
        List<ASMLine> insns) {
        if (n.opType() != OpType.ADD) {
            return Optional.empty();
        }

        var constTemp = BiPatternBuilder
            .left()
            .instOf(IRConst.class)
            .and(x -> x.constant() == 1 || x.constant() == 2 || x.constant() == 4 || x.constant() == 8)
            .right()
            .instOf(ASMTempArg.class)
            .finish()
            .mappingRight(IRExpr.class,
                (Function<IRExpr, ASMArg>)
                    node -> node.accept(tiler).result.get())
            .enableCommutes();

        var constTempPlusN = BiPatternBuilder
            .left()
            .instOf(IRBinOp.class)
            .and(x -> x.opType() == OpType.MUL)
            .and(x -> constTemp.matches(new Object[] { x.left(), x.right() }))
            .right()
            .instOf(IRConst.class)
            .finish()
            .enableCommutes();

        if (constTempPlusN.matches(new Object[]{ n.left(), n.right() })) {
            IRConst constArg = constTemp.leftObj();
            ASMTempArg tempArg = constTemp.rightObj();
            IRConst nArg = constTempPlusN.rightObj();

            insns.addAll(constTemp.preMapRight().getOptimalTiling().optimalInstructions);

            ASMAddrExpr addrExpr = arg.addr(
                    Optional.empty(),
                    ScaleValues.fromConst(constArg.constant()).get(),
                    Optional.of(arg.temp(tempArg.name, Size.QWORD)),
                    nArg.constant()
                );
            return Optional.of(addrExpr);
        }

        return Optional.empty();
    }
}
