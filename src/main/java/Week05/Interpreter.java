package Week05;

import Week04.Frame;
import org.json.JSONObject;

import java.util.Map;

public interface Interpreter {

    /** Runs the method
     * @param frame Frame
     * @param mu    Memory
     */
    void run(Frame frame, Map<Integer, JSONObject> mu);

    /**
     * Execute a single bytecode instruction in the method
     * @param state:    Record of the following
     * method:          The method to be executed
     * mu:              The memory
     * psi:             The method stack
     */
    //Set<State> step(State state);
}
