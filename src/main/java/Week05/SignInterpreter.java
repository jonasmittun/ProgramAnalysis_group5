package Week05;

import Week04.Main;
import Week04.Method;
import Week04.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static Week05.Sign.*;

public class SignInterpreter implements Interpreter {

    private final Map<String, JSONObject> classes; // Map<Classname, JSONObject>

    private final Map<String, Map<String, JSONObject>> class_methods;

    private final int depthLimit;

    public SignInterpreter(Map<String, JSONObject> classes, int depthLimit){
        this.classes = classes;
        this.depthLimit = depthLimit;

        // Map methods for all classes
        class_methods = new HashMap<>();
        for(Map.Entry<String, JSONObject> entry : classes.entrySet()) {
            Map<String, JSONObject> methods = new HashMap<>();

            JSONArray ms = entry.getValue().getJSONArray("methods");
            for(int i = 0; i < ms.length(); i++) {
                JSONObject m = ms.getJSONObject(i);
                methods.put(m.getString("name"), m);
            }

            class_methods.put(entry.getKey(), methods);
        }


    }

    public JSONObject toAbstract(JSONObject o) {
        if(!o.has("kind") && o.has("type") && o.has("value")) {
            int r = switch(o.getString("type")) {
                case "int", "integer"   -> Integer.compare(o.getInt("value"), 0);
                case "long"             -> Long.compare(o.getLong("value"), 0);
                case "float"            -> Float.compare(o.getFloat("value"), 0);
                case "double"           -> Double.compare(o.getDouble("value"), 0);
                default                 -> throw new IllegalStateException("Unsupported type: " + o.getString("type"));
            };

            o.put("sign", new JSONArray(Set.of(Sign.toSign(r))));
            return o;
        }

        return o;
    }

    private void addSigns(Method m) {

        for(JSONObject o : m.lambda()) {
            toAbstract(o);
        }

        for(JSONObject o : m.sigma()) {
            toAbstract(o);
        }
    }

    private JSONObject getMethod(String absolute_name) {
        int index = absolute_name.lastIndexOf("/") + 1;
        String classname = absolute_name.substring(0, index - 1);
        String methodname = absolute_name.substring(index);

        return class_methods.get(classname).get(methodname);
    }

    @Override
    public void run(Method method, Map<Integer, JSONObject> mu){
        Deque<Method> psi = new ArrayDeque<>();  // Method Stack
        addSigns(method);
        System.out.println(method);
        psi.push(method);

        Queue<State> queue = new LinkedList<>();
        queue.add(new State(psi, mu));
        int depthCounter = 1;
        int depth = 0;

        while(!queue.isEmpty() && depth < depthLimit){
            State current = queue.poll();

            Set<State> next = step(current);

            System.out.println("Generated: " + next.size());

            queue.addAll(next);

            depthCounter--;
            if(depthCounter == 0) {
                depth++;
                depthCounter = queue.size();
            }
        }
    }

