package Week04;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.module.ResolutionException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.IntStream;

import static Week04.Main.cloneJSONObject;

public class ConcreteInterpreter {

    private final Map<String, JSONObject> classes;                      // Map<Classname, JSONObject>

    public ConcreteInterpreter(Map<String, JSONObject> classes) {
        this.classes = classes;
    }

    /** Returns the resolved method as a JSONObject
     * @param classref      SimpleReferenceType: { "kind": "class", "name": &lt;ClassName&gt; }
     * @param name          String
     * @param args          SimpleType[]
     * @param return_type   nullable SimpleType
     * @return              The method - if correctly resolved
     */
    public static Method resolveMethod(Map<String, JSONObject> classes, JSONObject classref, String name, JSONArray args, Object return_type) {
        if(!classref.has("name")) throw new IllegalArgumentException("Incorrect reference type!");
        if(!classes.containsKey(classref.getString("name"))) throw new ResolutionException("Class " + classref.getString("name") + " not found!");
        JSONObject cls = classes.get(classref.getString("name"));

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

            return new Method(cls.getString("name"), method);
        }

        throw new ResolutionException(classref.getString("name") + "." + name + " " + args + " → " + return_type + " not found!");
    }

    /** Checks if the &lt;Type&gt; is equal to the &lt;SimpleType&gt; */
    public static boolean typeIsEqual(JSONObject Type, Object SimpleType) {
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
    public static JSONObject initialize(Map<String, JSONObject> classes, String classname, Map<Integer, JSONObject> mu) {
        if(!classes.containsKey(classname)) throw new InstantiationError(classname + " does not exist.");
        JSONObject cls = cloneJSONObject(classes.get(classname));

        // Initialize super class if it exists
        if(!cls.isNull("super")) {
            String superclassname = cls.getJSONObject("super").getString("name");

            mu.put(System.identityHashCode(cls), initialize(classes, superclassname, mu));
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
        if(isNull(object) || !object.has("fields")) return Optional.empty();

        JSONArray fields = object.getJSONArray("fields");
        for(int i = 0; i < fields.length(); i++) {
            JSONObject f = fields.getJSONObject(i);
            if(f.getString("name").equals(fieldname)) {
                if(f.isNull("value")) {
                    return Optional.of(SimpleType.createDefault(fieldtype, mu));
                } else {
                    return Optional.of(cloneJSONObject(f.getJSONObject("value")));
                }
            }
        }

        // Try superclass if exists
        return mu.containsKey(System.identityHashCode(object)) ? getField(mu.get(System.identityHashCode(object)), fieldname, fieldtype, mu) : Optional.empty();
    }

    /** Tries to put a value into a field of the object.
     * @param object    The object which contains the field.
     * @param fieldname The name of the field.
     * @param fieldtype The type of the field (a &lt;SimpleType&gt;).
     * @return          True when the field has been correctly put and false when not.
     */
    public static boolean putField(JSONObject object, String fieldname, Object fieldtype, JSONObject value, Map<Integer, JSONObject> mu) {
        if(isNull(object) || !object.has("fields")) return false;

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

        // Try superclass if exists
        return mu.containsKey(System.identityHashCode(object)) && putField(mu.get(System.identityHashCode(object)), fieldname, fieldtype, value, mu);
    }

    /** Creates an array of specified <code>dimension</code>, <code>length</code> and <code>type</code>.
     * @param type      A &lt;SimpleType&gt;
     * @param length    The length of the array.
     * @param dimension Number of array dimensions, ex. int[][] has 2 dimensions.
     * @param sigma     The operand stack.
     * @param mu        The memory.
     */
    public static JSONObject initializeArray(Object type, int length, int dimension, Deque<JSONObject> sigma, Map<Integer, JSONObject> mu) {
        if(length < 0) throw new NegativeArraySizeException(Integer.toString(length));

        JSONArray array = new JSONArray(length);
        if(dimension > 1) {
            JSONObject count = sigma.pop();
            int innerlength = count.getInt("value");

            for(int i = 0; i < length; i++) {
                JSONObject valueref = initializeArray(type, innerlength, dimension - 1, sigma, mu);
                array.put(i, valueref);
            }
        } else {
            for(int i = 0; i < length; i++) {
                array.put(i, SimpleType.createDefault(type, mu));
            }
        }

        JSONObject arrayref = new JSONObject(Map.of("kind", "array", "type", type));
        mu.put(System.identityHashCode(arrayref), new JSONObject(Map.of("type", type, "value", array)));

        return arrayref;
    }

    /** Determines if the class is an interface.
     * @param classname The name of the class.
     * @return          True when the class has the "interface" access modifier and false otherwise.
     */
    public static boolean isInterface(Map<String, JSONObject> classes, String classname) {
        if(!classes.containsKey(classname)) throw new RuntimeException("Class " + classname + " could not be found.");

        JSONArray access = classes.get(classname).getJSONArray("access");
        return IntStream.range(0, access.length()).anyMatch(i -> access.getString(i).equals("interface"));
    }

    /** Determines if an object is of a given type.
     * @param objectref The reference to the object.
     * @param type      A &lt;SimpleReferenceType&gt;
     * @return          True when the object is an instance of the type and false otherwise.
     */
    public static boolean isInstanceOf(Map<String, JSONObject> classes, JSONObject objectref, JSONObject type) {
        if(objectref == null) return false;

        switch(objectref.getString("kind")) {
            case "array" -> {
                if(type.getString("kind").equals("class")) {
                    String typename = type.getString("name");
                    // If Type is a class type, then it must be of type Object
                    if(!isInterface(classes, typename)) return typename.equals("java/lang/Object");
                    else {
                        throw new UnsupportedOperationException("instanceof on arrays when type is an interface has not been implemented!");
                    }
                } else {
                    // TODO: If they are reference-types, it should check if objectref-type can be cast to type-type
                    return SimpleType.equals(objectref.get("type"), type.get("type"));
                }
            }
            case "class" -> {
                if(!type.has("name")) throw new IllegalArgumentException("Type was not of kind \"class\"");

                String classname = objectref.getString("name");
                String typename = type.getString("name");

                boolean is_interface = isInterface(classes, typename);

                if(!isInterface(classes, classname)) { // It's an ordinary (nonarray) class
                    JSONObject o = classes.get(classname);
                    if(is_interface) { // Object should implement the type
                        while(o != null) {
                            JSONArray access = o.getJSONArray("interfaces");
                            for(int j = 0; j < access.length(); j++) {
                                JSONObject i = access.getJSONObject(j);
                                if(i.getString("name").equals(typename)) return true;
                            }

                            o = !o.isNull("super") ? classes.get(o.getJSONObject("super").getString("name")) : null;
                        }
                    } else { // Object should be of type or be a subclass of the type
                        while(o != null) {
                            if(o.getString("name").equals(typename)) return true;

                            o = !o.isNull("super") ? classes.get(o.getJSONObject("super").getString("name")) : null;
                        }
                    }
                } else { // It's an interface type
                    if(!is_interface) { // When Type is not an interface it must be of type Object
                        return typename.equals("java/lang/Object");
                    } else { // Type must be the same interface as S or a superinterface of S
                        JSONObject o = classes.get(typename);
                        while(o != null) {
                            if(o.getString("name").equals(typename)) return true;

                            o = !o.isNull("super") ? classes.get(o.getJSONObject("super").getString("name")) : null;
                        }
                    }
                }
            }
            default -> throw new RuntimeException("Unexpected kind: " + objectref.getString("kind"));
        }

        return false;
    }

    /** Returns a new JSONObject representing the null value: { "value": {@code JSONObject.NULL} } */
    public static JSONObject createNull() {
        return new JSONObject(Map.of("value", JSONObject.NULL));
    }

    /** Check if a JSONObject is null
     * @see Week04.ConcreteInterpreter#createNull()
     */
    public static boolean isNull(JSONObject o) {
        return o == null || (o.has("value") && o.isNull("value"));
    }

    public void run(Frame frame, Map<Integer, JSONObject> mu) {
        Deque<Frame> psi = new ArrayDeque<>();  // Method Stack
        psi.push(frame);

        System.out.println("Initial:\nΨ00\t" + frame + "\n");
        while(!psi.isEmpty()) {
            Frame f = psi.pop();

            step(f, mu, psi);
        }

        if(!mu.isEmpty()) {
            System.out.println("\nMemory (µ):");
            for(Map.Entry<Integer, JSONObject> entry : mu.entrySet()) {
                System.out.println(entry.getKey() + "\t" + entry.getValue());
            }
        }
    }

    public void step(Frame f, Map<Integer, JSONObject> mu, Deque<Frame> psi) {
        JSONObject instruction = f.iota().e1().method().getJSONObject("code").getJSONArray("bytecode").getJSONObject(f.iota().e2());
        System.out.println("Instruction: " + instruction);

        switch(instruction.getString("opr")) {
            case "array_load" -> {
                JSONObject index = f.sigma().pop();
                JSONObject arrayref = f.sigma().pop();

                if(isNull(arrayref)) throw new NullPointerException("Cannot load from array because \"arrayref\" is null");

                JSONObject actual = mu.get(System.identityHashCode(arrayref));
                JSONArray array = actual.getJSONArray("value");

                int index_value = index.getInt("value");
                if(array.length() < index_value) throw new ArrayIndexOutOfBoundsException("Index " + index_value + " out of bounds for length " + array.length());

                JSONObject value = array.getJSONObject(index_value);

                f.sigma().push(value.has("kind") ? value : cloneJSONObject(value));
                psi.push(new Frame(f.lambda(), f.sigma(), new Pair<>(f.iota().e1(), f.iota().e2() + 1)));
            }
            case "array_store" -> {
                JSONObject value = f.sigma().pop();
                JSONObject index = f.sigma().pop();
                JSONObject arrayref = f.sigma().pop();

                if(isNull(arrayref)) throw new NullPointerException("Cannot store to array because \"arrayref\" is null");

                JSONObject actual = mu.get(System.identityHashCode(arrayref));
                JSONArray array = actual.getJSONArray("value");

                int index_value = index.getInt("value");
                if(array.length() < index_value) throw new ArrayIndexOutOfBoundsException("Index " + index_value + " out of bounds for length " + array.length());

                array.put(index_value, value);
                psi.push(new Frame(f.lambda(), f.sigma(), new Pair<>(f.iota().e1(), f.iota().e2() + 1)));
            }
            case "push" -> {
                if(instruction.isNull("value")) {
                    f.sigma().push(createNull());
                } else {
                    JSONObject value = instruction.getJSONObject("value");
                    String type = value.getString("type");

                    switch(type) {
                        case "class" -> {
                            f.sigma().push(value);
                        }
                        case "string" -> {
                            // Create array reference for string value
                            JSONObject arrayref = new JSONObject(Map.of("kind", "array", "type", "byte"));
                            // Create array to hold string value as a byte[]
                            JSONObject array = new JSONObject(Map.of("type", "byte", "value", new JSONArray(value.getString("value").getBytes(StandardCharsets.UTF_8))));
                            mu.put(System.identityHashCode(arrayref), array);

                            // Create a new String object
                            JSONObject object = cloneJSONObject(classes.get("java/lang/String"));
                            // Update "value" field in this String object to the array reference
                            object.getJSONArray("fields").getJSONObject(0).put("value", arrayref);

                            // Create String object reference
                            JSONObject objectref = new JSONObject(Map.of("kind", "class", "name", "java/lang/String"));
                            mu.put(System.identityHashCode(objectref), object);

                            // Push object reference
                            f.sigma().push(objectref);
                        }
                        default -> {
                            f.sigma().push(cloneJSONObject(value));
                        }
                    }
                }

                psi.push(new Frame(f.lambda(), f.sigma(), new Pair<>(f.iota().e1(), f.iota().e2() + 1)));
            }
            case "load" -> {
                int index = instruction.getInt("index");
                JSONObject value = f.lambda()[index];

                if(value == null) {
                    f.sigma().push(createNull());
                } else if(value.has("kind")) { // Check if it's a reference type
                   f.sigma().push(value);
                } else {
                    f.sigma().push(cloneJSONObject(value));
                }

                psi.push(new Frame(f.lambda(), f.sigma(), new Pair<>(f.iota().e1(), f.iota().e2() + 1)));
            }
            case "store" -> {
                int index = instruction.getInt("index");
                JSONObject value = f.sigma().pop();
                f.lambda()[index] = value;
                psi.push(new Frame(f.lambda(), f.sigma(), new Pair<>(f.iota().e1(), f.iota().e2() + 1)));
            }
            case "incr" -> {
                int index = instruction.getInt("index");
                JSONObject value = f.lambda()[index];
                String type = value.getString("type");
                switch(type) {
                    case "int", "integer"   -> value.put("value", value.getInt("value") + instruction.getInt("amount"));
                    case "long"             -> value.put("value", value.getLong("value") + instruction.getLong("amount"));
                    case "float"            -> value.put("value", value.getFloat("value") + instruction.getFloat("amount"));
                    case "double"           -> value.put("value", value.getDouble("value") + instruction.getDouble("amount"));
                    default                 -> System.out.println("Unsupported \"incr\" type: " + type);
                }

                psi.push(new Frame(f.lambda(), f.sigma(), new Pair<>(f.iota().e1(), f.iota().e2() + 1)));
            }
            case "binary" -> {
                String type = instruction.getString("type"); // Arithmetic Type
                JSONObject value2 = f.sigma().pop();
                JSONObject value1 = f.sigma().pop();

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
                f.sigma().push(result);
                psi.push(new Frame(f.lambda(), f.sigma(), new Pair<>(f.iota().e1(), f.iota().e2() + 1)));
            }
            case "negate" -> {
                String type = instruction.getString("type"); // Arithmetic Type
                JSONObject value = f.sigma().pop();
                JSONObject result = new JSONObject();
                result.put("type", type);
                switch(type) {
                    case "int"      -> result.put("value", value.getInt("value") * -1);
                    case "long"     -> result.put("value", value.getLong("value") * -1L);
                    case "float"    -> result.put("value", value.getFloat("value") * -1.f);
                    case "double"   -> result.put("value", value.getDouble("value") * -1.d);
                }
                f.sigma().push(result);
                psi.push(new Frame(f.lambda(), f.sigma(), new Pair<>(f.iota().e1(), f.iota().e2() + 1)));
            }
            case "bitopr" -> {
                String type = instruction.getString("type"); // "int" | "long"
                JSONObject value = f.sigma().pop();

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
                f.sigma().push(result);
                psi.push(new Frame(f.lambda(), f.sigma(), new Pair<>(f.iota().e1(), f.iota().e2() + 1)));
            }
            case "cast" -> {
                String from = instruction.getString("from");    // "int" | Arithmetic Type
                String to = instruction.getString("to");        // Small types | Arithmetic Type

                JSONObject value = f.sigma().pop();

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

                f.sigma().push(result);
                psi.push(new Frame(f.lambda(), f.sigma(), new Pair<>(f.iota().e1(), f.iota().e2() + 1)));
            }
            case "comparelongs" -> {
                JSONObject value2 = f.sigma().pop();
                JSONObject value1 = f.sigma().pop();

                JSONObject result = new JSONObject();
                result.put("type", "int");
                result.put("value", (Long.compare(value1.getLong("value"), value2.getLong("value"))));

                f.sigma().push(result);
                psi.push(new Frame(f.lambda(), f.sigma(), new Pair<>(f.iota().e1(), f.iota().e2() + 1)));
            }
            case "comparefloating" -> {
                JSONObject value2 = f.sigma().pop();
                JSONObject value1 = f.sigma().pop();

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

                f.sigma().push(result);
                psi.push(new Frame(f.lambda(), f.sigma(), new Pair<>(f.iota().e1(), f.iota().e2() + 1)));
            }
            case "if" -> {
                int target = instruction.getInt("target");

                JSONObject value2 = f.sigma().pop();
                JSONObject value1 = f.sigma().pop();

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

                psi.push(new Frame(f.lambda(), f.sigma(), new Pair<>(f.iota().e1(), result ? target : f.iota().e2() + 1)));
            }
            case "ifz" -> {
                String condition = instruction.getString("condition");
                int target = instruction.getInt("target");

                JSONObject value = f.sigma().pop();

                boolean result;
                if(value.has("kind")) {
                    JSONObject v = mu.get(System.identityHashCode(value));
                    result = switch(condition) {
                        case "is"       -> isNull(v);
                        case "isnot"    -> !isNull(v);
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

                psi.push(new Frame(f.lambda(), f.sigma(), new Pair<>(f.iota().e1(), result ? target : f.iota().e2() + 1)));
            }
            case "goto" -> {
                psi.push(new Frame(f.lambda(), f.sigma(), new Pair<>(f.iota().e1(), instruction.getInt("target"))));
            }
            case "jsr" -> {
                int target = instruction.getInt("target");

                Frame next = psi.peek();
                JSONObject value = new JSONObject(Map.of("type", "int", "value", next.iota().e2()));
                f.sigma().push(value);

                psi.push(new Frame(f.lambda(), f.sigma(), new Pair<>(f.iota().e1(), target)));
            }
            case "ret" -> {
                JSONObject value = f.sigma().pop();
                int address = value.getInt("value");

                psi.push(new Frame(f.lambda(), f.sigma(), new Pair<>(f.iota().e1(), address)));
            }
            case "tableswitch" -> {
                int location = instruction.getInt("default");
                int low = instruction.getInt("low");

                JSONObject value = f.sigma().pop();
                int index = value.getInt("index");

                JSONArray targets = instruction.getJSONArray("targets");
                for(int i = 0; i < targets.length(); i++) {
                    int target = targets.getInt(i);
                    if(target == index) {
                        location = index - low;
                        break;
                    }
                }

                psi.push(new Frame(f.lambda(), f.sigma(), new Pair<>(f.iota().e1(), location)));
            }
            case "lookupswitch" -> {
                int location = instruction.getInt("default");

                JSONObject value = f.sigma().pop();
                int index = value.getInt("index");

                JSONArray targets = instruction.getJSONArray("targets");
                for(int i = 0; i < targets.length(); i++) {
                    JSONObject target = targets.getJSONObject(i);
                    if(target.getInt("key") == index) {
                        location = target.getInt("target");
                        break;
                    }
                }

                psi.push(new Frame(f.lambda(), f.sigma(), new Pair<>(f.iota().e1(), location)));
            }
            case "get" -> {
                JSONObject field = instruction.getJSONObject("field");
                String fieldname = field.getString("name");
                Object fieldtype = field.get("type");

                JSONObject object;
                if(instruction.getBoolean("static")) {
                    object = classes.get(field.getString("class"));
                } else {
                    JSONObject objectref = f.sigma().pop();

                    if(isNull(objectref)) throw new NullPointerException("Cannot get field from object because \"objectref\" is null");

                    object = mu.get(System.identityHashCode(objectref));
                }

                Optional<JSONObject> value = getField(object, fieldname, fieldtype, mu);
                if(value.isEmpty()) throw new NoSuchFieldError("The field \"" + field.getString("name") + "\" does not exist.");

                f.sigma().push(value.get());
                psi.push(new Frame(f.lambda(), f.sigma(), new Pair<>(f.iota().e1(), f.iota().e2() + 1)));
            }
            case "put" -> {
                JSONObject field = instruction.getJSONObject("field");
                String fieldname = field.getString("name");
                Object fieldtype = field.get("type");

                JSONObject value = f.sigma().pop();

                JSONObject object;
                if(instruction.getBoolean("static")) {
                    object = classes.get(field.getString("class"));
                } else {
                    JSONObject objectref = f.sigma().pop();

                    if(isNull(objectref)) throw new NullPointerException("Cannot put field in object because \"objectref\" is null");

                    object = mu.get(System.identityHashCode(objectref));
                }

                if(!putField(object, fieldname, fieldtype, value, mu)) throw new NoSuchFieldError("The field \"" + field.getString("name") + "\" does not exist in " + object.getString("name"));

                psi.push(new Frame(f.lambda(), f.sigma(), new Pair<>(f.iota().e1(), f.iota().e2() + 1)));
            }
            case "invoke" -> {
                JSONObject invoke_method = instruction.getJSONObject("method");
                String invoke_access = instruction.getString("access");

                String methodname = invoke_method.getString("name");
                JSONArray args = invoke_method.getJSONArray("args");
                Object returns = invoke_method.isNull("returns") ? null : invoke_method.get("returns");

                Method resolvedMethod = switch(invoke_access) {
                    case "virtual" -> {
                        JSONObject classref = invoke_method.getJSONObject("ref");

                        yield resolveMethod(classes, classref, methodname, args, returns);
                    }
                    case "special", "static" -> {
                        JSONObject classref = invoke_method.getJSONObject("ref");
                        boolean is_interface = invoke_method.getBoolean("is_interface");

                        yield resolveMethod(classes, classref, methodname, args, returns);
                    }
                    case "interface" -> {
                        JSONObject classref = invoke_method.getJSONObject("ref");
                        int stack_size = invoke_method.getInt("stack_size");

                        yield resolveMethod(classes, classref, methodname, args, returns);
                    }
                    case "dynamic" -> throw new RuntimeException("Dynamic invoke is not implemented");
                    default -> throw new IllegalArgumentException("Illegal invoke access: " + instruction.getString("access"));
                };

                if(f.sigma().size() < args.length()) throw new RuntimeException("Not enough elements in stack for invocation of method!");

                if(resolvedMethod.method().isNull("code")) {
                    throw new RuntimeException("Runtime method identification is not implemented");
                    // TODO: Find method in subclass / "Implement class" at runtime
                }

                JSONObject[] lambda = new JSONObject[resolvedMethod.method().getJSONObject("code").getInt("max_locals")];
                for(int i = 0; i < args.length(); i++) {
                    JSONObject arg = f.sigma().pop();

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
                        lambda[0] = f.sigma().pop();
                    }
                    default -> {}
                }

                psi.push(new Frame(f.lambda(), f.sigma(), new Pair<>(f.iota().e1(), f.iota().e2() + 1)));
                psi.push(new Frame(lambda, new ArrayDeque<>(), new Pair<>(resolvedMethod, 0)));
            }
            case "new" -> {
                String classname = instruction.getString("class");

                if(!classes.containsKey(classname)) throw new InstantiationError(classname + " does not exist.");
                List<Object> access = classes.get(classname).getJSONArray("access").toList();
                if(access.contains("abstract")) throw new InstantiationError(classname + " is abstract.");
                if(access.contains("interface")) throw new InstantiationError(classname + " is an interface.");

                JSONObject objectref = new JSONObject(Map.of("kind", "class", "name", classname));
                JSONObject object = initialize(classes, classname, mu);

                mu.put(System.identityHashCode(objectref), object);

                f.sigma().push(objectref);
                psi.push(new Frame(f.lambda(), f.sigma(), new Pair<>(f.iota().e1(), f.iota().e2() + 1)));
            }
            case "newarray" -> {
                int dim = instruction.getInt("dim");
                Object type = instruction.get("type"); // SimpleType

                JSONObject count = f.sigma().pop();
                int length = count.getInt("value");

                JSONObject arrayref = initializeArray(type, length, dim, f.sigma(), mu);

                f.sigma().push(arrayref);

                psi.push(new Frame(f.lambda(), f.sigma(), new Pair<>(f.iota().e1(), f.iota().e2() + 1)));
            }
            case "arraylength" -> {
                JSONObject arrayref = f.sigma().pop();
                JSONObject array = mu.get(System.identityHashCode(arrayref));
                int arraylength = array.getJSONArray("value").length();

                JSONObject value = new JSONObject(Map.of("type", "int", "value", arraylength));
                f.sigma().push(value);

                psi.push(new Frame(f.lambda(), f.sigma(), new Pair<>(f.iota().e1(), f.iota().e2() + 1)));
            }
            case "throw" -> {
                JSONObject objectref = f.sigma().pop();
                if(isNull(objectref)) throw new NullPointerException("Cannot throw because \"objectref\" is null");

                JSONObject exceptionhandler = null;
                while(exceptionhandler == null) {
                    int cl = f.iota().e1().method().getJSONObject("code").getJSONArray("bytecode").length();

                    JSONArray exceptionhandlers = f.iota().e1().method().getJSONArray("exceptions");
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
                        if(!psi.isEmpty()) f = psi.pop();
                        else break;
                    }
                }

                if(exceptionhandler != null) {
                    f.sigma().push(objectref);
                    psi.push(new Frame(f.lambda(), f.sigma(), new Pair<>(f.iota().e1(), exceptionhandler.getInt("handler"))));
                } else {
                    // Get Throwable object
                    JSONObject o = mu.get(System.identityHashCode(objectref));
                    while(!(o.has("name") && o.getString("name").equals("java/lang/Throwable"))) {
                        if(mu.containsKey(System.identityHashCode(o))) {
                            o = mu.get(System.identityHashCode(o));
                        } else {
                            o = null;
                            break;
                        }
                    }

                    if(o == null) throw new RuntimeException("\"object\" was not an instance of Throwable");
                    else {
                        System.err.println(objectref.getString("name").replace("/", "."));

                        // Print the stack trace
                        Optional<JSONObject> stacktraceref = getField(o, "stackTrace", new JSONObject(Map.of("kind", "array", "type", new JSONObject(Map.of("kind", "class", "name", "java/lang/StackTraceElement")))), mu);
                        if(stacktraceref.isPresent()) {
                            JSONObject stacktrace = mu.get(System.identityHashCode(stacktraceref.get()));
                            if(!isNull(stacktrace)) {
                                JSONArray array = stacktrace.getJSONArray("value");
                                for(int i = 0; i < array.length(); i++) {
                                    JSONObject stacktraceelementref = array.getJSONObject(i);
                                    JSONObject stacktraceelement = mu.get(System.identityHashCode(stacktraceelementref));
                                    if(stacktraceelement != null) {
                                        System.err.println("\tat " + stacktraceelement);
                                        // TODO: Format output (Extract fields)
                                    }
                                }
                            }
                        }

                        /*
                        // TODO: Add java/util/List
                        // Print suppressed exceptions, if any
                        Optional<JSONObject> suppressedexceptionsref = getField(o, "suppressedExceptions", new JSONObject(Map.of("kind", "class", "name", "java/util/List")), mu);
                        if(suppressedexceptionsref.isPresent()) {
                            JSONObject suppressedexceptions = mu.get(System.identityHashCode(suppressedexceptionsref));
                            if(suppressedexceptions != null) {

                            }
                        }
                        */

                        // Print the cause, if any
                        Optional<JSONObject> causeref = getField(o, "cause", new JSONObject(Map.of("kind", "class", "name", "java/lang/Throwable")), mu);
                        if(causeref.isPresent()) {
                            JSONObject cause = mu.get(System.identityHashCode(causeref));
                            if(!isNull(cause)) {
                                System.err.println("Caused by: " + cause.getString("name").replace("/", "."));
                            }
                        }
                    }
                }
            }
            case "checkcast" -> {
                JSONObject type = instruction.getJSONObject("type");

                JSONObject objectref = f.sigma().peek();

                if(!isNull(objectref) && !isInstanceOf(classes, objectref, type)) {
                    throw new ClassCastException(objectref + " cannot be cast to " + type);
                }

                psi.push(new Frame(f.lambda(), f.sigma(), new Pair<>(f.iota().e1(), f.iota().e2() + 1)));
            }
            case "instanceof" -> {
                JSONObject type = instruction.getJSONObject("type");

                JSONObject objectref = f.sigma().pop();

                boolean result = !isNull(objectref) && isInstanceOf(classes, objectref, type);

                f.sigma().push(new JSONObject(Map.of("type", "int", "value", result ? 1 : 0)));
                psi.push(new Frame(f.lambda(), f.sigma(), new Pair<>(f.iota().e1(), f.iota().e2() + 1)));
            }
            case "return" -> {
                if(instruction.isNull("type")) break;

                String type = instruction.getString("type"); // LocalType
                JSONObject value = f.sigma().pop();

                JSONObject result = type.equals("ref") ? value : cloneJSONObject(value);

                if(!psi.isEmpty()) {
                    Frame f2 = psi.peek();
                    f2.sigma().push(result);
                } else {
                    System.out.println(String.format("%-12s", "return") + Main.toFormattedString(result));
                    return;
                }
            }
            case "nop" -> {
                psi.push(new Frame(f.lambda(), f.sigma(), new Pair<>(f.iota().e1(), f.iota().e2() + 1)));
            }
            case "pop" -> {
                int words = instruction.getInt("words");

                while(!f.sigma().isEmpty() && words > 0) {
                    f.sigma().pop();
                    words--;
                }

                psi.push(new Frame(f.lambda(), f.sigma(), new Pair<>(f.iota().e1(), f.iota().e2() + 1)));
            }
            case "dup" -> {
                int words = instruction.getInt("words");
                if(f.sigma().size() < words) System.out.println("Not enough elements in stack for duplication");

                List<JSONObject> local = new ArrayList<>();
                for(int i = 0; i < words; i++) {
                    local.add(f.sigma().pop());
                }

                for(int i = 0; i < words+1; i++) {
                    for(JSONObject value : local) {
                        f.sigma().push(value.has("kind") ? value : cloneJSONObject(value));
                    }
                }

                psi.push(new Frame(f.lambda(), f.sigma(), new Pair<>(f.iota().e1(), f.iota().e2() + 1)));
            }
            case "dup_x1" -> {
                int words = instruction.getInt("words");
                if(f.sigma().size() < words + 1) System.out.println("Not enough elements in stack for duplication");

                List<JSONObject> local = new ArrayList<>();
                for(int i = 0; i < words; i++) {
                    local.add(f.sigma().pop());
                }

                JSONObject word = f.sigma().pop();

                for(int i = 0; i < words; i++) {
                    for(JSONObject value : local) {
                        f.sigma().push(value.has("kind") ? value : cloneJSONObject(value));
                    }
                }

                f.sigma().push(word);

                for(int i = 0; i < words; i++) {
                    for(JSONObject value : local) {
                        f.sigma().push(value.has("kind") ? value : cloneJSONObject(value));
                    }
                }

                psi.push(new Frame(f.lambda(), f.sigma(), new Pair<>(f.iota().e1(), f.iota().e2() + 1)));
            }
            case "dup_x2" -> {
                int words = instruction.getInt("words");
                if(f.sigma().size() < words + 2) System.out.println("Not enough elements in stack for duplication");

                List<JSONObject> local = new ArrayList<>();
                for(int i = 0; i < words; i++) {
                    local.add(f.sigma().pop());
                }

                JSONObject word1 = f.sigma().pop();
                JSONObject word2 = f.sigma().pop();

                for(int i = 0; i < words; i++) {
                    for(JSONObject value : local) {
                        f.sigma().push(value.has("kind") ? value : cloneJSONObject(value));
                    }
                }

                f.sigma().push(word2);
                f.sigma().push(word1);

                for(int i = 0; i < words; i++) {
                    for(JSONObject value : local) {
                        f.sigma().push(value.has("kind") ? value : cloneJSONObject(value));
                    }
                }

                psi.push(new Frame(f.lambda(), f.sigma(), new Pair<>(f.iota().e1(), f.iota().e2() + 1)));
            }
            case "swap" -> {
                JSONObject value2 = f.sigma().pop();
                JSONObject value1 = f.sigma().pop();

                f.sigma().push(value2);
                f.sigma().push(value1);

                psi.push(new Frame(f.lambda(), f.sigma(), new Pair<>(f.iota().e1(), f.iota().e2() + 1)));
            }
            default -> throw new UnsupportedOperationException("Unsupported instruction \"" + instruction.getString("opr") + "\"");
        }

        int index = 0;
        for(Frame fp : psi) {
            System.out.println("Ψ" + String.format("%02d", index) + "\t" + fp);
            index++;
        }
        System.out.println();
    }
}
