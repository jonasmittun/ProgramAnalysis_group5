import guru.nidi.graphviz.attribute.Color;
import guru.nidi.graphviz.attribute.Font;
import guru.nidi.graphviz.attribute.Rank;
import guru.nidi.graphviz.attribute.Style;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Graph;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static guru.nidi.graphviz.attribute.Attributes.attr;
import static guru.nidi.graphviz.attribute.Rank.RankDir.LEFT_TO_RIGHT;
import static guru.nidi.graphviz.model.Factory.graph;
import static guru.nidi.graphviz.model.Factory.node;
import static guru.nidi.graphviz.model.Link.to;


public class Main {
    public static void main(String[] args) {
        Graph g = graph("example1").directed()
                .graphAttr().with(Rank.dir(LEFT_TO_RIGHT))
                .nodeAttr().with(Font.name("Arial"))
                .linkAttr().with("class", "link-class")
                .with(
                        node("a").with(Color.RED).link(node("b")),
                        node("b").link(
                                to(node("c")).with(attr("weight", 5), Style.DASHED)
                        )
                );
        try {
            Graphviz.fromGraph(g).height(100).render(Format.PNG).toFile(new File("example/ex1.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Our files
        Map<String, String> map = getFiles(new HashMap<>(), new File("").getAbsolutePath() + "\\src\\main\\java\\dtu");

        System.out.println(map.get("Tricky.java"));


        //^import ((?>[a-zA-Z][0-9a-zA-Z]+\.)+([a-zA-Z][0-9a-zA-Z]+|\*));$
        Pattern imports = Pattern.compile("import", Pattern.MULTILINE);

        Matcher matcher = imports.matcher(map.get("Tricky.java"));
        System.out.println(matcher.find());
        System.out.println(matcher.matches());


        //Pattern p1 = Pattern.compile("^(?>\s*)((?>[a-zA-Z][0-9a-zA-Z]+\.)+[a-zA-Z][0-9a-zA-Z]+)");
    }

    /** Returns a map with every file in any directory and subdirectory of path together with its content as a string
     * @return  A Map<String, String> where the key is the filename and the value is the content as a string
     */
    public static Map<String, String> getFiles(Map<String, String> map, String path) {
        File directory = new File(path);

        System.out.println(path);

        File[] paths = directory.listFiles();
        if(paths == null) return null;

        for(File file : paths) {
            if(file.isFile()) {
                try {
                    map.put(file.getName(), Files.readString(Path.of(file.getAbsolutePath())));
                } catch(IOException ignore) {}
            } else {
                Map<String, String> submap = getFiles(map, file.getAbsolutePath());
                if(submap != null) {
                    map.putAll(submap);
                }
            }
        }


        return map;
    }

    /** Extracts the package name from a file content
     * @param FileContent File which package name's will be extracted
     * @return Package Name inside the file
     */
    private String getPackageName(String FileContent) {
        var packageName = Pattern.compile("/(?<=package ).*\\w");
        return packageName.toString();
    }
}