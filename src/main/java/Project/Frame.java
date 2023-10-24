package Project;

import Week04.Method;
import Week04.Pair;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Deque;
import java.util.Optional;

// TODO: Implement this in the abstract interpreter
/** A frame represents a method or function call and contains information about that specific method's execution context.
 * @param lambda    Local Variables
 * @param sigma     Operand Stack
 * @param iota      Program Counter
 */
public record Frame(JSONObject[] lambda, Deque<Pair<Optional<JSONObject>, Abstraction>> sigma, Pair<Method, Integer> iota) {
    @Override
    public String toString() {
        return "(λ" + Arrays.toString(lambda) + ", σ" + sigma + ", ι" + iota.toString() + ")";
    }
}