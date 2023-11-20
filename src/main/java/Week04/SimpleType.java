package Week04;

import org.json.JSONObject;

import java.util.Map;

/**
 * <pre>
 * BaseType: "byte", "char", "double", "float", "int", "long", "short" or "boolean"<br>
 * or<br>
 * SimpleReferenceType: { "kind": "class", "name": &lt;ClassName&gt; } or { "kind": "array", "name": &lt;SimpleType&gt; }
 * </pre>
 */
public class SimpleType {

    /** Returns true if the value's type is numeric and false otherwise. */
    public static boolean isNumeric(JSONObject value) {
        if(ConcreteInterpreter.isNull(value)) return false;
        else if(value.has("type") && value.get("type") instanceof String type) {
            return switch(type) {
                case "int", "integer", "float", "double", "long", "byte", "short" -> true;
                default -> false;
            };
        }

        return false;
    }

    /** Returns a new JSONObject of the specified type with the default value for that type.
     * @param SimpleType
     *  <pre>
     *  BaseType: "byte", "char", "double", "float", "int", "long", "short" or "boolean"<br>
     *  SimpleReferenceType: { "kind": "class", "name": &lt;ClassName&gt; } or { "kind": "array", "name": &lt;SimpleType&gt; }
     *  </pre>
     */
    public static JSONObject createDefault(Object SimpleType, Map<Integer, JSONObject> mu) {
        if(SimpleType instanceof String BaseType) {
            JSONObject result = new JSONObject();
            result.put("type", BaseType);
            switch(BaseType) {
                case "byte", "short", "int" -> result.put("value", 0);
                case "char"     -> result.put("value", '\u0000');
                case "double"   -> result.put("value", 0d);
                case "float"    -> result.put("value", 0f);
                case "long"     -> result.put("value", 0L);
                case "boolean"  -> result.put("value", false);
                default         -> throw new IllegalArgumentException("Unsupported BaseType: " + BaseType);
            }

            return result;
        } else if(SimpleType instanceof JSONObject SimpleReferenceType) {
            mu.put(System.identityHashCode(SimpleReferenceType), ConcreteInterpreter.createNull());
            return SimpleReferenceType;
        } else throw new IllegalArgumentException("Invalid SimpleType: " + SimpleType);
    }

    /** Converts the SimpleType object into a formatted string.
     * @param o     The &lt;SimpleType&gt; object.
     * @param full  When o is an array reference type, it determines if the formatter should recursively determine the subtypes - giving the full type name.
     */
    public static String toFormattedString(Object o, boolean full) {
        if(o == null) {
            return "null";
        } else if(o instanceof String BaseType) {
            if(BaseType.equals("integer")) return "int";
            else return BaseType;
        } else if(o instanceof JSONObject SimpleReferenceType) {
            String kind = SimpleReferenceType.getString("kind");
            if(kind.equals("class")) {
                return "classref " + SimpleReferenceType.getString("name");
            } else {
                if(full) {
                    return "arrayref (" + toFormattedString(SimpleReferenceType.get("type"), true) + ")[]";
                } else {
                    return "arrayref " + ((SimpleReferenceType.get("type") instanceof String type) ? type + "[]" : "ref[]");
                }
            }
        } else {
            return "unknown";
        }
    }

    /** Checks if two &lt;SimpleType&gt;'s are equal. */
    public static boolean equals(Object st1, Object st2) {
        if(st1 == null && st2 == null) {
            return true;
        } else if(st1 != null && st2 != null) {
            if(st1 instanceof String bt1 && st2 instanceof String bt2) {
                return bt1.equals(bt2);
            } else if(st1 instanceof JSONObject srt1 && st2 instanceof JSONObject srt2) {
                String kind1 = srt1.getString("kind");
                String kind2 = srt2.getString("kind");

                if(!kind1.equals(kind2)) return false;

                return switch(kind1) {
                    case "class" -> {
                        String name1 = srt1.getString("name");
                        String name2 = srt2.getString("name");
                        yield name1.equals(name2);
                    }
                    case "array" -> {
                        Object t1 = srt1.get("type");
                        Object t2 = srt2.get("type");
                        yield SimpleType.equals(t1, t2);
                    }
                    default -> false;
                };
            }
        }

        return false;
    }
}
