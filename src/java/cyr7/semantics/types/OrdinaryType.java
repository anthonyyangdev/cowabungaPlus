package cyr7.semantics.types;

public abstract class OrdinaryType {

    public enum Type {
        // OrdinaryType.Type.UNIT and OrdinaryType.Type are to be distinct
        // from ResultType.VOID and ResultType.UNIT.
        INT, BOOL, FLOAT, ARRAY, VOID, UNIT, GENERIC_ADD
    }

    public abstract Type type();

    public boolean isUnit() {
        return this.type() == Type.UNIT;
    }

    public boolean isVoid() {
        return this.type() == Type.VOID;
    }

    public boolean isFloat() { return this.type() == Type.FLOAT; }

    public boolean isInt() {
        return this.type() == Type.INT;
    }

    public boolean isBool() {
        return this.type() == Type.BOOL;
    }

    public boolean isArray() {
        return this.type() == Type.ARRAY;
    }

    public boolean isGenericAdd() {
        return this.type() == Type.GENERIC_ADD;
    }

    public abstract boolean isSubtypeOf(OrdinaryType expectedSupertype);

}
