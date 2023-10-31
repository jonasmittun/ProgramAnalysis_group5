package Project;

import Week04.Frame;
import Week05.Interpreter;
import Week05.State;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

import static Project.ANull.toAbstract;

public class NullInterpreter implements Interpreter {

    private final Map<String, JSONObject> classes; // Map<Classname, JSONObject>

    private final Map<String, Map<String, JSONObject>> class_methods;

    private final NullStepper stepper;

    public NullInterpreter(Map<String, JSONObject> classes) {
        this.classes = classes;

        // Map methods for all classes
        class_methods = new HashMap<>();
        for(Map.Entry<String, JSONObject> entry : classes.entrySet()) {
            Map<String, JSONObject> methods = new HashMap<>();

            JSONArray ms = entry.getValue().getJSONArray("methods");
            for(int i = 0; i < ms.length(); i++) {
                JSONObject m = ms.getJSONObject(i);
                methods.put(m.getString("name"), m);
            }

            class_methods.put(entry.getKey(), methods);
        }

        this.stepper = new NullStepper(classes);
    }

    @Override
    public void run(Frame frame, Map<Integer, JSONObject> mu){
        Deque<Frame> psi = new ArrayDeque<>();  // Method Stack

        // Transform values to abstract domain
        for(JSONObject o : frame.lambda()) toAbstract(o);
        for(JSONObject o : frame.sigma()) toAbstract(o);

        System.out.println(frame);
        psi.push(frame);

        Queue<State> queue = new LinkedList<>();
        queue.add(new State(psi, mu));

        while(!queue.isEmpty()) {
            State current = queue.poll();

            Set<State> next = stepper.step(current);

            System.out.println("Generated: " + next.size());

            queue.addAll(next);
        }
    }
}
