import guru.nidi.graphviz.attribute.Color;
import guru.nidi.graphviz.attribute.Font;
import guru.nidi.graphviz.attribute.Rank;
import guru.nidi.graphviz.attribute.Style;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Graph;
import guru.nidi.graphviz.model.MutableGraph;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static guru.nidi.graphviz.attribute.Attributes.attr;
import static guru.nidi.graphviz.attribute.Rank.RankDir.LEFT_TO_RIGHT;
import static guru.nidi.graphviz.model.Factory.*;
import static guru.nidi.graphviz.model.Link.to;


public class Main {
    public static void main(String[] args) {
        // Initialize graphviz object
        MutableGraph g = mutGraph("Graph").setDirected(true);

        // Our files
        Map<String, String> map = getFiles("src\\main\\java\\dtu");
        map.replaceAll((k, v) -> removeComments(v));

        Map<String, List<String>> graph = new HashMap<>();

        // Add nodes to graph object
        for(String fileContent : map.values()) {
            var name = getPackagePlusClassName(fileContent);

            // Add edges to graph object
            List<String> edges = new ArrayList<>();
            edges.addAll(getExplicitImports(fileContent));
            edges.addAll(getImplicitImports(fileContent));

            graph.put(name, edges);
        }

        // Draw graph here
        for(String nodes : graph.keySet()) {
            g = addNode(g, nodes);
        }

        for(Map.Entry<String, List<String>> entry : graph.entrySet()) {
            String from = entry.getKey();
            for(String to : entry.getValue()) {
                g = addEdges(g, from, to);
            }
        }

        System.out.println(map.keySet());

        // Generate and output the graph as a .png
        try {
            //Graphviz.fromGraph(g).height(400).render(Format.PNG).toFile(new File("graphs/graph.png"));
            Graphviz.fromGraph(g).height(600).render(Format.PNG).toFile(new File("graphs/graph.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /** Returns a string where any single- or multiline comments have been removed */
    public static String removeComments(String content) {
        content = content.replaceAll("//.*", "");               // Remove single line comment
        return content.replaceAll("/\\*[\\s\\S]*?\\*/", "");    // Remove multi line comment
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

    /** Extracts the package name from a file content and adds class name to the end
     * @param FileContent File which package and class name's will be extracted
     * @return Package + Class name inside the file
     */
    private static String getPackagePlusClassName(String FileContent) {
        Pattern packageName = Pattern.compile("(?<=package ).*\\w");
        Pattern className = Pattern.compile("(?<=public class )\\w+");
        Matcher matcher = packageName.matcher(FileContent);
        Matcher matcher2 = className.matcher(FileContent);

        String packageName_ = "";
        if(matcher.find()) packageName_ = FileContent.substring(matcher.start(), matcher.end());

        String className_ = "";
        if(matcher2.find()) className_ = FileContent.substring(matcher2.start(), matcher2.end());

        return packageName_ + '.' + className_;
    }

    private static List<String> getExplicitImports(String input) {
        List<String> imports = new ArrayList<>();

        Pattern pattern = Pattern.compile("(?<=import )(?>[a-zA-Z]\\w+\\.)+([a-zA-Z]\\w+|\\*)");
        Matcher matcher = pattern.matcher(input);
        while(matcher.find()) {
            String match = input.substring(matcher.start(), matcher.end());
            if(!match.startsWith("java")) imports.add(match);
            if (match.endsWith("*")) {
                imports.remove(match);
                // Get path
                var path = match.replace('.', '\\');
                path = path.substring(0, path.length()-2);
                // Get files with content
                Map<String, String> fileWithContent = getFiles("src\\main\\java\\" + path);
                // Add the results
                for (String fileName : fileWithContent.values()) {
                    imports.add(getPackagePlusClassName(fileName));
                }
            }
        }

        return imports;
    }

    private static List<String> getImplicitImports(String input) {
        List<String> imports = new ArrayList<>();

        Pattern pattern_functions = Pattern.compile("^(?>\\s*)(\\w+\\.)+\\w+(?=\\.\\w+\\(\\))", Pattern.MULTILINE);
        Pattern pattern_classes = Pattern.compile("^(?>\\s*)(\\w+\\.)+\\w+(?=\\s)", Pattern.MULTILINE);
        Matcher matcher;

        matcher = pattern_functions.matcher(input);
        while(matcher.find()) {
            String match = input.substring(matcher.start(), matcher.end()).trim();
            System.out.println(match);
            if(!match.startsWith("System")) imports.add(match);
        }

        matcher = pattern_classes.matcher(input);
        while(matcher.find()) {
            String match = input.substring(matcher.start(), matcher.end()).trim();
            System.out.println(match);
            if(!match.startsWith("System")) imports.add(match);
        }

        return imports;
    }

    private static MutableGraph addNode(MutableGraph graph, String nodeName) {
        graph.add(mutNode(nodeName));
        return graph;
    }

    private static MutableGraph addEdges (MutableGraph graph, String from, String to) {
        graph.add(mutNode(from).addLink(mutNode(to)));
        return graph;
    }
}