package Week05;

import Week04.Main;
import Week04.Frame;
import Week04.Pair;
import Week04.SimpleType;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

import static Week04.ConcreteInterpreter.initializeArray;
import static Week05.Sign.*;
import static Week05.SignInterpreter.clone_state;
import static Week05.SignInterpreter.toAbstract;

public class SignStepper implements AbstractStepper {

    private final Map<String, JSONObject> classes;

    public SignStepper(Map<String, JSONObject> classes) {
        this.classes = classes;
    }

    @Override
    public Set<State> step(State state) {
        Set<State> results = new HashSet<>();

        Map<Integer, JSONObject> mu = state.mu();
        Deque<Frame> psi = state.psi();
        Frame f = psi.pop();

        JSONObject instruction = f.iota().e1().method().getJSONObject("code").getJSONArray("bytecode").getJSONObject(f.iota().e2());

        switch(instruction.getString("opr")) {
            case "array_load" -> {
                JSONObject index = f.sigma().pop();
                JSONObject arrayref = f.sigma().pop();

                if(arrayref == null) throw new NullPointerException("Cannot load from array because \"arrayref\" is null");

                JSONObject actual = mu.get(System.identityHashCode(arrayref));
                JSONArray array = actual.getJSONArray("value");

                if(array == null) throw new NullPointerException("Cannot load from array because \"array\" is null");

                int index_value = index.getInt("value");
                if(array.length() < index_value) throw new ArrayIndexOutOfBoundsException("Index " + index_value + " out of bounds for length " + array.length());

                JSONObject value = array.getJSONObject(index_value);

                f.sigma().push(value.has("kind") ? value : new JSONObject(value.toMap()));
                psi.push(new Frame(f.lambda(), f.sigma(), new Pair<>(f.iota().e1(), f.iota().e2() + 1)));

                results.add(state);
            }
            case "array_store" -> {
                JSONObject value = f.sigma().pop();
                JSONObject index = f.sigma().pop();
                JSONObject arrayref = f.sigma().pop();

                if(arrayref == null) throw new NullPointerException("Cannot store to array because \"arrayref\" is null");

                JSONObject actual = mu.get(System.identityHashCode(arrayref));
                JSONArray array = actual.getJSONArray("value");

                if(array == null) throw new NullPointerException("Cannot store to array because \"array\" is null");

                int index_value = index.getInt("value");
                if(array.length() < index_value) throw new ArrayIndexOutOfBoundsException("Index " + index_value + " out of bounds for length " + array.length());

                array.put(index_value, value);
                psi.push(new Frame(f.lambda(), f.sigma(), new Pair<>(f.iota().e1(), f.iota().e2() + 1)));

                results.add(state);
            }
            case "push" -> {
                JSONObject value = instruction.getJSONObject("value");
                if(value.getString("type").equals("class")) { // Value is a <SimpleReferenceType>
                    f.sigma().push(value);
                } else {
                    f.sigma().push(toAbstract(new JSONObject(value.toMap())));
                }

                psi.push(new Frame(f.lambda(), f.sigma(), new Pair<>(f.iota().e1(), f.iota().e2() + 1)));

                results.add(state);
            }
            case "load" -> {
                int index = instruction.getInt("index");
                JSONObject value = f.lambda()[index];
                if(value.has("kind")) { // Check if it's a reference type
                    f.sigma().push(value);
                } else {
                    f.sigma().push(toAbstract(new JSONObject(value.toMap())));
                }

                psi.push(new Frame(f.lambda(), f.sigma(), new Pair<>(f.iota().e1(), f.iota().e2() + 1)));

                results.add(state);
            }
            case "store" -> {
                int index = instruction.getInt("index");
                JSONObject value = f.sigma().pop();
                f.lambda()[index] = value;

                psi.push(new Frame(f.lambda(), f.sigma(), new Pair<>(f.iota().e1(), f.iota().e2() + 1)));

                results.add(state);
            }
            case "incr" -> { // For now we're assuming everything is an integer :)
                int index = instruction.getInt("index");
                JSONObject value = f.lambda()[index];
                Sign amount = Sign.toSign(instruction.getInt("amount"));

                BiFunction<Sign, Sign, Set<Sign>> fun = (s1, s2) -> {
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

                for(Object sign : value.getJSONArray("sign")) {
                    Set<Sign> signs = fun.apply((Sign) sign, amount);

                    Triple<Frame, Deque<Frame>, Map<Integer, JSONObject>> t = clone_state(f, psi, mu);

                    Frame _f = t.e1();
                    Deque<Frame> _psi = t.e2();
                    Map<Integer, JSONObject> _mu = t.e3();

                    JSONObject value_new = new JSONObject(value.toMap());
                    value_new.put("sign", signs);
                    _f.lambda()[index] = value_new;

                    _psi.push(new Frame(_f.lambda(), _f.sigma(), new Pair<>(_f.iota().e1(), _f.iota().e2() + 1)));

                    results.add(new State(_psi, _mu));
                }
            }
            case "binary" -> {
                String type = instruction.getString("type"); // Arithmetic Type
                JSONObject value2 = f.sigma().pop();
                JSONObject value1 = f.sigma().pop();

                // value1 opr value2
                BiFunction<Sign, Sign, Set<Sign>> fun = (s1, s2) -> switch(instruction.getString("operant")) {
                    case "add" -> {
                        yield switch(s1) {
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
                    }
                    case "sub" -> {
                        yield switch(s1) {
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
                    }
                    case "mul" -> {
                        yield switch(s1) {
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
                    }
                    case "div" -> {
                        yield switch(s1) {
                            case NEGATIVE -> switch(s2) {
                                case NEGATIVE   -> Set.of(POSITIVE);
                                case ZERO       -> throw new ArithmeticException("Illegal divide by zero");
                                case POSITIVE   -> Set.of(NEGATIVE);
                            };
                            case ZERO -> switch(s2) {
                                case NEGATIVE   -> Set.of(ZERO);
                                case ZERO       -> throw new ArithmeticException("Illegal divide by zero");
                                case POSITIVE   -> Set.of(ZERO);
                            };
                            case POSITIVE -> switch(s2) {
                                case NEGATIVE   -> Set.of(NEGATIVE);
                                case ZERO       -> throw new ArithmeticException("Illegal divide by zero");
                                case POSITIVE   -> Set.of(POSITIVE);
                            };
                        };
                    }
                    default -> throw new IllegalStateException("Unexpected value: " + instruction.getString("operant"));
                };

                for(Object s1 : value1.getJSONArray("sign")) {
                    for(Object s2 : value2.getJSONArray("sign")) {
                        Set<Sign> signs = fun.apply((Sign) s1, (Sign) s2);

                        JSONObject result = new JSONObject(Map.of("sign", signs));

                        Triple<Frame, Deque<Frame>, Map<Integer, JSONObject>> t = clone_state(f, psi, mu);

                        Frame _f = t.e1();
                        Deque<Frame> _psi = t.e2();
                        Map<Integer, JSONObject> _mu = t.e3();

                        _f.sigma().push(result);

                        _psi.push(new Frame(_f.lambda(), _f.sigma(), new Pair<>(_f.iota().e1(), _f.iota().e2() + 1)));

                        results.add(new State(_psi, _mu));
                    }
                }
            }
            case "negate" -> {
                String type = instruction.getString("type"); // Arithmetic Type
                JSONObject value = f.sigma().pop();

                Function<Sign, Sign> negate = (sign) -> switch(sign) {
                    case POSITIVE   -> NEGATIVE;
                    case ZERO       -> ZERO;
                    case NEGATIVE   -> POSITIVE;
                };

                JSONArray signs = value.getJSONArray("sign");
                for(int i = 0; i < signs.length(); i++) {
                    signs.put(i, negate.apply((Sign) signs.get(i)));
                }

                f.sigma().push(value);
                psi.push(new Frame(f.lambda(), f.sigma(), new Pair<>(f.iota().e1(), f.iota().e2() + 1)));

                results.add(state);
            }
            /*
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
            }*/
            case "cast" -> {
                psi.push(new Frame(f.lambda(), f.sigma(), new Pair<>(f.iota().e1(), f.iota().e2() + 1)));

                results.add(state);
            }/*
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

                JSONObject value2 = f.sigma().pop();
                JSONObject value1 = f.sigma().pop();

                // Value1 opr Value2
                BiFunction<Sign, Sign, Set<Boolean>> fun = (s1, s2) -> switch(instruction.getString("condition")) {
                    case "eq"       -> {
                        if((s1 == NEGATIVE && s2 == NEGATIVE) || (s1 == POSITIVE && s2 == POSITIVE)) yield Set.of(true, false);
                        else if(s1 == ZERO && s2 == ZERO) yield Set.of(true);
                        else yield Set.of(false);
                    }
                    case "ne"       -> {
                        if(s1 == s2) yield Set.of(false);
                        else yield Set.of(true, false);
                    }
                    case "le"       -> {
                        yield switch(s1) {
                            case NEGATIVE   -> switch(s2) {
                                case NEGATIVE   -> Set.of(true, false);
                                case ZERO       -> Set.of(true);
                                case POSITIVE   -> Set.of(true);
                            };
                            case ZERO       -> switch(s2) {
                                case NEGATIVE   -> Set.of(false);
                                case ZERO       -> Set.of(false);
                                case POSITIVE   -> Set.of(true);
                            };
                            case POSITIVE   -> switch(s2) {
                                case NEGATIVE   -> Set.of(false);
                                case ZERO       -> Set.of(false);
                                case POSITIVE   -> Set.of(true, false);
                            };
                        };
                    }
                    case "lt"       -> {
                        yield switch(s1) {
                            case NEGATIVE   -> switch(s2) {
                                case NEGATIVE   -> Set.of(true, false);
                                case ZERO       -> Set.of(true);
                                case POSITIVE   -> Set.of(true);
                            };
                            case ZERO       -> switch(s2) {
                                case NEGATIVE   -> Set.of(false);
                                case ZERO       -> Set.of(true);
                                case POSITIVE   -> Set.of(true);
                            };
                            case POSITIVE   -> switch(s2) {
                                case NEGATIVE   -> Set.of(false);
                                case ZERO       -> Set.of(false);
                                case POSITIVE   -> Set.of(true, false);
                            };
                        };
                    }
                    case "ge"       -> {
                        yield switch(s1) {
                            case NEGATIVE   -> switch(s2) {
                                case NEGATIVE   -> Set.of(true, false);
                                case ZERO       -> Set.of(false);
                                case POSITIVE   -> Set.of(false);
                            };
                            case ZERO       -> switch(s2) {
                                case NEGATIVE   -> Set.of(true);
                                case ZERO       -> Set.of(true);
                                case POSITIVE   -> Set.of(false);
                            };
                            case POSITIVE   -> switch(s2) {
                                case NEGATIVE   -> Set.of(true);
                                case ZERO       -> Set.of(true);
                                case POSITIVE   -> Set.of(true, false);
                            };
                        };
                    }
                    case "gt"       -> {
                        yield switch(s1) {
                            case NEGATIVE   -> switch(s2) {
                                case NEGATIVE   -> Set.of(true, false);
                                case ZERO       -> Set.of(false);
                                case POSITIVE   -> Set.of(false);
                            };
                            case ZERO       -> switch(s2) {
                                case NEGATIVE   -> Set.of(false);
                                case ZERO       -> Set.of(false);
                                case POSITIVE   -> Set.of(false);
                            };
                            case POSITIVE   -> switch(s2) {
                                case NEGATIVE   -> Set.of(true);
                                case ZERO       -> Set.of(true);
                                case POSITIVE   -> Set.of(true, false);
                            };
                        };
                    }
                    // For object equality we assume both cases can be true/false
                    default         -> Set.of(true, false);
                };

                Set<Boolean> bools = new HashSet<>();
                for(Object s1 : value1.getJSONArray("sign")) {
                    for(Object s2 : value2.getJSONArray("sign")) {
                        bools.addAll(fun.apply((Sign) s1, (Sign) s2));
                        if(bools.size() > 1) break;
                    }
                    if(bools.size() > 1) break;
                }

                List<Boolean> if_results = bools.stream().toList();
                if(bools.size() > 1) {
                    Triple<Frame, Deque<Frame>, Map<Integer, JSONObject>> t = clone_state(f, psi, mu);

                    Frame _f = t.e1();
                    Deque<Frame> _psi = t.e2();
                    Map<Integer, JSONObject> _mu = t.e3();

                    _psi.push(new Frame(_f.lambda(), _f.sigma(), new Pair<>(_f.iota().e1(), if_results.get(1) ? target : _f.iota().e2() + 1)));
                    results.add(new State(_psi, _mu));
                }

                psi.push(new Frame(f.lambda(), f.sigma(), new Pair<>(f.iota().e1(), if_results.get(0) ? target : f.iota().e2() + 1)));
            }
            case "ifz" -> {
                String condition = instruction.getString("condition");
                int target = instruction.getInt("target");

                JSONObject value = f.sigma().pop();

                Function<Sign, Boolean> fun = (s) -> switch(condition) {
                    case "eq" -> {
                        yield switch(s) {
                            case NEGATIVE   -> false;
                            case ZERO       -> true;
                            case POSITIVE   -> false;
                        };
                    }
                    case "ne" -> {
                        yield switch (s) {
                            case NEGATIVE   -> true;
                            case ZERO       -> false;
                            case POSITIVE   -> true;
                        };
                    }
                    case "le" -> {
                        yield switch (s) {
                            case NEGATIVE   -> true;
                            case ZERO       -> true;
                            case POSITIVE   -> false;
                        };
                    }
                    case "lt" -> {
                        yield switch (s) {
                            case NEGATIVE   -> true;
                            case ZERO       -> false;
                            case POSITIVE   -> false;
                        };
                    }
                    case "ge" -> {
                        yield switch (s) {
                            case NEGATIVE   -> false;
                            case ZERO       -> true;
                            case POSITIVE   -> true;
                        };
                    }
                    case "gt" -> {
                        yield switch (s) {
                            case NEGATIVE   -> false;
                            case ZERO       -> false;
                            case POSITIVE   -> true;
                        };
                    }
                    default -> throw new IllegalStateException("Unexpected condition: " + condition);
                };

                List<Boolean> ifz_results = new ArrayList<>();
                if(value.has("sign")) {
                    Set<Boolean> local = new HashSet<>();
                    for(Object sign : value.getJSONArray("sign")) {
                        local.add(fun.apply((Sign) sign));
                    }
                    ifz_results.addAll(local);
                } else {
                    // For object equality we assume both cases can be true/false
                    ifz_results.add(true);
                    ifz_results.add(false);
                }

                if(ifz_results.size() > 1) {
                    Triple<Frame, Deque<Frame>, Map<Integer, JSONObject>> t = clone_state(f, psi, mu);

                    Frame _m = t.e1();
                    Deque<Frame> _psi = t.e2();
                    Map<Integer, JSONObject> _mu = t.e3();

                    _psi.push(new Frame(_m.lambda(), _m.sigma(), new Pair<>(_m.iota().e1(), ifz_results.get(1) ? target : _m.iota().e2() + 1)));
                    results.add(new State(_psi, _mu));
                }

                psi.push(new Frame(f.lambda(), f.sigma(), new Pair<>(f.iota().e1(), ifz_results.get(0) ? target : f.iota().e2() + 1)));
                results.add(state);
            }
            case "goto" -> {
                psi.push(new Frame(f.lambda(), f.sigma(), new Pair<>(f.iota().e1(), f.iota().e2() + 1)));
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
            }*/
            case "get" -> {
                JSONObject field_info = instruction.getJSONObject("field");

                JSONObject value = null;

                JSONObject object;
                if(instruction.getBoolean("static")) {
                    object = classes.get(field_info.getString("class"));
                } else {
                    JSONObject ref = f.sigma().pop();
                    object = mu.get(System.identityHashCode(ref));
                }

                JSONArray fields = object.getJSONArray("fields");
                for(int i = 0; i < fields.length(); i++) {
                    JSONObject field = fields.getJSONObject(i);
                    if(field.getString("name").equals(field_info.getString("name"))) {
                        if(field.isNull("value")) {
                            value = SimpleType.createDefault(field_info.get("type"), mu);
                        } else {
                            value = new JSONObject(field.getJSONObject("value").toMap());
                        }

                        value = toAbstract(value);
                        break;
                    }
                }

                f.sigma().push(value);
                psi.push(new Frame(f.lambda(), f.sigma(), new Pair<>(f.iota().e1(), f.iota().e2() + 1)));

                results.add(state);
            }/*
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
            }*/
            case "new" -> {
                String classname = instruction.getString("class");

                if(!classes.containsKey(classname)) throw new InstantiationError(classname + " does not exist.");
                List<Object> access = classes.get(classname).getJSONArray("access").toList();
                if(access.contains("abstract")) throw new InstantiationError(classname + " is abstract.");
                if(access.contains("interface")) throw new InstantiationError(classname + " is an interface.");

                JSONObject objectref = new JSONObject(Map.of("kind", "class", "name", classname));
                JSONObject value = new JSONObject(classes.get(classname).toMap());

                mu.put(System.identityHashCode(objectref), value);

                f.sigma().push(objectref);
                psi.push(new Frame(f.lambda(), f.sigma(), new Pair<>(f.iota().e1(), f.iota().e2() + 1)));

                results.add(state);
            }
            case "newarray" -> {
                int dim = instruction.getInt("dim");
                Object type = instruction.get("type"); // SimpleType

                JSONObject count = f.sigma().pop();
                int length = count.getInt("value");

                JSONObject arrayref = initializeArray(type, length, dim, f.sigma(), mu);

                // If type is a BaseType, add signs
                if(type instanceof String) {
                    JSONArray array = mu.get(System.identityHashCode(arrayref)).getJSONArray("value");
                    for(int i = 0; i < array.length(); i++) {
                        JSONObject value = array.getJSONObject(i);
                        value.put("sign", Set.of(ZERO));
                    }
                }

                f.sigma().push(arrayref);
                psi.push(new Frame(f.lambda(), f.sigma(), new Pair<>(f.iota().e1(), f.iota().e2() + 1)));

                results.add(state);
            }
            case "arraylength" -> {
                JSONObject ref = f.sigma().pop();

                JSONObject array = mu.get(System.identityHashCode(ref));
                int length = array.getJSONArray("value").length();

                JSONObject result = new JSONObject();
                result.put("type", "int");
                result.put("value", length);
                result.put("sign", Set.of(length > 0 ? POSITIVE : ZERO));

                f.sigma().push(result);

                psi.push(new Frame(f.lambda(), f.sigma(), new Pair<>(f.iota().e1(), f.iota().e2() + 1)));

                results.add(state);
            }
            case "return" -> {
                if(instruction.isNull("type")) break;

                String type = instruction.getString("type"); // LocalType
                JSONObject value = f.sigma().pop();

                JSONObject result = new JSONObject(value.toMap());

                if(!psi.isEmpty()) {
                    Frame f2 = psi.peek();
                    f2.sigma().push(result);

                    results.add(state);
                } else {
                    System.out.println(String.format("%-12s", "return") + Main.toFormattedString(result));
                }
            }
            case "nop" -> {
                psi.push(new Frame(f.lambda(), f.sigma(), new Pair<>(f.iota().e1(), f.iota().e2() + 1)));
                results.add(state);
            }
            case "pop" -> {
                int words = instruction.getInt("words");

                while(!f.sigma().isEmpty() && words > 0) {
                    f.sigma().pop();
                    words--;
                }

                psi.push(new Frame(f.lambda(), f.sigma(), new Pair<>(f.iota().e1(), f.iota().e2() + 1)));
                results.add(state);
            }
            case "dup" -> {
                int words = instruction.getInt("words");
                if(f.sigma().size() < words) System.out.println("Not enough elements in stack for duplication");

                List<JSONObject> local = new ArrayList<>();
                for(int i = 0; i < words; i++) {
                    local.add(f.sigma().pop());
                }

                for(int i = 0; i < words+1; i++) {
                    for(JSONObject value : local) {
                        f.sigma().push(value.has("kind") ? value : new JSONObject(value.toMap()));
                    }
                }

                psi.push(new Frame(f.lambda(), f.sigma(), new Pair<>(f.iota().e1(), f.iota().e2() + 1)));
                results.add(state);
            }
            case "dup_x1" -> {
                int words = instruction.getInt("words");
                if(f.sigma().size() < words + 1) System.out.println("Not enough elements in stack for duplication");

                List<JSONObject> local = new ArrayList<>();
                for(int i = 0; i < words; i++) {
                    local.add(f.sigma().pop());
                }

                JSONObject word = f.sigma().pop();

                for(int i = 0; i < words; i++) {
                    for(JSONObject value : local) {
                        f.sigma().push(value.has("kind") ? value : new JSONObject(value.toMap()));
                    }
                }

                f.sigma().push(word);

                for(int i = 0; i < words; i++) {
                    for(JSONObject value : local) {
                        f.sigma().push(value.has("kind") ? value : new JSONObject(value.toMap()));
                    }
                }

                psi.push(new Frame(f.lambda(), f.sigma(), new Pair<>(f.iota().e1(), f.iota().e2() + 1)));
                results.add(state);
            }
            case "dup_x2" -> {
                int words = instruction.getInt("words");
                if(f.sigma().size() < words + 2) System.out.println("Not enough elements in stack for duplication");

                List<JSONObject> local = new ArrayList<>();
                for(int i = 0; i < words; i++) {
                    local.add(f.sigma().pop());
                }

                JSONObject word1 = f.sigma().pop();
                JSONObject word2 = f.sigma().pop();

                for(int i = 0; i < words; i++) {
                    for(JSONObject value : local) {
                        f.sigma().push(value.has("kind") ? value : new JSONObject(value.toMap()));
                    }
                }

                f.sigma().push(word2);
                f.sigma().push(word1);

                for(int i = 0; i < words; i++) {
                    for(JSONObject value : local) {
                        f.sigma().push(value.has("kind") ? value : new JSONObject(value.toMap()));
                    }
                }

                psi.push(new Frame(f.lambda(), f.sigma(), new Pair<>(f.iota().e1(), f.iota().e2() + 1)));
                results.add(state);
            }
            case "swap" -> {
                JSONObject value2 = f.sigma().pop();
                JSONObject value1 = f.sigma().pop();

                f.sigma().push(value2);
                f.sigma().push(value1);

                psi.push(new Frame(f.lambda(), f.sigma(), new Pair<>(f.iota().e1(), f.iota().e2() + 1)));
                results.add(state);
            }
            default -> {
                System.out.println("Unsupported operation \"" + instruction.getString("opr") + "\"");
            }
        }

        for(State s : results) {
            System.out.println(String.format("%-12s", instruction.getString("opr")) + "Ψ" + s.psi());
        }

        return results;
    }
}
