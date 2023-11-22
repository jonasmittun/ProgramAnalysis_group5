package Project;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static Week04.ConcreteInterpreter.createNullArray;
import static Week04.ConcreteInterpreter.initializeString;
import static Week04.Main.cloneJSONObject;

public class ExceptionalNullTest extends TestSuperClass {

    @BeforeAll
    static void initAll() {
        TestSuperClass.initAll("Null.json");
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
            JSONObject object = cloneJSONObject(classes.get("java/lang/Object"));
            JSONObject objectref = new JSONObject(Map.of("kind", "class", "name", "java/lang/Object"));

            Map<Integer, JSONObject> mu = new HashMap<>();
            mu.put(System.identityHashCode(objectref), object);

            JSONObject[] lambda = createNullArray(2);
            lambda[0] = objectref;

            test("alwaysThrows2", lambda, mu, NullPointerException.class, null);
        }

        @Test
        void alwaysThrows3() {
            JSONObject object = cloneJSONObject(classes.get("java/lang/Object"));
            JSONObject objectref = new JSONObject(Map.of("kind", "class", "name", "java/lang/Object"));

            Map<Integer, JSONObject> mu = new HashMap<>();
            mu.put(System.identityHashCode(objectref), object);

            JSONObject[] lambda = createNullArray(2);
            lambda[0] = objectref;

            test("alwaysThrows3", lambda, mu, NullPointerException.class, null);
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
            JSONObject object = cloneJSONObject(classes.get("java/lang/Object"));
            JSONObject objectref = new JSONObject(Map.of("kind", "class", "name", "java/lang/Object"));

            Map<Integer, JSONObject> mu = new HashMap<>();
            mu.put(System.identityHashCode(objectref), object);

            JSONObject[] lambda = createNullArray(2);
            lambda[0] = objectref;

            test("neverThrows2", lambda, mu, null, null);
        }

        @Test
        void neverThrows3() {
            JSONObject integer1 = cloneJSONObject(classes.get("java/lang/Integer"));
            JSONObject integer1ref = new JSONObject(Map.of("kind", "class", "name", "java/lang/Integer"));

            JSONObject integer2 = cloneJSONObject(classes.get("java/lang/Integer"));
            JSONObject integer2ref = new JSONObject(Map.of("kind", "class", "name", "java/lang/Integer"));

            Map<Integer, JSONObject> mu = new HashMap<>();
            mu.put(System.identityHashCode(integer1ref), integer1);
            mu.put(System.identityHashCode(integer2ref), integer2);

            JSONObject[] lambda = createNullArray(2);
            lambda[0] = integer1ref;
            lambda[1] = integer2ref;

            test("neverThrows3", lambda, mu, null, null);
        }

        @Test
        void neverThrows4() {
            JSONObject object = cloneJSONObject(classes.get("eu/bogoe/dtu/exceptional/Null"));
            JSONObject objectref = new JSONObject(Map.of("kind", "class", "name", "eu/bogoe/dtu/exceptional/Null"));

            Map<Integer, JSONObject> mu = new HashMap<>();
            mu.put(System.identityHashCode(objectref), object);

            JSONObject[] lambda = createNullArray(1);
            lambda[0] = objectref;

            test("neverThrows4", lambda, mu, null, null);
        }

        @Test
        void neverThrows5() {
            Map<Integer, JSONObject> mu = new HashMap<>();

            JSONObject sref = initializeString(classes, "Test", mu);
            JSONObject notYourProblemref = initializeString(classes, "NotYourProblem", mu);

            JSONObject[] lambda = createNullArray(2);
            lambda[0] = sref;
            lambda[1] = notYourProblemref;

            test("neverThrows5", lambda, mu, null, null);
        }
    }

    @Test
    void interestingCase() {
        JSONObject object = cloneJSONObject(classes.get("java/lang/Object"));
        JSONObject objectref = new JSONObject(Map.of("kind", "class", "name", "java/lang/Object"));

        Map<Integer, JSONObject> mu = new HashMap<>();
        mu.put(System.identityHashCode(objectref), object);

        JSONObject[] lambda = createNullArray(2);
        lambda[0] = objectref;

        test("interestingCase", lambda, mu, null, null);
    }
}
