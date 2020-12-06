package cyr7.semantics.types;

public final class VoidType extends OrdinaryType {
    // The empty array {} is an array type of ArrayType(VoidType.VOID)

    public static final VoidType voidValue = new VoidType();

    private VoidType() {
    }

    @Override
    public Type type() {
        return Type.VOID;
    }

    @Override
    public String toString() {
        return "void";
    }

    /**
     * Void is always a subtype of any other type.
     * <p>
     * Returns {@code true} for any type {@code expectedSupertype}.
     */
    @Override
    public boolean isSubtypeOf(OrdinaryType expectedSupertype) {
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof VoidType;
    }

}
