package Week04;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.Deque;

/** A frame represents a method or function call and contains information about that specific method's execution context.
 * @param lambda    Local Variables
 * @param sigma     Operand Stack
 * @param iota      Program Counter
 */
public record Frame(JSONObject[] lambda, Deque<JSONObject> sigma, Pair<Method, Integer> iota) {
    @Override
    public String toString() {
        return "(λ" + Arrays.stream(lambda).map(Main::toFormattedString).toList() + ", σ" + sigma.stream().map(Main::toFormattedString).toList() + ", ι" + iota.toString() + ")";
    }
}