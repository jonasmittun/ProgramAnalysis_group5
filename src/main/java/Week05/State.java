package Week05;

import Week04.Method;
import org.json.JSONObject;

import java.util.AbstractMap;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.stream.Collectors;

public record State(Deque<Method> psi, Map<Integer, JSONObject> mu) {
    @Override
    public State clone() {
        Deque<Method> psi = this.psi.stream().map(Method::clone).collect(Collectors.toCollection(ArrayDeque::new));
        Map<Integer, JSONObject> mu = this.mu.entrySet().stream().map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), new JSONObject(e.getValue().toMap()))).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        return new State(psi, mu);
    }
}