    @Override
    public Set<State> step(State state) {
        Set<State> results = new HashSet<>();

        Map<Integer, JSONObject> mu = state.mu();
        Deque<Method> psi = state.psi();
        Method m = psi.pop();

        JSONObject instruction = getMethod(m.iota().e1()).getJSONObject("code").getJSONArray("bytecode").getJSONObject(m.iota().e2());

        switch(instruction.getString("opr")) {
            /*
            case "array_load" -> {
                JSONObject index = m.sigma().pop();
                JSONObject ref = m.sigma().pop();

                JSONObject actual = mu.get(System.identityHashCode(ref));
                JSONArray array = actual.getJSONArray("value");
                JSONObject value = array.getJSONObject(index.getInt("value"));

                // TODO: Unfinished
                m.sigma().push(value);
                psi.push(new Method(m.lambda(), m.sigma(), new Pair<>(m.iota().e1(), m.iota().e2() + 1)));
                results.add(new State(psi, mu));
            }
            case "array_store" -> {
                JSONObject value = m.sigma().pop();
                JSONObject index = m.sigma().pop();
                JSONObject ref = m.sigma().pop();

                JSONObject actual = mu.get(System.identityHashCode(ref));
                JSONArray array = actual.getJSONArray("value");
                array.put(index.getInt("value"), value);
            }
            */
            case "push" -> {
                JSONObject value = instruction.getJSONObject("value");
                if(value.getString("type").equals("class")) { // Value is a <SimpleReferenceType>
                    m.sigma().push(value);
                } else {
                    m.sigma().push(toAbstract(value));
                }

                state.psi().push(new Method(m.lambda(), m.sigma(), new Pair<>(m.iota().e1(), m.iota().e2() + 1)));

                results.add(state);
            }
            case "load" -> {
                int index = instruction.getInt("index");
                JSONObject value = m.lambda()[index];
                if(value.has("kind")) { // Check if it's a reference type
                    m.sigma().push(value);
                } else {
                    m.sigma().push(toAbstract(value));
                }

                state.psi().push(new Method(m.lambda(), m.sigma(), new Pair<>(m.iota().e1(), m.iota().e2() + 1)));

                results.add(state);
            }
            case "store" -> {
                int index = instruction.getInt("index");
                JSONObject value = m.sigma().pop();
                m.lambda()[index] = value;

                state.psi().push(new Method(m.lambda(), m.sigma(), new Pair<>(m.iota().e1(), m.iota().e2() + 1)));

                results.add(state);
            }
            case "incr" -> { // For now we're assuming everything is an integer :)
                int index = instruction.getInt("index");
                JSONObject value = m.lambda()[index];
                int amountInt = instruction.getInt("amount");
                JSONObject amount;
                if (amountInt < 0) {
                    amount = new JSONObject(Map.of("sign", new JSONArray(Set.of(NEGATIVE))));
                } else if (amountInt > 0) {
                    amount = new JSONObject(Map.of("sign", new JSONArray(Set.of(POSITIVE))));
                } else {
                    amount = new JSONObject(Map.of("sign", new JSONArray(Set.of(ZERO))));
                }

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
                            case POSITIVE   -> Set.of(POSITIVE);
                        };
                        case POSITIVE -> switch(s2) {
                            case NEGATIVE   -> Set.of(NEGATIVE, ZERO, POSITIVE);
                            case ZERO       -> Set.of(POSITIVE);
                            case POSITIVE   -> Set.of(POSITIVE);
                        };
                    };
                };

                /*switch(value.getString("type")) {
                    case "int"      -> value.put("value", value.getInt("value") + instruction.getInt("amount"));
                    case "long"     -> value.put("value", value.getLong("value") + instruction.getLong("amount"));
                    case "float"    -> value.put("value", value.getFloat("value") + instruction.getFloat("amount"));
                    case "double"   -> value.put("value", value.getDouble("value") + instruction.getDouble("amount"));
                }*/

