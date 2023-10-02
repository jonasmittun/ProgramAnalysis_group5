package Week04;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static Week01.Main.getFiles;

class ArrayTest {

    static Map<String, String> mapper = new HashMap<>();       // Map<Filename, Classname>
    static Map<String, JSONObject> classes = new HashMap<>();  // Map<Classname, JSONObject>

    static JSONObject cls;

    @BeforeAll
    static void initAll() {
        String path = "src\\main\\resources\\decompiled";
        for(Map.Entry<String, String> entry : getFiles(path).entrySet()) {
            String filename = entry.getKey();
            String content = entry.getValue();

            JSONObject file = new JSONObject(content);
            String classname = file.getString("name");

            classes.put(classname, file);
            mapper.put(filename, classname);
        }

        cls = classes.get(mapper.get("Array.json"));
    }

    private static JSONObject createArray(int[] array) {
        JSONArray value = new JSONArray(array.length);
        for(int i = 0; i < array.length; i++) {
            value.put(i, new JSONObject(Map.of("type", "int", "value", array[i])));
        }

        return new JSONObject(Map.of("type", "int", "value", value));
    }

    @Test
    void first() {
        JSONObject ref = new JSONObject(Map.of("kind", "array", "type", "int"));

        Method m = new Method(new JSONObject[] {ref}, new ArrayDeque<>(), new Pair<>(Main.simpleResolve(cls, "first"), 0));

        Map<Integer, JSONObject> mu = new HashMap<>();
        mu.put(System.identityHashCode(ref), createArray(new int[] { 1, 2, 3, 4 }));

        ConcreteInterpreter in = new ConcreteInterpreter(new HashMap<>(classes));
        in.run(m, mu);
    }

    @Test
    void firstSafe_success() {
        JSONObject ref = new JSONObject(Map.of("kind", "array", "type", "int"));

        Method m = new Method(new JSONObject[] {ref}, new ArrayDeque<>(), new Pair<>(Main.simpleResolve(cls, "firstSafe"), 0));

        Map<Integer, JSONObject> mu = new HashMap<>();
        mu.put(System.identityHashCode(ref), createArray(new int[] { 1, 2, 3, 4 }));

        ConcreteInterpreter in = new ConcreteInterpreter(new HashMap<>(classes));
        in.run(m, mu);
    }

    @Test
    void firstSafe_fail() {
        JSONObject ref = new JSONObject(Map.of("kind", "array", "type", "int"));

        Method m = new Method(new JSONObject[] {ref}, new ArrayDeque<>(), new Pair<>(Main.simpleResolve(cls, "firstSafe"), 0));

        Map<Integer, JSONObject> mu = new HashMap<>();
        mu.put(System.identityHashCode(ref), createArray(new int[] {}));

        ConcreteInterpreter in = new ConcreteInterpreter(new HashMap<>(classes));
        in.run(m, mu);
    }

    @Test
    void access() {
        JSONObject ref = new JSONObject(Map.of("kind", "array", "type", "int"));

        JSONObject i = new JSONObject(Map.of("type", "int", "value", 2));

        Method m = new Method(new JSONObject[] { i, ref }, new ArrayDeque<>(), new Pair<>(Main.simpleResolve(cls, "access"), 0));

        Map<Integer, JSONObject> mu = new HashMap<>();
        mu.put(System.identityHashCode(ref), createArray(new int[] { 1, 2, 3, 4 }));

        ConcreteInterpreter in = new ConcreteInterpreter(new HashMap<>(classes));
        in.run(m, mu);
    }
    @Test
    void newArray() {
        Method m = new Method(new JSONObject[1], new ArrayDeque<>(), new Pair<>(Main.simpleResolve(cls, "newArray"), 0));

        Map<Integer, JSONObject> mu = new HashMap<>();

        ConcreteInterpreter in = new ConcreteInterpreter(new HashMap<>(classes));
        in.run(m, mu);
    }

    @Test
    void newArrayOutOfBounds() {
        Method m = new Method(new JSONObject[1], new ArrayDeque<>(), new Pair<>(Main.simpleResolve(cls, "newArrayOutOfBounds"), 0));

        Map<Integer, JSONObject> mu = new HashMap<>();

        ConcreteInterpreter in = new ConcreteInterpreter(new HashMap<>(classes));
        in.run(m, mu);
    }

    @Test
    void accessSafe() {
        JSONObject ref = new JSONObject(Map.of("kind", "array", "type", "int"));

        JSONObject i = new JSONObject(Map.of("type", "int", "value", 2));

        Method m = new Method(new JSONObject[] { i, ref }, new ArrayDeque<>(), new Pair<>(Main.simpleResolve(cls, "accessSafe"), 0));

        Map<Integer, JSONObject> mu = new HashMap<>();
        mu.put(System.identityHashCode(ref), createArray(new int[] { 1, 2, 3, 4 }));

        ConcreteInterpreter in = new ConcreteInterpreter(new HashMap<>(classes));
        in.run(m, mu);
    }

    @Test
    @Timeout(value = 100, unit = TimeUnit.MILLISECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    void bubbleSort() {
        JSONObject ref = new JSONObject(Map.of("kind", "array", "type", "int"));

        JSONObject[] lambda = new JSONObject[5];
        lambda[0] = ref;

        Method m = new Method(lambda, new ArrayDeque<>(), new Pair<>(Main.simpleResolve(cls, "bubbleSort"), 0));

        Map<Integer, JSONObject> mu = new HashMap<>();
        mu.put(System.identityHashCode(ref), createArray(new int[] { 4, 3, 2, 1 }));

        ConcreteInterpreter in = new ConcreteInterpreter(new HashMap<>(classes));
        in.run(m, mu);
    }

    @Test
    void aWierdOneOutOfBounds() {
        Method m = new Method(new JSONObject[1], new ArrayDeque<>(), new Pair<>(Main.simpleResolve(cls, "aWierdOneOutOfBounds"), 0));

        Map<Integer, JSONObject> mu = new HashMap<>();

        ConcreteInterpreter in = new ConcreteInterpreter(new HashMap<>(classes));
        in.run(m, mu);
    }

    @Test
    void aWierdOneWithinBounds() {
        Method m = new Method(new JSONObject[1], new ArrayDeque<>(), new Pair<>(Main.simpleResolve(cls, "aWierdOneWithinBounds"), 0));

        Map<Integer, JSONObject> mu = new HashMap<>();

        ConcreteInterpreter in = new ConcreteInterpreter(new HashMap<>(classes));
        in.run(m, mu);
    }
}