package cyr7.semantics.types;

public abstract class OrdinaryType {

    enum Type {
        // OrdinaryType.Type.UNIT and OrdinaryType.Type are to be distinct
        // from ResultType.VOID and ResultType.UNIT.
        INT, BOOL, FLOAT, ARRAY, VOID, UNIT, GENERIC_ADD;
    }

    public abstract Type getType();

    public boolean isUnit() {
        return this.getType() == Type.UNIT;
    }

    public boolean isVoid() {
        return this.getType() == Type.VOID;
    }

    public boolean isFloat() { return this.getType() == Type.FLOAT; }

    public boolean isInt() {
        return this.getType() == Type.INT;
    }

    public boolean isBool() {
        return this.getType() == Type.BOOL;
    }

    public boolean isArray() {
        return this.getType() == Type.ARRAY;
    }

    public boolean isGenericAdd() {
        return this.getType() == Type.GENERIC_ADD;
    }

    public abstract boolean isSubtypeOf(OrdinaryType expectedSupertype);

}
