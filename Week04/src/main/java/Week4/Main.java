package Week4;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class Main {

    //private Map<String, ?> memory;
    //private Stack<?> stack;

    public static void main(String[] args) {
        // Directory where out test binaries are located
        String path = "src\\main\\java\\decompiled\\dtu\\compute\\exec";

        Map<String, JSONObject> map = getFiles(path).entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> new JSONObject(entry.getValue())));

        System.out.println(map.entrySet());

        JSONObject simple = map.get("Simple.json");

        JSONArray methods = simple.getJSONArray("methods");
        for(int i = 0; i < methods.length(); i++) {
            Interpreter in = new Interpreter(methods.getJSONObject(i));
        }

        // TODO: for each file in dir


    }

    // Mig
    public static ArrayList<JSONObject> peeler(JSONObject xdd){
        ArrayList<JSONObject> results = new ArrayList<JSONObject>();

        //For each case in the JSONObject add to the list

        //Temp thing so the ide stops crying
        results.add(xdd);

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
}
