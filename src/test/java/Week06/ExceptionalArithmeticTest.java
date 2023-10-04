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

public class ExceptionalArithmeticTest extends TestSuperclass {

    @BeforeAll
     static void initAll() {
        TestSuperclass.initAll("Arithmetics.json");
    }

    @Nested
    @DisplayName("alwaysThrows Tests")
    class alwaysThrows {
        @Test
        void alwaysThrows1() {
            test("alwaysThrows1", new JSONObject[]{}, ArithmeticException.class, "Illegal divide by zero");
        }

        @Test
        void alwaysThrows2() {
            test("alwaysThrows2", new JSONObject[] { new JSONObject(Map.of("type", "int", "value", 6, "sign", new JSONArray(Set.of(NEGATIVE, ZERO, POSITIVE)))), null }, ArithmeticException.class, "Illegal divide by zero");
        }

        @Test
        void alwaysThrows3() {
            test("alwaysThrows3", new JSONObject[]{new JSONObject(Map.of("type", "int", "value", 6, "sign", new JSONArray(Set.of(NEGATIVE, ZERO, POSITIVE)))), new JSONObject(Map.of("type", "int", "value", 0, "sign", new JSONArray(Set.of(NEGATIVE, ZERO, POSITIVE))))}, ArithmeticException.class, "Illegal divide by zero");
        }

        @Test
        void alwaysThrows4() {
            test("alwaysThrows4", new JSONObject[]{new JSONObject(Map.of("type", "int", "value", 6, "sign", new JSONArray(Set.of(NEGATIVE, ZERO, POSITIVE)))), new JSONObject(Map.of("type", "int", "value", 0, "sign", new JSONArray(Set.of(NEGATIVE, ZERO, POSITIVE))))}, ArithmeticException.class, "Illegal divide by zero");
        }

        @Test
        void alwaysThrows5() {
            test("alwaysThrows5", new JSONObject[]{new JSONObject(Map.of("type", "int", "value", 6, "sign", new JSONArray(Set.of(NEGATIVE, ZERO, POSITIVE)))), new JSONObject(Map.of("type", "int", "value", 0, "sign", new JSONArray(Set.of(NEGATIVE, ZERO, POSITIVE))))}, ArithmeticException.class, "Illegal divide by zero");
        }
    }

    @Nested
    @DisplayName("itDependsOnLattice Tests")
    class itDependsOnLattice {
        @Test
        void itDependsOnLattice1() {
            test("itDependsOnLattice1", new JSONObject[]{}, ArithmeticException.class, "Illegal divide by zero");
        }

        @Test
        void itDependsOnLattice2() {
            test("itDependsOnLattice2", new JSONObject[]{}, ArithmeticException.class, "Illegal divide by zero");
        }

        @Test
        void itDependsOnLattice3() {
            test("itDependsOnLattice3", new JSONObject[]{new JSONObject(Map.of("type", "int", "value", 6, "sign", new JSONArray(Set.of(NEGATIVE, ZERO, POSITIVE)))), new JSONObject(Map.of("type", "int", "value", 0, "sign", new JSONArray(Set.of(NEGATIVE, ZERO, POSITIVE))))}, ArithmeticException.class, "Illegal divide by zero");
        }

        @Test
        void itDependsOnLattice4() {
            test("itDependsOnLattice4", new JSONObject[]{}, ArithmeticException.class, "Illegal divide by zero");
        }

    }

    @Nested
    @DisplayName("neverThrows Tests")
    class neverThrows {
        @Test
        void neverThrows1() {
            test("neverThrows1", new JSONObject[] { new JSONObject(Map.of("type", "int", "value", 6, "sign", new JSONArray(Set.of(NEGATIVE, ZERO, POSITIVE)))), null }, null, null);
        }
    }

    @Test
    void speedVsPrecision() {
        test("speedVsPrecision", new JSONObject[]{}, ArithmeticException.class, "Illegal divide by zero");
    }


}
