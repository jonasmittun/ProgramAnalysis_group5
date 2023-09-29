package Week04;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.Stack;

/**
 * Method stack element:
 * lambda:  Local Variables
 * sigma:   Operand Stack
 * iota:    Program Counter
 */
public record Method(JSONObject[] lambda, Stack<JSONObject> sigma, Pair<String, Integer> iota) {
    @Override
    public String toString() {
        return "(λ" + Arrays.stream(lambda).map(Main::toFormattedString).toList() + ", σ" + sigma.stream().map(Main::toFormattedString).toList() + ", ι" + iota.toString() + ")";
    }
}