package custom;

import dtu.compute.exec.Case;

public class NewNull {
    public NewNull field;

    public int getter() {
        if(this.field != null) {
            return 1;
        } else {
            return -1;
        }
    }

    @Case
    public static NewNull alwaysThrows1() {
        NewNull nil = null;
        return nil.field;
    }

    @Case
    public static NewNull alwaysThrows2() {
        NewNull nil = null;
        int x = 1;
        if(x > 0) {
            return nil.field;
        } else {
            return new NewNull();
        }
    }

    @Case
    public static boolean alwaysThrows3() {
        NewNull nil1 = null;
        NewNull nil2 = new NewNull();
        return nil1.field.getter() > nil2.field.getter();
    }

    @Case
    public static Boolean alwaysThrows4() {
        String[] ss = new String[2];
        return ss[1].equals("test");
    }

    @Case
    public static int alwaysThrows5() {
        NewNull[] os = new NewNull[1];
        assert os.length > 0;
        return os[0].getter();
    }

    @Case
    public static Object neverThrows1() {
        String obj = null;
        String temp = obj;
        return temp;
    }

    @Case
    public static NewNull neverThrows2() {
        NewNull nil = null;
        int x = 0;
        if (x == 0) {
            return new NewNull();
        } else {
            return nil.field;
        }
    }

    @Case
    public static NewNull neverThrows3() {
        NewNull nil = null;
        int x = 1;
        if (x == 1) {
            nil = new NewNull();
            return nil.field;
        } else {
            return nil.field;
        }
    }

    @Case
    public static NewNull neverThrows4() {
        NewNull n = null;
        n = new NewNull();
        n.field = n;
        return n.field;
    }

    @Case
    public static NewNull neverThrows5() {
        NewNull s = new NewNull();
        int x = 0;
        x -= 2;
        if(x < 1) {
            return s.field;
        } else {
            return s.field.field;
        }
    }

    @Case
    public static NewNull neverThrows6() {
        NewNull s = new NewNull();
        int x = 1;
        if(x != 0) {
            return s.field;
        } else {
            return s.field.field;
        }
    }

    @Case
    public static NewNull neverThrows7() {
        NewNull s = new NewNull();
        int x = 2;
        x += 3;
        if(x > 1) {
            return s.field;
        } else {
            return s.field.field;
        }
    }

    @Case
    public static NewNull interestingCase() {
        NewNull nil = new NewNull();
        nil.field = new NewNull();
        if(nil.field.getter() != nil.getter()) {
            nil.field.field = new NewNull();
        }
        return nil;
    }

    @Case
    public static boolean dependsOnAmalgamation1() {
        String[] ss = new String[2];
        ss[0] = "1";
        ss[1] = "2";
        return ss[0].length() == ss[1].length();
    }

    @Case
    public static boolean dependsOnAmalgamation2() {
        String[] ss = new String[1000];
        ss[0] = "1";
        ss[1] = "2";
        return ss[0].length() == ss[1].length();
    }
}
