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
        Week05.SignInterpreter.addSigns(frame);
        System.out.println(frame);
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
