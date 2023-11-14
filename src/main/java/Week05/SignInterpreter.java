package Week05;

import Week04.Frame;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.stream.Collectors;

import static Week05.Sign.*;

public class SignInterpreter implements Interpreter {

    private final Map<String, JSONObject> classes; // Map<Classname, JSONObject>

    private final Map<String, Map<String, JSONObject>> class_methods;

    private final int depthLimit;

    private final SignStepper stepper;

    public SignInterpreter(Map<String, JSONObject> classes, int depthLimit){
        this.classes = classes;
        this.depthLimit = depthLimit;

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

    public static void addSigns(Frame f) {
        for(JSONObject o : f.lambda()) {
            toAbstract(o);
        }

        for(JSONObject o : f.sigma()) {
            toAbstract(o);
        }
    }

    /** Clones everything from a triple of a frame, psi and mu to a new triple */
    public static Triple<Frame, Deque<Frame>, Map<Integer, JSONObject>> clone_state(Frame frame, Deque<Frame> psi, Map<Integer, JSONObject> mu) {
        // Create new memory
        Map<Integer, JSONObject> mu_new = new HashMap<>();

        // Clone all elements in psi
        Deque<Frame> psi_new = psi.stream().map(f -> clone_frame(f, mu, mu_new)).collect(Collectors.toCollection(ArrayDeque::new));

        // Clone current Frame
        Frame frame_new = clone_frame(frame, mu, mu_new);

        return new Triple<>(frame_new, psi_new, mu_new);
    }

    /** Clone Helper: Clones the frame and copies any references from the old memory to the new memory */
    public static Frame clone_frame(Frame frame, Map<Integer, JSONObject> mu_old, Map<Integer, JSONObject> mu_new) {
        // Clone Lambda
        JSONObject[] lambda_new = new JSONObject[frame.lambda().length];
        for(int i = 0; i < lambda_new.length; i++) {
            JSONObject e_old = frame.lambda()[i];
            JSONObject e_new = (e_old == null) ? null : new JSONObject(e_old.toMap());

            clone_helper(e_old, e_new, mu_old, mu_new);

            lambda_new[i] = e_new;
        }

        // Clone Sigma
        Deque<JSONObject> sigma_new = new ArrayDeque<>();
        for(JSONObject e_old : frame.sigma()) {
            JSONObject e_new = new JSONObject(e_old.toMap());

            clone_helper(e_old, e_new, mu_old, mu_new);

            sigma_new.addLast(e_new);
        }

        return new Frame(lambda_new, sigma_new, frame.iota());
    }

    /** Clone Helper: Copies any object e_old that exists in mu_old to mu_new, but with e_new as the new reference */
    private static void clone_helper(JSONObject e_old, JSONObject e_new, Map<Integer, JSONObject> mu_old, Map<Integer, JSONObject> mu_new) {
        if(e_old == null) return;

        if(mu_old.containsKey(System.identityHashCode(e_old))) {
            Object v = mu_old.get(System.identityHashCode(e_old));
            if(v instanceof JSONObject o) {
                mu_new.put(System.identityHashCode(e_new), new JSONObject(o.toMap()));

                if(o.has("value")) {
                    if(o.get("value") instanceof JSONArray array) {
                        for(int i = 0; i < array.length(); i++) {
                            JSONObject va = array.getJSONObject(i);
                            if(va.has("kind")) {
                                clone_helper(va, new JSONObject(va.toMap()), mu_old, mu_new);
                            }
                        }
                    }
                }
            } else {
                mu_new.put(System.identityHashCode(e_new), null);
            }
        }
    }

    @Override
    public void run(Frame frame, Map<Integer, JSONObject> mu){
        Deque<Frame> psi = new ArrayDeque<>();  // Method Stack
        addSigns(frame);
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
