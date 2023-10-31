package Week05;

public enum Sign {
    POSITIVE,
    ZERO,
    NEGATIVE;

    public static Sign toSign(int i) {
        return switch(Integer.compare(i, 0)) {
            case -1 -> NEGATIVE;
            case 0  -> ZERO;
            case 1  -> POSITIVE;
            default -> throw new IllegalStateException("Unexpected value: " + i);
        };
    }

    @Override
    public String toString() {
        return switch(this) {
            case POSITIVE   -> "+";
            case ZERO       -> "0";
            case NEGATIVE   -> "-";
        };
    }
}