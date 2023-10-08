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
        @Test
        void dependsOnLattice1() {
            test("dependsOnLattice1", new JSONObject[]{new JSONObject(Map.of("type", "int[]", "value", new int[]{0, 1})), new JSONObject(Map.of("type", "int", "value", 1, "sign", new JSONArray(POSITIVE)))}, IndexOutOfBoundsException.class, "Index out of bound");
        }

        @Test
        void dependsOnLattice2() {
            test("dependsOnLattice2", new JSONObject[]{new JSONObject(Map.of("type", "int[]", "value", new int[]{0, 1}))}, IndexOutOfBoundsException.class, "Index out of bound");
        }

        @Test
        void dependsOnLattice3() {
            test("dependsOnLattice3", new JSONObject[]{new JSONObject(Map.of("type", "float[]", "value", new float[]{5.2f}))}, IndexOutOfBoundsException.class, "Index out of bound");
        }

        @Test
        void dependsOnLattice4() {
            test("dependsOnLattice4", new JSONObject[]{new JSONObject(Map.of("type", "int[]", "value", new int[]{4, 3, 2, 1, 0}))}, IndexOutOfBoundsException.class, "Index out of bound");
        }
    }

    @Nested
    @DisplayName("neverThrows Tests")
    class neverThrows {
        @Test
        void neverThrows1() {
            test("neverThrows1", new JSONObject[1], null, null);
        }

        @Test
        void neverThrows2() {
            test("neverThrows2", new JSONObject[1], null, null);
        }

        @Test
        void neverThrows3() {
            test("neverThrows3", new JSONObject[3], null, null);
        }
    }
}
