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
            test("alwaysThrows1", createNullArray(0), null, ArithmeticException.class, "Illegal divide by zero");
        }

        @Test
        void alwaysThrows2() {
            JSONObject[] lambda = createNullArray(2);
            lambda[0] = new JSONObject(Map.of("type", "int", "value", 6, "sign", Set.of(NEGATIVE, ZERO, POSITIVE)));

            test("alwaysThrows2", lambda, null, ArithmeticException.class, "Illegal divide by zero");
        }

        @Test
        void alwaysThrows3() {
            JSONObject[] lambda = createNullArray(2);
            lambda[0] = new JSONObject(Map.of("type", "float", "value", 6f, "sign", Set.of(NEGATIVE, ZERO, POSITIVE)));
            lambda[1] = new JSONObject(Map.of("type", "float", "value", 1f, "sign", Set.of(NEGATIVE, ZERO, POSITIVE)));

            test("alwaysThrows3", lambda, null, ArithmeticException.class, "Illegal divide by zero");
        }

        @Test
        void alwaysThrows4() {
            JSONObject[] lambda = createNullArray(2);
            lambda[0] = new JSONObject(Map.of("type", "int", "value", 6, "sign", Set.of(NEGATIVE, ZERO, POSITIVE)));
            lambda[1] = new JSONObject(Map.of("type", "int", "value", 1, "sign", Set.of(NEGATIVE, ZERO, POSITIVE)));

            test("alwaysThrows4", lambda, null, ArithmeticException.class, "Illegal divide by zero");
        }

        @Test
        void alwaysThrows5() {
            JSONObject[] lambda = createNullArray(2);
            lambda[0] = new JSONObject(Map.of("type", "int", "value", 6, "sign", Set.of(NEGATIVE, ZERO, POSITIVE)));
            lambda[1] = new JSONObject(Map.of("type", "int", "value", 1, "sign", Set.of(NEGATIVE, ZERO, POSITIVE)));

            test("alwaysThrows5", lambda, null, ArithmeticException.class, "Illegal divide by zero");
        }
    }

    @Nested
    @DisplayName("itDependsOnLattice Tests")
    class itDependsOnLattice {
        @Test
        void itDependsOnLattice1() {
            test("itDependsOnLattice1", createNullArray(1), null, ArithmeticException.class, "Illegal divide by zero");
        }

        @Test
        void itDependsOnLattice2() {
            test("itDependsOnLattice2", createNullArray(1), null, ArithmeticException.class, "Illegal divide by zero");
        }

        @Test
        void itDependsOnLattice3() {
            JSONObject[] lambda = createNullArray(3);
            lambda[0] = new JSONObject(Map.of("type", "int", "value", 6, "sign", Set.of(NEGATIVE, ZERO, POSITIVE)));
            lambda[1] = new JSONObject(Map.of("type", "int", "value", 1, "sign", Set.of(NEGATIVE, ZERO, POSITIVE)));

            test("itDependsOnLattice3", lambda, null, ArithmeticException.class, "Illegal divide by zero");
        }

        @Test
        void itDependsOnLattice4() {
            test("itDependsOnLattice4", createNullArray(1), null, ArithmeticException.class, "Illegal divide by zero");
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
            JSONObject[] lambda = createNullArray(1);
            lambda[0] = new JSONObject(Map.of("type", "int", "value", 6, "sign", Set.of(NEGATIVE, ZERO, POSITIVE)));

            test("neverThrows2", lambda, null, null, null);
        }

        @Test
        void neverThrows3() {
            JSONObject[] lambda = createNullArray(2);
            lambda[0] = new JSONObject(Map.of("type", "int", "value", 6, "sign", Set.of(NEGATIVE, ZERO, POSITIVE)));
            lambda[1] = new JSONObject(Map.of("type", "int", "value", 1, "sign", Set.of(NEGATIVE, ZERO, POSITIVE)));

            test("neverThrows3", lambda, null, null, null);
        }

        @Test
        void neverThrows4() {
            JSONObject[] lambda = createNullArray(1);
            lambda[0] = new JSONObject(Map.of("type", "int", "value", 6, "sign", Set.of(NEGATIVE, ZERO, POSITIVE)));

            test("neverThrows4", lambda, null, null, null);
        }

        @Test
        void neverThrows5() {
            JSONObject[] lambda = createNullArray(2);
            lambda[0] = new JSONObject(Map.of("type", "int", "value", 6, "sign", Set.of(NEGATIVE, ZERO, POSITIVE)));
            lambda[1] = new JSONObject(Map.of("type", "int", "value", 1, "sign", Set.of(NEGATIVE, ZERO, POSITIVE)));

            test("neverThrows5", lambda, null, null, null);
        }
    }

    @Test
    void speedVsPrecision() {
        test("speedVsPrecision", createNullArray(1), null, ArithmeticException.class, "Illegal divide by zero");
    }


}
