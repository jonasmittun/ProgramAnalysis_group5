package Week05;

import Week04.Frame;
import org.json.JSONObject;

import java.util.Deque;
import java.util.Map;

public record State(Deque<Frame> psi, Map<Integer, JSONObject> mu) {}
