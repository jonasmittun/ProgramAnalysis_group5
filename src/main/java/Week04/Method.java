package Week04;

import org.json.JSONObject;

public record Method(String classname, JSONObject method) {
    @Override
    public String toString() {
        return classname.substring(classname.lastIndexOf('/') + 1) + "." + method.getString("name");
    }
}
