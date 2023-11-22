package Week06;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static Week04.ConcreteInterpreter.createNullArray;
import static Week04.ConcreteInterpreter.initialize;
import static Week04.Main.cloneJSONObject;

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
            test("alwaysThrows1", createNullArray(1), null, NullPointerException.class, null);
        }

        @Test
        void alwaysThrows2() {
            Map<Integer, JSONObject> mu = new HashMap<>();

            JSONObject objectref = new JSONObject(Map.of("kind", "class", "name", "java/lang/Object"));
            JSONObject object = initialize(classes, "java/lang/Object", mu);

            mu.put(System.identityHashCode(objectref), object);

            JSONArray arrayvalue = new JSONArray(1);
            arrayvalue.put(0, objectref);

            JSONObject type = cloneJSONObject(objectref);
            JSONObject array = new JSONObject(Map.of("type", type, "value", arrayvalue));

            JSONObject arrayref = new JSONObject(Map.of("kind", "array", "type", type));

            mu.put(System.identityHashCode(arrayref), array);

            JSONObject[] lambda = createNullArray(1);
            lambda[0] = arrayref;

            test("alwaysThrows5", lambda, mu, NullPointerException.class, null);
        }
    }

    @Nested
    @DisplayName("dependsOnAmalgamation Tests")
    class dependsOnAmalgamation {
        @Test
        void dependsOnAmalgamation1() {
            test("dependsOnAmalgamation1", createNullArray(1), null, NullPointerException.class, null);
        }

        @Test
        void dependsOnAmalgamation2() {
            test("dependsOnAmalgamation2", createNullArray(1), null, NullPointerException.class, null);
        }
    }
}
