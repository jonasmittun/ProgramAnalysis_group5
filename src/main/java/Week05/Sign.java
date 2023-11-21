package Week05;

import java.util.Set;

public enum Sign {
    POSITIVE,
    ZERO,
    NEGATIVE;

    /** Addition: s1 + s2 */
    public static Set<Sign> add(Sign s1, Sign s2) {
        return switch(s1) {
            case NEGATIVE -> switch(s2) {
                case NEGATIVE   -> Set.of(NEGATIVE);
                case ZERO       -> Set.of(NEGATIVE);
                case POSITIVE   -> Set.of(NEGATIVE, ZERO, POSITIVE);
            };
            case ZERO -> switch(s2) {
                case NEGATIVE   -> Set.of(NEGATIVE);
                case ZERO       -> Set.of(ZERO);
                case POSITIVE   -> Set.of(POSITIVE);
            };
            case POSITIVE -> switch(s2) {
                case NEGATIVE   -> Set.of(NEGATIVE, ZERO, POSITIVE);
                case ZERO       -> Set.of(POSITIVE);
                case POSITIVE   -> Set.of(POSITIVE);
            };
        };
    }

    /** Subtraction: s1 - s2 */
    public static Set<Sign> sub(Sign s1, Sign s2) {
        return switch(s1) {
            case NEGATIVE -> switch(s2) {
                case NEGATIVE   -> Set.of(NEGATIVE, ZERO, POSITIVE);
                case ZERO       -> Set.of(NEGATIVE);
                case POSITIVE   -> Set.of(NEGATIVE);
            };
            case ZERO -> switch(s2) {
                case NEGATIVE   -> Set.of(POSITIVE);
                case ZERO       -> Set.of(ZERO);
                case POSITIVE   -> Set.of(NEGATIVE);
            };
            case POSITIVE -> switch(s2) {
                case NEGATIVE   -> Set.of(POSITIVE);
                case ZERO       -> Set.of(POSITIVE);
                case POSITIVE   -> Set.of(NEGATIVE, ZERO, POSITIVE);
            };
        };
    }

    /** Multiplication: s1 * s2 */
    public static Set<Sign> mul(Sign s1, Sign s2) {
        return switch(s1) {
            case NEGATIVE -> switch(s2) {
                case NEGATIVE   -> Set.of(POSITIVE);
                case ZERO       -> Set.of(ZERO);
                case POSITIVE   -> Set.of(NEGATIVE);
            };
            case ZERO -> Set.of(ZERO);
            case POSITIVE -> switch(s2) {
                case NEGATIVE   -> Set.of(NEGATIVE);
                case ZERO       -> Set.of(ZERO);
                case POSITIVE   -> Set.of(POSITIVE);
            };
        };
    }

    /** Division: s1 / s2 */
    public static Set<Sign> div(Sign s1, Sign s2) {
        return switch(s1) {
            case NEGATIVE -> switch(s2) {
                case NEGATIVE   -> Set.of(POSITIVE);
                case ZERO       -> throw new ArithmeticException("Illegal divide by zero");
                case POSITIVE   -> Set.of(NEGATIVE);
            };
            case ZERO -> switch(s2) {
                case NEGATIVE   -> Set.of(ZERO);
                case ZERO       -> throw new ArithmeticException("Illegal divide by zero");
                case POSITIVE   -> Set.of(ZERO);
            };
            case POSITIVE -> switch(s2) {
                case NEGATIVE   -> Set.of(NEGATIVE);
                case ZERO       -> throw new ArithmeticException("Illegal divide by zero");
                case POSITIVE   -> Set.of(POSITIVE);
            };
        };
    }

    /** Remainder: s1 % s2 */
    public static Set<Sign> rem(Sign s1, Sign s2) {
        return switch(s2) {
            case NEGATIVE   -> switch(s1) {
                case NEGATIVE   -> Set.of(NEGATIVE, ZERO);
                case ZERO       -> Set.of(ZERO);
                case POSITIVE   -> Set.of(NEGATIVE, ZERO);
            };
            case ZERO       -> throw new ArithmeticException("Illegal divide by zero");
            case POSITIVE   -> switch(s1) {
                case NEGATIVE   -> Set.of(ZERO, POSITIVE);
                case ZERO       -> Set.of(ZERO);
                case POSITIVE   -> Set.of(ZERO, POSITIVE);
            };
        };
    }

