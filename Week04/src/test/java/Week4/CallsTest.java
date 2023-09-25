package Week4;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import static Week4.Main.getFiles;

class CallsTest {

    static Map<String, String> mapper = new HashMap<>();       // Map<Filename, Classname>
    static Map<String, JSONObject> classes = new HashMap<>();  // Map<Classname, JSONObject>

    static String filename = "Calls.json";

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
    void fib() {
        Interpreter.Method m = new Interpreter.Method(new JSONObject[] { new JSONObject(Map.of("type", "int", "value", 6)) }, new Stack<>(), new Interpreter.Pair<>(mapper.get(filename) + "/" + "fib", 0));
        Map<Integer, JSONObject> mu = new HashMap<>();

        Interpreter in = new Interpreter(new HashMap<>(classes));
        in.run(m, mu);
    }

}