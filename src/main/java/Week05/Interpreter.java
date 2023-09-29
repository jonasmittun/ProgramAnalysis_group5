package Week05;

import Week04.Method;
import org.json.JSONObject;

import java.util.Deque;
import java.util.Map;
import java.util.Set;

public interface Interpreter {

    /** Runs the method
     * @param method:   Method
     * @param mu:       Memory
     */
    void run(Method method, Map<Integer, JSONObject> mu);

    /**
     * Execute a single bytecode instruction in the method
     * @param method:   The method to be executed
     * @param mu:       The memory
     * @param psi:      The method stack
     */
    Set<State> step(Method method, Map<Integer, JSONObject> mu, Deque<Method> psi);
}
