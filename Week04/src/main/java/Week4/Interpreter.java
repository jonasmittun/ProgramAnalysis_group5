package Week4;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

public class Interpreter {

    private Map<String, JSONObject> methods;    // Map of every method

    private Map<Integer, JSONObject> mu;    // Memory
    private Stack<Method> psi;              // Method Stack

    public Interpreter(HashMap<String, JSONObject> methods) {
        this.methods = methods;

        mu = new HashMap<>();
        psi = new Stack<>();

        // Example run of "add" method
        JSONObject m = methods.get("add");

        JSONObject[] lambda = new JSONObject[m.getJSONObject("code").getInt("max_locals")];

        JSONObject v1 = new JSONObject();
        v1.put("type", "int");
        v1.put("value", 1);

        JSONObject v2 = new JSONObject();
        v2.put("type", "int");
        v2.put("value", 2);

        lambda[0] = v1;
        lambda[1] = v2;

        psi.push(new Method(lambda, new Stack<>(), new Pair<>(m.getString("name"), 0)));
        run();
    }

    private record Pair<T1, T2>(T1 e1, T2 e2) {
        @Override
        public String toString() {
            return "(" + e1.toString() + ", " + e2.toString() + ")";
        }
    }

    /** Method stack element:
     * lambda:  Local Variables
     * sigma:   Operand Stack
     * iota:    Program Counter
     */
    public record Method(JSONObject[] lambda, Stack<JSONObject> sigma, Pair<String, Integer> iota) {
        @Override
        public String toString() {
            return "(λ" + Arrays.toString(lambda) + ", σ" + sigma + ", ι" + iota.toString() + ")";
        }
    }

