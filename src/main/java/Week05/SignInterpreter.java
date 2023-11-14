package Week05;

import Week04.ConcreteInterpreter;
import Week04.Frame;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.stream.Collectors;

import static Week04.Main.cloneJSONObject;
import static Week05.Sign.*;

public class SignInterpreter implements Interpreter {

    private final Map<String, JSONObject> classes; // Map<Classname, JSONObject>

    private final int depthLimit;

    private final SignStepper stepper;

    public SignInterpreter(Map<String, JSONObject> classes, int depthLimit){
        this.classes = classes;
        this.depthLimit = depthLimit;

        this.stepper = new SignStepper(classes);
    }

    public static JSONObject toAbstract(JSONObject o) {
        if(o != null && !o.has("sign")) {
            if(!o.has("kind") && o.has("type") && o.has("value")) {
                Sign r = switch (o.getString("type")) {
                    case "boolean" -> o.getBoolean("value") ? POSITIVE : ZERO;
                    case "int", "integer" -> toSign(Integer.compare(o.getInt("value"), 0));
                    case "long" -> toSign(Long.compare(o.getLong("value"), 0));
                    case "float" -> toSign(Float.compare(o.getFloat("value"), 0));
                    case "double" -> toSign(Double.compare(o.getDouble("value"), 0));
                    default -> {
                        System.out.println("Warning: Unsupported type: " + o.get("type"));
                        yield ZERO;
                    }
                };

                o.put("sign", Set.of(r));
                return o;
            }
        }

        return o;
    }

    /** Clones everything from a triple of a frame, psi and mu to a new triple */
    public static Triple<Frame, Deque<Frame>, Map<Integer, JSONObject>> clone_state(Frame frame, Deque<Frame> psi, Map<Integer, JSONObject> mu) {
        // Create new memory
        Map<Integer, JSONObject> mu_new = new HashMap<>();

        // Map to check if a reference has been cloned already (old reference -> new reference if exists)
        Map<Integer, JSONObject> mu_mapper = new HashMap<>();

        // Clone all elements in psi
        Deque<Frame> psi_new = psi.stream().map(f -> clone_frame(f, mu, mu_new, mu_mapper)).collect(Collectors.toCollection(ArrayDeque::new));

        // Clone current Frame
        Frame frame_new = clone_frame(frame, mu, mu_new, mu_mapper);

        return new Triple<>(frame_new, psi_new, mu_new);
    }

    /** Clone Helper: Clones the frame and copies any references from the old memory to the new memory */
    public static Frame clone_frame(Frame frame, Map<Integer, JSONObject> mu_old, Map<Integer, JSONObject> mu_new, Map<Integer, JSONObject> mu_mapper) {
        // Clone Lambda
        JSONObject[] lambda_new = new JSONObject[frame.lambda().length];
        for(int i = 0; i < lambda_new.length; i++) {
            JSONObject e_old = frame.lambda()[i];

            if(e_old == null) continue;

            if(e_old.has("kind")) {
                // Check if the reference has already been cloned
                if(mu_mapper.containsKey(System.identityHashCode(e_old))) {
                    lambda_new[i] = mu_mapper.get(System.identityHashCode(e_old));
                } else {
                    JSONObject e_new = cloneJSONObject(e_old);
                    clone_helper(e_old, e_new, mu_old, mu_new, mu_mapper);

                    lambda_new[i] = e_new;
                }
            } else {
                lambda_new[i] = cloneJSONObject(e_old);
            }
        }

        // Clone Sigma
        Deque<JSONObject> sigma_new = new ArrayDeque<>();
        for(JSONObject e_old : frame.sigma()) {
            if(e_old.has("kind")) {
                // Check if the reference has already been cloned
                if(mu_mapper.containsKey(System.identityHashCode(e_old))) {
                    sigma_new.addLast(mu_mapper.get(System.identityHashCode(e_old)));
                } else {
                    JSONObject e_new = cloneJSONObject(e_old);
                    clone_helper(e_old, e_new, mu_old, mu_new, mu_mapper);

                    sigma_new.addLast(e_new);
                }
            } else {
                sigma_new.addLast(cloneJSONObject(e_old));
            }
        }

        return new Frame(lambda_new, sigma_new, frame.iota());
    }

    /** Clone Helper: Copies any object e_old that exists in mu_old to mu_new, but with e_new as the new reference */
    private static void clone_helper(JSONObject e_old, JSONObject e_new, Map<Integer, JSONObject> mu_old, Map<Integer, JSONObject> mu_new, Map<Integer, JSONObject> mu_mapper) {
        if(e_old == null) return;

        if(e_old.has("kind") && !mu_mapper.containsKey(System.identityHashCode(e_old))) {
            JSONObject v_old = mu_old.get(System.identityHashCode(e_old));
            JSONObject v_new = cloneJSONObject(v_old);

            mu_new.put(System.identityHashCode(e_new), v_new);
            mu_mapper.put(System.identityHashCode(e_old), e_new);

            switch(e_new.getString("kind")) {
                case "array" -> {
                    // Clone inner values if they are reference types
                    JSONArray array = v_new.getJSONArray("value");
                    for(int i = 0; i < array.length(); i++) {
                        JSONObject v_inner = array.getJSONObject(i);

                        if(v_inner.has("kind")) {
                            clone_helper(v_inner, cloneJSONObject(v_inner), mu_old, mu_new, mu_mapper);
                        }
                    }
                }
                case "class" -> {
                    // Clone any fields that have references in memory
                    JSONArray fields = v_new.getJSONArray("fields");
                    for(int i = 0; i < fields.length(); i++) {
                        JSONObject field = fields.getJSONObject(i);

                        if(field.getJSONObject("type").has("kind") && !field.isNull("value")) {
                            JSONObject f_old = field.getJSONObject("value");
                            clone_helper(f_old, cloneJSONObject(f_old), mu_old, mu_new, mu_mapper);
                        }
                    }
                }
                default -> throw new RuntimeException("Unsupported reference type: " + e_old.get("kind"));
            }
        }
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
        int depthCounter = 1;
        int depth = 0;

        while(!queue.isEmpty() && depth < depthLimit){
            State current = queue.poll();

            Set<State> next = stepper.step(current);

            System.out.println("Generated: " + next.size() + "\n");

            queue.addAll(next);

            depthCounter--;
            if(depthCounter == 0) {
                depth++;
                depthCounter = queue.size();
            }
        }
    }
}
