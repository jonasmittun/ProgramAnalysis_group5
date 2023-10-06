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

public class ExceptionalNullTest extends TestSuperclass {

    @BeforeAll
    static void initAll() {
        TestSuperclass.initAll("Null.json");
    }

    @Nested
    @DisplayName("alwaysThrows Tests")
    class alwaysThrows {
        @Test
        void alwaysThrows1() {
            test("alwaysThrows1", new JSONObject[]{}, NullPointerException.class, null);
        }

        @Test
        void alwaysThrows2() {
            test("alwaysThrows2", new JSONObject[]{new JSONObject(Map.of("type", "Object", "value", new Object())), null}, NullPointerException.class, null);
        }

        @Test
        void alwaysThrows3() {
            test("alwaysThrows3", new JSONObject[]{new JSONObject(Map.of("type", "Object", "value", new Object())), null}, NullPointerException.class, null);
        }
    }

    @Nested
    @DisplayName("neverThrows Tests")
    class neverThrows {
        @Test
        void neverThrows1() {
            test("neverThrows1", new JSONObject[]{}, null, null);
        }

        @Test
        void neverThrows2() {
            test("neverThrows2", new JSONObject[]{new JSONObject(Map.of("type", "Object", "value", new Object()))}, null, null);
        }

        @Test
        void neverThrows3() {
            test("neverThrows3", new JSONObject[]{new JSONObject(Map.of("type", "Integer", "value", 1, "sign", new JSONArray(Set.of(NEGATIVE, ZERO, POSITIVE)))), new JSONObject(Map.of("type", "Integer", "value", 2, "sign", new JSONArray(Set.of(NEGATIVE, ZERO, POSITIVE))))}, null, null);
        }

        // TODO give null parameter
        /*void neverThrows4() {
            test("neverThrows4", new JSONObject[]{new JSONObject(Map.of("type", "Null", "value", new Null()))}, null, null);
        }*/

        @Test
        void neverThrows5() {
            test("neverThrows5", new JSONObject[]{new JSONObject(Map.of("type", "String", "value", "a")), new JSONObject(Map.of("type", "String", "value", "b"))}, null, null);
        }
    }

    void interestingCase() {
        test("interestingCase", new JSONObject[]{new JSONObject(Map.of("type", "Object", "value", new Object()))}, null, null);
    }
}
