package com.example.filereader.controller;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Exposes the latest files produced by the analysis tools.  Frontend pages must
 * read results through this controller instead of importing snapshots from
 * src/public, otherwise every analysis run would require rebuilding the UI.
 */
@RestController
@RequestMapping("/api/results")
public class AnalysisResultController {

    private static final Set<String> GAP_TYPES = Set.of("AWD", "CD", "CH", "DC", "FE", "MH", "SS");
    private static final Set<String> INTRUSIVE_FILES = Set.of(
            "class_access_modify_entities.csv", "method_access_modify_entities.csv",
            "variable_access_modify_entities.csv", "inner_extensive_class_entities.csv",
            "class_var_extensive_entities.csv", "class_var_remove_entities.csv",
            "class_annotation_modify_entities.csv", "method_annotation_modify_entities.csv",
            "variable_annotation_modify_entities.csv", "parent_interface_modify_entities.csv",
            "parent_class_modify_entities.csv", "inner_extensive_interface_entities.csv",
            "inner_remove_interface_entities.csv", "method_call_extensive_entities.csv",
            "method_call_remove_entities.csv", "param_add_entities.csv",
            "param_modify_entities.csv", "param_remove_entities.csv",
            "class_var_modify_entities.csv", "method_var_modify_entities.csv"
    );

    @GetMapping("/paths")
    public Map<String, String> getAnalysisPaths() {
        Path tools = toolsDir();
        Map<String, String> paths = new LinkedHashMap<>();
        paths.put("upstreamFilePath", tools.resolve("UpFiles").toString());
        paths.put("upstreamFileEnrePath", tools.resolve("UpFiles-enre-out/UpFiles-out.json").toString());
        paths.put("downstreamFilePath", tools.resolve("DownFiles").toString());
        paths.put("downstreamFileEnrePath", tools.resolve("DownFiles-enre-out/DownFiles-out.json").toString());
        return paths;
    }

    @GetMapping("/enre/{side}")
    public ResponseEntity<Resource> getEnreResult(@PathVariable String side) {
        String fileName;
        if ("upstream".equalsIgnoreCase(side)) {
            fileName = "UpFiles-enre-out/UpFiles-out.json";
        } else if ("downstream".equalsIgnoreCase(side)) {
            fileName = "DownFiles-enre-out/DownFiles-out.json";
        } else {
            return ResponseEntity.badRequest().build();
        }
        return serve(toolsDir().resolve(fileName), MediaType.APPLICATION_JSON);
    }

    @GetMapping("/gap/{type}")
    public ResponseEntity<Resource> getGapResult(@PathVariable String type) {
        String normalized = type.toUpperCase();
        if (!GAP_TYPES.contains(normalized)) {
            return ResponseEntity.badRequest().build();
        }
        return serve(toolsDir().resolve("DownFiles-gap-out/DownFiles-" + normalized + ".json"), MediaType.APPLICATION_JSON);
    }

    @GetMapping("/arch-violations")
    public ResponseEntity<Resource> getArchitectureViolations() {
        return serveFirst(List.of(
                toolsDir().resolve("maq/class.violations.csv"),
                toolsDir().resolve("maq/arch-out/class.violations.csv")
        ), "text/csv");
    }

    @GetMapping("/file/{name}")
    public ResponseEntity<Resource> getNamedResult(@PathVariable String name) {
        Map<String, List<String>> files = Map.ofEntries(
                Map.entry("pmd", List.of("results/pmd.csv", "pmd/pmd.csv", "PMD-main/pmd.csv")),
                Map.entry("facade", List.of("results/facade.json", "U-DCouplingAntiPattern/facade.json")),
                Map.entry("ownership", List.of("results/final_ownership.csv", "U-DCouplingAntiPattern/final_ownership.csv")),
                Map.entry("refactor", List.of("results/refactor.json", "RefactoringMiner/refactor.json")),
                Map.entry("metrics-evolution", List.of("results/metricsEvolution.json", "U-DCouplingAntiPattern/metricsEvolution.json")),
                Map.entry("metrics-description", List.of("results/description.json", "U-DCouplingAntiPattern/description.json")),
                Map.entry("metrics-pre", List.of("results/metricsTreePre.json", "U-DCouplingAntiPattern/metricsTreePre.json")),
                Map.entry("metrics-next", List.of("results/metricsTreeNex.json", "U-DCouplingAntiPattern/metricsTreeNex.json")),
                Map.entry("report", List.of("results/report.json", "report/report.json"))
        );
        List<String> candidates = files.get(name);
        if (candidates == null) {
            return ResponseEntity.badRequest().build();
        }
        List<Path> paths = candidates.stream().map(toolsDir()::resolve).toList();
        String contentType = name.equals("pmd") || name.equals("ownership") ? "text/csv" : "application/json";
        return serveFirst(paths, contentType);
    }

    @GetMapping("/intrusive/{fileName}")
    public ResponseEntity<Resource> getIntrusiveResult(@PathVariable String fileName) {
        if (!INTRUSIVE_FILES.contains(fileName)) {
            return ResponseEntity.badRequest().build();
        }
        return serveFirst(List.of(
                toolsDir().resolve("results/intrusive_analysis").resolve(fileName),
                toolsDir().resolve("U-DCouplingAntiPattern/intrusive_analysis").resolve(fileName)
        ), "text/csv");
    }

    private ResponseEntity<Resource> serveFirst(List<Path> candidates, String contentType) {
        for (Path candidate : candidates) {
            if (Files.isRegularFile(candidate)) {
                return serve(candidate, MediaType.parseMediaType(contentType));
            }
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    private ResponseEntity<Resource> serve(Path path, MediaType mediaType) {
        if (!Files.isRegularFile(path)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        Resource resource = new FileSystemResource(path);
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .contentType(mediaType)
                .body(resource);
    }

    private Path toolsDir() {
        Path current = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
        if (current.getFileName() != null && "tools".equalsIgnoreCase(current.getFileName().toString())) {
            return current;
        }
        if (Files.isDirectory(current.resolve("tools"))) {
            return current.resolve("tools");
        }
        Path backend = current.resolve("backend");
        if (Files.isDirectory(backend.resolve("tools"))) {
            return backend.resolve("tools");
        }
        return current.resolve("tools");
    }
}
