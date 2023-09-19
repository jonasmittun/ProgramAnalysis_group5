package Week4;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.stream.IntStream;

public class Interpreter {

    private int program_counter;
    private Map<Integer, JSONObject> bytecode;
    private Stack<Object> operant_stack;
    private List<Object> locals_variables;

    private Map<String, JSONObject> methods;

    public Interpreter(HashMap<String, JSONObject> cls) {
        this.methods = cls; // Consider renaming variable to be more representative
        /*
        this.methods = new HashMap<>();
        JSONArray methods = cls.getJSONArray("methods");
        for(int i = 0; i < methods.length(); i++) {
            JSONObject method = methods.getJSONObject(i);
            JSONArray annotations = method.getJSONArray("annotations");
            if(IntStream.range(0, annotations.length()).noneMatch(j -> annotations.getJSONObject(j).getString("type").equals("dtu/compute/exec/Case"))) continue;
            this.methods.put(method.getString("name"), method);
        }
        */

        JSONObject m = methods.get("add");
        run(m);
    }

    private record Pair<T1, T2>(T1 e1, T2 e2) {}

    /** Method stack elements
     * lambda:  Local Variables
     * sigma:   Operational stack
     * iota:    Program Counter
     */
    private record Method(JSONObject[] lambda, Stack<JSONObject> sigma, Pair<String, Integer> iota) {}

