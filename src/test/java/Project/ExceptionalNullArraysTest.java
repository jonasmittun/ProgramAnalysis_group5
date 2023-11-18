package Project;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static Week04.ConcreteInterpreter.createNullArray;

public class ExceptionalNullArraysTest extends TestSuperClass {

    @BeforeAll
    static void initAll() {
        TestSuperClass.initAll("NullArrays.json");
    }

    @Nested
    @DisplayName("alwaysThrows Tests")
    class alwaysThrows {
        @Test
        void alwaysThrows1() {
            test("alwaysThrows1", createNullArray(1), null, NullPointerException.class, null);
        }

        /*
        // TODO
        @Test
        void alwaysThrows2() {
            test("alwaysThrows2", new JSONObject[]{new JSONObject(Map.of("type", "Object[]", "value", new Object[]{}))}, NullPointerException.class, null);
        }
        */
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
