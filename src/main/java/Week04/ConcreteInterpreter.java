package Week04;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.module.ResolutionException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ConcreteInterpreter {

    private final Map<String, JSONObject> classes;                      // Map<Classname, JSONObject>
    private final Map<String, Map<String, JSONObject>> class_methods;   // Map<Classname, Map<Methodname, JSONObject>> // TODO: Fix overwrite if two methods have the same name

    public ConcreteInterpreter(Map<String, JSONObject> classes) {
        this.classes = classes;

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
    }

    /** Returns the resolved method as a JSONObject
     * @param ref           SimpleReferenceType: { "kind": "class", "name": &lt;ClassName&gt; }
     * @param name          String
     * @param args          SimpleType[]
     * @param return_type   nullable SimpleType
     * @return              The method - if correctly resolved
     */
    public JSONObject resolveMethod(JSONObject ref, String name, JSONArray args, Object return_type) {
        if(!ref.has("name")) throw new IllegalArgumentException("Incorrect reference type!");
        if(!classes.containsKey(ref.getString("name"))) throw new ResolutionException("Class " + ref.getString("name") + " not found!");
        JSONObject cls = classes.get(ref.getString("name"));

        JSONArray methods = cls.getJSONArray("methods");
        compare:
        for(int i = 0; i < methods.length(); i++) {
            JSONObject method = methods.getJSONObject(i);

            //System.out.println(method.getString("name") + " " + method.getJSONArray("params") + " → " + method.getJSONObject("returns"));

            // Check Name
            String method_name = method.getString("name");
            if(!method_name.equals(name)) continue;

            // Check Return Type
            JSONObject method_returns = method.getJSONObject("returns");
            JSONObject method_return_type = method_returns.isNull("type") ? null : method_returns.getJSONObject("type"); // <Type>
            if(!typeIsEqual(method_return_type, return_type)) continue;

            // Check Arguments
            JSONArray method_params = method.getJSONArray("params");
            if(args.length() != method_params.length()) continue;
            for(int j = 0; j < method_params.length(); j++) {
                JSONObject method_param = method_params.getJSONObject(j);
                if(!typeIsEqual(method_param.getJSONObject("type"), args.get(j))) continue compare;
                // TODO: Verify that the order of elements is the same
            }

            return method;
        }

        throw new ResolutionException(ref.getString("name") + "." + name + " " + args + " → " + return_type + " not found!");
    }

    /** Checks if the &lt;Type&gt; is equal to the &lt;SimpleType&gt; */
    public boolean typeIsEqual(JSONObject Type, Object SimpleType) {
        if(Type == null && SimpleType == null) {
            return true;
        } else if(Type != null && SimpleType != null) {
            if(SimpleType instanceof String BaseType) {
                if(!Type.has("base")) return false;
                else return BaseType.equals(Type.getString("base"));
            } else {
                if(!(SimpleType instanceof JSONObject SimpleReferenceType) || !Type.has("kind")) return false;

                return switch(Type.getString("kind")) {
                    case "class" -> {
                        if(!SimpleReferenceType.getString("kind").equals("class")) yield false;
                        else yield SimpleReferenceType.getString("name").equals(Type.getString("name"));
                    }
                    case "array" -> {
                        if(!SimpleReferenceType.getString("kind").equals("array")) yield false;
                        else yield typeIsEqual(Type.getJSONObject("type"), SimpleReferenceType.get("type"));
                    }
                    default -> false;
                };
            }
        } else {
            return false;
        }
    }

    /** Initializes a new object of some class
     * <pre>If class extends some superclass, then that can be fetched from memory by using this object as objectref.</pre>
     * @param classname The name of the class to be instantiated.
     * @param mu        The memory.
     * @return          A reference to the initialized class object
     */
    public JSONObject initialize(String classname, Map<Integer, JSONObject> mu) {
        if(!classes.containsKey(classname)) throw new InstantiationError(classname + " does not exist.");
        JSONObject cls = new JSONObject(classes.get(classname).toMap());

        // Initialize super class if it exists
        if(!cls.isNull("super")) {
            String superclassname = cls.getJSONObject("super").getString("name");

            mu.put(System.identityHashCode(cls), initialize(superclassname, mu));
        }

        return cls;
    }

    /** Tries to get field from the object
     * <pre>If the field is <code>null</code>, then the default value will be returned.</pre>
     * @param object    The object which "may" contain the field.
     * @param fieldname The name of the field.
     * @param fieldtype The type of the field (a &lt;SimpleType&gt;)
     * @param mu        The memory.
     * @return          An Optional&lt;JSONObject&gt; containing the value of the field if found, else an Optional.empty()
     */
    public static Optional<JSONObject> getField(JSONObject object, String fieldname, Object fieldtype, Map<Integer, JSONObject> mu) {
        if(!object.has("fields")) return Optional.empty();

        JSONArray fields = object.getJSONArray("fields");
        for(int i = 0; i < fields.length(); i++) {
            JSONObject f = fields.getJSONObject(i);
            if(f.getString("name").equals(fieldname)) {
                if(f.isNull("value")) {
                    return Optional.of(SimpleType.createDefault(fieldtype, mu));
                } else {
                    return Optional.of(new JSONObject(f.getJSONObject("value").toMap()));
                }
            }
        }

        return Optional.empty();
    }

    /** Tries to put a value into a field of the object.
     * @param object    The object which contains the field.
     * @param fieldname The name of the field.
     * @param fieldtype The type of the field (a &lt;SimpleType&gt;).
     * @return          True when the field has been correctly put and false when not.
     */
    public static boolean putField(JSONObject object, String fieldname, Object fieldtype, JSONObject value) {
        if(!object.has("fields")) return false;

        JSONArray fields = object.getJSONArray("fields");
        for(int i = 0; i < fields.length(); i++) {
            JSONObject f = fields.getJSONObject(i);
            if(f.getString("name").equals(fieldname)) {
                if(SimpleType.equals(f.get("type"), fieldtype)) {

                    // TODO: Check if value-type is the same as the field
                    if(value.has("kind")) {
                        f.put("value", value);

                        return true;
                    } else if(value.has("type")) {
                        switch(value.getString("type")) {
                            case "boolean"                  -> f.put("value", value.getBoolean("value"));
                            case "int", "integer"           -> f.put("value", value.getInt("value"));
                            case "long"                     -> f.put("value", value.getLong("value"));
                            case "float"                    -> f.put("value", value.getFloat("value"));
                            case "double"                   -> f.put("value", value.getDouble("value"));
                            case "short", "byte", "char"    -> f.put("value", value.get("value"));
                            default -> throw new IllegalArgumentException("Unsupported type " + f.get("type") + " in \"put\"");
                        }

                        return true;
                    } else return false;
                }
            }
        }

        return false;
    }

    public void run(Method method, Map<Integer, JSONObject> mu) {
        Deque<Method> psi = new ArrayDeque<>();  // Method Stack
        psi.push(method);

        System.out.println("Initial:\nΨ00\t" + method + "\n");
        while(!psi.isEmpty()) {
            Method m = psi.pop();

            step(m, mu, psi);
        }

        if(!mu.isEmpty()) {
            System.out.println("\nMemory (µ):");
            for(Map.Entry<Integer, JSONObject> entry : mu.entrySet()) {
                System.out.println(entry.getKey() + "\t" + entry.getValue());
            }
        }
    }

    public void step(Method m, Map<Integer, JSONObject> mu, Deque<Method> psi) {
        JSONObject instruction = m.iota().e1().getJSONObject("code").getJSONArray("bytecode").getJSONObject(m.iota().e2());
        System.out.println("Instruction: " + instruction);

        switch(instruction.getString("opr")) {
            case "array_load" -> {
                JSONObject index = m.sigma().pop();
                JSONObject arrayref = m.sigma().pop();

                if(arrayref == null) throw new NullPointerException("Cannot load from array because \"arrayref\" is null");

                JSONObject actual = mu.get(System.identityHashCode(arrayref));
                JSONArray array = actual.getJSONArray("value");

                if(array == null) throw new NullPointerException("Cannot load from array because \"array\" is null");

                int index_value = index.getInt("value");
                if(array.length() < index_value) throw new ArrayIndexOutOfBoundsException("Index " + index_value + " out of bounds for length " + array.length());

                JSONObject value = array.getJSONObject(index_value);

                m.sigma().push(value.has("kind") ? value : new JSONObject(value.toMap()));
                psi.push(new Method(m.lambda(), m.sigma(), new Pair<>(m.iota().e1(), m.iota().e2() + 1)));
            }
            case "array_store" -> {
                JSONObject value = m.sigma().pop();
                JSONObject index = m.sigma().pop();
                JSONObject arrayref = m.sigma().pop();

                if(arrayref == null) throw new NullPointerException("Cannot store to array because \"arrayref\" is null");

                JSONObject actual = mu.get(System.identityHashCode(arrayref));
                JSONArray array = actual.getJSONArray("value");

                if(array == null) throw new NullPointerException("Cannot store to array because \"array\" is null");

                int index_value = index.getInt("value");
                if(array.length() < index_value) throw new ArrayIndexOutOfBoundsException("Index " + index_value + " out of bounds for length " + array.length());

                array.put(index_value, value);
                psi.push(new Method(m.lambda(), m.sigma(), new Pair<>(m.iota().e1(), m.iota().e2() + 1)));
            }
            case "push" -> {
                JSONObject value = instruction.getJSONObject("value");
                String type = value.getString("type");

                switch(type) {
                    case "class" -> {
                        m.sigma().push(value);
                    }
                    case "string" -> {
                        // Create array reference for string value
                        JSONObject arrayref = new JSONObject(Map.of("kind", "array", "type", "byte"));
                        // Create array to hold string value as a byte[]
                        JSONObject array = new JSONObject(Map.of("type", "byte", "value", new JSONArray(value.getString("value").getBytes(StandardCharsets.UTF_8))));
                        mu.put(System.identityHashCode(arrayref), array);

                        // Create a new String object
                        JSONObject object = new JSONObject(classes.get("java/lang/String").toMap());
                        // Update "value" field in this String object to the array reference
                        object.getJSONArray("fields").getJSONObject(0).put("value", arrayref);

                        // Create String object reference
                        JSONObject objectref = new JSONObject(Map.of("kind", "class", "name", "java/lang/String"));
                        mu.put(System.identityHashCode(objectref), object);

                        // Push object reference
                        m.sigma().push(objectref);
                    }
                    default -> {
                        m.sigma().push(new JSONObject(value.toMap()));
                    }
                }

                psi.push(new Method(m.lambda(), m.sigma(), new Pair<>(m.iota().e1(), m.iota().e2() + 1)));
            }
            case "load" -> {
                int index = instruction.getInt("index");
                JSONObject value = m.lambda()[index];
                if(value.has("kind")) { // Check if it's a reference type
                   m.sigma().push(value);
                } else {
                    m.sigma().push(new JSONObject(value.toMap()));
                }
                psi.push(new Method(m.lambda(), m.sigma(), new Pair<>(m.iota().e1(), m.iota().e2() + 1)));
            }
            case "store" -> {
                int index = instruction.getInt("index");
                JSONObject value = m.sigma().pop();
                m.lambda()[index] = value;
                psi.push(new Method(m.lambda(), m.sigma(), new Pair<>(m.iota().e1(), m.iota().e2() + 1)));
            }
            case "incr" -> {
                int index = instruction.getInt("index");
                JSONObject value = m.lambda()[index];
                String type = value.getString("type");
                switch(type) {
                    case "int", "integer"   -> value.put("value", value.getInt("value") + instruction.getInt("amount"));
                    case "long"             -> value.put("value", value.getLong("value") + instruction.getLong("amount"));
                    case "float"            -> value.put("value", value.getFloat("value") + instruction.getFloat("amount"));
                    case "double"           -> value.put("value", value.getDouble("value") + instruction.getDouble("amount"));
                    default                 -> System.out.println("Unsupported \"incr\" type: " + type);
                }

                psi.push(new Method(m.lambda(), m.sigma(), new Pair<>(m.iota().e1(), m.iota().e2() + 1)));
            }
            case "binary" -> {
                String type = instruction.getString("type"); // Arithmetic Type
                JSONObject value2 = m.sigma().pop();
                JSONObject value1 = m.sigma().pop();

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
                m.sigma().push(result);
                psi.push(new Method(m.lambda(), m.sigma(), new Pair<>(m.iota().e1(), m.iota().e2() + 1)));
            }
            case "negate" -> {
                String type = instruction.getString("type"); // Arithmetic Type
                JSONObject value1 = m.sigma().pop();
                JSONObject result = new JSONObject();
                result.put("type", type);
                switch(type) {
                    case "int"      -> result.put("value", value1.getInt("value") * -1);
                    case "long"     -> result.put("value", value1.getLong("value") * -1L);
                    case "float"    -> result.put("value", value1.getFloat("value") * -1.f);
                    case "double"   -> result.put("value", value1.getDouble("value") * -1.d);
                }
                m.sigma().push(result);
                psi.push(new Method(m.lambda(), m.sigma(), new Pair<>(m.iota().e1(), m.iota().e2() + 1)));
            }
            case "bitopr" -> {
                String type = instruction.getString("type"); // "int" | "long"
                JSONObject value = m.sigma().pop();

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
                m.sigma().push(result);
                psi.push(new Method(m.lambda(), m.sigma(), new Pair<>(m.iota().e1(), m.iota().e2() + 1)));
            }
            case "cast" -> {
                String from = instruction.getString("from");    // "int" | Arithmetic Type
                String to = instruction.getString("to");        // Small types | Arithmetic Type

                JSONObject value = m.sigma().pop();

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

                m.sigma().push(result);
                psi.push(new Method(m.lambda(), m.sigma(), new Pair<>(m.iota().e1(), m.iota().e2() + 1)));
            }
            case "comparelongs" -> {
                JSONObject value2 = m.sigma().pop();
                JSONObject value1 = m.sigma().pop();

                JSONObject result = new JSONObject();
                result.put("type", "int");
                result.put("value", (Long.compare(value1.getLong("value"), value2.getLong("value"))));

                m.sigma().push(result);
                psi.push(new Method(m.lambda(), m.sigma(), new Pair<>(m.iota().e1(), m.iota().e2() + 1)));
            }
            case "comparefloating" -> {
                JSONObject value2 = m.sigma().pop();
                JSONObject value1 = m.sigma().pop();

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

                m.sigma().push(result);
                psi.push(new Method(m.lambda(), m.sigma(), new Pair<>(m.iota().e1(), m.iota().e2() + 1)));
            }
            case "if" -> {
                int target = instruction.getInt("target");

                JSONObject value2 = m.sigma().pop();
                JSONObject value1 = m.sigma().pop();

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

                psi.push(new Method(m.lambda(), m.sigma(), new Pair<>(m.iota().e1(), result ? target : m.iota().e2() + 1)));
            }
            case "ifz" -> {
                String condition = instruction.getString("condition");
                int target = instruction.getInt("target");

                JSONObject value = m.sigma().pop();

                boolean result;
                if(value.has("kind")) {
                    JSONObject v = mu.get(System.identityHashCode(value));
                    result = switch(condition) {
                        case "is"       -> v == null;
                        case "isnot"    -> v != null;
                        default  -> throw new IllegalArgumentException("Unsupported ifz condition in \"ref\": " + condition);
                    };
                } else {
                    int v = switch(value.getString("type")) {
                        case "boolean"  -> value.getBoolean("value") ? 1 : 0;
                        case "int"      -> value.getInt("value");
                        default -> throw new IllegalArgumentException("Unsupported ifz value type: " + value.get("type"));
                    };
                    result = switch(condition) {
                        case "eq" -> v == 0;
                        case "ne" -> v != 0;
                        case "le" -> v <= 0;
                        case "lt" -> v < 0;
                        case "ge" -> v >= 0;
                        case "gt" -> v > 0;
                        default -> throw new IllegalArgumentException("Unsupported ifz condition in \"int\": " + condition);
                    };
                }

                psi.push(new Method(m.lambda(), m.sigma(), new Pair<>(m.iota().e1(), result ? target : m.iota().e2() + 1)));
            }
            case "goto" -> {
                psi.push(new Method(m.lambda(), m.sigma(), new Pair<>(m.iota().e1(), instruction.getInt("target"))));
            }
            case "jsr" -> {
                int target = instruction.getInt("target");

                Method next = psi.peek();
                JSONObject value = new JSONObject();
                value.put("type", "int");
                value.put("value", next.iota().e2());
                m.sigma().push(value);

                psi.push(new Method(m.lambda(), m.sigma(), new Pair<>(m.iota().e1(), target)));
            }
            case "ret" -> {
                JSONObject value = m.sigma().pop();
                int address = value.getInt("value");

                psi.push(new Method(m.lambda(), m.sigma(), new Pair<>(m.iota().e1(), address)));
            }
            case "tableswitch" -> {
                int location = instruction.getInt("default");
                int low = instruction.getInt("low");

                JSONObject value = m.sigma().pop();
                int index = value.getInt("index");

                JSONArray targets = instruction.getJSONArray("targets");
                for(int i = 0; i < targets.length(); i++) {
                    int target = targets.getInt(i);
                    if(target == index) {
                        location = index - low;
                        break;
                    }
                }

                psi.push(new Method(m.lambda(), m.sigma(), new Pair<>(m.iota().e1(), location)));
            }
            case "lookupswitch" -> {
                int location = instruction.getInt("default");

                JSONObject value = m.sigma().pop();
                int index = value.getInt("index");

                JSONArray targets = instruction.getJSONArray("targets");
                for(int i = 0; i < targets.length(); i++) {
                    JSONObject target = targets.getJSONObject(i);
                    if(target.getInt("key") == index) {
                        location = target.getInt("target");
                        break;
                    }
                }

                psi.push(new Method(m.lambda(), m.sigma(), new Pair<>(m.iota().e1(), location)));
            }
            case "get" -> {
                JSONObject field = instruction.getJSONObject("field");
                String fieldname = field.getString("name");
                Object fieldtype = field.get("type");

                JSONObject object;
                if(instruction.getBoolean("static")) {
                    object = classes.get(field.getString("class"));
                } else {
                    JSONObject ref = m.sigma().pop();
                    object = mu.get(System.identityHashCode(ref));
                }

                Optional<JSONObject> value = Optional.empty();
                while(value.isEmpty()) {
                    value = getField(object, fieldname, fieldtype, mu);

                    if(value.isEmpty()) {
                        // Get superclass if exists
                        if(mu.containsKey(System.identityHashCode(object))) {
                            object = mu.get(System.identityHashCode(object));
                        } else break;
                    }
                }

                if(value.isEmpty()) throw new NoSuchFieldError("The field \"" + field.getString("name") + "\" does not exist.");

                m.sigma().push(value.get());
                psi.push(new Method(m.lambda(), m.sigma(), new Pair<>(m.iota().e1(), m.iota().e2() + 1)));
            }
            case "put" -> {
                JSONObject field = instruction.getJSONObject("field");
                String fieldname = field.getString("name");
                Object fieldtype = field.get("type");

                JSONObject value = m.sigma().pop();

                JSONObject object;
                if(instruction.getBoolean("static")) {
                    object = classes.get(field.getString("class"));
                } else {
                    JSONObject ref = m.sigma().pop();
                    object = mu.get(System.identityHashCode(ref));
                }

                while(!putField(object, fieldname, fieldtype, value)) {
                    // Get superclass if exists
                    if(mu.containsKey(System.identityHashCode(object))) {
                        object = mu.get(System.identityHashCode(object));
                    } else throw new NoSuchFieldError("The field \"" + field.getString("name") + "\" does not exist.");
                }

                psi.push(new Method(m.lambda(), m.sigma(), new Pair<>(m.iota().e1(), m.iota().e2() + 1)));
            }
            case "invoke" -> {
                JSONObject invoke_method = instruction.getJSONObject("method");
                String invoke_access = instruction.getString("access");

                String methodname = invoke_method.getString("name");
                JSONArray args = invoke_method.getJSONArray("args");
                Object returns = invoke_method.isNull("returns") ? null : invoke_method.get("returns");

                JSONObject resolvedMethod = switch(invoke_access) {
                    case "virtual" -> {
                        JSONObject classref = invoke_method.getJSONObject("ref");

                        yield resolveMethod(classref, methodname, args, returns);
                    }
                    case "special", "static" -> {
                        JSONObject classref = invoke_method.getJSONObject("ref");
                        boolean is_interface = invoke_method.getBoolean("is_interface");

                        yield resolveMethod(classref, methodname, args, returns);
                    }
                    case "interface" -> {
                        JSONObject classref = invoke_method.getJSONObject("ref");
                        int stack_size = invoke_method.getInt("stack_size");

                        yield resolveMethod(classref, methodname, args, returns);
                    }
                    case "dynamic" -> throw new RuntimeException("Dynamic invoke is not implemented");
                    default -> throw new IllegalArgumentException("Illegal invoke access: " + instruction.getString("access"));
                };

                if(m.sigma().size() < args.length()) throw new RuntimeException("Not enough elements in stack for invocation of method!");

                if(resolvedMethod.isNull("code")) {
                    throw new RuntimeException("Runtime method identification is not implemented");
                    // TODO: Find method in subclass / "Implement class" at runtime
                }

                JSONObject[] lambda = new JSONObject[resolvedMethod.getJSONObject("code").getInt("max_locals")];
                for(int i = 0; i < args.length(); i++) {
                    JSONObject arg = m.sigma().pop();

                    Object type_expected = args.get(i);
                    Object type_actual = arg.has("kind") ? arg : arg.getString("type");

                    if(!SimpleType.equals(type_expected, type_actual)) {
                        throw new IllegalArgumentException("Type mismatch: Expected " + type_expected + " but was " + type_actual);
                    }

                    lambda[i] = arg;
                }

                switch(invoke_access) {
                    case "virtual", "special", "interface" -> {
                        // Shift elements in lambda right
                        System.arraycopy(lambda, 0, lambda, 1, lambda.length - 1);
                        // Objectref
                        lambda[0] = m.sigma().pop();
                    }
                    default -> {}
                }

                psi.push(new Method(m.lambda(), m.sigma(), new Pair<>(m.iota().e1(), m.iota().e2() + 1)));
                psi.push(new Method(lambda, new ArrayDeque<>(), new Pair<>(resolvedMethod, 0)));
            }
            case "new" -> {
                String classname = instruction.getString("class");

                if(!classes.containsKey(classname)) throw new InstantiationError(classname + " does not exist.");
                List<Object> access = classes.get(classname).getJSONArray("access").toList();
                if(access.contains("abstract")) throw new InstantiationError(classname + " is abstract.");
                if(access.contains("interface")) throw new InstantiationError(classname + " is an interface.");

                JSONObject objectref = new JSONObject(Map.of("kind", "class", "name", classname));
                JSONObject object = initialize(classname, mu);

                mu.put(System.identityHashCode(objectref), object);

                m.sigma().push(objectref);
                psi.push(new Method(m.lambda(), m.sigma(), new Pair<>(m.iota().e1(), m.iota().e2() + 1)));
            }
            case "newarray" -> {
                int dim = instruction.getInt("dim"); // Recurse / While-loop magic
                Object type = instruction.get("type"); // SimpleType

                JSONObject value_length = m.sigma().pop();
                int length = value_length.getInt("value");

                // Create value
                JSONArray value = new JSONArray(length);
                for(int i = 0; i < length; i++) {
                    value.put(i, SimpleType.createDefault(type, mu));
                }
                JSONObject result = new JSONObject(Map.of("type", type, "value", value));

                // Create reference to value
                JSONObject ref = new JSONObject(Map.of("kind", "array", "type", type));

                mu.put(System.identityHashCode(ref), result);
                m.sigma().push(ref);

                psi.push(new Method(m.lambda(), m.sigma(), new Pair<>(m.iota().e1(), m.iota().e2() + 1)));
            }
            case "arraylength" -> {
                JSONObject ref = m.sigma().pop();

                JSONObject array = mu.get(System.identityHashCode(ref));

                JSONObject result = new JSONObject();
                result.put("type", "int");
                result.put("value", array.getJSONArray("value").length());

                m.sigma().push(result);

                psi.push(new Method(m.lambda(), m.sigma(), new Pair<>(m.iota().e1(), m.iota().e2() + 1)));
            }
            case "throw" -> {
                JSONObject objectref = m.sigma().pop();
                if(objectref == null) throw new NullPointerException("Cannot throw because \"objectref\" is null");

                JSONObject exceptionhandler = null;
                while(exceptionhandler == null) {
                    int cl = m.iota().e1().getJSONObject("code").getJSONArray("bytecode").length();

                    JSONArray exceptionhandlers = m.iota().e1().getJSONArray("exceptions");
                    for(int i = 0; i < exceptionhandlers.length(); i++) {
                        JSONObject eh = exceptionhandlers.getJSONObject(i);

                        Object catchtype = eh.get("catchType");
                        if(!catchtype.equals(objectref.get("name"))) continue;

                        int handler = eh.getInt("handler");
                        if(handler > cl) continue;

                        int start = eh.getInt("start");
                        int end = eh.getInt("end");
                        if(start > cl || end >= cl) continue;

                        exceptionhandler = eh;
                        break;
                    }

                    if(exceptionhandler == null) {
                        if(!psi.isEmpty()) m = psi.pop();
                        else break;
                    }
                }

                if(exceptionhandler != null) {
                    m.sigma().push(objectref);
                    psi.push(new Method(m.lambda(), m.sigma(), new Pair<>(m.iota().e1(), exceptionhandler.getInt("handler"))));
                }
            }
            case "return" -> {
                if(instruction.isNull("type")) break;

                String type = instruction.getString("type"); // LocalType
                JSONObject value = m.sigma().pop();

                JSONObject result = type.equals("ref") ? value : new JSONObject(value.toMap());

                if(!psi.isEmpty()) {
                    Method m2 = psi.peek();
                    m2.sigma().push(result);
                } else {
                    System.out.println(String.format("%-12s", "return") + Main.toFormattedString(result));
                    return;
                }
            }
            case "nop" -> {
                psi.push(new Method(m.lambda(), m.sigma(), new Pair<>(m.iota().e1(), m.iota().e2() + 1)));
            }
            case "pop" -> {
                int words = instruction.getInt("words");

                while(!m.sigma().isEmpty() && words > 0) {
                    m.sigma().pop();
                    words--;
                }

                psi.push(new Method(m.lambda(), m.sigma(), new Pair<>(m.iota().e1(), m.iota().e2() + 1)));
            }
            case "dup" -> {
                int words = instruction.getInt("words");
                if(m.sigma().size() < words) System.out.println("Not enough elements in stack for duplication");

                List<JSONObject> local = new ArrayList<>();
                for(int i = 0; i < words; i++) {
                    local.add(m.sigma().pop());
                }

                for(int i = 0; i < words+1; i++) {
                    for(JSONObject value : local) {
                        m.sigma().push(value.has("kind") ? value : new JSONObject(value.toMap()));
                    }
                }

                psi.push(new Method(m.lambda(), m.sigma(), new Pair<>(m.iota().e1(), m.iota().e2() + 1)));
            }
            case "dup_x1" -> {
                int words = instruction.getInt("words");
                if(m.sigma().size() < words + 1) System.out.println("Not enough elements in stack for duplication");

                List<JSONObject> local = new ArrayList<>();
                for(int i = 0; i < words; i++) {
                    local.add(m.sigma().pop());
                }

                JSONObject word = m.sigma().pop();

                for(int i = 0; i < words; i++) {
                    for(JSONObject value : local) {
                        m.sigma().push(value.has("kind") ? value : new JSONObject(value.toMap()));
                    }
                }

                m.sigma().push(word);

                for(int i = 0; i < words; i++) {
                    for(JSONObject value : local) {
                        m.sigma().push(value.has("kind") ? value : new JSONObject(value.toMap()));
                    }
                }

                psi.push(new Method(m.lambda(), m.sigma(), new Pair<>(m.iota().e1(), m.iota().e2() + 1)));
            }
            case "dup_x2" -> {
                int words = instruction.getInt("words");
                if(m.sigma().size() < words + 2) System.out.println("Not enough elements in stack for duplication");

                List<JSONObject> local = new ArrayList<>();
                for(int i = 0; i < words; i++) {
                    local.add(m.sigma().pop());
                }

                JSONObject word1 = m.sigma().pop();
                JSONObject word2 = m.sigma().pop();

                for(int i = 0; i < words; i++) {
                    for(JSONObject value : local) {
                        m.sigma().push(value.has("kind") ? value : new JSONObject(value.toMap()));
                    }
                }

                m.sigma().push(word2);
                m.sigma().push(word1);

                for(int i = 0; i < words; i++) {
                    for(JSONObject value : local) {
                        m.sigma().push(value.has("kind") ? value : new JSONObject(value.toMap()));
                    }
                }

                psi.push(new Method(m.lambda(), m.sigma(), new Pair<>(m.iota().e1(), m.iota().e2() + 1)));
            }
            case "swap" -> {
                JSONObject value2 = m.sigma().pop();
                JSONObject value1 = m.sigma().pop();

                m.sigma().push(value2);
                m.sigma().push(value1);

                psi.push(new Method(m.lambda(), m.sigma(), new Pair<>(m.iota().e1(), m.iota().e2() + 1)));
            }
            default -> throw new UnsupportedOperationException("Unsupported instruction \"" + instruction.getString("opr") + "\"");
        }

        int index = 0;
        for(Method mp : psi) {
            System.out.println("Ψ" + String.format("%02d", index) + "\t" + mp);
            index++;
        }
        System.out.println();
    }
}
