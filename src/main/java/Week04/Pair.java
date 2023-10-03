package Week04;

import org.json.JSONObject;

public record Pair<T1, T2>(T1 e1, T2 e2) {
    @Override
    public String toString() {
        String fst = (e1 instanceof JSONObject method) ? method.getString("name") : e1.toString();
        return "(" + fst + ", " + e2.toString() + ")";
    }
}
