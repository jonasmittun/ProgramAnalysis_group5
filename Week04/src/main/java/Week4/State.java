package Week4;

import org.json.JSONObject;

import java.util.Map;
import java.util.Stack;

public record State(Stack<Method> psi, Map<Integer, JSONObject> mu) {
    // TODO: Make a method that will create a deep clone
}
