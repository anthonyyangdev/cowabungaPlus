package cyr7.x86.patternmappers;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import cyr7.ir.nodes.IRBinOp;
import cyr7.ir.nodes.IRBinOp.OpType;
import cyr7.ir.nodes.IRInteger;
import cyr7.ir.nodes.IRExpr;
import cyr7.x86.asm.ASMAddrExpr;
import cyr7.x86.asm.ASMAddrExpr.ScaleValues;
import cyr7.x86.asm.ASMArg;
import cyr7.x86.asm.ASMLine;
import cyr7.x86.asm.ASMLineFactory;
import cyr7.x86.asm.ASMTempArg;
import cyr7.x86.asm.ASMRegSize;
import cyr7.x86.pattern.BiPatternBuilder;
import cyr7.x86.tiler.ComplexTiler;

// This matches c*t + n
public class Temp_PlusConstTimesTemp_PlusOffset extends MemoryAddrPattern {

    public Temp_PlusConstTimesTemp_PlusOffset(boolean isMemPattern) {
        super(isMemPattern);
    }

    @Override
    protected Optional<ASMAddrExpr> matchAddress(
        IRBinOp n,
        ComplexTiler tiler,
        ASMLineFactory make,
        List<ASMLine> insns) {
        if (n.opType() != OpType.ADD_INT) {
            return Optional.empty();
        }

        var constTimesTemp = BiPatternBuilder
            .left()
            .instOf(IRInteger.class)
            .and(x -> x.constant() == 1 || x.constant() == 2 || x.constant() == 4 || x.constant() == 8)
            .right()
            .instOf(ASMTempArg.class)
            .finish()
            .mappingRight(IRExpr.class,
                (Function<IRExpr, ASMArg>)
                    node -> node.accept(tiler).result.get())
            .enableCommutes();

        var constTempPlusOffset = BiPatternBuilder
            .left()
            .instOf(IRBinOp.class)
            .and(x -> x.opType() == OpType.MUL_INT)
            .and(x -> constTimesTemp.matches(new Object[] { x.left(), x.right() }))
            .right()
            .instOf(IRInteger.class)
            .and(x -> Is32Bits.check(x.constant()))
            .finish()
            .enableCommutes();

        var tempPlusRest = BiPatternBuilder
            .left()
            .instOf(ASMTempArg.class)
            .right()
            .instOf(IRBinOp.class)
            .and(x -> x.opType() == OpType.ADD_INT)
            .and(x -> constTempPlusOffset.matches(new Object[] {x.left(), x.right()}))
            .finish()
            .mappingLeft(IRExpr.class,
                (Function<IRExpr, ASMArg>)
                    node -> node.accept(tiler).result.get())
            .enableCommutes();

        if (tempPlusRest.matches(new Object[]{ n.left(), n.right() })) {
            ASMTempArg baseArg = tempPlusRest.leftObj();
            IRInteger scaleArg = constTimesTemp.leftObj();
            ASMTempArg indexArg = constTimesTemp.rightObj();
            IRInteger offsetArg = constTempPlusOffset.rightObj();

            insns.addAll(tempPlusRest.preMapLeft().getOptimalTiling().optimalInstructions);
            insns.addAll(constTimesTemp.preMapRight().getOptimalTiling().optimalInstructions);

            this.setCost(1 + tempPlusRest.preMapLeft().getOptimalTiling().tileCost
                           + constTimesTemp.preMapRight().getOptimalTiling().tileCost);

            ASMAddrExpr addrExpr = arg.addr(
                    Optional.of(arg.temp(baseArg.name, ASMRegSize.QWORD)),
                    ScaleValues.fromConst(scaleArg.constant()).get(),
                    Optional.of(arg.temp(indexArg.name, ASMRegSize.QWORD)),
                    offsetArg.constant()
                );
            return Optional.of(addrExpr);
        }

        return Optional.empty();
    }
}
