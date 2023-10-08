package Week06;

import Week04.Frame;
import Week05.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class SignInterpreter implements Interpreter {

    private final Map<String, JSONObject> classes; // Map<Classname, JSONObject>

    private final Map<String, Map<String, JSONObject>> class_methods;

    private final SignStepper stepper;

    public SignInterpreter(Map<String, JSONObject> classes){
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

        this.stepper = new SignStepper(classes);
    }

    @Override
    public void run(Frame frame, Map<Integer, JSONObject> mu){
        Deque<Frame> psi = new ArrayDeque<>();  // Method Stack
        Week05.SignInterpreter.addSigns(frame);
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
