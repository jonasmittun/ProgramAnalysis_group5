package Week04;

public record Pair<T1, T2>(T1 e1, T2 e2) {
    @Override
    public String toString() {
        return "(" + e1.toString() + ", " + e2.toString() + ")";
    }

    @Override
    public Pair<T1, T2> clone() {
        return new Pair<>(e1(), e2());
    }
}
