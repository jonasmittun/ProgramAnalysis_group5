package Week06;

import Week04.Main;
import Week04.Method;
import Week04.Pair;
import org.json.JSONObject;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;

import static Week01.Main.getFiles;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestSuperclass {

    static Map<String, String> mapper = new HashMap<>();       // Map<Filename, Classname>
    static Map<String, JSONObject> classes = new HashMap<>();  // Map<Classname, JSONObject>

    static JSONObject cls;

    static void initAll(String decompiledFileName) {
        String path = "src\\main\\resources\\decompiled";
        for(Map.Entry<String, String> entry : getFiles(path).entrySet()) {
            String filename = entry.getKey();
            String content = entry.getValue();

            JSONObject file = new JSONObject(content);
            String classname = file.getString("name");

            classes.put(classname, file);
            mapper.put(filename, classname);
        }

        cls = classes.get(mapper.get(decompiledFileName));
    }

    protected <T extends Throwable> void test(String methodName, JSONObject[] parameter, Class<T> exceptionClass, String exceptionMessage) {
        Method m = new Method(parameter, new ArrayDeque<>(), new Pair<>(Main.simpleResolve(cls, methodName), 0));
        Map<Integer, JSONObject> mu = new HashMap<>();

        SignInterpreter in = new SignInterpreter(new HashMap<>(classes));
        if (exceptionClass != null) {
            Exception exception = (Exception) assertThrows(exceptionClass, () -> in.run(m, mu));
            assertTrue(exception.getMessage().contains(exceptionMessage));
        } else {
            in.run(m, mu);
        }
    }
}
