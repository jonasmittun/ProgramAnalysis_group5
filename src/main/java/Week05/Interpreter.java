package Week05;

import Week04.Method;
import org.json.JSONObject;

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
     * @param state:    Record of the following
     * method:          The method to be executed
     * mu:              The memory
     * psi:             The method stack
     */
    Set<State> step(State state);
}
