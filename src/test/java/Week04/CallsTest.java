package Week04;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;

import static Week01.Main.getFiles;
import static Week04.ConcreteInterpreter.createNullArray;

class CallsTest {

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

        cls = classes.get(mapper.get("Calls.json"));
    }

    @Test
    void helloWorld() {
        Frame f = new Frame(createNullArray(0), new ArrayDeque<>(), new Pair<>(Main.simpleResolve(cls, "helloWorld"), 0));
        Map<Integer, JSONObject> mu = new HashMap<>();

        ConcreteInterpreter in = new ConcreteInterpreter(new HashMap<>(classes));
        in.run(f, mu);
    }

    @Test
    void fib() {
        Frame f = new Frame(new JSONObject[] { new JSONObject(Map.of("type", "int", "value", 6)) }, new ArrayDeque<>(), new Pair<>(Main.simpleResolve(cls, "fib"), 0));
        Map<Integer, JSONObject> mu = new HashMap<>();

        ConcreteInterpreter in = new ConcreteInterpreter(new HashMap<>(classes));
        in.run(f, mu);
    }

}