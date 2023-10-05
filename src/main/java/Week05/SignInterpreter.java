package Week05;

import Week04.Method;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

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
            if (!o.has("kind") && o.has("type") && o.has("value")) {
                Optional<Integer> r = switch (o.getString("type")) {
                    case "int", "integer" -> Optional.of(Integer.compare(o.getInt("value"), 0));
                    case "long" -> Optional.of(Long.compare(o.getLong("value"), 0));
                    case "float" -> Optional.of(Float.compare(o.getFloat("value"), 0));
                    case "double" -> Optional.of(Double.compare(o.getDouble("value"), 0));
                    default -> Optional.empty();
                };

                r.ifPresent(integer -> o.put("sign", Set.of(Sign.toSign(integer))));
                return o;
            }
        }

        return o;
    }

    public static void addSigns(Method m) {
        for(JSONObject o : m.lambda()) {
            toAbstract(o);
        }

        for(JSONObject o : m.sigma()) {
            toAbstract(o);
        }
    }

    /** Clones everything from a triple of a method, psi and mu to a new triple */
    public static Triple<Method, Deque<Method>, Map<Integer, JSONObject>> clone_state(Method method, Deque<Method> psi, Map<Integer, JSONObject> mu) {
        Map<Integer, JSONObject> mu_new = new HashMap<>();
        Deque<Method> psi_new = new ArrayDeque<>();

        // Clone all elements in psi
        for(Method m : psi) {
            // Clone elements in lambda
            JSONObject[] lambda = new JSONObject[m.lambda().length];
            for(int i = 0; i < lambda.length; i++) {
                JSONObject e_old = m.lambda()[i];
                JSONObject e_new = (e_old == null) ? null : new JSONObject(e_old.toMap());

                clone_helper(e_old, e_new, mu, mu_new);

                lambda[i] = e_new;
            }

            // Clone elements in sigma
            Deque<JSONObject> sigma = new ArrayDeque<>();
            for(JSONObject e_old : m.sigma()) {
                JSONObject e_new = (e_old == null) ? null : new JSONObject(e_old.toMap());

                clone_helper(e_old, e_new, mu, mu_new);

                sigma.addLast(e_new);
            }

            psi_new.addLast(new Method(lambda, sigma, m.iota()));
        }

        // --- Clone current method ---
        // Clone elements in lambda
        JSONObject[] lambda_new = new JSONObject[method.lambda().length];
        for(int i = 0; i < lambda_new.length; i++) {
            JSONObject e_old = method.lambda()[i];
            JSONObject e_new = (e_old == null) ? null : new JSONObject(e_old.toMap());

            clone_helper(e_old, e_new, mu, mu_new);

            lambda_new[i] = e_new;
        }

        // Clone elements in sigma
        Deque<JSONObject> sigma_new = new ArrayDeque<>();
        for(JSONObject e_old : method.sigma()) {
            JSONObject e_new = (e_old == null) ? null : new JSONObject(e_old.toMap());

            clone_helper(e_old, e_new, mu, mu_new);

            sigma_new.addLast(e_new);
        }

        Method method_new = new Method(lambda_new, sigma_new, method.iota());

        return new Triple<>(method_new, psi_new, mu_new);
    }

    /** Copies any object e_old that exists in mu_old to mu_new, but with e_new as the new reference */
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
    public void run(Method method, Map<Integer, JSONObject> mu){
        Deque<Method> psi = new ArrayDeque<>();  // Method Stack
        addSigns(method);
        System.out.println(method);
        psi.push(method);

        Queue<State> queue = new LinkedList<>();
        queue.add(new State(psi, mu));
        int depthCounter = 1;
        int depth = 0;

        while(!queue.isEmpty() && depth < depthLimit){
            State current = queue.poll();

            Set<State> next = stepper.step(current);

            System.out.println("Generated: " + next.size());

            queue.addAll(next);

            depthCounter--;
            if(depthCounter == 0) {
                depth++;
                depthCounter = queue.size();
            }
        }
    }
}
