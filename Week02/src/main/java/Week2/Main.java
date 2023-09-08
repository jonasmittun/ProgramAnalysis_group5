package Week2;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.nodeTypes.NodeWithName;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.resolution.SymbolResolver;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import guru.nidi.graphviz.attribute.*;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static guru.nidi.graphviz.attribute.Attributes.attrs;
import static guru.nidi.graphviz.model.Factory.mutGraph;
import static guru.nidi.graphviz.model.Factory.mutNode;

public class Main {
    public static void main(String[] args) {
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(new ReflectionTypeSolver());
        combinedTypeSolver.add(new JavaParserTypeSolver(new File("src\\main\\java")));

        SymbolResolver symbolResolver = new JavaSymbolSolver(combinedTypeSolver);

        // Configure the parser with the symbol solver
        ParserConfiguration parserConfiguration = new ParserConfiguration()
                .setAttributeComments(false)
                .setSymbolResolver(symbolResolver);

        // Set up the parser
        JavaParser parser = new JavaParser(parserConfiguration);

        // List of records
        List<Class> classes = new ArrayList<>();
        for(String content : getFiles("src\\main\\java\\dtu").values()) {
            classes.addAll(parseToRecord(parser, content));
        }

        // Initialize graphviz object
        MutableGraph g = mutGraph("Graph").setDirected(true);

        for(Class i : classes) {
            g = addClass(g, i);
        }

        // Do some magic here to add edges (lmao)

        //g.add(mutNode("Tricky").addLink(mutNode("Other")));

        try {
            Graphviz.fromGraph(g).height(600).render(Format.PNG).toFile(new File("graphs/graph.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

    private record Field(List<String> names, Type type) {
        @Override
        public String toString() {
            return names + " : " + getQualifiedName(type.resolve());
        }
    }

    private record Method(String name, List<Type> parameterTypes, Type returnType) {
        @Override
        public String toString() {
            return name + " : " + parameterTypes.stream().map(t -> getQualifiedName(t.resolve())).toList() + " → " + returnType.asString();
        }
    }

    private record Class(Optional<String> packageName, String name, Optional<ClassOrInterfaceType> inheritance, List<Type> realizations, List<String> compositions, List<Field> fields, List<Method> methods) {
        public String getFullyQualifiedName() {
            return packageName.map(s -> s + "." + name).orElse(name);
        }

        public List<Type> getDependencies() {
            List<Type> dependencies = new ArrayList<>();

            for(Field field : fields) {
                if(!field.type.isPrimitiveType()) dependencies.add(field.type);
            }

            for(Method method : methods) {
                if(!method.returnType.isPrimitiveType()) dependencies.add(method.returnType);

                for(Type parameter : method.parameterTypes) {
                    if(!parameter.isPrimitiveType()) dependencies.add(parameter);
                }
            }

            return dependencies;
        }
    }

    private static List<Class> parseToRecord(JavaParser parser, String content) {
        CompilationUnit cu;
        ParseResult<CompilationUnit> parseResult = parser.parse(content);
        if(!parseResult.isSuccessful() || parseResult.getResult().isEmpty()) return Collections.emptyList();
        else cu = parseResult.getResult().get();

        Optional<String> packageName = cu.getPackageDeclaration().map(NodeWithName::getNameAsString);

        return cu.findAll(ClassOrInterfaceDeclaration.class).stream().map(declaration -> {
            // Class Name
            String className = declaration.getNameAsString();

            // Class Extension
            Optional<ClassOrInterfaceType> inheritance = declaration.getExtendedTypes().stream().findFirst();

            // Class Implementations
            List<Type> realizations = declaration.getImplementedTypes().stream().map(Type.class::cast).toList();

            // Class compositions
            List<String> compositions = declaration.getMembers().stream().filter(member -> member.isClassOrInterfaceDeclaration() && !member.asClassOrInterfaceDeclaration().isStatic()).map(member -> packageName.map(s -> s + "." + member.asClassOrInterfaceDeclaration().getNameAsString()).orElseGet(() -> member.asClassOrInterfaceDeclaration().getNameAsString())).toList();

            // Extract Fields
            List<Field> fields = declaration.getFields().stream().map(field -> new Field(field.getVariables().stream().map(NodeWithSimpleName::getNameAsString).toList(), field.getElementType())).toList();

            // Extract Methods
            List<Method> methods = declaration.getMethods().stream().map(method -> new Method(method.getNameAsString(), method.getParameters().stream().map(Parameter::getType).toList(), method.getType())).toList();

            return new Class(packageName, className, inheritance, realizations, compositions, fields, methods);
        }).toList();
    }

    private static String getQualifiedName(ResolvedType type) {
        String name = "?";

        if(type.isReferenceType()) {
            name = type.asReferenceType().getQualifiedName();
        } else if(type.isTypeVariable()) {
            name = type.asTypeVariable().qualifiedName();
        } else if(type.isArray()) {
            name = type.asArrayType().describe();
        } else if(type.isPrimitive()) {
            name = type.asPrimitive().getBoxTypeQName();
        }

        return name;
    }

    private static MutableGraph addClass(MutableGraph graph, Class cls) {
        MutableNode temp = mutNode(cls.name()).add(attrs(Shape.NONE));
        temp.attrs().add(Label.html(
            "<table border=\"0\" cellborder=\"1\" cellspacing=\"0\" cellpadding=\"4\">\n" +
            "\t\t\t<tr> <td> <b>" + cls.name + "</b> </td> </tr>\n" +
            "\t\t\t<tr> <td>\n" +
            "\t\t\t\t<table border=\"0\" cellborder=\"0\" cellspacing=\"0\" >\n" +
            String.join("", cls.fields.stream().map(field -> "\t\t\t\t\t<tr> <td port=\"ss2\" align=\"left\" >- " + field.replaceAll("<", "").replaceAll(">", "") + "</td> </tr>\n").toList()) +
            "\t\t\t\t\t<tr> <td port=\"ss2\" align=\"left\" ></td> </tr>\n" +
            "\t\t\t\t</table>\n" +
            "\t\t\t</td> </tr>\n" +
            "\t\t\t<tr> <td>\n" +
            "\t\t\t\t<table border=\"0\" cellborder=\"0\" cellspacing=\"0\" >\n" +
            String.join("", cls.methods.stream().map(method -> "\t\t\t\t\t<tr> <td align=\"left\" >+ " + method.replaceAll("<", "").replaceAll(">", "") + "</td> </tr>\n").toList()) +
            "\t\t\t\t</table>\n" +
            "\t\t\t</td> </tr>\n" +
            "\t\t</table>"));
        graph.add(temp);
        return graph;
    }
}