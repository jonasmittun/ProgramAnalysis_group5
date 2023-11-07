package Project;

import org.json.JSONObject;

import java.util.Map;

public enum ANull {
    NULL,
    NULLABLE,
    NOTNULL;

    /** Translates JSONObject o to an abstract type
     * @param o JSONObject
     */
    public static JSONObject toAbstract(JSONObject o) {
        if(o == null || o.isEmpty()) {
            o = new JSONObject(Map.of("abstract", NULL));
        } else {
            if(!o.has("abstract")) {
                if (o.has("kind")) { // SimpleReferenceType
                    o.put("abstract", NULLABLE);
                } else if(o.has("type")) { // BaseType
                    o.put("abstract", NOTNULL);
                } else throw new RuntimeException(o + " is not a SimpleType!");
            }
        }

        return o;
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
