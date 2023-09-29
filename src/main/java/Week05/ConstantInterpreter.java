package Week05;

import Week04.Method;
import org.json.JSONObject;

import java.util.Deque;
import java.util.Map;
import java.util.Set;

public class ConstantInterpreter implements Interpreter {
    @Override
    public void run(Method method, Map<Integer, JSONObject> mu) {

    }

    @Override
    public Set<State> step(Method method, Map<Integer, JSONObject> mu, Deque<Method> psi) {
        return null;
    }
}
