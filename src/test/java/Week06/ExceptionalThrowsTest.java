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
            test("alwaysThrows1", new JSONObject[]{}, UnsupportedOperationException.class, "Straight forward");
        }

        @Test
        void alwaysThrows2() {
            test("alwaysThrows2", new JSONObject[]{new JSONObject(Map.of("type", "int", "value", 1))}, UnsupportedOperationException.class, "Error");
        }

        @Test
        void alwaysThrows3() {
            test("alwaysThrows3", new JSONObject[]{new JSONObject(Map.of("type", "int", "value", 0, "sign", new JSONArray(Set.of(NEGATIVE, ZERO, POSITIVE))))}, UnsupportedOperationException.class, "Positive Test");
        }
    }

    @Nested
    @DisplayName("dependsOnLattice Tests")
    class dependsOnLattice {
        @Test
        void dependsOnLattice1() {
            test("dependsOnLattice1", new JSONObject[]{new JSONObject(Map.of("type", "int", "value", 1, "sign", new JSONArray(Set.of(NEGATIVE, ZERO, POSITIVE)))), new JSONObject(Map.of("type", "int", "value", 0, "sign", new JSONArray(Set.of(NEGATIVE, ZERO, POSITIVE))))}, UnsupportedOperationException.class, "Straight forward");
        }

        @Test
        void dependsOnLattice2() {
            test("dependsOnLattice2", new JSONObject[]{}, UnsupportedOperationException.class, "How?");
        }

        @Test
        void dependsOnLattice3() {
            test("dependsOnLattice3", new JSONObject[]{new JSONObject(Map.of("type", "int", "value", 2, "sign", new JSONArray(Set.of(NEGATIVE, ZERO, POSITIVE)))), new JSONObject(Map.of("type", "int", "value", -2, "sign", new JSONArray(Set.of(NEGATIVE, ZERO, POSITIVE))))}, UnsupportedOperationException.class, "...");
        }
    }

    @Nested
    @DisplayName("neverThrows Tests")
    class neverThrows {
        @Test
        void neverThrows1() {
            test("neverThrows1", new JSONObject[]{new JSONObject(Map.of("type", "int", "value", 1, "sign", new JSONArray(Set.of(NEGATIVE, ZERO, POSITIVE))))}, null, null);
        }

        @Test
        void neverThrows2() {
            test("neverThrows2", new JSONObject[]{new JSONObject(Map.of("type", "int", "value", 1, "sign", new JSONArray(Set.of(NEGATIVE, ZERO, POSITIVE)))), new JSONObject(Map.of("type", "int", "value", -1, "sign", new JSONArray(Set.of(NEGATIVE, ZERO, POSITIVE))))}, null, null);
        }

        @Test
        void neverThrows3() {
            test("neverThrows3", new JSONObject[]{new JSONObject(Map.of("type", "int", "value", -1, "sign", new JSONArray(Set.of(NEGATIVE, ZERO, POSITIVE))))}, null, null);
        }
    }
}
