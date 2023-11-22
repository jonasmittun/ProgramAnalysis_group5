package Week05;

import Week04.*;
import Week04.Main;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

import static Week04.ConcreteInterpreter.*;
import static Week04.ConcreteInterpreter.initialize;
import static Week04.Main.cloneJSONObject;
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
        System.out.println("Instruction: " + instruction);

        switch(instruction.getString("opr")) {
            case "array_load" -> {
                String type = instruction.getString("type");
                JSONObject index = f.sigma().pop();
                JSONObject arrayref = f.sigma().pop();

                if(isNull(arrayref)) throw new NullPointerException("Cannot load from array because \"arrayref\" is null");

                JSONObject actual = mu.get(System.identityHashCode(arrayref));
                JSONArray array = actual.getJSONArray("value");

                int index_value = index.getInt("value");
                if(array.length() < index_value) throw new ArrayIndexOutOfBoundsException("Index " + index_value + " out of bounds for length " + array.length());

                JSONObject value = array.getJSONObject(index_value);

                f.sigma().push(type.equals("ref") ? value : cloneJSONObject(value));
                psi.push(new Frame(f.lambda(), f.sigma(), new Pair<>(f.iota().e1(), f.iota().e2() + 1)));

                results.add(state);
            }
            case "array_store" -> {
                JSONObject value = f.sigma().pop();
                JSONObject index = f.sigma().pop();
                JSONObject arrayref = f.sigma().pop();

                if(isNull(arrayref)) throw new NullPointerException("Cannot store to array because \"arrayref\" is null");

                JSONObject actual = mu.get(System.identityHashCode(arrayref));
                JSONArray array = actual.getJSONArray("value");

                int index_value = index.getInt("value");
                if(array.length() < index_value) throw new ArrayIndexOutOfBoundsException("Index " + index_value + " out of bounds for length " + array.length());

                array.put(index_value, value);
                psi.push(new Frame(f.lambda(), f.sigma(), new Pair<>(f.iota().e1(), f.iota().e2() + 1)));

                results.add(state);
            }
            case "push" -> {
                JSONObject o;
                if(instruction.isNull("value")) {
                    o = createNull();
                } else {
                    JSONObject value = instruction.getJSONObject("value");
                    String type = value.getString("type");

                    switch(type) {
                        case "class"    -> o = value;
                        case "string"   -> o = initializeString(classes, value.getString("value"), mu);
                        default         -> o = cloneJSONObject(value);
                    }
                }

                f.sigma().push(toAbstract(o));

                psi.push(new Frame(f.lambda(), f.sigma(), new Pair<>(f.iota().e1(), f.iota().e2() + 1)));

                results.add(state);
            }
            case "load" -> {
                int index = instruction.getInt("index");
                String type = instruction.getString("type");
                JSONObject value = f.lambda()[index];

                f.sigma().push(type.equals("ref") ? value : cloneJSONObject(value));

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

                for(Object sign : value.getJSONArray("sign")) {
                    Set<Sign> signs = Sign.add(Sign.toSign(sign), amount);

                    Triple<Frame, Deque<Frame>, Map<Integer, JSONObject>> t = clone_state(f, psi, mu);

                    Frame _f = t.e1();
                    Deque<Frame> _psi = t.e2();
                    Map<Integer, JSONObject> _mu = t.e3();

                    JSONObject value_new = cloneJSONObject(value);
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
                    case "add" -> Sign.add(s1, s2);
                    case "sub" -> Sign.sub(s1, s2);
                    case "mul" -> Sign.mul(s1, s2);
                    case "div" -> Sign.div(s1, s2);
                    case "rem" -> Sign.rem(s1, s2);
                    default -> throw new IllegalStateException("Unexpected value: " + instruction.getString("operant"));
                };

                for(Object s1 : value1.getJSONArray("sign")) {
                    for(Object s2 : value2.getJSONArray("sign")) {
                        Set<Sign> signs = fun.apply(Sign.toSign(s1), Sign.toSign(s2));

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

                JSONArray signs = value.getJSONArray("sign");
                for(int i = 0; i < signs.length(); i++) {
                    signs.put(i, Sign.negate(Sign.toSign(signs.get(i))));
                }

                f.sigma().push(value);
                psi.push(new Frame(f.lambda(), f.sigma(), new Pair<>(f.iota().e1(), f.iota().e2() + 1)));

                results.add(state);
            }
            case "bitopr" -> {
                psi.push(new Frame(f.lambda(), f.sigma(), new Pair<>(f.iota().e1(), f.iota().e2() + 1)));

                results.add(state);
            }
            case "cast" -> {
                psi.push(new Frame(f.lambda(), f.sigma(), new Pair<>(f.iota().e1(), f.iota().e2() + 1)));

                results.add(state);
            }
            case "comparelongs" -> {
                f.sigma().pop();
                f.sigma().pop();

                // Dummy result
                JSONObject dummy = toAbstract(new JSONObject(Map.of("type", "int", "value", 0, "sign", Set.of(ZERO))));
                f.sigma().push(dummy);

                psi.push(new Frame(f.lambda(), f.sigma(), new Pair<>(f.iota().e1(), f.iota().e2() + 1)));

                results.add(state);
            }
            case "comparefloating" -> {
                f.sigma().pop();
                f.sigma().pop();

                // Dummy result
                JSONObject dummy = toAbstract(new JSONObject(Map.of("type", "int", "value", 0, "sign", Set.of(ZERO))));
                f.sigma().push(dummy);

                psi.push(new Frame(f.lambda(), f.sigma(), new Pair<>(f.iota().e1(), f.iota().e2() + 1)));

                results.add(state);
            }
            case "if" -> {
                String condition = instruction.getString("condition");
                int target = instruction.getInt("target");

                JSONObject value2 = f.sigma().pop();
                JSONObject value1 = f.sigma().pop();

                Set<Boolean> bools = switch(condition) {
                    case "is":
                    case "isnot":
                        yield Set.of(true, false);
                    default:
                        if(!value1.has("sign") || !value2.has("sign")) yield Set.of(true, false);
                        else {
                            // Value1 opr Value2
                            BiFunction<Sign, Sign, Set<Boolean>> fun = (s1, s2) -> switch(condition) {
                                case "eq"   -> Sign.eq(s1, s2);
                                case "ne"   -> Sign.ne(s1, s2);
                                case "le"   -> Sign.le(s1, s2);
                                case "lt"   -> Sign.lt(s1, s2);
                                case "ge"   -> Sign.ge(s1, s2);
                                case "gt"   -> Sign.gt(s1, s2);
                                // For object equality we assume both cases can be true/false
                                default     -> Set.of(true, false);
                            };

                            Set<Boolean> local = new HashSet<>();
                            for(Object s1 : value1.getJSONArray("sign")) {
                                for(Object s2 : value2.getJSONArray("sign")) {
                                    local.addAll(fun.apply(Sign.toSign(s1), Sign.toSign(s2)));
                                    if(local.size() > 1) break;
                                }
                                if(local.size() > 1) break;
                            }

                            yield local;
                        }
                };

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
                results.add(state);
            }
            case "ifz" -> {
                String condition = instruction.getString("condition");
                int target = instruction.getInt("target");

                JSONObject value = f.sigma().pop();

                Function<Sign, Set<Boolean>> fun = (s) -> switch(condition) {
                    case "eq" -> Sign.eq(s, ZERO);
                    case "ne" -> Sign.ne(s, ZERO);
                    case "le" -> Sign.le(s, ZERO);
                    case "lt" -> Sign.lt(s, ZERO);
                    case "ge" -> Sign.ge(s, ZERO);
                    case "gt" -> Sign.gt(s, ZERO);
                    default -> throw new IllegalStateException("Unexpected condition: " + condition);
                };

                List<Boolean> ifz_results = new ArrayList<>();
                if(value.has("sign")) {
                    for(Object sign : value.getJSONArray("sign")) {
                        ifz_results.addAll(fun.apply(Sign.toSign(sign)));
                        if(ifz_results.size() > 1) break;
                    }
                } else {
                    // For object equality we assume both cases can be true/false
                    ifz_results = List.of(true, false);
                }

                if(ifz_results.size() > 1) {
                    Triple<Frame, Deque<Frame>, Map<Integer, JSONObject>> t = clone_state(f, psi, mu);

                    Frame _f = t.e1();
                    Deque<Frame> _psi = t.e2();
                    Map<Integer, JSONObject> _mu = t.e3();

                    _psi.push(new Frame(_f.lambda(), _f.sigma(), new Pair<>(_f.iota().e1(), ifz_results.get(1) ? target : _f.iota().e2() + 1)));
                    results.add(new State(_psi, _mu));
                }

                psi.push(new Frame(f.lambda(), f.sigma(), new Pair<>(f.iota().e1(), ifz_results.get(0) ? target : f.iota().e2() + 1)));
                results.add(state);
            }
            case "goto" -> {
                psi.push(new Frame(f.lambda(), f.sigma(), new Pair<>(f.iota().e1(), instruction.getInt("target"))));
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
                JSONObject field = instruction.getJSONObject("field");
                String fieldname = field.getString("name");
                Object fieldtype = field.get("type");

                JSONObject object;
                if(instruction.getBoolean("static")) {
                    object = classes.get(field.getString("class"));
                } else {
                    JSONObject objectref = f.sigma().pop();
                    if(isNull(objectref)) throw new NullPointerException("Cannot get field from object because \"objectref\" is null");

                    object = mu.get(System.identityHashCode(objectref));
                    if(isNull(object)) throw new NullPointerException("Cannot get field from object because \"object\" is null");
                }

                Optional<JSONObject> value = getField(object, fieldname, fieldtype, mu);
                if(value.isEmpty()) throw new NoSuchFieldError("The field \"" + field.getString("name") + "\" does not exist.");

                f.sigma().push(toAbstract(value.get()));
                psi.push(new Frame(f.lambda(), f.sigma(), new Pair<>(f.iota().e1(), f.iota().e2() + 1)));

                results.add(state);
            }
            case "put" -> {
                JSONObject field = instruction.getJSONObject("field");
                String fieldname = field.getString("name");
                Object fieldtype = field.get("type");

                JSONObject value = f.sigma().pop();

                JSONObject object;
                if(instruction.getBoolean("static")) {
                    object = classes.get(field.getString("class"));
                } else {
                    JSONObject objectref = f.sigma().pop();
                    if(isNull(objectref)) throw new NullPointerException("Cannot put field in object because \"objectref\" is null");

                    object = mu.get(System.identityHashCode(objectref));
                    if(isNull(object)) throw new NullPointerException("Cannot put field in object because \"object\" is null");
                }

                if(!putField(object, fieldname, fieldtype, value, mu)) throw new NoSuchFieldError("The field \"" + field.getString("name") + "\" does not exist in " + object.getString("name"));

                psi.push(new Frame(f.lambda(), f.sigma(), new Pair<>(f.iota().e1(), f.iota().e2() + 1)));

                results.add(state);
            }
            case "invoke" -> {
                JSONObject invoke_method = instruction.getJSONObject("method");
                String invoke_access = instruction.getString("access");

                String methodname = invoke_method.getString("name");
                JSONArray args = invoke_method.getJSONArray("args");
                Object returns = invoke_method.isNull("returns") ? null : invoke_method.get("returns");

                Method resolvedMethod = switch(invoke_access) {
                    case "virtual" -> {
                        JSONObject classref = invoke_method.getJSONObject("ref");

                        yield resolveMethod(classes, classref, methodname, args, returns);
                    }
                    case "special", "static" -> {
                        JSONObject classref = invoke_method.getJSONObject("ref");
                        boolean is_interface = invoke_method.getBoolean("is_interface");

                        yield resolveMethod(classes, classref, methodname, args, returns);
                    }
                    case "interface" -> {
                        JSONObject classref = invoke_method.getJSONObject("ref");
                        int stack_size = invoke_method.getInt("stack_size");

                        yield resolveMethod(classes, classref, methodname, args, returns);
                    }
                    case "dynamic" -> throw new RuntimeException("Dynamic invoke is not implemented");
                    default -> throw new IllegalArgumentException("Illegal invoke access: " + instruction.getString("access"));
                };

                if(f.sigma().size() < args.length()) throw new RuntimeException("Not enough elements in stack for invocation of method!");

                if(resolvedMethod.method().isNull("code")) {
                    throw new RuntimeException("Runtime method identification is not implemented");
                    // TODO: Find method in subclass / "Implement class" at runtime
                }

                JSONObject[] lambda = createNullArray(resolvedMethod.method().getJSONObject("code").getInt("max_locals"));
                for(int i = args.length() - 1; i >= 0; i--) {
                    JSONObject arg = f.sigma().pop();

                    /*
                    Object type_expected = args.get(i);
                    Object type_actual = arg.has("kind") ? arg : arg.getString("type");

                    if(!SimpleType.equals(type_expected, type_actual)) {
                        throw new IllegalArgumentException("Type mismatch: Expected " + type_expected + " but was " + type_actual);
                    }*/

                    lambda[i] = arg;
                }

                switch(invoke_access) {
                    case "virtual", "special", "interface" -> {
                        // Shift elements in lambda right
                        System.arraycopy(lambda, 0, lambda, 1, lambda.length - 1);
                        // Objectref
                        lambda[0] = f.sigma().pop();
                    }
                    default -> {}
                }

                psi.push(new Frame(f.lambda(), f.sigma(), new Pair<>(f.iota().e1(), f.iota().e2() + 1)));
                psi.push(new Frame(lambda, new ArrayDeque<>(), new Pair<>(resolvedMethod, 0)));

                results.add(state);
            }
            case "new" -> {
                String classname = instruction.getString("class");

                if(!classes.containsKey(classname)) throw new InstantiationError(classname + " does not exist.");
                List<Object> access = classes.get(classname).getJSONArray("access").toList();
                if(access.contains("abstract")) throw new InstantiationError(classname + " is abstract.");
                if(access.contains("interface")) throw new InstantiationError(classname + " is an interface.");

                JSONObject objectref = new JSONObject(Map.of("kind", "class", "name", classname));
                JSONObject value = initialize(classes, classname, mu);

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
                JSONObject arrayref = f.sigma().pop();
                if(isNull(arrayref)) throw new NullPointerException("Cannot throw because \"arrayref\" is null");

                JSONObject array = mu.get(System.identityHashCode(arrayref));
                int arraylength = array.getJSONArray("value").length();

                JSONObject value = new JSONObject(Map.of("type", "int", "value", arraylength, "sign", Set.of(arraylength > 0 ? POSITIVE : ZERO)));
                f.sigma().push(value);

                psi.push(new Frame(f.lambda(), f.sigma(), new Pair<>(f.iota().e1(), f.iota().e2() + 1)));

                results.add(state);
            }
            case "throw" -> {
                JSONObject objectref = f.sigma().pop();
                if(isNull(objectref)) throw new NullPointerException("Cannot throw because \"objectref\" is null");

                JSONObject exceptionhandler = null;
                while(exceptionhandler == null) {
                    int cl = f.iota().e1().method().getJSONObject("code").getJSONArray("bytecode").length();

                    JSONArray exceptionhandlers = f.iota().e1().method().getJSONArray("exceptions");
                    for(int i = 0; i < exceptionhandlers.length(); i++) {
                        JSONObject eh = exceptionhandlers.getJSONObject(i);

                        Object catchtype = eh.get("catchType");
                        if(!catchtype.equals(objectref.get("name"))) continue;

                        int handler = eh.getInt("handler");
                        if(handler > cl) continue;

                        int start = eh.getInt("start");
                        int end = eh.getInt("end");
                        if(start > cl || end >= cl) continue;

                        exceptionhandler = eh;
                        break;
                    }

                    if(exceptionhandler == null) {
                        if(!psi.isEmpty()) f = psi.pop();
                        else break;
                    }
                }

                if(exceptionhandler != null) {
                    f.sigma().push(objectref);
                    psi.push(new Frame(f.lambda(), f.sigma(), new Pair<>(f.iota().e1(), exceptionhandler.getInt("handler"))));
                    results.add(state);
                } else {
                    // Get Throwable object
                    JSONObject o = mu.get(System.identityHashCode(objectref));
                    while(!(o.has("name") && o.getString("name").equals("java/lang/Throwable"))) {
                        if(mu.containsKey(System.identityHashCode(o))) {
                            o = mu.get(System.identityHashCode(o));
                        } else {
                            o = null;
                            break;
                        }
                    }

                    if(o == null) throw new RuntimeException("\"object\" was not an instance of Throwable");
                    else {
                        System.err.println(objectref.getString("name").replace("/", "."));

                        // Print the stack trace
                        Optional<JSONObject> stacktraceref = getField(o, "stackTrace", new JSONObject(Map.of("kind", "array", "type", new JSONObject(Map.of("kind", "class", "name", "java/lang/StackTraceElement")))), mu);
                        if(stacktraceref.isPresent()) {
                            JSONObject stacktrace = mu.get(System.identityHashCode(stacktraceref.get()));
                            if(!isNull(stacktrace)) {
                                JSONArray array = stacktrace.getJSONArray("value");
                                for(int i = 0; i < array.length(); i++) {
                                    JSONObject stacktraceelementref = array.getJSONObject(i);
                                    JSONObject stacktraceelement = mu.get(System.identityHashCode(stacktraceelementref));
                                    if(stacktraceelement != null) {
                                        System.err.println("\tat " + stacktraceelement);
                                        // TODO: Format output (Extract fields)
                                    }
                                }
                            }
                        }

                        /*
                        // TODO: Add java/util/List
                        // Print suppressed exceptions, if any
                        Optional<JSONObject> suppressedexceptionsref = getField(o, "suppressedExceptions", new JSONObject(Map.of("kind", "class", "name", "java/util/List")), mu);
                        if(suppressedexceptionsref.isPresent()) {
                            JSONObject suppressedexceptions = mu.get(System.identityHashCode(suppressedexceptionsref));
                            if(suppressedexceptions != null) {

                            }
                        }
                        */

                        // Print the cause, if any
                        Optional<JSONObject> causeref = getField(o, "cause", new JSONObject(Map.of("kind", "class", "name", "java/lang/Throwable")), mu);
                        if(causeref.isPresent()) {
                            JSONObject cause = mu.get(System.identityHashCode(causeref.get()));
                            if(!isNull(cause)) {
                                System.err.println("Caused by: " + cause.getString("name").replace("/", "."));
                            }
                        }
                    }
                }
            }
            case "checkcast" -> {
                JSONObject type = instruction.getJSONObject("type");

                JSONObject objectref = f.sigma().peek();

                if(isNull(objectref)) throw new NullPointerException("Could not check cast because \"objectref\" was null");

                if(!isInstanceOf(classes, objectref, type)) throw new ClassCastException(objectref + " cannot be cast to " + type);

                psi.push(new Frame(f.lambda(), f.sigma(), new Pair<>(f.iota().e1(), f.iota().e2() + 1)));
                results.add(state);
            }
            case "instanceof" -> {
                JSONObject type = instruction.getJSONObject("type");

                JSONObject objectref = f.sigma().pop();

                if(isNull(objectref)) throw new NullPointerException("Could not check cast because \"objectref\" was null");

                boolean result = isInstanceOf(classes, objectref, type);

                f.sigma().push(new JSONObject(Map.of("type", "int", "value", result ? 1 : 0)));
                psi.push(new Frame(f.lambda(), f.sigma(), new Pair<>(f.iota().e1(), f.iota().e2() + 1)));
                results.add(state);
            }
            case "return" -> {
                if(instruction.isNull("type")) {
                    if(!state.psi().isEmpty()) results.add(state);
                } else {
                    String type = instruction.getString("type"); // LocalType
                    JSONObject value = f.sigma().pop();

                    JSONObject result = type.equals("ref") ? value : cloneJSONObject(value);

                    if(!psi.isEmpty()) {
                        Frame f2 = psi.peek();
                        f2.sigma().push(result);

                        results.add(state);
                    } else {
                        System.out.println(String.format("%-12s", "return") + Main.toFormattedString(result));
                    }
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
                        f.sigma().push(value.has("kind") ? value : cloneJSONObject(value));
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
                        f.sigma().push(value.has("kind") ? value : cloneJSONObject(value));
                    }
                }

                f.sigma().push(word);

                for(int i = 0; i < words; i++) {
                    for(JSONObject value : local) {
                        f.sigma().push(value.has("kind") ? value : cloneJSONObject(value));
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
                        f.sigma().push(value.has("kind") ? value : cloneJSONObject(value));
                    }
                }

                f.sigma().push(word2);
                f.sigma().push(word1);

                for(int i = 0; i < words; i++) {
                    for(JSONObject value : local) {
                        f.sigma().push(value.has("kind") ? value : cloneJSONObject(value));
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
            default -> throw new UnsupportedOperationException("Unsupported instruction \"" + instruction.getString("opr") + "\"");
        }

        for(State s : results) {
            System.out.println("Ψ" + s.psi());
        }

        return results;
    }
}
