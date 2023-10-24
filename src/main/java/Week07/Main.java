package Week07;

import Week04.Frame;
import Week04.Pair;
import com.microsoft.z3.*;
import org.json.JSONObject;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;

import static Week01.Main.getFiles;

public class Main {

    public static void main(String[] args) {
        // Directory where out test binaries are located
        String path = "src\\main\\resources\\decompiled";

        Map<String, String> files = getFiles(path);         // Map<Filename, Content>

        Map<String, String> mapper = new HashMap<>();       // Map<Filename, Classname>
        Map<String, JSONObject> classes = new HashMap<>();  // Map<Classname, JSONObject>
        for(Map.Entry<String, String> entry : files.entrySet()) {
            String filename = entry.getKey();
            String content = entry.getValue();

            JSONObject file = new JSONObject(content);
            String classname = file.getString("name");

            classes.put(classname, file);
            mapper.put(filename, classname);
        }

        ConcolicInterpreter in = new ConcolicInterpreter(classes);

        Frame f = new Frame(new JSONObject[] { new JSONObject(Map.of("type", "int", "value", 1)), new JSONObject(Map.of("type", "int", "value", 2)) }, new ArrayDeque<>(), new Pair<>(Week04.Main.simpleResolve(classes.get(mapper.get("Simple.json")), "add"), 0));
        Map<Integer, JSONObject> mu = new HashMap<>();

        in.run(f, mu);
    }

    public void test() {
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