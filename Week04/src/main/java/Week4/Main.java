package Week4;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Main {

    public static void main(String[] args) {
        // Directory where out test binaries are located
        String path = "src\\main\\java\\decompiled";

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

        Interpreter in = new Interpreter(new HashMap<>(classes));
        in.run(new Interpreter.Method(new JSONObject[] { new JSONObject(Map.of("type", "int", "value", 1)), new JSONObject(Map.of("type", "int", "value", 2)) }, new Stack<>(), new Interpreter.Pair<>(mapper.get("Simple.json") + "/" + "add", 0)));
        //in.run(new Interpreter.Method(new JSONObject[] { new JSONObject(Map.of("type", "int", "value", 5)), new JSONObject(Map.of("type", "int", "value", 2)) }, new Stack<>(), new Interpreter.Pair<>(mapper.get("Simple.json") + "/" + "factorial", 0)));
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

    /** Returns a map with every file in any directory and subdirectory of path together with its content as a string
     * @return  A Map<String, String> where the key is the filename and the value is the content as a string
     */
    public static Map<String, String> getFiles(String path) {
        Map<String, String> map = new HashMap<>();

        Queue<File> files = new LinkedList<>();
        files.add(new File(path));

        while(!files.isEmpty()) {
            File file = files.poll();

            if(file.isFile()) {
                try {
                    map.put(file.getName(), Files.readString(Path.of(file.getAbsolutePath())));
                } catch(IOException ignore) {}
            } else {
                File[] content = file.listFiles();
                if(content != null) {
                    Collections.addAll(files, content);
                }
            }
        }

        return map;
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
