package Week4;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {

    public static void main(String[] args) {
        // Directory where out test binaries are located
        String path = "src\\main\\java\\decompiled";

        Map<String, JSONObject> map = getFiles(path).entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> new JSONObject(entry.getValue())));

        ArrayList<JSONObject> files = new ArrayList<>();
        for(int i = 0; i < map.keySet().size(); i++) {
            String index = map.keySet().toArray()[i].toString();
            files.add(map.get(index));
        }

        //For single files

        JSONObject simple = map.get("Simple.json");
        HashMap<String, JSONObject> peeledMethods = peeler(simple);
        //
        Interpreter in = new Interpreter(peeledMethods);

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
