package Week3;

import org.json.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import guru.nidi.graphviz.attribute.*;
import guru.nidi.graphviz.model.Link;
import guru.nidi.graphviz.model.MutableGraph;

import static guru.nidi.graphviz.attribute.Attributes.attrs;
import static guru.nidi.graphviz.model.Factory.mutGraph;
import static guru.nidi.graphviz.model.Factory.mutNode;

public class Main {
    private static final boolean LOGGING = false;
    private static final int height = 1200;
    private static final int width = 1800;

    public static void main(String[] args) {
        String path = "src\\main\\java\\com\\mojang";

        Map<String, JSONObject> map = getFiles(path).entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> new JSONObject(entry.getValue())));

        List<Class> classes = new ArrayList<>();
        for(JSONObject object : map.values()) {
            classes.add(process(object));
        }

        // Initialize graphviz object
        MutableGraph g = mutGraph("Graph").setDirected(true);

        g = addClasses(g, classes);

        g = addConnections(g, classes);

        try {
            //Graphviz.fromGraph(g).height(height).width(width).render(Format.SVG).toFile(new File("graphs/graph.svg"));
            Graphviz.fromGraph(g).render(Format.SVG).toFile(new File("graphs/graph.svg"));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static MutableGraph addClasses(MutableGraph g, List<Class> classes) {
        for(Class cls : classes) {
            MutableNode temp = mutNode(cls.name).add(attrs(Shape.NONE));
            
            String labelString = "";
            
            labelString +=
            "<table border=\"0\" cellborder=\"1\" cellspacing=\"0\" cellpadding=\"4\">\n" +
            "\t\t\t<tr> <td> <b>" + cls.getName() + "</b> </td> </tr>\n" +
            "\t\t\t<tr> <td>\n" +
            "\t\t\t\t<table border=\"0\" cellborder=\"0\" cellspacing=\"0\" >\n";

            labelString += String.join("", cls.fields.stream().map(field -> "\t\t\t\t\t<tr> <td port=\"ss2\" align=\"left\" >- " + field.toString().replaceAll("<", "&lt;").replaceAll(">", "&gt;") + "</td> </tr>\n").toList());

            labelString +=
            "\t\t\t\t\t<tr> <td port=\"ss2\" align=\"left\" ></td> </tr>\n" +
            "\t\t\t\t</table>\n" +
            "\t\t\t</td> </tr>\n" +
            "\t\t\t<tr> <td>\n" +
            "\t\t\t\t<table border=\"0\" cellborder=\"0\" cellspacing=\"0\" >\n";

            labelString += String.join("", cls.methods.stream().map(method -> "\t\t\t\t\t<tr> <td align=\"left\" >+ " + method.toString().replaceAll("<", "&lt;").replaceAll(">", "&gt;") + "</td> </tr>\n").toList());

            labelString +=
            "\t\t\t\t</table>\n" +
            "\t\t\t</td> </tr>\n" +
            "\t\t</table>";

            temp.attrs().add(Label.html(labelString));
            g.add(temp);
        }

        return g;
    }

    private static MutableGraph addConnections(MutableGraph g, List<Class> classes) {
        for(Class cls: classes) {
            // Inheritance
            if (!cls.extension.isEmpty()) {
                Link linkTarget = mutNode(cls.extension).linkTo();
                linkTarget.add(Arrow.EMPTY);
                MutableNode link = mutNode(cls.name).addLink(linkTarget);

                g.add(link);
            }

            // Realization
            for (String implementedInterface : cls.interfaces) {
                Link linkTarget = mutNode(implementedInterface).linkTo();
                linkTarget.add(Style.DASHED);
                MutableNode link = mutNode(cls.name).addLink(linkTarget);

                g.add(link);
            }

            // Aggregation
            for(Field field : cls.fields) {
                Link linkTarget = mutNode(field.type).linkTo();
                linkTarget.add(Arrow.EDIAMOND);

                MutableNode link = mutNode(cls.name).addLink(linkTarget);

                g.add(link);
            }

            // Composition
            for(String composition : cls.compositions) {
                Link linkTarget = mutNode(composition).linkTo();
                linkTarget.add(Arrow.DIAMOND);
                MutableNode link = mutNode(cls.name).addLink(linkTarget);

                g.add(link);
            }

            // Dependencies
            for(String dependency : cls.getDependencies()) {
                Link linkTarget = mutNode(dependency).linkTo();
                linkTarget.add(Style.DASHED);
                linkTarget.add(Arrow.VEE);

                MutableNode link = mutNode(cls.name).addLink(linkTarget);

                g.add(link);
            }
        }
        return g;
    }

    /** Returns a map with every file in any directory and subdirectory of path together with its content as a string
     * @return  A Map<String, String> where the key is the filename and the value is the content as a string
     */
    public static Map<String, String> getFiles(String path) {
        Map<String, String> map = new HashMap<>();

        Queue<File> files = new LinkedList<>();
        files.add(new File(path));

        while(!files.isEmpty()) {
            File file = files.poll();

            if(file.isFile()) {
                try {
                    map.put(file.getName(), Files.readString(Path.of(file.getAbsolutePath())));
                } catch(IOException ignore) {}
            } else {
                File[] content = file.listFiles();
                if(content != null) {
                    Collections.addAll(files, content);
                }
            }
        }

        return map;
    }

    private record Field(List<String> access, String name, String type) {
        @Override
        public String toString() {
            return String.join(" ", access) + " " + name + " : " + type;
        }
    }

    private record Method(List<String> access, String name, List<String> parameters, String returnType) {
        @Override
        public String toString() {
            return String.join(" ", access) + " " + name + " : " + parameters + " → " + returnType;
        }
    }

    private record Class(List<String> access, String name, String extension, List<String> interfaces, List<String> compositions, List<Field> fields, List<Method> methods) {
        public String getName() {
            int index = name.lastIndexOf("/");
            return index != -1 ? name.substring(index + 1) : name;
        }

        public Set<String> getDependencies() {
            Set<String> dependencies = new HashSet<>();

            dependencies.addAll(fields.stream().map(field -> field.type).toList());

            for(Method method : methods) {
                dependencies.addAll(method.parameters);
                dependencies.add(method.returnType);
            }

            return dependencies;
        }
    }

    private static Class process(JSONObject object) {
        JSONArray v_access = object.getJSONArray("access");
        List<String> access = IntStream.range(0, v_access.length()).mapToObj(i -> (String) v_access.get(i)).toList();
        if(LOGGING) System.out.println("Access" + access);

        String name = object.getString("name");
        if(LOGGING) System.out.println(name);

        JSONObject extension = object.getJSONObject("super");
        String extensionName = (String) extension.get("name");
        if(LOGGING) System.out.println("Extension" + extensionName);

        JSONArray v_interfaces = object.getJSONArray("interfaces");
        List<String> interfaces = new ArrayList<>();
        for(int i = 0; i < v_interfaces.length(); i++) {
            JSONObject o = v_interfaces.getJSONObject(i);

            interfaces.add(o.getString("name"));
        }
        if(LOGGING) System.out.println("Implements " + interfaces);

        JSONArray v_compositions = object.getJSONArray("innerclasses");
        List<String> compositions = new ArrayList<>();
        for(int i = 0; i < v_compositions.length(); i++) {
            JSONObject o = v_compositions.getJSONObject(i);

            if(!o.isNull("name")) compositions.add(o.getString("name"));
        }
        if(LOGGING) System.out.println("Compositions " + compositions);

        JSONArray v_fields = object.getJSONArray("fields");
        List<Field> fields = new ArrayList<>();
        for(int i = 0; i < v_fields.length(); i++) {
            JSONObject o = v_fields.getJSONObject(i);

            JSONArray v_fieldAccess = o.getJSONArray("access");
            List<String> fieldAccess = IntStream.range(0, v_fieldAccess.length()).mapToObj(j -> (String) v_fieldAccess.get(j)).toList();

            String fieldName = o.getString("name");

            JSONObject v_fieldType = o.getJSONObject("type");
            String fieldType = getType(v_fieldType);

            fields.add(new Field(fieldAccess, fieldName, fieldType));
        }
        if(LOGGING) System.out.println("Fields " + fields);

        JSONArray v_methods = object.getJSONArray("methods");
        List<Method> methods = new ArrayList<>();
        for(int i = 0; i < v_methods.length(); i++) {
            JSONObject o_o = v_methods.getJSONObject(i);

            JSONArray v_methodAccess = o_o.getJSONArray("access");
            List<String> methodAccess = IntStream.range(0, v_methodAccess.length()).mapToObj(j -> (String) v_methodAccess.get(j)).toList();

            String methodName = o_o.getString("name");

            JSONArray v_methodParams = o_o.getJSONArray("params");
            List<String> methodParams = new ArrayList<>();
            for(int j = 0; j < v_methodParams.length(); j++) {
                JSONObject o_i = v_methodParams.getJSONObject(j);

                JSONObject v_methodType = o_i.getJSONObject("type");
                String methodType = getType(v_methodType);

                methodParams.add(methodType);
            }

            JSONObject v_methodReturnType = o_o.getJSONObject("returns");
            String methodReturnType = !v_methodReturnType.isNull("type") ? getType(v_methodReturnType.getJSONObject("type")) : "void";

            methods.add(new Method(methodAccess, methodName, methodParams, methodReturnType));
        }
        if(LOGGING) System.out.println("Methods " + methods + "\n");

        return new Class(access, name, extensionName, interfaces, compositions, fields, methods);
    }

    private static String getType(JSONObject type) {
        if(type.has("kind") && type.getString("kind").equals("array")) {
            return getType(type.getJSONObject("type")) + "[]";
        } else if(type.has("name")) {
            return type.getString("name");
        } else {
            return type.getString("base");
        }
    }
}
