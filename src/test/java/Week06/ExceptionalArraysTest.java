package Week06;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static Week04.ConcreteInterpreter.createNullArray;
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
            test("alwaysThrows1", createNullArray(1), null, ArithmeticException.class, "Illegal divide by zero");
        }

        @Test
        void alwaysThrows2() {
            JSONObject arrayref = new JSONObject(Map.of("kind", "array", "type", "int"));

            JSONArray array = new JSONArray(3);
            for(int i = 0; i < array.length(); i++) {
                array.put(i, new JSONObject(Map.of("type", "int", "value", i, "sign", Set.of(toSign(i)))));
            }

            Map<Integer, JSONObject> mu = new HashMap<>();
            mu.put(System.identityHashCode(arrayref), new JSONObject(Map.of("type", "int", "value", array)));

            JSONObject[] lambda = createNullArray(1);
            lambda[0] = arrayref;

            test("alwaysThrows2", lambda, mu, ArithmeticException.class, "Illegal divide by zero");
        }

        @Test
        void alwaysThrows3() {
            test("alwaysThrows3", createNullArray(2), null, ArithmeticException.class, "Illegal divide by zero");
        }

        @Test
        void alwaysThrows4() {
            JSONObject arrayref = new JSONObject(Map.of("kind", "array", "type", "float"));

            JSONArray array = new JSONArray(3);
            for(int i = 0; i < array.length(); i++) {
                array.put(i, new JSONObject(Map.of("type", "float", "value", (float) i, "sign", Set.of(toSign(i)))));
            }

            Map<Integer, JSONObject> mu = new HashMap<>();
            mu.put(System.identityHashCode(arrayref), new JSONObject(Map.of("type", "float", "value", array)));

            JSONObject[] lambda = createNullArray(1);
            lambda[0] = arrayref;

            test("alwaysThrows4", lambda, mu, ArithmeticException.class, "Illegal divide by zero");
        }

        @Test
        void alwaysThrows5() {
            JSONObject[] lambda = createNullArray(3);
            lambda[0] = new JSONObject(Map.of("type", "int", "value", 6, "sign", Set.of(NEGATIVE, ZERO, POSITIVE)));
            lambda[1] = new JSONObject(Map.of("type", "int", "value", 0, "sign", Set.of(NEGATIVE, ZERO, POSITIVE)));

            test("alwaysThrows5", lambda, null, ArithmeticException.class, "Illegal divide by zero");
        }
    }

    @Nested
    @DisplayName("dependsOnLattice Tests")
    class dependsOnLattice {
        @Test
        void dependsOnLattice1() {
            JSONObject arrayref = new JSONObject(Map.of("kind", "array", "type", "int"));

            JSONArray array = new JSONArray(3);
            for(int i = 0; i < array.length(); i++) {
                array.put(i, new JSONObject(Map.of("type", "int", "value", i, "sign", Set.of(toSign(i)))));
            }

            Map<Integer, JSONObject> mu = new HashMap<>();
            mu.put(System.identityHashCode(arrayref), new JSONObject(Map.of("type", "int", "value", array)));

            JSONObject[] lambda = createNullArray(2);
            lambda[0] = arrayref;
            lambda[1] = new JSONObject(Map.of("type", "int", "value", "1", "sign", Set.of(POSITIVE)));

            test("dependsOnLattice1", lambda, mu, IndexOutOfBoundsException.class, "Index out of bound");
        }

        @Test
        void dependsOnLattice2() {
            JSONObject arrayref = new JSONObject(Map.of("kind", "array", "type", "int"));

            JSONArray array = new JSONArray(3);
            for(int i = 0; i < array.length(); i++) {
                array.put(i, new JSONObject(Map.of("type", "int", "value", i, "sign", Set.of(toSign(i)))));
            }

            Map<Integer, JSONObject> mu = new HashMap<>();
            mu.put(System.identityHashCode(arrayref), new JSONObject(Map.of("type", "int", "value", array)));

            JSONObject[] lambda = createNullArray(2);
            lambda[0] = arrayref;

            test("dependsOnLattice2", lambda, mu, IndexOutOfBoundsException.class, "Index out of bound");
        }

        @Test
        void dependsOnLattice3() {
            JSONObject arrayref = new JSONObject(Map.of("kind", "array", "type", "float"));

            JSONArray array = new JSONArray(3);
            for(int i = 0; i < array.length(); i++) {
                array.put(i, new JSONObject(Map.of("type", "float", "value", (float) i, "sign", Set.of(toSign(i)))));
            }

            Map<Integer, JSONObject> mu = new HashMap<>();
            mu.put(System.identityHashCode(arrayref), new JSONObject(Map.of("type", "float", "value", array)));

            JSONObject[] lambda = createNullArray(1);
            lambda[0] = arrayref;

            test("dependsOnLattice3", lambda, mu, IndexOutOfBoundsException.class, "Index out of bound");
        }

        @Test
        void dependsOnLattice4() {
            JSONObject arrayref = new JSONObject(Map.of("kind", "array", "type", "int"));

            JSONArray array = new JSONArray(3);
            for(int i = 0; i < array.length(); i++) {
                array.put(i, new JSONObject(Map.of("type", "int", "value", i, "sign", Set.of(toSign(i)))));
            }

            Map<Integer, JSONObject> mu = new HashMap<>();
            mu.put(System.identityHashCode(arrayref), new JSONObject(Map.of("type", "int", "value", array)));

            JSONObject[] lambda = createNullArray(1);
            lambda[0] = arrayref;

            test("dependsOnLattice4", lambda, mu, IndexOutOfBoundsException.class, "Index out of bound");
        }

        @Test
        void dependsOnLattice5() {
            JSONObject[] lambda = createNullArray(2);
            lambda[0] = new JSONObject(Map.of("type", "int", "value", 1, "sign", Set.of(POSITIVE)));

            test("dependsOnLattice5", lambda, null, IndexOutOfBoundsException.class, "Index out of bound");
        }
    }

    @Nested
    @DisplayName("neverThrows Tests")
    class neverThrows {
        @Test
        void neverThrows1() {
            test("neverThrows1", createNullArray(1), null, null, null);
        }

        @Test
        void neverThrows2() {
            test("neverThrows2", createNullArray(1), null, null, null);
        }

        @Test
        void neverThrows3() {
            test("neverThrows3", createNullArray(3), null, null, null);
        }
    }
}
