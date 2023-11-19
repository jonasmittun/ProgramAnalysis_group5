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

    /** Returns true if the JSONObject's abstract value is greater or equal to some level and false otherwise. */
    public boolean overflows(JSONObject o, int LEVEL) {
        return (o.has("abstract") && o.get("abstract") instanceof ANull a && a.toInt() >= LEVEL);
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
