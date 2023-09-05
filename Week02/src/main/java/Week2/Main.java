package Week2;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.resolution.SymbolResolver;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import java.io.File;
import java.io.FileNotFoundException;

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

        try {
            ParseResult<CompilationUnit> result = parser.parse(new File("src\\main\\java\\dtu\\deps\\tricky\\Tricky.java"));

            if(!result.isSuccessful() || result.getResult().isEmpty()) return;

            CompilationUnit cu = result.getResult().get();

            cu.findAll(ClassOrInterfaceDeclaration.class).forEach(declaration -> {
                // Class Name
                System.out.println(declaration.getNameAsString());

                // Extract Fields
                declaration.getFields().forEach(field -> {
                    ResolvedType type = field.getElementType().resolve();

                    String name = getQualifiedName(type);

                    System.out.println(field.getVariables() + " : " + name);
                });

                // Extract Methods
                declaration.getMethods().forEach(method -> {
                    System.out.println(method.getNameAsString() + " : " + method.getParameters().stream().map(p -> {
                        ResolvedType type = p.getType().resolve();

                        return getQualifiedName(type);
                    }).toList() + " -> " + method.getTypeAsString());
                });
            });
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
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