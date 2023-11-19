package custom;

import dtu.compute.exec.Case;

public class NewNull {
    public NewNull field;

    @Case
    public static Boolean alwaysThrows1() {
        Object obj = null;
        return obj.equals(new Object());
    }

    @Case
    public static void alwaysThrows2() {
        NewNull nil = new NewNull();
        if (nil.field.equals(nil)) {
            System.out.println("Equal");
        } else {
            System.out.println("Not Equal");
        }
    }

    @Case
    public static void alwaysThrows3() {
        NewNull nil = new NewNull();
        nil.field = new NewNull();
        if (nil.hashCode() == nil.field.hashCode()) {
            System.out.println("Equal");
        } else {
            System.out.println(nil.field.field.toString());
        }
    }

    @Case
    public static Object neverThrows1() {
        String obj = null;
        String temp = obj;
        return temp;
    }

    @Case
    public static void neverThrows2() {
        NewNull nil = new NewNull();
        if (nil.equals(nil.field)) {
            System.out.println("Equal");
        } else {
            System.out.println("Not Equal");
        }
    }

    @Case
    public static int neverThrows3(Integer i, Integer j) {
        assert i != null && j != null;
        if (i > j) {
            return i;
        }
        return j;
    }

    @Case
    public static boolean neverThrows4(NewNull n) {
        assert n == null;
        n = new NewNull();
        assert n != null;
        n.field = n;
        while (n != n.field) {
            n = null;
        }
        return n.equals(n.field);
    }

    @Case
    public static Boolean neverThrows5(String s, String notYourProblem) {
        assert s != null;
        return s.equals(notYourProblem);
    }

    @Case
    public static NewNull interestingCase(Object o) {
        NewNull nil = new NewNull();
        nil.field = new NewNull();
        nil.field.field = new NewNull();
        while (!nil.field.equals(nil)) {
            nil = nil.field;
        }
        return nil;
    }

    @Case
    public static Boolean alwaysThrows4() {
        String[] ss = new String[2];
        return ss[1].equals("test");
    }

    @Case
    public static String alwaysThrows5(Object[] os) {
        assert os.length > 0;
        return os[0].toString();
    }

    @Case
    public static void dependsOnAmalgamation1() {
        String[] ss = new String[2];
        ss[0] = "1";
        ss[1] = "2";
        System.out.println(ss[0].equals(ss[1]));
    }

    @Case
    public static void dependsOnAmalgamation2() {
        String[] ss = new String[10000];
        ss[0] = "1";
        ss[1] = "2";
        System.out.println(ss[0].equals(ss[1]));
    }
}
