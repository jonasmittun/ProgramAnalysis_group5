package Week06;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static Week05.Sign.*;
import static Week05.Sign.POSITIVE;

public class ExceptionalArraysTest extends TestSuperclass {

    @BeforeAll
    static void initAll() {
        TestSuperclass.initAll("Arrays.json");
    }

    @Nested
    @DisplayName("alwaysThrows Tests")
    class alwaysThrows {
        @Test
        void alwaysThrows1() {
            test("alwaysThrows1", new JSONObject[]{}, IndexOutOfBoundsException.class, "Index out of bound");
        }

        @Test
        void alwaysThrows2() {
            test("alwaysThrows2", new JSONObject[]{new JSONObject(Map.of("type", "int[]"))}, IndexOutOfBoundsException.class, "Index out of bound");
        }

        @Test
        void alwaysThrows3() {
            test("alwaysThrows3", new JSONObject[]{}, IndexOutOfBoundsException.class, "Index out of bound");
        }

        @Test
        void alwaysThrows4() {
            test("alwaysThrows4", new JSONObject[]{new JSONObject(Map.of("type", "float[]"))}, IndexOutOfBoundsException.class, "Index out of bound");
        }

        @Test
        void alwaysThrows5() {
            test("alwaysThrows5", new JSONObject[]{new JSONObject(Map.of("type", "int", "value", 0, "sign", new JSONArray(Set.of(NEGATIVE, ZERO, POSITIVE)))), new JSONObject(Map.of("type", "int", "value", 1, "sign", new JSONArray(Set.of(NEGATIVE, ZERO, POSITIVE))))}, IndexOutOfBoundsException.class, "Index out of bound");
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
