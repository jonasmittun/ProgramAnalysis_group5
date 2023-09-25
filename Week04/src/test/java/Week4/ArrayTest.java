package Week4;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import static Week4.Main.getFiles;

class ArrayTest {

    static Map<String, String> mapper = new HashMap<>();       // Map<Filename, Classname>
    static Map<String, JSONObject> classes = new HashMap<>();  // Map<Classname, JSONObject>

    static String filename = "Array.json";

    @BeforeAll
    static void initAll() {
        String path = "src\\main\\java\\decompiled\\dtu\\compute\\exec";
        Map<String, String> files = getFiles(path);
        for(Map.Entry<String, String> entry : files.entrySet()) {
            String filename = entry.getKey();
            String content = entry.getValue();

            JSONObject file = new JSONObject(content);
            String classname = file.getString("name");

            classes.put(classname, file);
            mapper.put(filename, classname);
        }
    }

    private JSONObject createArray(int[] array) {
        // Create value
        JSONObject result = new JSONObject();
        result.put("type", "int");
        JSONArray value = new JSONArray(array.length);
        for(int i = 0; i < array.length; i++) {
            JSONObject value_inner = new JSONObject();
            value_inner.put("type", "int");
            value_inner.put("value", array[i]);
            value.put(i, value_inner);
        }
        result.put("value", value);

        return result;
    }

    @Test
    void first() {
        JSONObject ref = new JSONObject();
        ref.put("type", "ref");
        ref.put("kind", "array");

        Interpreter.Method m = new Interpreter.Method(new JSONObject[] {ref}, new Stack<>(), new Interpreter.Pair<>(mapper.get("Array.json") + "/" + "first", 0));

        Map<Integer, JSONObject> mu = new HashMap<>();
        mu.put(System.identityHashCode(ref), createArray(new int[] { 1, 2, 3, 4 }));

        Interpreter in = new Interpreter(new HashMap<>(classes));
        in.run(m, mu);
    }

    @Test
    void firstSafe() {
        JSONObject ref = new JSONObject();
        ref.put("type", "ref");
        ref.put("kind", "array");

        Interpreter.Method m = new Interpreter.Method(new JSONObject[] {ref}, new Stack<>(), new Interpreter.Pair<>(mapper.get(filename) + "/" + "firstSafe", 0));

        Map<Integer, JSONObject> mu = new HashMap<>();
        mu.put(System.identityHashCode(ref), createArray(new int[] { 1, 2, 3, 4 }));

        Interpreter in = new Interpreter(new HashMap<>(classes));
        in.run(m, mu);
    }

    @Test
    void access() {
        JSONObject ref = new JSONObject();
        ref.put("type", "ref");
        ref.put("kind", "array");

        JSONObject i = new JSONObject(Map.of("type", "int", "value", 2));

        Interpreter.Method m = new Interpreter.Method(new JSONObject[] { i, ref }, new Stack<>(), new Interpreter.Pair<>(mapper.get(filename) + "/" + "access", 0));

        Map<Integer, JSONObject> mu = new HashMap<>();
        mu.put(System.identityHashCode(ref), createArray(new int[] { 1, 2, 3, 4 }));

        Interpreter in = new Interpreter(new HashMap<>(classes));
        in.run(m, mu);
    }
    @Test
    void newArray() {
        Interpreter.Method m = new Interpreter.Method(new JSONObject[1], new Stack<>(), new Interpreter.Pair<>(mapper.get(filename) + "/" + "newArray", 0));

        Map<Integer, JSONObject> mu = new HashMap<>();

        Interpreter in = new Interpreter(new HashMap<>(classes));
        in.run(m, mu);
    }

    @Test
    void newArrayOutOfBounds() {
        Interpreter.Method m = new Interpreter.Method(new JSONObject[1], new Stack<>(), new Interpreter.Pair<>(mapper.get(filename) + "/" + "newArrayOutOfBounds", 0));

        Map<Integer, JSONObject> mu = new HashMap<>();

        Interpreter in = new Interpreter(new HashMap<>(classes));
        in.run(m, mu);
    }

    @Test
    void accessSafe() {
        JSONObject ref = new JSONObject();
        ref.put("type", "ref");
        ref.put("kind", "array");

        JSONObject i = new JSONObject(Map.of("type", "int", "value", 2));

        Interpreter.Method m = new Interpreter.Method(new JSONObject[] { i, ref }, new Stack<>(), new Interpreter.Pair<>(mapper.get(filename) + "/" + "accessSafe", 0));

        Map<Integer, JSONObject> mu = new HashMap<>();
        mu.put(System.identityHashCode(ref), createArray(new int[] { 1, 2, 3, 4 }));

        Interpreter in = new Interpreter(new HashMap<>(classes));
        in.run(m, mu);
    }

    @Test
    void bubbleSort() {
        JSONObject ref = new JSONObject();
        ref.put("type", "ref");
        ref.put("kind", "array");

        JSONObject i = new JSONObject(Map.of("type", "int", "value", 2));

        JSONObject[] lambda = new JSONObject[5];
        lambda[0] = i;
        lambda[1] = ref;

        Interpreter.Method m = new Interpreter.Method(lambda, new Stack<>(), new Interpreter.Pair<>(mapper.get(filename) + "/" + "bubbleSort", 0));

        Map<Integer, JSONObject> mu = new HashMap<>();
        mu.put(System.identityHashCode(ref), createArray(new int[] { 4, 3, 2, 1 }));

        Interpreter in = new Interpreter(new HashMap<>(classes));
        in.run(m, mu);
    }

    @Test
    void aWierdOneOutOfBounds() {
        Interpreter.Method m = new Interpreter.Method(new JSONObject[1], new Stack<>(), new Interpreter.Pair<>(mapper.get(filename) + "/" + "aWierdOneOutOfBounds", 0));

        Map<Integer, JSONObject> mu = new HashMap<>();

        Interpreter in = new Interpreter(new HashMap<>(classes));
        in.run(m, mu);
    }

    @Test
    void aWierdOneWithinBounds() {
        Interpreter.Method m = new Interpreter.Method(new JSONObject[1], new Stack<>(), new Interpreter.Pair<>(mapper.get(filename) + "/" + "aWierdOneWithinBounds", 0));

        Map<Integer, JSONObject> mu = new HashMap<>();

        Interpreter in = new Interpreter(new HashMap<>(classes));
        in.run(m, mu);
    }
}