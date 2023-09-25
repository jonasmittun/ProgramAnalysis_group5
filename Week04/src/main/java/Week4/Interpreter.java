package Week4;

import org.json.JSONObject;

import java.util.*;

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
    void step(Method method, Map<Integer, JSONObject> mu, Stack<Method> psi);
}