    public void run(JSONObject method) {
        Map<String, Object> mu = new HashMap<>(); // Memory
        Stack<Method> phi = new Stack<>(); // Method Stack

        phi.push(new Method(new JSONObject[method.getJSONObject("code").getInt("max_locals")], new Stack<>(), new Pair<>(method.getString("name"), 0)));
        while(!phi.isEmpty()) {
            Method m = phi.pop();

            JSONObject instruction = methods.get(m.iota.e1).getJSONObject("code").getJSONArray("bytecode").getJSONObject(m.iota.e2);

            switch(instruction.getString("opr")) {
                case "push" -> {
                    JSONObject value = instruction.getJSONObject("value");
                    m.sigma.push(value);
                    phi.push(new Method(m.lambda, m.sigma, new Pair<>(m.iota.e1, m.iota.e2 + 1)));
                }
                case "return" -> {
                    return;
                }
                case "load" -> {
                    int index = instruction.getInt("index");
                    m.sigma.push(m.lambda[index]);
                    phi.push(new Method(m.lambda, m.sigma, new Pair<>(m.iota.e1, m.iota.e2 + 1)));
                }
                case "store" -> {
                    int index = instruction.getInt("index");
                    m.lambda[index] = instruction.getJSONObject("value");
                    phi.push(new Method(m.lambda, m.sigma, new Pair<>(m.iota.e1, m.iota.e2 + 1)));
                }
                case "binary" -> {
                    String type = instruction.getString("type"); // Arithmetic Type
                    JSONObject value1 = m.sigma.pop();
                    JSONObject value2 = m.sigma.pop();

                    JSONObject result = new JSONObject();
                    result.append("type", type);

                    switch(instruction.getString("operant")) {
                        case "add" -> {
                            switch(type) {
                                case "int"      -> result.append("value", value1.getInt("value") + value2.getInt("value"));
                                case "long"     -> result.append("value", value1.getLong("value") + value2.getLong("value"));
                                case "float"    -> result.append("value", value1.getFloat("value") + value2.getFloat("value"));
                                case "double"   -> result.append("value", value1.getDouble("value") + value2.getDouble("value"));
                            }
                        }
                        case "sub" -> {
                            switch(type) {
                                case "int"      -> result.append("value", value1.getInt("value") - value2.getInt("value"));
                                case "long"     -> result.append("value", value1.getLong("value") - value2.getLong("value"));
                                case "float"    -> result.append("value", value1.getFloat("value") - value2.getFloat("value"));
                                case "double"   -> result.append("value", value1.getDouble("value") - value2.getDouble("value"));
                            }
                        }
                        case "mul" -> {
                            switch(type) {
                                case "int"      -> result.append("value", value1.getInt("value") * value2.getInt("value"));
                                case "long"     -> result.append("value", value1.getLong("value") * value2.getLong("value"));
                                case "float"    -> result.append("value", value1.getFloat("value") * value2.getFloat("value"));
                                case "double"   -> result.append("value", value1.getDouble("value") * value2.getDouble("value"));
                            }
                        }
                        case "div" -> {
                            switch(type) {
                                case "int"      -> result.append("value", value1.getInt("value") / value2.getInt("value"));
                                case "long"     -> result.append("value", value1.getLong("value") / value2.getLong("value"));
                                case "float"    -> result.append("value", value1.getFloat("value") / value2.getFloat("value"));
                                case "double"   -> result.append("value", value1.getDouble("value") / value2.getDouble("value"));
                            }
                        }
                    }
                    m.sigma.push(result);
                    phi.push(new Method(m.lambda, m.sigma, new Pair<>(m.iota.e1, m.iota.e2 + 1)));
                }
                case "negate" -> {
                    String type = instruction.getString("type"); // Arithmetic Type
                    int index = instruction.getInt("index");
                    JSONObject value1 = m.sigma.pop();
                    JSONObject result = new JSONObject();
                    result.append("type", type);
                    switch(type) {
                        case "int"      -> result.append("value", value1.getInt("value") * -1);
                        case "long"     -> result.append("value", value1.getLong("value") * -1L);
                        case "float"    -> result.append("value", value1.getFloat("value") * -1.f);
                        case "double"   -> result.append("value", value1.getDouble("value") * -1.d);
                    }
                    m.sigma.push(result);
                    phi.push(new Method(m.lambda, m.sigma, new Pair<>(m.iota.e1, m.iota.e2 + 1)));
                }
                case "bitopr" -> {
                    String type = instruction.getString("type"); // "int" | "long"
                    JSONObject value = m.sigma.pop();

                    JSONObject result = new JSONObject();
                    result.append("type", type);

                    switch(instruction.getString("operant")) {
                        case "shl"  -> {
                            switch(type) {
                                case "int"  -> result.append("value", value.getInt("value") << 1);
                                case "long" -> result.append("value", value.getLong("value") << 1);
                            }
                        }
                        case "shr"  -> {
                            switch(type) {
                                case "int"  -> result.append("value", value.getInt("value") >> 1);
                                case "long" -> result.append("value", value.getLong("value") >> 1);
                            }
                        }
                        case "ushr" -> {
                            switch(type) {
                                case "int"  -> result.append("value", value.getInt("value") >>> 1);
                                case "long" -> result.append("value", value.getLong("value") >>> 1);
                            }
                        }
                        case "and"  -> {
                            switch(type) {
                                case "int"  -> {
                                    int n = value.getInt("value");
                                    result.append("value", ((n+1) & n) == 0 && (n!=0));
                                }
                                case "long" -> {
                                    long n = value.getLong("value");
                                    result.append("value", ((n+1) & n) == 0 && (n!=0));
                                }
                            }
                        }
                        case "or"   -> {
                            switch(type) {
                                case "int" -> {
                                    int n = value.getInt("value");
                                    result.append("value", (n & ~(n & -n)) > 0);
                                }
                                case "long" -> {
                                    long n = value.getInt("value");
                                    result.append("value", (n & ~(n & -n)) > 0);
                                }
                            }
                        }
                        case "xor"  -> {
                            switch(type) {
                                case "int" -> {
                                    int n = value.getInt("value");
                                    result.append("value", (n & ~(n & -n)) == 0);
                                }
                                case "long" -> {
                                    long n = value.getInt("value");
                                    result.append("value", (n & ~(n & -n)) == 0);
                                }
                            }
                        }
                    }
                    m.sigma.push(result);
                    phi.push(new Method(m.lambda, m.sigma, new Pair<>(m.iota.e1, m.iota.e2 + 1)));
                }
                case "cast" -> {
                    String from = instruction.getString("from"); // "int" | arith
                    String to = instruction.getString("to"); // smalls | arith
                    JSONObject value = m.sigma.pop();
                    JSONObject result = new JSONObject();
                    result.append("from", from);
                    result.append("to", to);
                    switch (from) {
                        case "int" -> {
                            switch (to) {
                                case "byte"     -> result.append("value", (byte) value.getInt("value"));
                                case "char"     -> result.append("value", (char) value.getInt("value"));
                                case "short"    -> result.append("value", (short) value.getInt("value"));
                                case "long"     -> result.append("value", (long) value.getInt("value"));
                                case "float"    -> result.append("value", (float) value.getInt("value"));
                                case "double"   -> result.append("value", (double) value.getInt("value"));
                            }
                        }
                        case "long" -> {
                            switch (to) {
                                case "int"      -> result.append("value", (int) value.getLong("value"));
                                case "float"    -> result.append("value", (float) value.getLong("value"));
                                case "double"   -> result.append("value", (double) value.getLong("value"));
                                default         -> System.out.println("Unsupported cast target for long type");
                            }
                        }
                        case "float" -> {
                            switch (to) {
                                case "long"     -> result.append("value", (long) value.getFloat("value"));
                                case "int"      -> result.append("value", (int) value.getFloat("value"));
                                case "double"   -> result.append("value", (double) value.getFloat("value"));
                                default         -> System.out.println("Unsupported cast target for float type");
                            }
                        }
                        case "double" -> {
                            switch (to) {
                                case "long"     -> result.append("value", (long) value.getDouble("value"));
                                case "float"    -> result.append("value", (float) value.getDouble("value"));
                                case "int"      -> result.append("value", (int) value.getDouble("value"));
                                default         -> System.out.println("Unsupported cast target for double type");
                            }
                        }
                        default -> {
                            System.out.println("Casting from this type is unsupported");
                        }
                    }

                    m.sigma.push(result);
                    phi.push(new Method(m.lambda, m.sigma, new Pair<>(m.iota.e1, m.iota.e2 + 1)));
                }
                case "comparelongs" -> {
                    JSONObject value1 = m.sigma.pop();
                    JSONObject value2 = m.sigma.pop();

                    long v1 = value1.getLong("value");
                    long v2 = value2.getLong("value");

                    JSONObject result = new JSONObject();
                    result.append("type", "int");
                    result.append("value", (Long.compare(v1, v2)));

                    m.sigma.push(result);
                    phi.push(new Method(m.lambda, m.sigma, new Pair<>(m.iota.e1, m.iota.e2 + 1)));
                }
                case "comparefloating" -> {
                    JSONObject value1 = m.sigma.pop();
                    JSONObject value2 = m.sigma.pop();

                    float v1 = value1.getFloat("value");
                    float v2 = value2.getFloat("value");

                    JSONObject result = new JSONObject();
                    result.append("type", "int");
                    result.append("value", (Float.compare(v1, v2)));

                    m.sigma.push(result);
                    phi.push(new Method(m.lambda, m.sigma, new Pair<>(m.iota.e1, m.iota.e2 + 1)));
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
                        default         -> false;
                    };

                    phi.push(new Method(m.lambda, m.sigma, new Pair<>(m.iota.e1, result ? target : m.iota.e2 + 1)));
                }
                default -> {
                    System.out.println("Unsupported operation");
                }
            }

            System.out.println();
        }
    }
}
