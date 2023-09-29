package Week04;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.stream.Collectors;

/**
 * Method stack element:
 * lambda:  Local Variables
 * sigma:   Operand Stack
 * iota:    Program Counter
 */
public record Method(JSONObject[] lambda, Deque<JSONObject> sigma, Pair<String, Integer> iota) {
    @Override
    public String toString() {
        //return "(λ" + Arrays.stream(lambda).map(Main::toFormattedString).toList() + ", σ" + sigma.stream().map(Main::toFormattedString).toList() + ", ι" + iota.toString() + ")";
        return "(λ" + Arrays.toString(lambda) + ", σ" + sigma + ", ι" + iota.toString() + ")";
    }

    @Override
    public Method clone() {
        JSONObject[] lambda = Arrays.stream(this.lambda).map(o -> new JSONObject(o.toMap())).toArray(JSONObject[]::new);
        Deque<JSONObject> sigma = this.sigma.stream().map(o -> new JSONObject(o.toMap())).collect(Collectors.toCollection(ArrayDeque::new));
        Pair<String, Integer> iota = this.iota.clone();

        return new Method(lambda, sigma, iota);
    }
}