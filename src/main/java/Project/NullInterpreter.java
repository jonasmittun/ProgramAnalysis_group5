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

    private final NullStepper stepper;

    public NullInterpreter(Map<String, JSONObject> classes) {
        this.classes = classes;

        this.stepper = new NullStepper(classes);
    }

    @Override
    public void run(Frame frame, Map<Integer, JSONObject> mu){
        Deque<Frame> psi = new ArrayDeque<>();  // Method Stack

        // Transform values to abstract domain
        for(JSONObject o : frame.lambda()) o = toAbstract(o);
        for(JSONObject o : frame.sigma()) o = toAbstract(o);

        System.out.println("Initial: " + "\nΨ[" + frame + "]\n");
        psi.push(frame);

        Queue<State> queue = new LinkedList<>();
        queue.add(new State(psi, mu));

        while(!queue.isEmpty()) {
            State current = queue.poll();

            Set<State> next = stepper.step(current);

            System.out.println("Generated: " + next.size() + "\n");

            queue.addAll(next);
        }
    }
}
