package Week06;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class ExceptionalNullArraysTest extends TestSuperclass {

    @BeforeAll
    static void initAll() {
        TestSuperclass.initAll("NullArrays.json");
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
            test("alwaysThrows2", new JSONObject[]{new JSONObject(Map.of("type", "Object[]", "value", new Object[]{}))}, NullPointerException.class, null);
        }
    }

    @Nested
    @DisplayName("dependsOnAmalgamation Tests")
    class dependsOnAmalgamation {
        @Test
        void dependsOnAmalgamation1() {
            // TODO
            // test("dependsOnAmalgamation1", new JSONObject[]{}, NullPointerException.class, null);
        }

        @Test
        void dependsOnAmalgamation2() {
            // TODO
            // test("dependsOnAmalgamation2", new JSONObject[]{}, NullPointerException.class, null);
        }
    }
}
