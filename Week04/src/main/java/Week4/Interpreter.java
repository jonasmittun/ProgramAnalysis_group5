package Week4;

import org.json.JSONObject;

import java.util.*;

public interface Interpreter {

    /** Runs the method
     * mu:      Memory
     * first:   Method
     * */
    void run(Method first, Map<Integer, JSONObject> mu);
}
