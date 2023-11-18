package Week06;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static Week04.ConcreteInterpreter.createNullArray;
import static Week05.Sign.*;

public class ExceptionalThrowsTest extends TestSuperclass {
    @BeforeAll
    static void initAll() {
        TestSuperclass.initAll("Throws.json");
    }

    @Nested
    @DisplayName("alwaysThrows Tests")
    class alwaysThrows {
        @Test
        void alwaysThrows1() {
            test("alwaysThrows1", createNullArray(0), null, UnsupportedOperationException.class, "Straight forward");
        }

        @Test
        void alwaysThrows2() {
            JSONObject[] lambda = createNullArray(1);
            lambda[0] = new JSONObject(Map.of("type", "int", "value", 6, "sign", Set.of(NEGATIVE, ZERO, POSITIVE)));

            test("alwaysThrows2", lambda, null, UnsupportedOperationException.class, "Error");
        }

        @Test
        void alwaysThrows3() {
            JSONObject[] lambda = createNullArray(1);
            lambda[0] = new JSONObject(Map.of("type", "int", "value", 6, "sign", Set.of(NEGATIVE, ZERO, POSITIVE)));

            test("alwaysThrows3", lambda, null, UnsupportedOperationException.class, "Positive Test");
        }
    }

    @Nested
    @DisplayName("dependsOnLattice Tests")
    class dependsOnLattice {
        @Test
        void dependsOnLattice1() {
            JSONObject[] lambda = createNullArray(2);
            lambda[0] = new JSONObject(Map.of("type", "int", "value", 6, "sign", Set.of(NEGATIVE, ZERO, POSITIVE)));
            lambda[1] = new JSONObject(Map.of("type", "int", "value", 2, "sign", Set.of(NEGATIVE, ZERO, POSITIVE)));

            test("dependsOnLattice1", lambda, null, UnsupportedOperationException.class, "Straight forward");
        }

        @Test
        void dependsOnLattice2() {
            test("dependsOnLattice2", createNullArray(1), null, UnsupportedOperationException.class, "How?");
        }

        @Test
        void dependsOnLattice3() {
            JSONObject[] lambda = createNullArray(2);
            lambda[0] = new JSONObject(Map.of("type", "int", "value", 6, "sign", Set.of(NEGATIVE, ZERO, POSITIVE)));
            lambda[1] = new JSONObject(Map.of("type", "int", "value", 2, "sign", Set.of(NEGATIVE, ZERO, POSITIVE)));

            test("dependsOnLattice3", lambda, null, UnsupportedOperationException.class, "...");
        }
    }

    @Nested
    @DisplayName("neverThrows Tests")
    class neverThrows {
        @Test
        void neverThrows1() {
            JSONObject[] lambda = createNullArray(1);
            lambda[0] = new JSONObject(Map.of("type", "int", "value", 6, "sign", Set.of(NEGATIVE, ZERO, POSITIVE)));

            test("neverThrows1", lambda, null, null, null);
        }

        @Test
        void neverThrows2() {
            JSONObject[] lambda = createNullArray(2);
            lambda[0] = new JSONObject(Map.of("type", "int", "value", 6, "sign", Set.of(NEGATIVE, ZERO, POSITIVE)));
            lambda[1] = new JSONObject(Map.of("type", "int", "value", 2, "sign", Set.of(NEGATIVE, ZERO, POSITIVE)));

            test("neverThrows2", lambda, null, null, null);
        }

        @Test
        void neverThrows3() {
            JSONObject[] lambda = createNullArray(1);
            lambda[0] = new JSONObject(Map.of("type", "int", "value", 6, "sign", Set.of(NEGATIVE, ZERO, POSITIVE)));

            test("neverThrows3", lambda, null, null, null);
        }
    }
}
