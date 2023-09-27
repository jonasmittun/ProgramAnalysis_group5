package Week02;

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
import guru.nidi.graphviz.attribute.Arrow;
import guru.nidi.graphviz.attribute.Label;
import guru.nidi.graphviz.attribute.Shape;
import guru.nidi.graphviz.attribute.Style;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Link;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static Week01.Main.getFiles;
import static guru.nidi.graphviz.attribute.Attributes.attrs;
import static guru.nidi.graphviz.model.Factory.mutGraph;
import static guru.nidi.graphviz.model.Factory.mutNode;

public class Main {
    public static void main(String[] args) {
        String path = "src\\main\\resources";

        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(new ReflectionTypeSolver());
        combinedTypeSolver.add(new JavaParserTypeSolver(new File(path)));

        SymbolResolver symbolResolver = new JavaSymbolSolver(combinedTypeSolver);

        // Configure the parser with the symbol solver
        ParserConfiguration parserConfiguration = new ParserConfiguration()
                .setAttributeComments(false)
                .setSymbolResolver(symbolResolver);

        // Set up the parser
        JavaParser parser = new JavaParser(parserConfiguration);

        // List of records
        List<Class> classes = new ArrayList<>();
        for(String content : getFiles(path + "\\dtu").values()) {
            classes.addAll(parseToRecord(parser, content));
        }

        // Initialize graphviz object
        MutableGraph g = mutGraph("Graph").setDirected(true);

        for(Class i : classes) {
            g = addClass(g, i);
        }

        // Set of node names (Nodes we want to draw to)
        Set<String> nodes = g.nodes().stream().map(node -> node.name().toString()).collect(Collectors.toSet());

        // Map to keep track of links
        Map<String, Set<String>> links = new HashMap<>();

        for(Class cls : classes) {
            g = drawArrows(g, nodes, links, cls, false, true);
        }

        try {
            Graphviz.fromGraph(g).height(600).render(Format.PNG).toFile(new File("graphs/graph.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
        MutableNode temp = mutNode(cls.getFullyQualifiedName()).add(attrs(Shape.NONE));
        temp.attrs().add(Label.html(
            "<table border=\"0\" cellborder=\"1\" cellspacing=\"0\" cellpadding=\"4\">\n" +
            "\t\t\t<tr> <td> <b>" + cls.name() + "</b> </td> </tr>\n" +
            "\t\t\t<tr> <td>\n" +
            "\t\t\t\t<table border=\"0\" cellborder=\"0\" cellspacing=\"0\" >\n" +
            String.join("", cls.fields.stream().map(field -> "\t\t\t\t\t<tr> <td port=\"ss2\" align=\"left\" >- " + field.toString().replaceAll("<", "&lt;").replaceAll(">", "&gt;") + "</td> </tr>\n").toList()) +
            "\t\t\t\t\t<tr> <td port=\"ss2\" align=\"left\" ></td> </tr>\n" +
            "\t\t\t\t</table>\n" +
            "\t\t\t</td> </tr>\n" +
            "\t\t\t<tr> <td>\n" +
            "\t\t\t\t<table border=\"0\" cellborder=\"0\" cellspacing=\"0\" >\n" +
            String.join("", cls.methods.stream().map(method -> "\t\t\t\t\t<tr> <td align=\"left\" >+ " + method.toString().replaceAll("<", "&lt;").replaceAll(">", "&gt;") + "</td> </tr>\n").toList()) +
            "\t\t\t\t</table>\n" +
            "\t\t\t</td> </tr>\n" +
            "\t\t</table>"));
        graph.add(temp);
        return graph;
    }

    /**
     * Returns a MutableGraph where links (arrows) have been drawn according to the content of the class record.
     * @param nodes             Arrows will only be drawn to nodes that exist in this set unless include_external is set to true.
     * @param links             If single_link is set to true, a link will only be drawn if the link doesn't exist in the map.
     * @param include_external  Toggles whether to include edges for external classes.
     * @param single_link       Toggles if multiple links between two nodes are allowed or not.
     */
    private static MutableGraph drawArrows(MutableGraph graph, Set<String> nodes, Map<String, Set<String>> links, Class cls, boolean include_external, boolean single_link) {
        String _FQN = cls.getFullyQualifiedName();

        // Inheritance
        cls.inheritance.ifPresent(child -> {
            String FQN_ = getQualifiedName(child.resolve());

            Set<String> set = links.computeIfAbsent(_FQN, k -> new HashSet<>());

            if(!single_link || set.add(FQN_)) {
                Link linkTarget = mutNode(FQN_).linkTo();
                linkTarget.add(Arrow.EMPTY);
                MutableNode link = mutNode(_FQN).addLink(linkTarget);

                graph.add(link);
            }
        });

        // Realization
        for(Type type : cls.realizations) {
            String FQN_ = getQualifiedName(type.resolve());

            Set<String> set = links.computeIfAbsent(_FQN, k -> new HashSet<>());

            if(include_external || nodes.contains(FQN_)) {
                if(!single_link || set.add(FQN_)) {
                    Link linkTarget = mutNode(FQN_).linkTo();
                    linkTarget.add(Style.DASHED);
                    MutableNode link = mutNode(_FQN).addLink(linkTarget);

                    graph.add(link);
                }
            }
        }

        // Aggregation
        for(Field field : cls.fields) {
            String FQN_ = getQualifiedName(field.type.resolve());

            Set<String> set = links.computeIfAbsent(_FQN, k -> new HashSet<>());

            if(include_external || nodes.contains(FQN_)) {
                if(!single_link || set.add(FQN_)) {
                    Link linkTarget = mutNode(FQN_).linkTo();
                    linkTarget.add(Arrow.EDIAMOND);
                    MutableNode link = mutNode(_FQN).addLink(linkTarget);

                    graph.add(link);
                }
            }
        }

        // Composition
        for(String FQN_ : cls.compositions) {
            Set<String> set = links.computeIfAbsent(_FQN, k -> new HashSet<>());

            if(!single_link || set.add(FQN_)) {
                Link linkTarget = mutNode(FQN_).linkTo();
                linkTarget.add(Arrow.DIAMOND);
                MutableNode link = mutNode(_FQN).addLink(linkTarget);

                graph.add(link);
            }
        }

        // Dependencies
        for(Type type : cls.getDependencies()) {
            if(type.isVoidType()) continue;

            String FQN_ = getQualifiedName(type.resolve());

            Set<String> set = links.computeIfAbsent(_FQN, k -> new HashSet<>());

            if(include_external || nodes.contains(FQN_)) {
                if(!single_link || set.add(FQN_)) {
                    Link linkTarget = mutNode(FQN_).linkTo();
                    linkTarget.add(Style.DASHED);
                    linkTarget.add(Arrow.VEE);
                    MutableNode link = mutNode(_FQN).addLink(linkTarget);

                    graph.add(link);
                }
            }
        }

        return graph;
    }
}