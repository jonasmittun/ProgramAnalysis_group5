package Week05;

import Week04.ConcreteInterpreter;
import Week04.Frame;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.stream.Collectors;

import static Week04.ConcreteInterpreter.isNull;
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

    public static JSONObject toAbstract(Object o) {
        if(o == null) {
            return ConcreteInterpreter.createNull();
        } else if(o instanceof JSONObject jo) {
            if(!jo.has("sign") && !jo.has("kind") && jo.has("type") && jo.has("value")) {
                Sign r = switch (jo.getString("type")) {
                    case "boolean" -> jo.getBoolean("value") ? POSITIVE : ZERO;
                    case "int", "integer" -> toSign(Integer.compare(jo.getInt("value"), 0));
                    case "long" -> toSign(Long.compare(jo.getLong("value"), 0));
                    case "float" -> toSign(Float.compare(jo.getFloat("value"), 0));
                    case "double" -> toSign(Double.compare(jo.getDouble("value"), 0));
                    default -> {
                        System.out.println("Warning: Unsupported type: " + jo.get("type"));
                        yield ZERO;
                    }
                };

                jo.put("sign", Set.of(r));
            }

            return jo;
        } else throw new RuntimeException(o + " is not a JSONObject");
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

    /** Clone Helper: Copies any reference e_old that exists in mu_old to mu_new, but with e_new as the new reference */
    private static void clone_helper(JSONObject e_old, JSONObject e_new, Map<Integer, JSONObject> mu_old, Map<Integer, JSONObject> mu_new, Map<Integer, JSONObject> mu_mapper) {
        if(e_old == null) return;

        if(e_old.has("kind") && mu_old.containsKey(System.identityHashCode(e_old)) && !mu_mapper.containsKey(System.identityHashCode(e_old))) {
            JSONObject v_old = mu_old.get(System.identityHashCode(e_old));
            JSONObject v_new = cloneJSONObject(v_old);

            mu_new.put(System.identityHashCode(e_new), v_new);
            mu_mapper.put(System.identityHashCode(e_old), e_new);

            if(!isNull(v_old)) {
                switch(e_old.getString("kind")) {
                    case "array" -> {
                        // Clone inner values if they are reference types
                        JSONArray array_old = v_old.getJSONArray("value");
                        JSONArray array_new = v_new.getJSONArray("value");
                        for(int i = 0; i < array_old.length(); i++) {
                            JSONObject inner_old = array_old.getJSONObject(i);
                            JSONObject inner_new = array_new.getJSONObject(i);

                            if(inner_old.has("kind")) {
                                clone_helper(inner_old, inner_new, mu_old, mu_new, mu_mapper);
                            }
                        }
                    }
                    case "class" -> clone_class(v_old, v_new, mu_old, mu_new, mu_mapper);
                    default -> throw new RuntimeException("Unsupported reference type: " + e_old.get("kind"));
                }
            }
        }
    }

    /** Clone Helper: Recursively copies the class from mu_old to mu_new until no superclass can be found
     * @param class_old The old class object
     * @param class_new The copy of class_old
     */
    private static void clone_class(JSONObject class_old, JSONObject class_new, Map<Integer, JSONObject> mu_old, Map<Integer, JSONObject> mu_new, Map<Integer, JSONObject> mu_mapper) {
        // Clone fields
        JSONArray fields_old = class_old.getJSONArray("fields");
        JSONArray fields_new = class_new.getJSONArray("fields");
        for(int i = 0; i < fields_old.length(); i++) {
            JSONObject field_old = fields_old.getJSONObject(i);
            JSONObject field_new = fields_new.getJSONObject(i);

            if(field_old.getJSONObject("type").has("kind") && !field_old.isNull("value")) {
                JSONObject f_old = field_old.getJSONObject("value");
                JSONObject f_new = field_new.getJSONObject("value");

                clone_helper(f_old, f_new, mu_old, mu_new, mu_mapper);
            }
        }

        // Clone superclass if exists
        if(mu_old.containsKey(System.identityHashCode(class_old))) {
            JSONObject super_old = mu_old.get(System.identityHashCode(class_old));
            JSONObject super_new = cloneJSONObject(super_old);

            mu_new.put(System.identityHashCode(class_new), super_new);
            mu_mapper.put(System.identityHashCode(class_old), class_new);

            clone_class(super_old, super_new, mu_old, mu_new, mu_mapper);
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
