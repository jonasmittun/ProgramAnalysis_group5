package Week4;

import org.json.JSONObject;

import java.util.*;

public class Interpreter {

    private Map<String, JSONObject> methods;    // Map of every method

    private Map<String, Object> mu; // Memory
    private Stack<Method> psi;      // Method Stack

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
     * sigma:   Operational stack
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
                case "push" -> {
                    JSONObject value = instruction.getJSONObject("value");
                    m.sigma.push(value);
                    psi.push(new Method(m.lambda, m.sigma, new Pair<>(m.iota.e1, m.iota.e2 + 1)));
                }
                case "return" -> {
                    JSONObject value = m.sigma.pop();

                    switch(instruction.getString("type")) {
                        case "int"      -> value.put("value", value.getInt("value"));
                        case "long"     -> value.put("value", value.getLong("value"));
                        case "float"    -> value.put("value", value.getFloat("value"));
                        case "double"   -> value.put("value", value.getDouble("value"));
                        case "ref"      -> value.put("value", value.getJSONObject("value"));
                    }

                    if(!psi.isEmpty()) {
                        Method m2 = psi.peek();
                        m2.sigma.push(value);
                    } else {
                        System.out.println("Returned " + value);
                        return;
                    }
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
                    int index = instruction.getInt("index");
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
                    String from = instruction.getString("from"); // "int" | arith
                    String to = instruction.getString("to"); // smalls | arith
                    JSONObject value = m.sigma.pop();
                    JSONObject result = new JSONObject();
                    result.put("from", from);
                    result.put("to", to);
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
                                case "long"     -> result.put("value", (long) value.getFloat("value"));
                                case "int"      -> result.put("value", (int) value.getFloat("value"));
                                case "double"   -> result.put("value", (double) value.getFloat("value"));
                                default         -> System.out.println("Unsupported cast target for float type");
                            }
                        }
                        case "double" -> {
                            switch (to) {
                                case "long"     -> result.put("value", (long) value.getDouble("value"));
                                case "float"    -> result.put("value", (float) value.getDouble("value"));
                                case "int"      -> result.put("value", (int) value.getDouble("value"));
                                default         -> System.out.println("Unsupported cast target for double type");
                            }
                        }
                        default -> {
                            System.out.println("Casting from this type is unsupported");
                        }
                    }

                    m.sigma.push(result);
                    psi.push(new Method(m.lambda, m.sigma, new Pair<>(m.iota.e1, m.iota.e2 + 1)));
                }
                case "comparelongs" -> {
                    JSONObject value1 = m.sigma.pop();
                    JSONObject value2 = m.sigma.pop();

                    long v1 = value1.getLong("value");
                    long v2 = value2.getLong("value");

                    JSONObject result = new JSONObject();
                    result.put("type", "int");
                    result.put("value", (Long.compare(v1, v2)));

                    m.sigma.push(result);
                    psi.push(new Method(m.lambda, m.sigma, new Pair<>(m.iota.e1, m.iota.e2 + 1)));
                }
                case "comparefloating" -> {
                    JSONObject value1 = m.sigma.pop();
                    JSONObject value2 = m.sigma.pop();

                    float v1 = value1.getFloat("value");
                    float v2 = value2.getFloat("value");

                    JSONObject result = new JSONObject();
                    result.put("type", "int");
                    result.put("value", (Float.compare(v1, v2)));

                    m.sigma.push(result);
                    psi.push(new Method(m.lambda, m.sigma, new Pair<>(m.iota.e1, m.iota.e2 + 1)));
                }
                case "if" -> { // TODO: Is it correct to compare two elements in sigma?
                    int target = instruction.getInt("target");

                    JSONObject value1 = m.sigma.pop();
                    JSONObject value2 = m.sigma.pop();
                    int v1 = value1.getInt("value");
                    int v2 = value2.getInt("value");

                    boolean result = switch(instruction.getString("condition")) {
                        case "eq"       -> v1 == v2;
                        case "ne"       -> v1 != v2;
                        case "le"       -> v1 <= v2;
                        case "lt"       -> v1 < v2;
                        case "ge"       -> v1 >= v2;
                        case "gt"       -> v1 > v2;
                        case "is"       -> value1.equals(value2); // Arithmetic Compare Equality?
                        case "isnot"    -> !value1.equals(value2);
                        default         -> {
                            System.out.println("Unsupported condition");
                            yield false;
                        }
                    };

                    psi.push(new Method(m.lambda, m.sigma, new Pair<>(m.iota.e1, result ? target : m.iota.e2 + 1)));
                }
                case "goto" -> {
                    psi.push(new Method(m.lambda, m.sigma, new Pair<>(m.iota.e1, instruction.getInt("target"))));
                }
                case "get" -> {
                    boolean is_static = instruction.getBoolean("static");
                    Object field = instruction.get("field");
                    // What is "value"?
                }
                default -> {
                    System.out.println("Unsupported operation");
                }
            }

            System.out.println(psi);
        }
    }
}
