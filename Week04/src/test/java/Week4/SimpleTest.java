package Week4;

import static Week4.Main.getFiles;
import static org.junit.jupiter.api.Assertions.*;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import java.util.*;

class SimpleTest {

    static Map<String, String> mapper = new HashMap<>();       // Map<Filename, Classname>
    static Map<String, JSONObject> classes = new HashMap<>();  // Map<Classname, JSONObject>

    @BeforeAll
    static void initAll() {
        String path = "src\\test\\decompiled\\dtu\\compute\\exec";
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
    void identity() {
        Interpreter in = new Interpreter(new HashMap<>(classes));
        in.run(new Interpreter.Method(new JSONObject[] { new JSONObject(Map.of("type", "int", "value", 7)) }, new Stack<>(), new Interpreter.Pair<>(mapper.get("Simple.json") + "/" + "identity", 0)));
    }

    @Test
    void add() {
        Interpreter in = new Interpreter(new HashMap<>(classes));
        in.run(new Interpreter.Method(new JSONObject[] { new JSONObject(Map.of("type", "int", "value", 1)), new JSONObject(Map.of("type", "int", "value", 2)) }, new Stack<>(), new Interpreter.Pair<>(mapper.get("Simple.json") + "/" + "add", 0)));
    }

    @Test
    void min() {
        Interpreter in = new Interpreter(new HashMap<>(classes));
        in.run(new Interpreter.Method(new JSONObject[] { new JSONObject(Map.of("type", "int", "value", 1)), new JSONObject(Map.of("type", "int", "value", 2)) }, new Stack<>(), new Interpreter.Pair<>(mapper.get("Simple.json") + "/" + "min", 0)));
    }

    @Test
    void factorial() {
        Interpreter in = new Interpreter(new HashMap<>(classes));
        in.run(new Interpreter.Method(new JSONObject[] { new JSONObject(Map.of("type", "int", "value", 5)), new JSONObject(Map.of("type", "int", "value", 2)) }, new Stack<>(), new Interpreter.Pair<>(mapper.get("Simple.json") + "/" + "factorial", 0)));
    }


}