package Week3;

import org.json.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        File file = new File("src\\main\\java\\dtu\\json\\Outer.json");

        try {
            String content = new String(Files.readAllBytes(Paths.get(file.toURI())));
            JSONObject o = new JSONObject(content);

            process(o);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public static void process(JSONObject object) {
        List<Object> access = object.getJSONArray("access").toList();
        System.out.println(access);

        List<Object> typeParams = object.getJSONArray("typeparams").toList();
        System.out.println(typeParams);

        String name = object.getString("name");
        System.out.println(name);
    }
}
