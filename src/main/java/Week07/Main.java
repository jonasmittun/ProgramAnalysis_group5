package Week07;

import com.microsoft.z3.*;

public class Main {

    public static void main(String[] args) {
        Context ctx = new Context();
        IntExpr a = ctx.mkIntConst("a");
        IntExpr b = ctx.mkIntConst("b");
        IntExpr x1 = ctx.mkIntConst("x1");
        IntExpr y1 = ctx.mkIntConst("y1");
        BoolExpr e1 = ctx.mkEq(x1, ctx.mkMul(a, ctx.mkInt(20)));
        BoolExpr e2 = ctx.mkEq(y1, ctx.mkAdd(b, ctx.mkInt(5)));
        BoolExpr e3 = ctx.mkLt(y1, x1);
        Solver solver = ctx.mkSolver();
        solver.add(new BoolExpr[]{e1, e2, e3});
        Status result = solver.check();
        System.out.println(result);
        if (result == Status.SATISFIABLE) {
            System.out.println(solver.getModel());
        }
    }
}