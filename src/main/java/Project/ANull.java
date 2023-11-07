package Project;

import org.json.JSONObject;

import java.util.Map;

public enum ANull {
    NULL,
    NULLABLE,
    NOTNULL;

    /** Translates JSONObject o to an abstract type
     * @param o JSONObject
     * @return  Returns o with another field "abstract" with value NULL/NULLABLE/NOTNULL
     */
    public static JSONObject toAbstract(Object o) {
        if(o == null) {
            return new JSONObject(Map.of("abstract", NULL));
        } else if(o instanceof JSONObject jo) {
            if(!jo.has("abstract")) {
                if (jo.has("kind")) { // SimpleReferenceType
                    jo.put("abstract", NULLABLE);
                } else if(jo.has("type")) { // BaseType
                    jo.put("abstract", NOTNULL);
                } else throw new RuntimeException(jo + " is not a SimpleType!");
            }

            return jo;
        } else throw new RuntimeException(o + " is not a JSONObject");
    }

    @Override
    public String toString() {
        return switch(this) {
            case NULL       -> "Null";
            case NULLABLE   -> "Nullable";
            case NOTNULL    -> "Not Null";
        };
    }
}
