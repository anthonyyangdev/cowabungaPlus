package cyr7.x86.tiler;

import cyr7.ir.nodes.IRBinOp;
import cyr7.ir.nodes.IRBinOp.OpType;
import org.junit.jupiter.api.Test;

import static cyr7.x86.tiler.ASMTestUtils.assertEqualsTiled;
import static cyr7.x86.tiler.ASMTestUtils.makeIR;

public class Test_ConstTimesTemp_PlusOffset {

    @Test
    void testConstTimesTempAndOffset() {
        IRBinOp constTempOffset = makeIR(make ->
                make.IRBinOp(OpType.ADD_INT,
                        make.IRBinOp(OpType.MUL_INT,
                                make.IRInteger(4),
                                make.IRTemp("bleh")),
                        make.IRInteger(8))
        );

        assertEqualsTiled(constTempOffset, "leaq _t0, [ 4 * bleh + 8 ]");
    }

    @Test
    void testConstTimesOffsetAndTemp() {
        IRBinOp constTempOffset = makeIR(make ->
                make.IRBinOp(OpType.ADD_INT,
                        make.IRBinOp(OpType.MUL_INT,
                                make.IRTemp("bleh"),
                                make.IRInteger(4)),
                        make.IRInteger(8))
        );

        assertEqualsTiled(constTempOffset, "leaq _t0, [ 4 * bleh + 8 ]");
    }

    @Test
    void testOffsetAndConstTimesTemp() {
        IRBinOp constTempOffset = makeIR(make ->
                make.IRBinOp(OpType.ADD_INT,
                        make.IRInteger(8),
                        make.IRBinOp(OpType.MUL_INT,
                                make.IRInteger(4),
                                make.IRTemp("bleh"))
                )
        );

        assertEqualsTiled(constTempOffset, "leaq _t0, [ 4 * bleh + 8 ]");
    }

    @Test
    void testOffsetAndTempTimesConstant() {
        IRBinOp constTempOffset = makeIR(make ->
                make.IRBinOp(OpType.ADD_INT,
                        make.IRInteger(8),
                        make.IRBinOp(OpType.MUL_INT,
                                make.IRTemp("bleh"),
                                make.IRInteger(4))
                )
        );

        assertEqualsTiled(constTempOffset, "leaq _t0, [ 4 * bleh + 8 ]");
    }

    @Test
    void testConstOver32Bits_TimesTempAndOffset() {
        IRBinOp constTempOffset = makeIR(make ->
            make.IRBinOp(OpType.ADD_INT,
                make.IRBinOp(OpType.MUL_INT,
                    make.IRInteger(4),
                    make.IRTemp("bleh")),
                make.IRInteger(1099511627776L))
        );

        assertEqualsTiled(constTempOffset,
            "movq _t0, 1099511627776",
            "leaq _t1, [ _t0 + 4 * bleh ]");
    }

}
