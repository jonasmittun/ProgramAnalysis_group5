package Week05;

public enum Sign {
    POSITIVE,
    ZERO,
    NEGATIVE;

    public static Sign toSign(int i) {
        return switch(i) {
            case -1 -> NEGATIVE;
            case 0  -> ZERO;
            case 1  -> POSITIVE;
            default -> throw new IllegalStateException("Unexpected value: " + i);
        };
    }
}