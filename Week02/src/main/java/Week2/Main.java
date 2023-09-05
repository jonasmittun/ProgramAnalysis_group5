package Week2;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.resolution.SymbolResolver;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import guru.nidi.graphviz.attribute.Label;
import guru.nidi.graphviz.attribute.Records;
import guru.nidi.graphviz.attribute.Shape;
import guru.nidi.graphviz.attribute.Style;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.io.IOException;

import static guru.nidi.graphviz.attribute.Attributes.attrs;
import static guru.nidi.graphviz.attribute.Records.rec;
import static guru.nidi.graphviz.attribute.Records.turn;
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
            Optional<Class> c = parseToRecord(parser, content);
            c.ifPresent(classes::add);
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

    private record Class(String name, List<String> fields, List<String> methods) {}

    private static Optional<Class> parseToRecord(JavaParser parser, String content) {
        CompilationUnit cu;
        ParseResult<CompilationUnit> parseResult = parser.parse(content);
        if(!parseResult.isSuccessful() || parseResult.getResult().isEmpty()) return Optional.empty();
        else cu = parseResult.getResult().get();

        Optional<ClassOrInterfaceDeclaration> result = cu.findFirst(ClassOrInterfaceDeclaration.class);
        if(result.isEmpty()) return Optional.empty();

        ClassOrInterfaceDeclaration declaration = result.get();

        // Class Name
        String classname = declaration.getNameAsString();

        // Extract Fields
        List<String> fields = declaration.getFields().stream().map(field -> {
            ResolvedType type = field.getElementType().resolve();

            return field.getVariables().stream().map(VariableDeclarator::getName).toList() + " : " + getQualifiedName(type);
        }).toList();

        // Extract Methods
        List<String> methods = declaration.getMethods().stream().map(method -> (method.getNameAsString() + " : " + method.getParameters().stream().map(p -> {
            ResolvedType type = p.getType().resolve();

            return getQualifiedName(type);
        }).toList() + " → " + method.getTypeAsString())).toList();

        return Optional.of(new Class(classname, fields, methods));
    }

    private static String getQualifiedName(ResolvedType type) {
        String name = null;

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
}