package Week4;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.Stack;

/**
 * Method stack element:
 * lambda:  Local Variables
 * sigma:   Operand Stack
 * iota:    Program Counter
 */
record Method(JSONObject[] lambda, Stack<JSONObject> sigma, Pair<String, Integer> iota) {
    @Override
    public String toString() {
        return "(λ" + Arrays.toString(lambda) + ", σ" + sigma + ", ι" + iota.toString() + ")";
    }
}