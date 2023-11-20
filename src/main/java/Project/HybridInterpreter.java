package Project;

import Week04.Frame;
import Week05.Interpreter;
import Week05.Sign;
import Week05.State;
import org.json.JSONObject;

import java.util.*;

import static Week04.ConcreteInterpreter.isNull;

public class HybridInterpreter implements Interpreter {

    private final Map<String, JSONObject> classes; // Map<Classname, JSONObject>

    private final HybridStepper stepper;

    public HybridInterpreter(Map<String, JSONObject> classes) {
        this.classes = classes;

        this.stepper = new HybridStepper(classes);
    }

    private void toAbstract(JSONObject o) {
        if(isNull(o)) {
            o.put("abstract", ANull.NULL);
        } else if(o.has("kind")) {
            o.put("abstract", ANull.NULLABLE);
        } else if(o.has("type") && o.has("value")) {
            switch(o.getString("type")) {
                case "int", "integer", "float", "double", "long" -> o.put("sign", Set.of(Sign.toSign(o.getInt("value"))));
            }
        } else throw new RuntimeException("Unexpected value: " + o);
    }

    @Override
    public void run(Frame frame, Map<Integer, JSONObject> mu){
        Deque<Frame> psi = new ArrayDeque<>();  // Method Stack

        // Transform values to abstract domain
        for(JSONObject o : frame.lambda()) toAbstract(o);
        for(JSONObject o : frame.sigma()) toAbstract(o);

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