    /** Negation: -sign */
    public static Sign negate(Sign sign) {
        return switch(sign) {
            case POSITIVE   -> NEGATIVE;
            case ZERO       -> ZERO;
            case NEGATIVE   -> POSITIVE;
        };
    }

    /** Equals: s1 == s2 */
    public static Set<Boolean> eq(Sign s1, Sign s2) {
        if((s1 == NEGATIVE && s2 == NEGATIVE) || (s1 == POSITIVE && s2 == POSITIVE)) return Set.of(true, false);
        else if(s1 == ZERO && s2 == ZERO) return Set.of(true);
        else return Set.of(false);
    }

    /** Not Equals: s1 != s2 */
    public static Set<Boolean> ne(Sign s1, Sign s2) {
        return (s1 == s2) ? Set.of(false) : Set.of(true, false);
    }

    /** Less or equal: s1 <= s2 */
    public static Set<Boolean> le(Sign s1, Sign s2) {
        return switch(s1) {
            case NEGATIVE   -> switch(s2) {
                case NEGATIVE   -> Set.of(true, false);
                case ZERO       -> Set.of(true);
                case POSITIVE   -> Set.of(true);
            };
            case ZERO       -> switch(s2) {
                case NEGATIVE   -> Set.of(false);
                case ZERO       -> Set.of(false);
                case POSITIVE   -> Set.of(true);
            };
            case POSITIVE   -> switch(s2) {
                case NEGATIVE   -> Set.of(false);
                case ZERO       -> Set.of(false);
                case POSITIVE   -> Set.of(true, false);
            };
        };
    }

    /** Less than: s1 < s2 */
    public static Set<Boolean> lt(Sign s1, Sign s2) {
        return switch(s1) {
            case NEGATIVE   -> switch(s2) {
                case NEGATIVE   -> Set.of(true, false);
                case ZERO       -> Set.of(true);
                case POSITIVE   -> Set.of(true);
            };
            case ZERO       -> switch(s2) {
                case NEGATIVE   -> Set.of(false);
                case ZERO       -> Set.of(true);
                case POSITIVE   -> Set.of(true);
            };
            case POSITIVE   -> switch(s2) {
                case NEGATIVE   -> Set.of(false);
                case ZERO       -> Set.of(false);
                case POSITIVE   -> Set.of(true, false);
            };
        };
    }

    /** Greater or equal: s1 >= s2 */
    public static Set<Boolean> ge(Sign s1, Sign s2) {
        return switch(s1) {
            case NEGATIVE   -> switch(s2) {
                case NEGATIVE   -> Set.of(true, false);
                case ZERO       -> Set.of(false);
                case POSITIVE   -> Set.of(false);
            };
            case ZERO       -> switch(s2) {
                case NEGATIVE   -> Set.of(true);
                case ZERO       -> Set.of(true);
                case POSITIVE   -> Set.of(false);
            };
            case POSITIVE   -> switch(s2) {
                case NEGATIVE   -> Set.of(true);
                case ZERO       -> Set.of(true);
                case POSITIVE   -> Set.of(true, false);
            };
        };
    }

    /** Greater than: s1 > s2 */
    public static Set<Boolean> gt(Sign s1, Sign s2) {
        return switch(s1) {
            case NEGATIVE   -> switch(s2) {
                case NEGATIVE   -> Set.of(true, false);
                case ZERO       -> Set.of(false);
                case POSITIVE   -> Set.of(false);
            };
            case ZERO       -> switch(s2) {
                case NEGATIVE   -> Set.of(false);
                case ZERO       -> Set.of(false);
                case POSITIVE   -> Set.of(false);
            };
            case POSITIVE   -> switch(s2) {
                case NEGATIVE   -> Set.of(true);
                case ZERO       -> Set.of(true);
                case POSITIVE   -> Set.of(true, false);
            };
        };
    }

    /** Converts an integer to a Sign value */
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