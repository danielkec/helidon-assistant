package io.helidon.assistant.rag;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

import io.helidon.common.features.api.HelidonFlavor;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Attributes;
import org.asciidoctor.Options;
import org.asciidoctor.SafeMode;
import org.asciidoctor.ast.ListItem;
import org.asciidoctor.ast.Section;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.ast.Table;

public class AsciiDocPreprocessor {

    private final Asciidoctor asciidoctor;

    public AsciiDocPreprocessor() {
        this.asciidoctor = Asciidoctor.Factory.create();
    }

    public List<Chunk> extractChunks(File adocFile, Path rootDir, HelidonFlavor flavor) {
        var flavorLc = flavor.name().toLowerCase();
        var flavorUc = flavor.name().toUpperCase();
        var rootDirPath = rootDir.toAbsolutePath();
        var options = Options.builder()
                .baseDir(adocFile.getParentFile()) // enables include:: to resolve
                .attributes(Attributes.builder()
                                    .attribute("sourcedir", rootDir.getParent()
                                            .resolve(Path.of("java", "io", "helidon", "docs")).toAbsolutePath().toString())
                                    .attribute("flavor-lc", flavorLc)
                                    .attribute("flavor-uc", flavorUc)
                                    .attribute("health-page",
                                               String.format("%s/%s/health.adoc", rootDirPath, flavorLc))
                                    .attribute("metrics-page",
                                               String.format("%s/%s/metrics/metrics.adoc", rootDirPath, flavorLc))
                                    .attribute("openapi-page",
                                               String.format("%s/%s/openapi/openapi.adoc", rootDirPath, flavorLc))
                                    .attribute("tracing-page",
                                               String.format("%s/%s/tracing.adoc", rootDirPath, flavorLc))
                                    .build())
                .safe(SafeMode.UNSAFE)             // allows full access (use cautiously)
                .build();

        var doc = asciidoctor.loadFile(adocFile, options);
        var chunks = new ArrayList<Chunk>();
        walk(doc, chunks, new ArrayDeque<>());
        return chunks;
    }

    private void walk(StructuralNode node, List<Chunk> chunks, Deque<String> sectionStack) {
        if (node instanceof Section) {
            sectionStack.push(((Section) node).getTitle());
        }

        var context = node.getContext();
        var content = node.getContent();
        var sectionPath = joinSection(sectionStack);

        switch (context) {
            case "paragraph":
                chunks.add(new Chunk(strip(content.toString()), Chunk.Type.PARAGRAPH, sectionPath));
                break;
            case "listing":
                chunks.add(new Chunk("// code:\n" + content, Chunk.Type.CODE, sectionPath));
                break;
            case "table":
                chunks.add(new Chunk(tableToText((Table) node), Chunk.Type.TABLE, sectionPath));
                break;
            case "ulist":
            case "olist":
                chunks.add(new Chunk(listToText(node), Chunk.Type.LIST, sectionPath));
                break;
        }

        for (var child : node.getBlocks()) {
            walk(child, chunks, sectionStack);
        }

        if (node instanceof Section) {
            sectionStack.pop();
        }
    }

    private String joinSection(Deque<String> sections) {
        var list = new ArrayList<String>(sections);
        Collections.reverse(list);
        return list.isEmpty() ? "" : String.join(" > ", list);
    }

    private String strip(String text) {
        return text.replaceAll("\\*|_+|\\[.+?\\]|`+", "").trim();
    }

    private String tableToText(Table table) {
        var sb = new StringBuilder();
        for (var row : table.getBody()) {
            var cols = new ArrayList<String>();
            for (var cell : row.getCells()) {
                cols.add(cell.getText());
            }
            sb.append(String.join(" | ", cols)).append("\n");
        }
        return sb.toString().trim();
    }

    private String listToText(StructuralNode listNode) {
        var sb = new StringBuilder();

        var list = (org.asciidoctor.ast.List) listNode;
        for (var item: list.getItems()) {
            var listItem = (ListItem) item;
            sb.append("- ").append(strip(listItem.getText())).append("\n");
        }
        return sb.toString().trim();
    }
}
