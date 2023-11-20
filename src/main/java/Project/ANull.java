package Project;

import Week04.ConcreteInterpreter;
import org.json.JSONObject;

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
            JSONObject jo = ConcreteInterpreter.createNull();
            jo.put("abstract", NULL);
            return jo;
        } else if(o instanceof JSONObject jo) {
            if(!jo.has("abstract")) {
                if (jo.has("kind")) { // SimpleReferenceType
                    jo.put("abstract", NULLABLE);
                } else if(jo.has("type")) { // BaseType
                    jo.put("abstract", NOTNULL);
                } else if(ConcreteInterpreter.isNull(jo)) {
                    jo.put("abstract", NULL);
                } else throw new RuntimeException(jo + " is not a SimpleType!");
            }

            return jo;
        } else throw new RuntimeException(o + " is not a JSONObject");
    }

    /** Returns the JSONObject's abstract value if it has one and -1 otherwise. */
    public int getInt(JSONObject o) {
        return (o.has("abstract") && o.get("abstract") instanceof ANull a) ? a.toInt() : -1;
    }

    /** Converts the abstract value to an integer. */
    public int toInt() {
        return switch(this) {
            case NULL       -> 2;
            case NULLABLE   -> 1;
            case NOTNULL    -> 0;
        };
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