                for(Object s1 : value.getJSONArray("sign")) {
                    for(Object s2 : amount.getJSONArray("sign")) {
                        Set<Sign> signs = f.apply((Sign) s1, (Sign) s2);
                        if(signs.isEmpty()) System.out.println("Error msg");

                        JSONObject result = new JSONObject(Map.of("sign", signs));

                        Deque<Method> _psi = psi.stream().map(Method::clone).collect(Collectors.toCollection(ArrayDeque::new));
                        Map<Integer, JSONObject> _mu = mu.entrySet().stream().map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), new JSONObject(e.getValue().toMap()))).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                        Method _m = m.clone();

                        _m.sigma().push(result);

                        _psi.push(new Method(_m.lambda(), _m.sigma(), new Pair<>(_m.iota().e1(), _m.iota().e2() + 1)));

                        results.add(new State(_psi, _mu));
                    }
                }
            }
            case "binary" -> {
                String type = instruction.getString("type"); // Arithmetic Type
                JSONObject value2 = m.sigma().pop();
                JSONObject value1 = m.sigma().pop();

                // value1 opr value2
                BiFunction<Sign, Sign, Set<Sign>> f = switch(instruction.getString("operant")) {
                    case "add" -> {
                        yield (s1, s2) -> {
                            return switch(s1) {
                                case NEGATIVE -> switch(s2) {
                                    case NEGATIVE   -> Set.of(NEGATIVE);
                                    case ZERO       -> Set.of(NEGATIVE);
                                    case POSITIVE   -> Set.of(NEGATIVE, ZERO, POSITIVE);
                                };
                                case ZERO -> switch(s2) {
                                    case NEGATIVE   -> Set.of(NEGATIVE);
                                    case ZERO       -> Set.of(ZERO);
                                    case POSITIVE   -> Set.of(POSITIVE);
                                };
                                case POSITIVE -> switch(s2) {
                                    case NEGATIVE   -> Set.of(NEGATIVE, ZERO, POSITIVE);
                                    case ZERO       -> Set.of(POSITIVE);
                                    case POSITIVE   -> Set.of(POSITIVE);
                                };
                            };
                        };
                    }
                    case "sub" -> {
                        yield (s1, s2) -> {
                            return switch(s1) {
                                case NEGATIVE -> switch(s2) {
                                    case NEGATIVE   -> Set.of(NEGATIVE, ZERO, POSITIVE);
                                    case ZERO       -> Set.of(NEGATIVE);
                                    case POSITIVE   -> Set.of(NEGATIVE);
                                };
                                case ZERO -> switch(s2) {
                                    case NEGATIVE   -> Set.of(POSITIVE);
                                    case ZERO       -> Set.of(ZERO);
                                    case POSITIVE   -> Set.of(NEGATIVE);
                                };
                                case POSITIVE -> switch(s2) {
                                    case NEGATIVE   -> Set.of(POSITIVE);
                                    case ZERO       -> Set.of(POSITIVE);
                                    case POSITIVE   -> Set.of(NEGATIVE, ZERO, POSITIVE);
                                };
                            };
                        };
                    }
                    case "mul" -> {
                        yield (s1, s2) -> {
                            return switch(s1) {
                                case NEGATIVE -> switch(s2) {
                                    case NEGATIVE   -> Set.of(POSITIVE);
                                    case ZERO       -> Set.of(ZERO);
                                    case POSITIVE   -> Set.of(NEGATIVE);
                                };
                                case ZERO -> Set.of(ZERO);
                                case POSITIVE -> switch(s2) {
                                    case NEGATIVE   -> Set.of(NEGATIVE);
                                    case ZERO       -> Set.of(ZERO);
                                    case POSITIVE   -> Set.of(POSITIVE);
                                };
                            };
                        };
                    }
                    case "div" -> {
                        yield (s1, s2) -> {
                            return switch(s1) {
                                case NEGATIVE -> switch(s2) {
                                    case NEGATIVE   -> Set.of(POSITIVE);
                                    case ZERO       -> Set.of();
                                    case POSITIVE   -> Set.of(NEGATIVE);
                                };
                                case ZERO -> switch(s2) {
                                    case NEGATIVE   -> Set.of(ZERO);
                                    case ZERO       -> Set.of();
                                    case POSITIVE   -> Set.of(ZERO);
                                };
                                case POSITIVE -> switch(s2) {
                                    case NEGATIVE   -> Set.of(NEGATIVE);
                                    case ZERO       -> Set.of();
                                    case POSITIVE   -> Set.of(POSITIVE);
                                };
                            };
                        };
                    }
                    default -> throw new IllegalStateException("Unexpected value: " + instruction.getString("operant"));
                };

                for(Object s1 : value1.getJSONArray("sign")) {
                    for(Object s2 : value2.getJSONArray("sign")) {
                        Set<Sign> signs = f.apply((Sign) s1, (Sign) s2);
                        if(signs.isEmpty()) System.out.println("Error msg");

                        JSONObject result = new JSONObject(Map.of("sign", signs));

                        Deque<Method> _psi = psi.stream().map(Method::clone).collect(Collectors.toCollection(ArrayDeque::new));
                        Map<Integer, JSONObject> _mu = mu.entrySet().stream().map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), new JSONObject(e.getValue().toMap()))).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                        Method _m = m.clone();

                        _m.sigma().push(result);

                        _psi.push(new Method(_m.lambda(), _m.sigma(), new Pair<>(_m.iota().e1(), _m.iota().e2() + 1)));

                        results.add(new State(_psi, _mu));
                    }
                }
            }
            /*
            case "negate" -> {
                String type = instruction.getString("type"); // Arithmetic Type
                JSONObject value1 = m.sigma().pop();
                JSONObject result = new JSONObject();
                result.put("type", type);
                switch(value1.getString("value")) {
                    case "-"    -> result.put("value", Set.of('+'));
                    case "0"    -> result.put("value", Set.of('0'));
                    case "+"    -> result.put("value", Set.of('-'));
                }
                m.sigma().push(result);
                psi.push(new Method(m.lambda(), m.sigma(), new Pair<>(m.iota().e1(), m.iota().e2() + 1)));
            }
            case "bitopr" -> {
                String type = instruction.getString("type"); // "int" | "long"
                JSONObject value = m.sigma().pop();

                JSONObject result = new JSONObject();
                result.put("type", type);

                switch(instruction.getString("operant")) {
                    case "shl"  -> {
                        switch(type) {
                            case "int"  -> result.put("value", value.getInt("value") << 1);
                            case "long" -> result.put("value", value.getLong("value") << 1);
                        }
                    }
                    case "shr"  -> {
                        switch(type) {
                            case "int"  -> result.put("value", value.getInt("value") >> 1);
                            case "long" -> result.put("value", value.getLong("value") >> 1);
                        }
                    }
                    case "ushr" -> {
                        switch(type) {
                            case "int"  -> result.put("value", value.getInt("value") >>> 1);
                            case "long" -> result.put("value", value.getLong("value") >>> 1);
                        }
                    }
                    case "and"  -> {
                        switch(type) {
                            case "int"  -> {
                                int n = value.getInt("value");
                                result.put("value", ((n+1) & n) == 0 && (n!=0));
                            }
                            case "long" -> {
                                long n = value.getLong("value");
                                result.put("value", ((n+1) & n) == 0 && (n!=0));
                            }
                        }
                    }
                    case "or"   -> {
                        switch(type) {
                            case "int" -> {
                                int n = value.getInt("value");
                                result.put("value", (n & ~(n & -n)) > 0);
                            }
                            case "long" -> {
                                long n = value.getInt("value");
                                result.put("value", (n & ~(n & -n)) > 0);
                            }
                        }
                    }
                    case "xor"  -> {
                        switch(type) {
                            case "int" -> {
                                int n = value.getInt("value");
                                result.put("value", (n & ~(n & -n)) == 0);
                            }
                            case "long" -> {
                                long n = value.getInt("value");
                                result.put("value", (n & ~(n & -n)) == 0);
                            }
                        }
                    }
                }

                m.sigma().push(result);
                psi.push(new Method(m.lambda(), m.sigma(), new Pair<>(m.iota().e1(), m.iota().e2() + 1)));
            }
            case "cast" -> {
                String from = instruction.getString("from");    // "int" | Arithmetic Type
                String to = instruction.getString("to");        // Small types | Arithmetic Type

                JSONObject value = m.sigma().pop();

                JSONObject result = new JSONObject();
                result.put("type", to);

                switch (from) {
                    case "int" -> {
                        switch (to) {
                            case "byte"     -> result.put("value", (byte) value.getInt("value"));
                            case "char"     -> result.put("value", (char) value.getInt("value"));
                            case "short"    -> result.put("value", (short) value.getInt("value"));
                            case "long"     -> result.put("value", (long) value.getInt("value"));
                            case "float"    -> result.put("value", (float) value.getInt("value"));
                            case "double"   -> result.put("value", (double) value.getInt("value"));
                        }
                    }
                    case "long" -> {
                        switch (to) {
                            case "int"      -> result.put("value", (int) value.getLong("value"));
                            case "float"    -> result.put("value", (float) value.getLong("value"));
                            case "double"   -> result.put("value", (double) value.getLong("value"));
                            default         -> System.out.println("Unsupported cast target for long type");
                        }
                    }
                    case "float" -> {
                        switch (to) {
                            case "int"      -> result.put("value", (int) value.getFloat("value"));
                            case "long"     -> result.put("value", (long) value.getFloat("value"));
                            case "double"   -> result.put("value", (double) value.getFloat("value"));
                            default         -> System.out.println("Unsupported cast target for float type");
                        }
                    }
                    case "double" -> {
                        switch (to) {
                            case "int"      -> result.put("value", (int) value.getDouble("value"));
                            case "long"     -> result.put("value", (long) value.getDouble("value"));
                            case "float"    -> result.put("value", (float) value.getDouble("value"));
                            default         -> System.out.println("Unsupported cast target for double type");
                        }
                    }
                    default -> System.out.println("Casting from this type is unsupported");
                }

                m.sigma().push(result);
                psi.push(new Method(m.lambda(), m.sigma(), new Pair<>(m.iota().e1(), m.iota().e2() + 1)));
            }
            case "comparelongs" -> {
                JSONObject value2 = m.sigma().pop();
                JSONObject value1 = m.sigma().pop();

                JSONObject result = new JSONObject();
                result.put("type", "int");
                result.put("value", (Long.compare(value1.getLong("value"), value2.getLong("value"))));

                m.sigma().push(result);
                psi.push(new Method(m.lambda(), m.sigma(), new Pair<>(m.iota().e1(), m.iota().e2() + 1)));
            }
            case "comparefloating" -> {
                JSONObject value2 = m.sigma().pop();
                JSONObject value1 = m.sigma().pop();

                JSONObject result = new JSONObject();
                result.put("type", "int");
                switch(instruction.getString("type")) {
                    case "float" -> {
                        try {
                            float v1 = value1.getFloat("value");
                            float v2 = value2.getFloat("value");
                            result.put("value", (Float.compare(v1, v2)));
                        } catch(JSONException e) {
                            result.put("value", instruction.getInt("onnan"));
                        }
                    }
                    case "double" -> {
                        try {
                            double v1 = value1.getDouble("value");
                            double v2 = value2.getDouble("value");
                            result.put("value", (Double.compare(v1, v2)));
                        } catch(JSONException e) {
                            result.put("value", instruction.getInt("onnan"));
                        }
                    }
                }

                m.sigma().push(result);
                psi.push(new Method(m.lambda(), m.sigma(), new Pair<>(m.iota().e1(), m.iota().e2() + 1)));
            }*/
            case "if" -> {
                int target = instruction.getInt("target");

                JSONObject value2 = m.sigma().pop();
                JSONObject value1 = m.sigma().pop();

                boolean result = switch(instruction.getString("condition")) {
                    case "eq"       -> value1.getInt("value") == value2.getInt("value");
                    case "ne"       -> value1.getInt("value") != value2.getInt("value");
                    case "le"       -> value1.getInt("value") <= value2.getInt("value");
                    case "lt"       -> value1.getInt("value") < value2.getInt("value");
                    case "ge"       -> value1.getInt("value") >= value2.getInt("value");
                    case "gt"       -> value1.getInt("value") > value2.getInt("value");
                    case "is"       -> mu.get(System.identityHashCode(value1)).equals(mu.get(System.identityHashCode(value2)));
                    case "isnot"    -> !mu.get(System.identityHashCode(value1)).equals(mu.get(System.identityHashCode(value2)));
                    default         -> {
                        System.out.println("Unsupported condition");
                        yield false;
                    }
                };

                psi.push(new Method(m.lambda(), m.sigma(), new Pair<>(m.iota().e1(), result ? target : m.iota().e2() + 1)));
            }
            case "ifz" -> {
                String condition = instruction.getString("condition");
                int target = instruction.getInt("target");

                JSONObject value = m.sigma().pop();

                boolean result;
                switch(value.getString("type")) {
                    case "int" -> {
                        // int v = value.getInt("value"); We don't use this (?)
                        Function<Sign, Set<Boolean>> f = switch(condition) {
                            case "eq"       -> {
                                yield (s) -> {
                                    return switch (s) {
                                        case NEGATIVE -> Set.of(false);
                                        case ZERO -> Set.of(true);
                                        case POSITIVE -> Set.of(false);
                                    };
                                };
                            }
                            case "ne"       -> {
                                yield (s) -> {
                                    return switch (s) {
                                        case NEGATIVE -> Set.of(true);
                                        case ZERO -> Set.of(false);
                                        case POSITIVE -> Set.of(true);
                                    };
                                };
                            }
                            case "le"       -> {
                                yield (s) -> {
                                    return switch (s) {
                                        case NEGATIVE -> Set.of(true);
                                        case ZERO -> Set.of(true);
                                        case POSITIVE -> Set.of(false);
                                    };
                                };
                            }
                            case "lt"       -> {
                                yield (s) -> {
                                    return switch (s) {
                                        case NEGATIVE -> Set.of(true);
                                        case ZERO -> Set.of(false);
                                        case POSITIVE -> Set.of(false);
                                    };
                                };
                            }
                            case "ge"       -> {
                                yield (s) -> {
                                    return switch (s) {
                                        case NEGATIVE -> Set.of(false);
                                        case ZERO -> Set.of(true);
                                        case POSITIVE -> Set.of(true);
                                    };
                                };
                            }
                            case "gt"       -> {
                                yield (s) -> {
                                    return switch (s) {
                                        case NEGATIVE -> Set.of(false);
                                        case ZERO -> Set.of(false);
                                        case POSITIVE -> Set.of(true);
                                    };
                                };
                            }
                            default -> throw new IllegalStateException("Unexpected value: " + condition);
                        };

                        for(Object s : value.getJSONArray("sign")) {
                            Set<Boolean> signs = f.apply((Sign) s);
                            if(signs.isEmpty()) System.out.println("Error msg");

                            JSONObject signResult = new JSONObject(Map.of("sign", signs));

                            Deque<Method> _psi = psi.stream().map(Method::clone).collect(Collectors.toCollection(ArrayDeque::new));
                            Map<Integer, JSONObject> _mu = mu.entrySet().stream().map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), new JSONObject(e.getValue().toMap()))).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                            Method _m = m.clone();

                            _m.sigma().push(signResult);

                            _psi.push(new Method(_m.lambda(), _m.sigma(), new Pair<>(_m.iota().e1(), _m.iota().e2() + 1)));

                            results.add(new State(_psi, _mu));
                        }

                    }
                    case "ref" -> {
                        JSONObject v = mu.get(System.identityHashCode(value));
                        result = switch(condition) {
                            case "is"       -> v == null;
                            case "isnot"    -> v != null;
                            default         -> {
                                System.out.println("Unsupported condition in \"ref\"");
                                yield false;
                            }
                        };
                    }
                    default -> {
                        System.out.println("Unsupported type");
                        result = false;
                    }
                }

                //psi.push(new Method(m.lambda(), m.sigma(), new Pair<>(m.iota().e1(), result ? target : m.iota().e2() + 1)));
            }
            case "goto" -> {
                psi.push(new Method(m.lambda(), m.sigma(), new Pair<>(m.iota().e1(), m.iota().e2() + 1)));
                results.add(new State(psi, mu));
            }/*
            case "jsr" -> {
                int target = instruction.getInt("target");

                Method next = psi.peek();
                JSONObject value = new JSONObject();
                value.put("type", "int");
                value.put("value", next.iota().e2());
                m.sigma().push(value);

                psi.push(new Method(m.lambda(), m.sigma(), new Pair<>(m.iota().e1(), target)));
            }
            case "ret" -> {
                JSONObject value = m.sigma().pop();
                int address = value.getInt("value");

                psi.push(new Method(m.lambda(), m.sigma(), new Pair<>(m.iota().e1(), address)));
            }
            case "tableswitch" -> {
                int location = instruction.getInt("default");
                int low = instruction.getInt("low");

                JSONObject value = m.sigma().pop();
                int index = value.getInt("index");

                JSONArray targets = instruction.getJSONArray("targets");
                for(int i = 0; i < targets.length(); i++) {
                    int target = targets.getInt(i);
                    if(target == index) {
                        location = index - low;
                        break;
                    }
                }

                psi.push(new Method(m.lambda(), m.sigma(), new Pair<>(m.iota().e1(), location)));
            }
            case "lookupswitch" -> {
                int location = instruction.getInt("default");

                JSONObject value = m.sigma().pop();
                int index = value.getInt("index");

                JSONArray targets = instruction.getJSONArray("targets");
                for(int i = 0; i < targets.length(); i++) {
                    JSONObject target = targets.getJSONObject(i);
                    if(target.getInt("key") == index) {
                        location = target.getInt("target");
                        break;
                    }
                }

                psi.push(new Method(m.lambda(), m.sigma(), new Pair<>(m.iota().e1(), location)));
            }
            case "get" -> {
                JSONObject field = instruction.getJSONObject("field");

                JSONObject value = null;

                JSONObject object;
                if(instruction.getBoolean("static")) {
                    object = classes.get(field.getString("class"));
                } else {
                    JSONObject ref = m.sigma().pop();
                    object = mu.get(System.identityHashCode(ref));
                }

                JSONArray fields = object.getJSONArray("fields");
                for(int i = 0; i < fields.length(); i++) {
                    JSONObject f = fields.getJSONObject(i);
                    if(f.getString("name").equals(field.getString("name"))) {
                        value = f.getJSONObject("value");
                        break;
                    }
                }

                m.sigma().push(value);
                psi.push(new Method(m.lambda(), m.sigma(), new Pair<>(m.iota().e1(), m.iota().e2() + 1)));
            }
            case "put" -> {
                JSONObject field = instruction.getJSONObject("field");

                JSONObject value = m.sigma().pop();

                JSONObject object;
                if(instruction.getBoolean("static")) {
                    object = classes.get(field.getString("class"));
                } else {
                    JSONObject ref = m.sigma().pop();
                    object = mu.get(System.identityHashCode(ref));
                }

                JSONArray fields = object.getJSONArray("fields");
                for(int i = 0; i < fields.length(); i++) {
                    JSONObject f = fields.getJSONObject(i);
                    if(f.getString("name").equals(field.getString("name"))) {
                        switch(f.getString("type")) {
                            case "integer"  -> f.put("value", value.getInt("value"));
                            case "long"     -> f.put("value", value.getLong("value"));
                            case "float"    -> f.put("value", value.getFloat("value"));
                            case "double"   -> f.put("value", value.getDouble("value"));
                            case "string"   -> f.put("value", value.getString("value"));
                            case "class"    -> f.put("value", value.getJSONObject("value"));
                            default         -> System.out.println("Unsupported type");
                        }
                    }
                }

                psi.push(new Method(m.lambda(), m.sigma(), new Pair<>(m.iota().e1(), m.iota().e2() + 1)));
            }
            case "invoke" -> {
                JSONObject method = instruction.getJSONObject("method");

                switch(instruction.getString("access")) {
                    case "special" -> {}
                    case "virtual" -> {}
                    case "static" -> {
                        JSONObject m_ref = method.getJSONObject("ref");
                        String classname = m_ref.getString("name");
                        String methodname = method.getString("name");
                        JSONArray args = method.getJSONArray("args");

                        if(m.sigma().size() < args.length()) System.out.println("Not enough elements in stack for invocation of method!");

                        JSONObject[] lambda = new JSONObject[args.length()];
                        for(int i = 0; i < args.length(); i++) {
                            JSONObject arg = m.sigma().pop();

                            String type_expected = args.get(i) instanceof String ? args.getString(i) : (args.getJSONObject(i).has("kind") ? "ref" : args.getJSONObject(i).getString("type"));
                            String type_actual = arg.getString("type");

                            if(!type_actual.equals(type_expected)) {
                                System.out.println("Type mismatch: Expected " + type_expected + " but was " + type_actual);
                            }

                            lambda[i] = arg;
                        }

                        psi.push(new Method(m.lambda(), m.sigma(), new Pair<>(m.iota().e1(), m.iota().e2() + 1)));
                        psi.push(new Method(lambda, new ArrayDeque<>(), new Pair<>(classname + "/" + methodname, 0)));
                    }
                    case "interface" -> {}
                    case "dynamic" -> {}
                }
            }
            case "new" -> {
                String classname = instruction.getString("class");

                JSONObject objectref = new JSONObject();
                objectref.put("kind", "class");
                objectref.put("name", classname);

                if(!classes.containsKey(classname)) {
                    System.out.println("Operation \"new\" failed: Class " + classname + " is not available!");
                }

                JSONObject result = new JSONObject(classes.get(classname).toString());

                mu.put(System.identityHashCode(objectref), result);

                m.sigma().push(objectref);
                psi.push(new Method(m.lambda(), m.sigma(), new Pair<>(m.iota().e1(), m.iota().e2() + 1)));
            }
            case "newarray" -> {
                int dim = instruction.getInt("dim"); // Recurse / While-loop magic
                String type = instruction.getString("type");

                JSONObject value_length = m.sigma().pop();
                int length = value_length.getInt("value");

                // Create value
                JSONObject result = new JSONObject();
                result.put("type", type);
                JSONArray value = new JSONArray(length);
                for(int i = 0; i < length; i++) {
                    JSONObject value_inner = new JSONObject();
                    value_inner.put("type", type);
                    value_inner.put("value", 0); // Set default value?
                    value.put(i, value_inner);
                }
                result.put("value", value);

                // Create reference to value
                JSONObject ref = new JSONObject(Map.of("kind", "array", "type", type));

                mu.put(System.identityHashCode(ref), result);
                m.sigma().push(ref);

                psi.push(new Method(m.lambda(), m.sigma(), new Pair<>(m.iota().e1(), m.iota().e2() + 1)));
            }
            case "arraylength" -> {
                JSONObject ref = m.sigma().pop();

                JSONObject array = mu.get(System.identityHashCode(ref));

                JSONObject result = new JSONObject();
                result.put("type", "int");
                result.put("value", array.getJSONArray("value").length());

                m.sigma().push(result);

                psi.push(new Method(m.lambda(), m.sigma(), new Pair<>(m.iota().e1(), m.iota().e2() + 1)));
            }
            */
            case "return" -> {
                if(instruction.isNull("type")) break;

                String type = instruction.getString("type"); // LocalType
                JSONObject value = m.sigma().pop();

                JSONObject result = new JSONObject(value.toMap());
                /*
                result.put("type", type);
                switch(type) {
                    case "int"      -> result.put("value", value.getInt("value"));
                    case "long"     -> result.put("value", value.getLong("value"));
                    case "float"    -> result.put("value", value.getFloat("value"));
                    case "double"   -> result.put("value", value.getDouble("value"));
                    case "ref"      -> result.put("value", value);
                }
                */

                if(!psi.isEmpty()) {
                    Method m2 = psi.peek();
                    m2.sigma().push(result);

                    results.add(state);
                } else {
                    System.out.println(String.format("%-12s", "return") + Main.toFormattedString(result));
                }
            }
            /*
            case "nop" -> {
                psi.push(new Method(m.lambda(), m.sigma(), new Pair<>(m.iota().e1(), m.iota().e2() + 1)));
            }
            case "pop" -> {
                int words = instruction.getInt("words");

                while(!m.sigma().isEmpty() && words > 0) {
                    m.sigma().pop();
                    words--;
                }

                psi.push(new Method(m.lambda(), m.sigma(), new Pair<>(m.iota().e1(), m.iota().e2() + 1)));
            }
            case "dup" -> {
                int words = instruction.getInt("words");
                if(m.sigma().size() < words) System.out.println("Not enough elements in stack for duplication");

                List<JSONObject> local = new ArrayList<>();
                for(int i = 0; i < words; i++) {
                    local.add(m.sigma().pop());
                }

                for(int i = 0; i < words+1; i++) {
                    for(JSONObject jsonObject : local) {
                        m.sigma().push(jsonObject);
                    }
                }

                psi.push(new Method(m.lambda(), m.sigma(), new Pair<>(m.iota().e1(), m.iota().e2() + 1)));
            }
            case "dup_x1" -> {
                int words = instruction.getInt("words");
                if(m.sigma().size() < words + 1) System.out.println("Not enough elements in stack for duplication");

                List<JSONObject> local = new ArrayList<>();
                for(int i = 0; i < words; i++) {
                    local.add(m.sigma().pop());
                }

                JSONObject word = m.sigma().pop();

                for(int i = 0; i < words; i++) {
                    for(JSONObject jsonObject : local) {
                        m.sigma().push(jsonObject);
                    }
                }

                m.sigma().push(word);

                for(int i = 0; i < words; i++) {
                    for(JSONObject jsonObject : local) {
                        m.sigma().push(jsonObject);
                    }
                }

                psi.push(new Method(m.lambda(), m.sigma(), new Pair<>(m.iota().e1(), m.iota().e2() + 1)));
            }
            case "dup_x2" -> {
                int words = instruction.getInt("words");
                if(m.sigma().size() < words + 2) System.out.println("Not enough elements in stack for duplication");

                List<JSONObject> local = new ArrayList<>();
                for(int i = 0; i < words; i++) {
                    local.add(m.sigma().pop());
                }

                JSONObject word1 = m.sigma().pop();
                JSONObject word2 = m.sigma().pop();

                for(int i = 0; i < words; i++) {
                    for(JSONObject jsonObject : local) {
                        m.sigma().push(jsonObject);
                    }
                }

                m.sigma().push(word2);
                m.sigma().push(word1);

                for(int i = 0; i < words; i++) {
                    for(JSONObject jsonObject : local) {
                        m.sigma().push(jsonObject);
                    }
                }

                psi.push(new Method(m.lambda(), m.sigma(), new Pair<>(m.iota().e1(), m.iota().e2() + 1)));
            }
            case "swap" -> {
                JSONObject value2 = m.sigma().pop();
                JSONObject value1 = m.sigma().pop();

                m.sigma().push(value2);
                m.sigma().push(value1);

                psi.push(new Method(m.lambda(), m.sigma(), new Pair<>(m.iota().e1(), m.iota().e2() + 1)));
            }
            */
            default -> {
                System.out.println("Unsupported operation + \"" + instruction.getString("opr") + "\"");
            }
        }

        for(State s : results) {
            System.out.println(String.format("%-12s", instruction.getString("opr")) + "Ψ" + s.psi());
        }

        return results;
    }
}
