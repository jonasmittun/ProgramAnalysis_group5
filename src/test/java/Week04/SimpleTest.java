package Week04;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;

import static Week01.Main.getFiles;

class SimpleTest {

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

        cls = classes.get(mapper.get("Simple.json"));
    }

    @Test
    void noop() {
        Frame f = new Frame(new JSONObject[] {}, new ArrayDeque<>(), new Pair<>(Main.simpleResolve(cls, "noop"), 0));
        Map<Integer, JSONObject> mu = new HashMap<>();

        ConcreteInterpreter in = new ConcreteInterpreter(new HashMap<>(classes));
        in.run(f, mu);
    }

    @Test
    void zero() {
        Frame f = new Frame(new JSONObject[] {}, new ArrayDeque<>(), new Pair<>(Main.simpleResolve(cls, "zero"), 0));
        Map<Integer, JSONObject> mu = new HashMap<>();

        ConcreteInterpreter in = new ConcreteInterpreter(new HashMap<>(classes));
        in.run(f, mu);
    }

    @Test
    void hundredAndTwo() {
        Frame f = new Frame(new JSONObject[] {}, new ArrayDeque<>(), new Pair<>(Main.simpleResolve(cls, "hundredAndTwo"), 0));
        Map<Integer, JSONObject> mu = new HashMap<>();

        ConcreteInterpreter in = new ConcreteInterpreter(new HashMap<>(classes));
        in.run(f, mu);
    }
    @Test
    void identity() {
        Frame f = new Frame(new JSONObject[] { new JSONObject(Map.of("type", "int", "value", 7)) }, new ArrayDeque<>(), new Pair<>(Main.simpleResolve(cls, "identity"), 0));
        Map<Integer, JSONObject> mu = new HashMap<>();

        ConcreteInterpreter in = new ConcreteInterpreter(new HashMap<>(classes));
        in.run(f, mu);
    }

    @Test
    void add() {
        Frame f = new Frame(new JSONObject[] { new JSONObject(Map.of("type", "int", "value", 1)), new JSONObject(Map.of("type", "int", "value", 2)) }, new ArrayDeque<>(), new Pair<>(Main.simpleResolve(cls, "add"), 0));
        Map<Integer, JSONObject> mu = new HashMap<>();

        ConcreteInterpreter in = new ConcreteInterpreter(new HashMap<>(classes));
        in.run(f, mu);
    }

    @Test
    void min() {
        Frame f = new Frame(new JSONObject[] { new JSONObject(Map.of("type", "int", "value", 1)), new JSONObject(Map.of("type", "int", "value", 2)) }, new ArrayDeque<>(), new Pair<>(Main.simpleResolve(cls, "min"), 0));
        Map<Integer, JSONObject> mu = new HashMap<>();

        ConcreteInterpreter in = new ConcreteInterpreter(new HashMap<>(classes));
        in.run(f, mu);
    }

    @Test
    void div_success() {
        Frame f = new Frame(new JSONObject[] { new JSONObject(Map.of("type", "int", "value", 4)), new JSONObject(Map.of("type", "int", "value", 2)) }, new ArrayDeque<>(), new Pair<>(Main.simpleResolve(cls, "div"), 0));
        Map<Integer, JSONObject> mu = new HashMap<>();

        ConcreteInterpreter in = new ConcreteInterpreter(new HashMap<>(classes));
        in.run(f, mu);
    }

    @Test
    void div_fail() {
        Frame f = new Frame(new JSONObject[] { new JSONObject(Map.of("type", "int", "value", 4)), new JSONObject(Map.of("type", "int", "value", 0)) }, new ArrayDeque<>(), new Pair<>(Main.simpleResolve(cls, "div"), 0));
        Map<Integer, JSONObject> mu = new HashMap<>();

        ConcreteInterpreter in = new ConcreteInterpreter(new HashMap<>(classes));
        in.run(f, mu);
    }

    @Test
    void factorial() {
        Frame f = new Frame(new JSONObject[] { new JSONObject(Map.of("type", "int", "value", 6)), new JSONObject(Map.of("type", "int", "value", 0)) }, new ArrayDeque<>(), new Pair<>(Main.simpleResolve(cls, "factorial"), 0));
        Map<Integer, JSONObject> mu = new HashMap<>();

        ConcreteInterpreter in = new ConcreteInterpreter(new HashMap<>(classes));
        in.run(f, mu);
    }
}