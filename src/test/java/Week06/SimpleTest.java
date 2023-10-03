package Week06;

import Week04.Main;
import Week04.Method;
import Week04.Pair;
import Week06.SignInterpreter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static Week01.Main.getFiles;
import static Week05.Sign.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        Method m = new Method(new JSONObject[] {}, new ArrayDeque<>(), new Pair<>(Main.simpleResolve(cls, "noop"), 0));
        Map<Integer, JSONObject> mu = new HashMap<>();

        SignInterpreter in = new SignInterpreter(new HashMap<>(classes));
        in.run(m, mu);
    }

    @Test
    void zero() {
        Method m = new Method(new JSONObject[] {}, new ArrayDeque<>(), new Pair<>(Main.simpleResolve(cls, "zero"), 0));
        Map<Integer, JSONObject> mu = new HashMap<>();

        SignInterpreter in = new SignInterpreter(new HashMap<>(classes));
        in.run(m, mu);
    }

    @Test
    void hundredAndTwo() {
        Method m = new Method(new JSONObject[] {}, new ArrayDeque<>(), new Pair<>(Main.simpleResolve(cls, "hundredAndTwo"), 0));
        Map<Integer, JSONObject> mu = new HashMap<>();

        SignInterpreter in = new SignInterpreter(new HashMap<>(classes));
        in.run(m, mu);
    }

    @Test
    void identity() {
        Method m = new Method(new JSONObject[] { new JSONObject(Map.of("type", "int", "value", 7)) }, new ArrayDeque<>(), new Pair<>(Main.simpleResolve(cls, "identity"), 0));
        Map<Integer, JSONObject> mu = new HashMap<>();

        SignInterpreter in = new SignInterpreter(new HashMap<>(classes));
        in.run(m, mu);
    }

    @Nested
    @DisplayName("add Tests")
    class Add {
        @Test
        void add_int_input() {
            Method m = new Method(new JSONObject[] { new JSONObject(Map.of("type", "int", "value", -1)), new JSONObject(Map.of("type", "int", "value", 2)) }, new ArrayDeque<>(), new Pair<>(Main.simpleResolve(cls, "add"), 0));
            Map<Integer, JSONObject> mu = new HashMap<>();

            SignInterpreter in = new SignInterpreter(new HashMap<>(classes));
            in.run(m, mu);
        }
        @Test
        void add_full_sets_input() {
            Method m = new Method(new JSONObject[] { new JSONObject(Map.of("type", "int", "value", 1, "sign", new JSONArray(Set.of(NEGATIVE, ZERO, POSITIVE)))), new JSONObject(Map.of("type", "int", "value", 2, "sign", new JSONArray(Set.of(NEGATIVE, ZERO, POSITIVE)))) }, new ArrayDeque<>(), new Pair<>(Main.simpleResolve(cls, "add"), 0));
            Map<Integer, JSONObject> mu = new HashMap<>();

            SignInterpreter in = new SignInterpreter(new HashMap<>(classes));
            in.run(m, mu);
        }
    }


    @Nested
    @DisplayName("min Tests")
    class Min {
        @Test
        void min_int_input() {
            Method m = new Method(new JSONObject[] { new JSONObject(Map.of("type", "int", "value", 1)), new JSONObject(Map.of("type", "int", "value", 2)) }, new ArrayDeque<>(), new Pair<>(Main.simpleResolve(cls, "min"), 0));
            Map<Integer, JSONObject> mu = new HashMap<>();

            SignInterpreter in = new SignInterpreter(new HashMap<>(classes));
            in.run(m, mu);
        }
        @Test
        void min_full_sets_input() {
            Method m = new Method(new JSONObject[] { new JSONObject(Map.of("type", "int", "value", 1, "sign", new JSONArray(Set.of(NEGATIVE, ZERO, POSITIVE)))), new JSONObject(Map.of("type", "int", "value", 2, "sign", new JSONArray(Set.of(NEGATIVE, ZERO, POSITIVE)))) }, new ArrayDeque<>(), new Pair<>(Main.simpleResolve(cls, "min"), 0));
            Map<Integer, JSONObject> mu = new HashMap<>();

            SignInterpreter in = new SignInterpreter(new HashMap<>(classes));
            in.run(m, mu);
        }
    }

    @Nested
    @DisplayName("div Tests")
    class div {
        @Test
        void div_int_input() {
            Method m = new Method(new JSONObject[] { new JSONObject(Map.of("type", "int", "value", 4)), new JSONObject(Map.of("type", "int", "value", 2)) }, new ArrayDeque<>(), new Pair<>(Main.simpleResolve(cls, "div"), 0));
            Map<Integer, JSONObject> mu = new HashMap<>();

            SignInterpreter in = new SignInterpreter(new HashMap<>(classes));
            in.run(m, mu);
        }

        @Test
        void div_int_input_fail() {
            Method m = new Method(new JSONObject[] { new JSONObject(Map.of("type", "int", "value", 4)), new JSONObject(Map.of("type", "int", "value", 0)) }, new ArrayDeque<>(), new Pair<>(Main.simpleResolve(cls, "div"), 0));
            Map<Integer, JSONObject> mu = new HashMap<>();

            SignInterpreter in = new SignInterpreter(new HashMap<>(classes));
            Exception exception = assertThrows(java.lang.ArithmeticException.class, () -> in.run(m, mu));

            assertTrue(exception.getMessage().contains("Illegal divide by zero"));
        }

        @Test
        void div_sets_input() {
            Method m = new Method(new JSONObject[] { new JSONObject(Map.of("type", "int", "value", 1, "sign", new JSONArray(Set.of(NEGATIVE, ZERO, POSITIVE)))), new JSONObject(Map.of("type", "int", "value", 2, "sign", new JSONArray(Set.of(NEGATIVE, POSITIVE)))) }, new ArrayDeque<>(), new Pair<>(Main.simpleResolve(cls, "div"), 0));
            Map<Integer, JSONObject> mu = new HashMap<>();

            SignInterpreter in = new SignInterpreter(new HashMap<>(classes));
            in.run(m, mu);
        }

        @Test
        void div_sets_input_fail() {
            Method m = new Method(new JSONObject[] { new JSONObject(Map.of("type", "int", "value", 1, "sign", new JSONArray(Set.of(NEGATIVE, ZERO, POSITIVE)))), new JSONObject(Map.of("type", "int", "value", 0, "sign", new JSONArray(Set.of(ZERO)))) }, new ArrayDeque<>(), new Pair<>(Main.simpleResolve(cls, "div"), 0));
            Map<Integer, JSONObject> mu = new HashMap<>();

            SignInterpreter in = new SignInterpreter(new HashMap<>(classes));
            Exception exception = assertThrows(java.lang.ArithmeticException.class, () -> in.run(m, mu));

            assertTrue(exception.getMessage().contains("Illegal divide by zero"));
        }

        @Test
        void div_full_sets_input_fail() {
            Method m = new Method(new JSONObject[] { new JSONObject(Map.of("type", "int", "value", 1, "sign", new JSONArray(Set.of(NEGATIVE, ZERO, POSITIVE)))), new JSONObject(Map.of("type", "int", "value", 0, "sign", new JSONArray(Set.of(NEGATIVE, ZERO, POSITIVE)))) }, new ArrayDeque<>(), new Pair<>(Main.simpleResolve(cls, "div"), 0));
            Map<Integer, JSONObject> mu = new HashMap<>();

            SignInterpreter in = new SignInterpreter(new HashMap<>(classes));
            Exception exception = assertThrows(java.lang.ArithmeticException.class, () -> in.run(m, mu));

            assertTrue(exception.getMessage().contains("Illegal divide by zero"));
        }
    }


    @Nested
    @DisplayName("Factorial tests")
    class Factorial {
        @Test
        void factorial_int() {
            Method m = new Method(new JSONObject[] { new JSONObject(Map.of("type", "int", "value", 6)), new JSONObject(Map.of("type", "int", "value", 0)) }, new ArrayDeque<>(), new Pair<>(Main.simpleResolve(cls, "factorial"), 0));
            Map<Integer, JSONObject> mu = new HashMap<>();

            SignInterpreter in = new SignInterpreter(new HashMap<>(classes));
            in.run(m, mu);
        }

        @Test
        void factorial_sets() {
            Method m = new Method(new JSONObject[] { new JSONObject(Map.of("type", "int", "value", 6, "sign", new JSONArray(Set.of(NEGATIVE, ZERO, POSITIVE)))), new JSONObject(Map.of("type", "int", "value", 0, "sign", new JSONArray(Set.of(NEGATIVE, ZERO, POSITIVE)))) }, new ArrayDeque<>(), new Pair<>(Main.simpleResolve(cls, "factorial"), 0));
            Map<Integer, JSONObject> mu = new HashMap<>();

            SignInterpreter in = new SignInterpreter(new HashMap<>(classes));
            in.run(m, mu);
        }
    }
}