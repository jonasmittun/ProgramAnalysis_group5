package Week04;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

import static Week01.Main.getFiles;

public class Main {

    public static void main(String[] args) {
        // Directory where out test binaries are located
        String path = "src\\main\\resources\\decompiled";

        Map<String, String> files = getFiles(path);         // Map<Filename, Content>

    }

    /** Returns a clone of the JSONObject */
    public static JSONObject cloneJSONObject(JSONObject o) {
        return new JSONObject(o.toString());
    }

    /** Converts a JSONObject into a formatted value string. */
    public static String toFormattedString(JSONObject o) {
        if(ConcreteInterpreter.isNull(o)) {
            return "(null)";
        } else if(o.has("kind")) {
            return "(" + SimpleType.toFormattedString(o, false) + ")";
        } else {
            if (o.has("sign")) {
                StringJoiner sj = new StringJoiner(", ");
                for (Object sign : o.getJSONArray("sign")) {
                    sj.add(sign.toString());
                }

                return "{" + sj + "}";
            } else if(o.has("abstract")) {
                return o.get("abstract").toString();
            } else {
                String type = o.getString("type");
                if(type.equals("integer")) type = "int";

                String value = o.get("value").toString();
                if(o.get("value") instanceof String) {
                    value = "\"" + value.replace("\n", "\\n") + "\"";
                }

                return "(" + type + " " + value + ")";
            }
        }
    }

    /** Resolves method only by name */
    public static Method simpleResolve(JSONObject cls, String methodname) {
        JSONArray methods = cls.getJSONArray("methods");
        for(int i = 0; i < methods.length(); i++) {
            JSONObject method = methods.getJSONObject(i);

            if(method.getString("name").equals(methodname)) return new Method(cls.getString("name"), method);
        }

        throw new RuntimeException(cls.getString("name") + "." + methodname + " could not be found!");
    }
}
