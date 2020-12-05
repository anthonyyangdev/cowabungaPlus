package cyr7.ir;

import cyr7.ir.nodes.IRBinOp;
import edu.cornell.cs.cs4120.util.InternalCompilerError;

import java.math.BigInteger;

public enum BinOpInterpreter {

    ;

    public static long interpret(IRBinOp.OpType type, long l, long r) {
        long value;

        // Copied from staff-given interpreter code.
        switch (type) {
            case ADD_INT:
                value = l + r;
                break;
            case SUB_INT:
                value = l - r;
                break;
            case MUL_INT:
                value = l * r;
                break;
            case HMUL_INT:
                value = BigInteger.valueOf(l).multiply(BigInteger.valueOf(r))
                    .shiftRight(64).longValue();
                break;
            case DIV_INT:
                value = l / r;
                break;
            case MOD_INT:
                value = l % r;
                break;
            case AND:
                value = l & r;
                break;
            case OR:
                value = l | r;
                break;
            case XOR:
                value = l ^ r;
                break;
            case LSHIFT:
                value = l << r;
                break;
            case RSHIFT:
                value = l >>> r;
                break;
            case ARSHIFT:
                value = l >> r;
                break;
            case EQ:
                value = l == r ? 1 : 0;
                break;
            case NEQ:
                value = l != r ? 1 : 0;
                break;
            case LT:
                value = l < r ? 1 : 0;
                break;
            case GT:
                value = l > r ? 1 : 0;
                break;
            case LEQ:
                value = l <= r ? 1 : 0;
                break;
            case GEQ:
                value = l >= r ? 1 : 0;
                break;
            default:
                throw new InternalCompilerError("Invalid binary operation");
        }

        return value;
    }

}
