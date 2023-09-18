package Week4;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.stream.IntStream;

public class Interpreter {

    private int program_counter;
    private Map<Integer, JSONObject> bytecode;
    private Stack<Object> operant_stack;
    private List<Object> locals_variables;

    public Interpreter(JSONObject method) {
        JSONArray annotations = method.getJSONArray("annotations");
        if(IntStream.range(0, annotations.length()).noneMatch(i -> annotations.getJSONObject(i).getString("type").equals("dtu/compute/exec/Case"))) {
            System.out.println("Interpreter not initialized correctly!");
            return;
        }

        JSONObject code = method.getJSONObject("code");
        JSONArray bytecode = code.getJSONArray("bytecode");

        this.bytecode = new HashMap<>();
        for(int i = 0; i < bytecode.length(); i++) {
            JSONObject instruction = bytecode.getJSONObject(i);
            this.bytecode.put(i, instruction);
        }
        System.out.println(this.bytecode);
    }

    public void run(JSONObject instruction) {
        String operation = instruction.getString("opr");
    }
}
