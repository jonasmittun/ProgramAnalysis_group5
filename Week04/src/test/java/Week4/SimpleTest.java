package Week4;

import static Week4.Main.getFiles;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import java.util.*;

class SimpleTest {

    static Map<String, String> mapper = new HashMap<>();       // Map<Filename, Classname>
    static Map<String, JSONObject> classes = new HashMap<>();  // Map<Classname, JSONObject>

    static String filename = "Simple.json";

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

    @Test
    void noop() {
        Method m = new Method(new JSONObject[] {}, new Stack<>(), new Pair<>(mapper.get(filename) + "/" + "noop", 0));
        Map<Integer, JSONObject> mu = new HashMap<>();

        ConcreteInterpreter in = new ConcreteInterpreter(new HashMap<>(classes));
        in.run(m, mu);
    }

    @Test
    void zero() {
        Method m = new Method(new JSONObject[] {}, new Stack<>(), new Pair<>(mapper.get(filename) + "/" + "zero", 0));
        Map<Integer, JSONObject> mu = new HashMap<>();

        ConcreteInterpreter in = new ConcreteInterpreter(new HashMap<>(classes));
        in.run(m, mu);
    }

    @Test
    void hundredAndTwo() {
        Method m = new Method(new JSONObject[] {}, new Stack<>(), new Pair<>(mapper.get(filename) + "/" + "hundredAndTwo", 0));
        Map<Integer, JSONObject> mu = new HashMap<>();

        ConcreteInterpreter in = new ConcreteInterpreter(new HashMap<>(classes));
        in.run(m, mu);
    }
    @Test
    void identity() {
        Method m = new Method(new JSONObject[] { new JSONObject(Map.of("type", "int", "value", 7)) }, new Stack<>(), new Pair<>(mapper.get(filename) + "/" + "identity", 0));
        Map<Integer, JSONObject> mu = new HashMap<>();

        ConcreteInterpreter in = new ConcreteInterpreter(new HashMap<>(classes));
        in.run(m, mu);
    }

    @Test
    void add() {
        Method m = new Method(new JSONObject[] { new JSONObject(Map.of("type", "int", "value", 1)), new JSONObject(Map.of("type", "int", "value", 2)) }, new Stack<>(), new Pair<>(mapper.get(filename) + "/" + "add", 0));
        Map<Integer, JSONObject> mu = new HashMap<>();

        ConcreteInterpreter in = new ConcreteInterpreter(new HashMap<>(classes));
        in.run(m, mu);
    }

    @Test
    void min() {
        Method m = new Method(new JSONObject[] { new JSONObject(Map.of("type", "int", "value", 1)), new JSONObject(Map.of("type", "int", "value", 2)) }, new Stack<>(), new Pair<>(mapper.get(filename) + "/" + "min", 0));
        Map<Integer, JSONObject> mu = new HashMap<>();

        ConcreteInterpreter in = new ConcreteInterpreter(new HashMap<>(classes));
        in.run(m, mu);
    }

    @Test
    void factorial() {
        Method m = new Method(new JSONObject[] { new JSONObject(Map.of("type", "int", "value", 6)), new JSONObject(Map.of("type", "int", "value", 0)) }, new Stack<>(), new Pair<>(mapper.get(filename) + "/" + "factorial", 0));
        Map<Integer, JSONObject> mu = new HashMap<>();

        ConcreteInterpreter in = new ConcreteInterpreter(new HashMap<>(classes));
        in.run(m, mu);
    }
}