    public void run() {
        System.out.println(psi);
        while(!psi.isEmpty()) {
            Method m = psi.pop();

            JSONObject instruction = methods.get(m.iota.e1).getJSONObject("code").getJSONArray("bytecode").getJSONObject(m.iota.e2);

            switch(instruction.getString("opr")) {
                case "array_load" -> {
                    JSONObject index = m.sigma.pop();
                    JSONObject ref = m.sigma.pop();

                    JSONObject actual = mu.get(System.identityHashCode(ref));
                    JSONArray array = actual.getJSONArray("value");
                    JSONObject value = array.getJSONObject(index.getInt("value"));

                    m.sigma.push(value);
                    psi.push(new Method(m.lambda, m.sigma, new Pair<>(m.iota.e1, m.iota.e2 + 1)));
                }
                case "array_store" -> {
                    JSONObject value = m.sigma.pop();
                    JSONObject index = m.sigma.pop();
                    JSONObject ref = m.sigma.pop();

                    JSONObject actual = mu.get(System.identityHashCode(ref));
                    JSONArray array = actual.getJSONArray("value");
                    array.put(index.getInt("value"), value);
                }
                case "push" -> {
                    JSONObject value = instruction.getJSONObject("value");
                    m.sigma.push(value);
                    psi.push(new Method(m.lambda, m.sigma, new Pair<>(m.iota.e1, m.iota.e2 + 1)));
                }
                case "load" -> {
                    int index = instruction.getInt("index");
                    m.sigma.push(m.lambda[index]);
                    psi.push(new Method(m.lambda, m.sigma, new Pair<>(m.iota.e1, m.iota.e2 + 1)));
                }
                case "store" -> {
                    int index = instruction.getInt("index");
                    m.lambda[index] = instruction.getJSONObject("value");
                    psi.push(new Method(m.lambda, m.sigma, new Pair<>(m.iota.e1, m.iota.e2 + 1)));
                }
                case "binary" -> {
                    String type = instruction.getString("type"); // Arithmetic Type
                    JSONObject value1 = m.sigma.pop();
                    JSONObject value2 = m.sigma.pop();

                    JSONObject result = new JSONObject();
                    result.put("type", type);

                    switch(instruction.getString("operant")) {
                        case "add" -> {
                            switch(type) {
                                case "int"      -> result.put("value", value1.getInt("value") + value2.getInt("value"));
                                case "long"     -> result.put("value", value1.getLong("value") + value2.getLong("value"));
                                case "float"    -> result.put("value", value1.getFloat("value") + value2.getFloat("value"));
                                case "double"   -> result.put("value", value1.getDouble("value") + value2.getDouble("value"));
                            }
                        }
                        case "sub" -> {
                            switch(type) {
                                case "int"      -> result.put("value", value1.getInt("value") - value2.getInt("value"));
                                case "long"     -> result.put("value", value1.getLong("value") - value2.getLong("value"));
                                case "float"    -> result.put("value", value1.getFloat("value") - value2.getFloat("value"));
                                case "double"   -> result.put("value", value1.getDouble("value") - value2.getDouble("value"));
                            }
                        }
                        case "mul" -> {
                            switch(type) {
                                case "int"      -> result.put("value", value1.getInt("value") * value2.getInt("value"));
                                case "long"     -> result.put("value", value1.getLong("value") * value2.getLong("value"));
                                case "float"    -> result.put("value", value1.getFloat("value") * value2.getFloat("value"));
                                case "double"   -> result.put("value", value1.getDouble("value") * value2.getDouble("value"));
                            }
                        }
                        case "div" -> {
                            switch(type) {
                                case "int"      -> result.put("value", value1.getInt("value") / value2.getInt("value"));
                                case "long"     -> result.put("value", value1.getLong("value") / value2.getLong("value"));
                                case "float"    -> result.put("value", value1.getFloat("value") / value2.getFloat("value"));
                                case "double"   -> result.put("value", value1.getDouble("value") / value2.getDouble("value"));
                            }
                        }
                    }
                    m.sigma.push(result);
                    psi.push(new Method(m.lambda, m.sigma, new Pair<>(m.iota.e1, m.iota.e2 + 1)));
                }
                case "negate" -> {
                    String type = instruction.getString("type"); // Arithmetic Type
                    JSONObject value1 = m.sigma.pop();
                    JSONObject result = new JSONObject();
                    result.put("type", type);
                    switch(type) {
                        case "int"      -> result.put("value", value1.getInt("value") * -1);
                        case "long"     -> result.put("value", value1.getLong("value") * -1L);
                        case "float"    -> result.put("value", value1.getFloat("value") * -1.f);
                        case "double"   -> result.put("value", value1.getDouble("value") * -1.d);
                    }
                    m.sigma.push(result);
                    psi.push(new Method(m.lambda, m.sigma, new Pair<>(m.iota.e1, m.iota.e2 + 1)));
                }
                case "bitopr" -> {
                    String type = instruction.getString("type"); // "int" | "long"
                    JSONObject value = m.sigma.pop();

                    JSONObject result = new JSONObject();
                    result.put("type", type);

                    switch(instruction.getString("operant")) {
                        case "shl"  -> {
                            switch(type) {
                                case "int"  -> result.put("value", value.getInt("value") << 1);
                                case "long" -> result.put("value", value.getLong("value") << 1);
                            }
                        }
                        case "shr"  -> {
                            switch(type) {
                                case "int"  -> result.put("value", value.getInt("value") >> 1);
                                case "long" -> result.put("value", value.getLong("value") >> 1);
                            }
                        }
                        case "ushr" -> {
                            switch(type) {
                                case "int"  -> result.put("value", value.getInt("value") >>> 1);
                                case "long" -> result.put("value", value.getLong("value") >>> 1);
                            }
                        }
                        case "and"  -> {
                            switch(type) {
                                case "int"  -> {
                                    int n = value.getInt("value");
                                    result.put("value", ((n+1) & n) == 0 && (n!=0));
                                }
                                case "long" -> {
                                    long n = value.getLong("value");
                                    result.put("value", ((n+1) & n) == 0 && (n!=0));
                                }
                            }
                        }
                        case "or"   -> {
                            switch(type) {
                                case "int" -> {
                                    int n = value.getInt("value");
                                    result.put("value", (n & ~(n & -n)) > 0);
                                }
                                case "long" -> {
                                    long n = value.getInt("value");
                                    result.put("value", (n & ~(n & -n)) > 0);
                                }
                            }
                        }
                        case "xor"  -> {
                            switch(type) {
                                case "int" -> {
                                    int n = value.getInt("value");
                                    result.put("value", (n & ~(n & -n)) == 0);
                                }
                                case "long" -> {
                                    long n = value.getInt("value");
                                    result.put("value", (n & ~(n & -n)) == 0);
                                }
                            }
                        }
                    }
                    m.sigma.push(result);
                    psi.push(new Method(m.lambda, m.sigma, new Pair<>(m.iota.e1, m.iota.e2 + 1)));
                }
                case "cast" -> {
                    String from = instruction.getString("from");    // "int" | Arithmetic Type
                    String to = instruction.getString("to");        // Small types | Arithmetic Type

                    JSONObject value = m.sigma.pop();

                    JSONObject result = new JSONObject();
                    result.put("type", to);

                    switch (from) {
                        case "int" -> {
                            switch (to) {
                                case "byte"     -> result.put("value", (byte) value.getInt("value"));
                                case "char"     -> result.put("value", (char) value.getInt("value"));
                                case "short"    -> result.put("value", (short) value.getInt("value"));
                                case "long"     -> result.put("value", (long) value.getInt("value"));
                                case "float"    -> result.put("value", (float) value.getInt("value"));
                                case "double"   -> result.put("value", (double) value.getInt("value"));
                            }
                        }
                        case "long" -> {
                            switch (to) {
                                case "int"      -> result.put("value", (int) value.getLong("value"));
                                case "float"    -> result.put("value", (float) value.getLong("value"));
                                case "double"   -> result.put("value", (double) value.getLong("value"));
                                default         -> System.out.println("Unsupported cast target for long type");
                            }
                        }
                        case "float" -> {
                            switch (to) {
                                case "int"      -> result.put("value", (int) value.getFloat("value"));
                                case "long"     -> result.put("value", (long) value.getFloat("value"));
                                case "double"   -> result.put("value", (double) value.getFloat("value"));
                                default         -> System.out.println("Unsupported cast target for float type");
                            }
                        }
                        case "double" -> {
                            switch (to) {
                                case "int"      -> result.put("value", (int) value.getDouble("value"));
                                case "long"     -> result.put("value", (long) value.getDouble("value"));
                                case "float"    -> result.put("value", (float) value.getDouble("value"));
                                default         -> System.out.println("Unsupported cast target for double type");
                            }
                        }
                        default -> System.out.println("Casting from this type is unsupported");
                    }

                    m.sigma.push(result);
                    psi.push(new Method(m.lambda, m.sigma, new Pair<>(m.iota.e1, m.iota.e2 + 1)));
                }
                case "comparelongs" -> {
                    JSONObject value1 = m.sigma.pop();
                    JSONObject value2 = m.sigma.pop();

                    JSONObject result = new JSONObject();
                    result.put("type", "int");
                    result.put("value", (Long.compare(value1.getLong("value"), value2.getLong("value"))));

                    m.sigma.push(result);
                    psi.push(new Method(m.lambda, m.sigma, new Pair<>(m.iota.e1, m.iota.e2 + 1)));
                }
                case "comparefloating" -> {
                    JSONObject value1 = m.sigma.pop();
                    JSONObject value2 = m.sigma.pop();

                    JSONObject result = new JSONObject();
                    result.put("type", "int");
                    switch(instruction.getString("type")) {
                        case "float" -> {
                            try {
                                float v1 = value1.getFloat("value");
                                float v2 = value2.getFloat("value");
                                result.put("value", (Float.compare(v1, v2)));
                            } catch(JSONException e) {
                                result.put("value", instruction.getInt("onnan"));
                            }
                        }
                        case "double" -> {
                            try {
                                double v1 = value1.getDouble("value");
                                double v2 = value2.getDouble("value");
                                result.put("value", (Double.compare(v1, v2)));
                            } catch(JSONException e) {
                                result.put("value", instruction.getInt("onnan"));
                            }
                        }
                    }

                    m.sigma.push(result);
                    psi.push(new Method(m.lambda, m.sigma, new Pair<>(m.iota.e1, m.iota.e2 + 1)));
                }
                case "if" -> {
                    int target = instruction.getInt("target");

                    JSONObject value1 = m.sigma.pop();
                    JSONObject value2 = m.sigma.pop();
                    String type1 = value1.getString("type");
                    String type2 = value2.getString("type");

                    if(!type1.equals(type2)) {
                        System.out.println("Type mismatch in comparison: " + type1  + " != " + type2);
                        return;
                    }

                    boolean result = switch(instruction.getString("condition")) {
                        case "eq"       -> value1.getInt("value") == value2.getInt("value");
                        case "ne"       -> value1.getInt("value") != value2.getInt("value");
                        case "le"       -> value1.getInt("value") <= value2.getInt("value");
                        case "lt"       -> value1.getInt("value") < value2.getInt("value");
                        case "ge"       -> value1.getInt("value") >= value2.getInt("value");
                        case "gt"       -> value1.getInt("value") > value2.getInt("value");
                        case "is"       -> mu.get(System.identityHashCode(value1)).equals(mu.get(System.identityHashCode(value2)));
                        case "isnot"    -> !mu.get(System.identityHashCode(value1)).equals(mu.get(System.identityHashCode(value2)));
                        default         -> {
                            System.out.println("Unsupported condition");
                            yield false;
                        }
                    };

                    psi.push(new Method(m.lambda, m.sigma, new Pair<>(m.iota.e1, result ? target : m.iota.e2 + 1)));
                }
                case "ifz" -> {
                    String condition = instruction.getString("condition");
                    int target = instruction.getInt("target");

                    JSONObject value = m.sigma.pop();

                    boolean result;
                    switch(value.getString("type")) {
                        case "int" -> {
                            int v = value.getInt("value");
                            result = switch(condition) {
                                case "eq"       -> v == 0;
                                case "ne"       -> v != 0;
                                case "le"       -> v <= 0;
                                case "lt"       -> v < 0;
                                case "ge"       -> v >= 0;
                                case "gt"       -> v > 0;
                                default         -> {
                                    System.out.println("Unsupported condition in \"int\"");
                                    yield false;
                                }
                            };
                        }
                        case "ref" -> {
                            JSONObject v = mu.get(System.identityHashCode(value));
                            result = switch(condition) {
                                case "is"       -> v == null;
                                case "isnot"    -> v != null;
                                default         -> {
                                    System.out.println("Unsupported condition in \"ref\"");
                                    yield false;
                                }
                            };
                        }
                        default -> {
                            System.out.println("Unsupported type");
                            result = false;
                        }
                    }

                    psi.push(new Method(m.lambda, m.sigma, new Pair<>(m.iota.e1, result ? target : m.iota.e2 + 1)));
                }
                case "goto" -> {
                    psi.push(new Method(m.lambda, m.sigma, new Pair<>(m.iota.e1, instruction.getInt("target"))));
                }
                case "jsr" -> {
                    int target = instruction.getInt("target");

                    Method next = psi.peek();
                    JSONObject value = new JSONObject();
                    value.put("type", "int");
                    value.put("value", next.iota.e2);
                    m.sigma.push(value);

                    psi.push(new Method(m.lambda, m.sigma, new Pair<>(m.iota.e1, target)));
                }
                case "ret" -> {
                    JSONObject value = m.sigma.pop();
                    int address = value.getInt("value");

                    psi.push(new Method(m.lambda, m.sigma, new Pair<>(m.iota.e1, address)));
                }
                case "tableswitch" -> {
                    int location = instruction.getInt("default");
                    int low = instruction.getInt("low");

                    JSONObject value = m.sigma.pop();
                    int index = value.getInt("index");

                    JSONArray targets = instruction.getJSONArray("targets");
                    for(int i = 0; i < targets.length(); i++) {
                        int target = targets.getInt(i);
                        if(target == index) {
                            location = index - low;
                            break;
                        }
                    }

                    psi.push(new Method(m.lambda, m.sigma, new Pair<>(m.iota.e1, location)));
                }
                case "lookupswitch" -> {
                    int location = instruction.getInt("default");

                    JSONObject value = m.sigma.pop();
                    int index = value.getInt("index");

                    JSONArray targets = instruction.getJSONArray("targets");
                    for(int i = 0; i < targets.length(); i++) {
                        JSONObject target = targets.getJSONObject(i);
                        if(target.getInt("key") == index) {
                            location = target.getInt("target");
                            break;
                        }
                    }

                    psi.push(new Method(m.lambda, m.sigma, new Pair<>(m.iota.e1, location)));
                }
                case "get" -> {
                    boolean is_static = instruction.getBoolean("static");
                    JSONObject field = instruction.getJSONObject("field");

                    JSONObject ref = m.sigma.pop();

                    JSONObject object = mu.get(System.identityHashCode(ref));

                }
                case "newarray" -> {
                    int dim = instruction.getInt("dim"); // Recurse / While-loop magic
                    String type = instruction.getString("type");

                    JSONObject value_length = m.sigma.pop();
                    int length = value_length.getInt("value");

                    // Create value
                    JSONObject result = new JSONObject();
                    result.put("type", type);
                    JSONArray value = new JSONArray(length);
                    for(int i = 0; i < length; i++) {
                        JSONObject value_inner = new JSONObject();
                        value_inner.put("type", type);
                        value_inner.put("value", 0); // Set default value?
                        value.put(i, value_inner);
                    }
                    result.put("value", value);

                    // Create reference to value
                    JSONObject ref = new JSONObject();
                    ref.put("type", "ref");
                    ref.put("kind", "array");

                    mu.put(System.identityHashCode(ref), result);
                    m.sigma.push(ref);

                    psi.push(new Method(m.lambda, m.sigma, new Pair<>(m.iota.e1, m.iota.e2 + 1)));
                }
                case "arraylength" -> {
                    JSONObject ref = m.sigma.pop();

                    JSONObject array = mu.get(System.identityHashCode(ref));

                    JSONObject result = new JSONObject();
                    result.put("type", "int");
                    result.put("value", array.getJSONArray("value").length());

                    m.sigma.push(result);

                    psi.push(new Method(m.lambda, m.sigma, new Pair<>(m.iota.e1, m.iota.e2 + 1)));
                }
                case "return" -> {
                    if(instruction.isNull("type")) continue;

                    String type = instruction.getString("type");
                    JSONObject value = m.sigma.pop();

                    JSONObject result = new JSONObject();
                    result.put("type", type);
                    switch(type) {
                        case "int"      -> result.put("value", value.getInt("value"));
                        case "long"     -> result.put("value", value.getLong("value"));
                        case "float"    -> result.put("value", value.getFloat("value"));
                        case "double"   -> result.put("value", value.getDouble("value"));
                        case "ref"      -> result.put("value", value.getJSONObject("value"));
                    }

                    if(!psi.isEmpty()) {
                        Method m2 = psi.peek();
                        m2.sigma.push(result);
                    } else {
                        System.out.println("Returned " + result);
                        return;
                    }
                }
                case "nop" -> {
                    psi.push(new Method(m.lambda, m.sigma, new Pair<>(m.iota.e1, m.iota.e2 + 1)));
                }
                case "pop" -> {
                    int words = instruction.getInt("words");

                    while(!m.sigma.empty() && words > 0) {
                        m.sigma.pop();
                        words--;
                    }

                    psi.push(new Method(m.lambda, m.sigma, new Pair<>(m.iota.e1, m.iota.e2 + 1)));
                }
                case "dup" -> {
                    int words = instruction.getInt("words");

                    List<JSONObject> local = new ArrayList<>();
                    for(int i = 0; i < words; i++) {
                        if(m.sigma.isEmpty()) {
                            System.out.println("Duplication ended early");
                            break;
                        }
                        local.add(m.sigma.pop());
                    }

                    for(int i = 0; i < words; i++) {
                        for(JSONObject jsonObject : local) {
                            m.sigma.push(jsonObject);
                        }
                    }

                    psi.push(new Method(m.lambda, m.sigma, new Pair<>(m.iota.e1, m.iota.e2 + 1)));
                }
                case "swap" -> {
                    JSONObject value1 = m.sigma.pop();
                    JSONObject value2 = m.sigma.pop();

                    m.sigma.push(value1);
                    m.sigma.push(value2);

                    psi.push(new Method(m.lambda, m.sigma, new Pair<>(m.iota.e1, m.iota.e2 + 1)));
                }
                default -> {
                    System.out.println("Unsupported operation");
                }
            }

            System.out.println(psi);
        }
    }
}
