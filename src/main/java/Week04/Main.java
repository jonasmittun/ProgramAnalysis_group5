package Week04;

import Week05.Sign;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

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

        //Interpreter in = new Interpreter(new HashMap<>(classes));
        //in.run(new Interpreter.Method(new JSONObject[] { new JSONObject(Map.of("type", "int", "value", 1)), new JSONObject(Map.of("type", "int", "value", 2)) }, new ArrayDeque<>(), new Interpreter.Pair<>(mapper.get("Simple.json") + "/" + "add", 0)));
        //in.run(new Interpreter.Method(new JSONObject[] { new JSONObject(Map.of("type", "int", "value", 5)), new JSONObject(Map.of("type", "int", "value", 2)) }, new ArrayDeque<>(), new Interpreter.Pair<>(mapper.get("Simple.json") + "/" + "factorial", 0)));
        /* For multiple files
        HashMap<String, JSONObject> peeledMethods2 = new HashMap<String, JSONObject>();
        for(int i = 0; i < files.size(); i++) {
            HashMap<String, JSONObject> temp = peeler(files.get(i));
            peeledMethods2.putAll(temp);
        }
        Interpreter in2 = new Interpreter(peeledMethods2);
        */

        // TODO create goldenFile (now it finds in this directory)
        // TODO for every testcase run goldenTestTrace method

    }

    /** Converts a JSONObject into a formatted value string. */
    public static String toFormattedString(JSONObject o) {
        if(o == null) {
            return "(null)";
        } else if(o.has("kind")) {
            return "(" + o.getString("kind") + "ref" + " r)";
        } else {
            if(o.has("sign")) {
                StringJoiner sj = new StringJoiner(", ");
                for(Object sign : o.getJSONArray("sign")) {
                    switch((Sign) sign) {
                        case NEGATIVE   -> sj.add("-");
                        case ZERO       -> sj.add("0");
                        case POSITIVE   -> sj.add("+");
                    }
                }
                return "{" + sj + "}";
            } else {
                String inner = switch(o.getString("type")) {
                    case "int", "integer" -> "int";
                    default -> o.getString("type");
                };

                String value = o.get("value").toString();
                if(o.get("value") instanceof String) {
                    value = "\"" + value.replace("\n", "\\n") + "\"";
                }

                return "(" + inner + " " + value + ")";
            }
        }
    }

    // Might not be needed
    public static HashMap<String, JSONObject> peeler(JSONObject theObject){
        HashMap<String, JSONObject> results = new HashMap<>();

        //For each case in the JSONObject add to the list
        JSONArray methods = theObject.getJSONArray("methods");
        for(int i = 0; i < methods.length(); i++){
            JSONArray annotations = methods.getJSONObject(i).getJSONArray("annotations");
            for (int j = 0; j < annotations.length(); j++) {
                if (annotations.getJSONObject(j).get("type").toString().equals("dtu/compute/exec/Case")){
                    //System.out.println("xdd");
                    results.put(methods.getJSONObject(i).getString("name"),methods.getJSONObject(i));
                }
            }
        }

        return results;
    }

    public static void goldenTestTrace(byte[] trace, String testcaseName) {
        try {
            byte[] expected = Files.readAllBytes(Paths.get("Week04\\src\\main\\java\\Week4\\goldenFile.txt"));

            if (!Arrays.equals(expected, trace)) {
                // Test Failure
                System.err.println("Test failed for " + testcaseName);
            }
        } catch (IOException e) {
            // Handle error
            System.err.println("Failed to read goldenFile with error: " + e);
        }
    }
}
