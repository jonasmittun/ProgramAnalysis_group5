package Week06;

import Week04.Main;
import Week04.Method;
import Week04.Pair;
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
import static Week05.Sign.POSITIVE;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExceptionalArraysTest {
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

        cls = classes.get(mapper.get("Arrays.json"));
    }

    @Nested
    @DisplayName("alwaysThrows Tests")
    class alwaysThrows {
        @Test
        void alwaysThrows1() {
            Method m = new Method(new JSONObject[]{}, new ArrayDeque<>(), new Pair<>(Week04.Main.simpleResolve(cls, "alwaysThrows1"), 0));
            Map<Integer, JSONObject> mu = new HashMap<>();

            SignInterpreter in = new SignInterpreter(new HashMap<>(classes));
            Exception exception = assertThrows(IndexOutOfBoundsException.class, () -> in.run(m, mu));

            assertTrue(exception.getMessage().contains("Index out of bound"));
        }

        @Test
        void alwaysThrows2() {
            Method m = new Method(new JSONObject[]{new JSONObject(Map.of("type", "int[]"))}, new ArrayDeque<>(), new Pair<>(Week04.Main.simpleResolve(cls, "alwaysThrows2"), 0));
            Map<Integer, JSONObject> mu = new HashMap<>();

            SignInterpreter in = new SignInterpreter(new HashMap<>(classes));
            Exception exception = assertThrows(IndexOutOfBoundsException.class, () -> in.run(m, mu));

            assertTrue(exception.getMessage().contains("Index out of bound"));
        }

        @Test
        void alwaysThrows3() {
            Method m = new Method(new JSONObject[]{}, new ArrayDeque<>(), new Pair<>(Week04.Main.simpleResolve(cls, "alwaysThrows3"), 0));
            Map<Integer, JSONObject> mu = new HashMap<>();

            SignInterpreter in = new SignInterpreter(new HashMap<>(classes));
            Exception exception = assertThrows(IndexOutOfBoundsException.class, () -> in.run(m, mu));

            assertTrue(exception.getMessage().contains("Index out of bound"));
        }

        @Test
        void alwaysThrows4() {
            Method m = new Method(new JSONObject[]{new JSONObject(Map.of("type", "float[]"))}, new ArrayDeque<>(), new Pair<>(Week04.Main.simpleResolve(cls, "alwaysThrows4"), 0));
            Map<Integer, JSONObject> mu = new HashMap<>();

            SignInterpreter in = new SignInterpreter(new HashMap<>(classes));
            Exception exception = assertThrows(IndexOutOfBoundsException.class, () -> in.run(m, mu));

            assertTrue(exception.getMessage().contains("Index out of bound"));
        }

        @Test
        void alwaysThrows5() {
            Method m = new Method(new JSONObject[]{new JSONObject(Map.of("type", "int", "value", 0, "sign", new JSONArray(Set.of(NEGATIVE, ZERO, POSITIVE)))), new JSONObject(Map.of("type", "int", "value", 1, "sign", new JSONArray(Set.of(NEGATIVE, ZERO, POSITIVE))))}, new ArrayDeque<>(), new Pair<>(Main.simpleResolve(cls, "alwaysThrows5"), 0));
            Map<Integer, JSONObject> mu = new HashMap<>();

            SignInterpreter in = new SignInterpreter(new HashMap<>(classes));
            Exception exception = assertThrows(IndexOutOfBoundsException.class, () -> in.run(m, mu));

            assertTrue(exception.getMessage().contains("Index out of bound"));
        }
    }

    @Nested
    @DisplayName("dependsOnLattice Tests")
    class dependsOnLattice {
        /*@Test
        void itDependsOnLattice1() {
            Method m = new Method(new JSONObject[]{}, new ArrayDeque<>(), new Pair<>(Main.simpleResolve(cls, "itDependsOnLattice1"), 0));
            Map<Integer, JSONObject> mu = new HashMap<>();

            SignInterpreter in = new SignInterpreter(new HashMap<>(classes));
            Exception exception = assertThrows(ArithmeticException.class, () -> in.run(m, mu));

            assertTrue(exception.getMessage().contains("Illegal divide by zero"));
        }

        @Test
        void itDependsOnLattice2() {
            Method m = new Method(new JSONObject[]{}, new ArrayDeque<>(), new Pair<>(Main.simpleResolve(cls, "itDependsOnLattice2"), 0));
            Map<Integer, JSONObject> mu = new HashMap<>();

            SignInterpreter in = new SignInterpreter(new HashMap<>(classes));
            Exception exception = assertThrows(ArithmeticException.class, () -> in.run(m, mu));

            assertTrue(exception.getMessage().contains("Illegal divide by zero"));
        }

        @Test
        void itDependsOnLattice3() {
            Method m = new Method(new JSONObject[]{new JSONObject(Map.of("type", "int", "value", 6, "sign", new JSONArray(Set.of(NEGATIVE, ZERO, POSITIVE)))), new JSONObject(Map.of("type", "int", "value", 0, "sign", new JSONArray(Set.of(NEGATIVE, ZERO, POSITIVE))))}, new ArrayDeque<>(), new Pair<>(Main.simpleResolve(cls, "itDependsOnLattice3"), 0));
            Map<Integer, JSONObject> mu = new HashMap<>();

            SignInterpreter in = new SignInterpreter(new HashMap<>(classes));
            Exception exception = assertThrows(ArithmeticException.class, () -> in.run(m, mu));

            assertTrue(exception.getMessage().contains("Illegal divide by zero"));
        }

        @Test
        void itDependsOnLattice4() {
            Method m = new Method(new JSONObject[]{}, new ArrayDeque<>(), new Pair<>(Main.simpleResolve(cls, "itDependsOnLattice4"), 0));
            Map<Integer, JSONObject> mu = new HashMap<>();

            SignInterpreter in = new SignInterpreter(new HashMap<>(classes));
            Exception exception = assertThrows(ArithmeticException.class, () -> in.run(m, mu));

            assertTrue(exception.getMessage().contains("Illegal divide by zero"));
        }*/
    }
}
