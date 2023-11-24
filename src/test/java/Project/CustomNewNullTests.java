package Project;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static Week04.ConcreteInterpreter.*;

public class CustomNewNullTests extends TestSuperClass{

    @BeforeAll
    static void initAll() {
        TestSuperClass.initAll("NewNull.json");
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
            test("alwaysThrows2", createNullArray(2), null, NullPointerException.class, null);
        }

        @Test
        void alwaysThrows3() {
            test("alwaysThrows3", createNullArray(2), null, NullPointerException.class, null);
        }

        @Test
        void alwaysThrows4() {
            test("alwaysThrows4", createNullArray(1), null, NullPointerException.class, null);
        }

        @Test
        void alwaysThrows5() {
            test("alwaysThrows5", createNullArray(1), null, NullPointerException.class, null);
        }
    }

    @Nested
    @DisplayName("neverThrows Tests")
    class neverThrows {
        @Test
        void neverThrows1() {
            test("neverThrows1", createNullArray(2), null, null, null);
        }

        @Test
        void neverThrows2() {
            test("neverThrows2", createNullArray(2), null, null, null);
        }

        @Test
        void neverThrows3() {
            test("neverThrows3", createNullArray(2), null, null, null);
        }

        @Test
        void neverThrows4() {
            test("neverThrows4", createNullArray(1), null, null, null);
        }

        @Test
        void neverThrows5() {
            test("neverThrows5", createNullArray(2), null, null, null);
        }

        @Test
        void neverThrows6() {
            test("neverThrows6", createNullArray(2), null, null, null);
        }

        @Test
        void neverThrows7() {
            test("neverThrows7", createNullArray(2), null, null, null);
        }
    }

    @Test
    void interestingCase() {
        test("interestingCase", createNullArray(1), null, null, null);
    }

    @Nested
    @DisplayName("dependsOnAmalgamation Tests")
    class dependsOnAmalgamation {
        @Test
        void dependsOnAmalgamation1() {
            test("dependsOnAmalgamation1", createNullArray(1), null, null, null);
        }

        @Test
        void dependsOnAmalgamation2() {
            test("dependsOnAmalgamation2", createNullArray(1), null, null, null);
        }
    }
}
