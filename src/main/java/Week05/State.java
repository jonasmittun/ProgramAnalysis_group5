package Week05;

import Week04.Method;
import org.json.JSONObject;

import java.util.Deque;
import java.util.Map;

public record State(Deque<Method> psi, Map<Integer, JSONObject> mu) {}
