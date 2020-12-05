package cyr7.ir.nodes;

import java.util.Objects;

import cyr7.visitor.MyIRVisitor;
import edu.cornell.cs.cs4120.util.SExpPrinter;
import java_cup.runtime.ComplexSymbolFactory.Location;

/**
 * An intermediate representation for a 64-bit integer constant.
 * INTEGER(n)
 */
public class IRInteger extends IRExpr_c {
    private long value;

    /**
     *
     * @param value value of this constant
     */
    public IRInteger(Location location, long value) {
        super(location);
        this.value = value;
    }

    public long value() {
        return value;
    }

    @Override
    public String label() {
        return "INT(" + value + ")";
    }

    @Override
    public boolean isConstant() {
        return true;
    }

    @Override
    public long constant() {
        return value;
    }

    @Override
    public void printSExp(SExpPrinter p) {
        p.startList();
        p.printAtom("INT");
        p.printAtom(String.valueOf(value));
        p.endList();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IRInteger irInteger = (IRInteger) o;
        return value == irInteger.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public <T> T accept(MyIRVisitor<T> v) {
        return v.visit(this);
    }

    @Override
    public String userFriendlyString() {
        return String.valueOf(this.value);
    }
}
