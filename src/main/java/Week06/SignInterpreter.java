package Week06;

import Week04.Frame;
import Week05.*;
import org.json.JSONObject;

import java.util.*;

public class SignInterpreter implements Interpreter {

    private final Map<String, JSONObject> classes; // Map<Classname, JSONObject>

    private final SignStepper stepper;

    public SignInterpreter(Map<String, JSONObject> classes){
        this.classes = classes;

        this.stepper = new SignStepper(classes);
    }

    @Override
    public void run(Frame frame, Map<Integer, JSONObject> mu){
        Deque<Frame> psi = new ArrayDeque<>();  // Method Stack

        // Transform values to abstract domain
        for(JSONObject o : frame.lambda()) o = Week05.SignInterpreter.toAbstract(o);
        for(JSONObject o : frame.sigma()) o = Week05.SignInterpreter.toAbstract(o);

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
