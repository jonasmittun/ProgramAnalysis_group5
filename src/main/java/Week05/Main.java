package Week05;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

import static Week05.Sign.*;
import static Week05.Sign.POSITIVE;

public class Main {

    public static void main(String[] args) {
        // Directory where out test binaries are located
        String path = "src\\main\\java\\decompiled";

        BiFunction<Sign, Sign, Set<Sign>> f = (s1, s2) -> {
            return switch(s1) {
                case NEGATIVE -> switch(s2) {
                    case NEGATIVE   -> Set.of(NEGATIVE);
                    case ZERO       -> Set.of(NEGATIVE);
                    case POSITIVE   -> Set.of(NEGATIVE, ZERO, POSITIVE);
                };
                case ZERO -> switch(s2) {
                    case NEGATIVE   -> Set.of(NEGATIVE);
                    case ZERO       -> Set.of(ZERO);
                    case POSITIVE   -> Set.of(NEGATIVE, ZERO, POSITIVE);
                };
                case POSITIVE -> switch(s2) {
                    case NEGATIVE   -> Set.of(NEGATIVE, ZERO, POSITIVE);
                    case ZERO       -> Set.of(POSITIVE);
                    case POSITIVE   -> Set.of(POSITIVE);
                };
            };
        };

        JSONObject v1 = new JSONObject(Map.of("sign", new JSONArray(Set.of(NEGATIVE, ZERO, POSITIVE))));
        JSONObject v2 = new JSONObject(Map.of("sign", new JSONArray(Set.of(NEGATIVE, ZERO, POSITIVE))));

        for(Object s1 : v1.getJSONArray("sign")) {
            for(Object s2 : v2.getJSONArray("sign")) {
                JSONObject result = new JSONObject(Map.of("sign", f.apply(toSign(s1), toSign(s2))));
                System.out.println(s1 + " + " + s2 + " = " + result);
            }
        }
    }
}
