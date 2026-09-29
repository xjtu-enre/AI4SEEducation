package com.example.filereader.controller;

import com.example.filereader.dao.Entity;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@RestController
@RequestMapping("/api")
public class FileController {

    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile[] files) {
        System.out.println("1111");
        StringBuilder uploadResult = new StringBuilder();
        for (MultipartFile file : files) {
            try {
                String fileName = file.getOriginalFilename();
                File dest = new File("path/to/save/directory/" + fileName);
                file.transferTo(dest);
                uploadResult.append("文件上传成功: ").append(fileName).append("\n");
            } catch (IOException e) {
                uploadResult.append("文件上传失败: ").append(e.getMessage()).append("\n");
            }
        }
        return uploadResult.toString();
    }

    // 接收上传的文件路径信息，并返回处理结果
    @PostMapping("/processUpFiles")
    public ResponseEntity<Object> processUpFiles(@RequestParam("files") List<MultipartFile> files) {
        System.out.println("intoUp");

        // 获取当前工作目录并构建目标目录路径
        String currentDir = System.getProperty("user.dir");
        String toolsDirPath = currentDir + File.separator + "tools";
        String upFilesDirPath = toolsDirPath + File.separator + "UpFiles";
        String enreOutPath = toolsDirPath + File.separator + "UpFiles-enre-out" + File.separator + "UpFiles-out.json";

        // 命令行语句
        String enreCommand = "java -jar " + toolsDirPath + File.separator + "enre_java.jar java " + upFilesDirPath + " " + "UpFiles";

        // 确保文件夹存在，不存在则创建
        createDirectory(toolsDirPath);
        createDirectory(upFilesDirPath);

        try {
            // 遍历上传的文件
            for (MultipartFile file : files) {
                System.out.println("上传的文件名: " + file.getOriginalFilename());

                // 获取文件的目标路径（避免文件名冲突，给文件加时间戳）
                String uniqueFileName = file.getOriginalFilename();
                File destFile = new File(upFilesDirPath, uniqueFileName);
                // 如果目标路径的父目录不存在，则创建它
                File parentDir = destFile.getParentFile();
                if (!parentDir.exists()) {
                    parentDir.mkdirs();  // 创建父目录
                }
                // 将文件保存到服务器指定目录
                file.transferTo(destFile);

                System.out.println("文件已保存: " + destFile.getAbsolutePath());
            }

            // 设置新的工作目录
            boolean success = setWorkingDirectory(toolsDirPath);
            if (success) {
                System.out.println("成功更改工作目录到: " + toolsDirPath);
            } else {
                System.out.println("无法更改工作目录");
            }

            ProcessBuilder processBuilder1 = new ProcessBuilder(enreCommand);
            processBuilder1.directory(new File(toolsDirPath));
            processBuilder1.command(enreCommand.split(" "));
            Process process1 = processBuilder1.start();
            int exitCode1 = process1.waitFor();
            System.out.println("Command executed with exit code: " + exitCode1);

            // 构造返回结果
            Map<String, Object> result = Map.of(
                    "UpFilesDirPath", upFilesDirPath,
                    "UpFilesEnreDirPath", enreOutPath,
                    "enreResultUrl", "/api/results/enre/upstream",
                    "pathsUrl", "/api/results/paths"
            );
            // 修复工作目录
            setWorkingDirectory(currentDir);
            // 返回处理成功的响应
            return ResponseEntity.ok(result);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("文件处理失败");
        }
    }

    @PostMapping("/processDownFiles")
    public ResponseEntity<Object> processDownFiles(@RequestParam("files") List<MultipartFile> files) {
        System.out.println("intoDown");

        // 获取当前工作目录并构建目标目录路径
        String currentDir = System.getProperty("user.dir");
        String toolsDirPath = currentDir + File.separator + "tools";
        String downFilesDirPath = toolsDirPath + File.separator + "DownFiles";
        String enreOutPath = toolsDirPath + File.separator + "DownFiles-enre-out" + File.separator + "DownFiles-out.json";

        // 命令行语句
        String enreCommand = "java -jar " + toolsDirPath + File.separator + "enre_java.jar java " + downFilesDirPath + " " + "DownFiles";
        String GAPCommand = "java -jar GAP-1.0.jar detect -n DownFiles -d " + enreOutPath;

        // 确保文件夹存在，不存在则创建
        createDirectory(toolsDirPath);
        createDirectory(downFilesDirPath);

        try {
            // 遍历上传的文件
            for (MultipartFile file : files) {
                System.out.println("上传的文件名: " + file.getOriginalFilename());

                // 获取文件的目标路径（避免文件名冲突，给文件加时间戳）
                String uniqueFileName = file.getOriginalFilename();
                File destFile = new File(downFilesDirPath, uniqueFileName);
                // 如果目标路径的父目录不存在，则创建它
                File parentDir = destFile.getParentFile();
                if (!parentDir.exists()) {
                    parentDir.mkdirs();  // 创建父目录
                }
                // 将文件保存到服务器指定目录
                file.transferTo(destFile);

                System.out.println("文件已保存: " + destFile.getAbsolutePath());
            }

            // 设置新的工作目录
            boolean success = setWorkingDirectory(toolsDirPath);
            if (success) {
                System.out.println("成功更改工作目录到: " + toolsDirPath);
            } else {
                System.out.println("无法更改工作目录");
            }

            ProcessBuilder processBuilder1 = new ProcessBuilder(enreCommand);
            processBuilder1.directory(new File(toolsDirPath));
            processBuilder1.command(enreCommand.split(" "));
            Process process1 = processBuilder1.start();
            int exitCode1 = process1.waitFor();
            System.out.println("Command executed with exit code: " + exitCode1);

            ProcessBuilder processBuilder2 = new ProcessBuilder(GAPCommand);
            processBuilder2.directory(new File(toolsDirPath));
            processBuilder2.command(GAPCommand.split(" "));
            Process process2 = processBuilder2.start();
            int exitCode2 = process2.waitFor();
            System.out.println("Command executed with exit code: " + exitCode2);


            // 构造返回结果
            Map<String, Object> result = Map.of(
                    "DownFilesDirPath", downFilesDirPath,
                    "DownFilesEnreDirPath", enreOutPath,
                    "enreResultUrl", "/api/results/enre/downstream",
                    "gapResultBaseUrl", "/api/results/gap",
                    "pathsUrl", "/api/results/paths"
            );
            // 修复工作目录
            setWorkingDirectory(currentDir);
            // 返回处理成功的响应
            return ResponseEntity.ok(result);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("文件处理失败");
        }
    }

    @PostMapping("/getIntrusiveCode")
    public ResponseEntity<Object> getEntityDetails(@RequestBody Map<String, Object> request) {
        String upstreamDependencyPath = (String) request.get("upstreamDependencyPath");
        String downstreamDependencyPath = (String) request.get("downstreamDependencyPath");
        String upstreamProjectPath = (String) request.get("upstreamProjectPath");
        String downstreamProjectPath = (String) request.get("downstreamProjectPath");
        Map<String, Object> entityData = (Map<String, Object>) request.get("entity");

        if (upstreamProjectPath == null || downstreamProjectPath == null ||
                upstreamDependencyPath == null || downstreamDependencyPath == null || entityData == null) {
            return ResponseEntity.badRequest().body("请求参数不完整");
        }

        try {
            // 使用 ObjectMapper 将 JSON 数据映射为 Entity 类
            ObjectMapper objectMapper = new ObjectMapper();
            Entity entity = objectMapper.convertValue(entityData, Entity.class);

            // 使用 Jackson Streaming API 提取 "variables" 节点
            JsonNode upstreamNodes = extractVariablesJsonIfPresent(upstreamDependencyPath, objectMapper);
            JsonNode downstreamNodes = extractVariablesJsonIfPresent(downstreamDependencyPath, objectMapper);
            JsonNode upstreamNode = null;
            JsonNode downstreamNode = null;

            if (upstreamNodes != null && upstreamNodes.isArray()) {
                for (JsonNode variable : upstreamNodes) {
                    if (variable.has("File") && variable.get("File").asText().equals(entity.getFilePath())
                            && variable.has("qualifiedName")
                            && variable.get("qualifiedName").asText().equals(entity.getQualifiedName())) {
                        upstreamNode = variable;
                        break;
                    }
                }
            }

            if (downstreamNodes != null && downstreamNodes.isArray()) {
                for (JsonNode variable : downstreamNodes) {
                    if (variable.has("File") && variable.get("File").asText().equals(entity.getFilePath())
                            && variable.has("qualifiedName")
                            && variable.get("qualifiedName").asText().equals(entity.getQualifiedName())) {
                        downstreamNode = variable;
                        break;
                    }
                }
            }

            String upstreamCode = null;
            String downstreamCode = null;

            if(upstreamNode != null){
                JsonNode upLocationNode = upstreamNode.get("location");
                int startLine = upLocationNode != null && upLocationNode.has("startLine") ?
                        upLocationNode.get("startLine").asInt() : 0;
                int endLine = upLocationNode != null && upLocationNode.has("endLine") ?
                        upLocationNode.get("endLine").asInt() : Integer.MAX_VALUE;

                // 拼接文件路径
                String upstreamFilePath = upstreamProjectPath + File.separator + upstreamNode.get("File").asText();
                // 读取代码片段
                upstreamCode = getCodeSnippetOrMessage(upstreamFilePath, startLine, endLine);
            }

            if(downstreamNode != null){
                JsonNode downLocationNode = downstreamNode.get("location");
                int startLine = downLocationNode != null && downLocationNode.has("startLine") ?
                        downLocationNode.get("startLine").asInt() : 0;
                int endLine = downLocationNode != null && downLocationNode.has("endLine") ?
                        downLocationNode.get("endLine").asInt() : Integer.MAX_VALUE;
                // 拼接文件路径
                String downstreamFilePath = downstreamProjectPath + File.separator + downstreamNode.get("File").asText();
                // 读取代码片段
                downstreamCode = getCodeSnippetOrMessage(downstreamFilePath, startLine, endLine);
            }

            if (upstreamCode == null) {
                upstreamCode = getCodeSnippetOrMessage(
                        upstreamProjectPath + File.separator + entity.getFilePath(), 1, 80);
            }
            if (downstreamCode == null) {
                downstreamCode = getCodeSnippetOrMessage(
                        downstreamProjectPath + File.separator + entity.getFilePath(), 1, 80);
            }

            // 构造返回结果
            Map<String, Object> result = Map.of(
                    "upstreamCode", upstreamCode,
                    "downstreamCode", downstreamCode
            );

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("处理请求时发生错误：" + e.getMessage());
        }
    }

    @PostMapping("/getPMDCode")
    public ResponseEntity<Object> getPMDDetails(@RequestBody Map<String, Object> request) {
        String filePath = (String) request.get("file");
        Object lineValue = request.get("line");
        if (filePath == null || lineValue == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "请求参数不完整"));
        }

        try {
            int line = lineValue instanceof Number
                    ? ((Number) lineValue).intValue()
                    : Integer.parseInt(lineValue.toString());
            String PMDCode = getCodeSnippet(filePath, line, line + 30);

            return ResponseEntity.ok(Map.of("PMDCode", PMDCode));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "代码行号格式不正确"));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/getFacadeCode")
    public ResponseEntity<Object> getFacadeCode(@RequestBody Map<String, Object> request) {
        String srcFile = (String) request.get("src_file");
        int srcStartLine = (int) request.get("src_start");
        int srcEndLine = (int) request.get("src_end");
        String srcOwnership = (String) request.get("src_ownership");
        String destFile = (String) request.get("dest_file");
        int destStartLine = (int) request.get("dest_start");
        int destEndLine = (int) request.get("dest_end");
        String destOwnership = (String) request.get("dest_ownership");
        String upProjectPath = (String) request.get("upProjectPath");
        String downProjectPath = (String) request.get("downProjectPath");
        System.out.println("getFacadeCode:upProjectPath");
        System.out.println("getFacadeCode:downProjectPath");
        System.out.println(upProjectPath);
        System.out.println(downProjectPath);

        if (srcFile == null || destFile == null) {
            return ResponseEntity.badRequest().body("请求参数不完整");
        }

        try {
            String upstreamCode = null;
            String downstreamCode = null;

            // 拼接文件路径
            String upstreamFilePath = srcOwnership.equals("extensive")
                    ? downProjectPath + File.separator + srcFile
                    : upProjectPath + File.separator + srcFile;
            // 读取代码片段
            upstreamCode = getCodeSnippetOrMessage(upstreamFilePath, srcStartLine, srcEndLine);

            // 拼接文件路径
            String downstreamFilePath = destOwnership.equals("extensive")
                    ? downProjectPath + File.separator + destFile
                    : upProjectPath + File.separator + destFile;
            // 读取代码片段
            downstreamCode = getCodeSnippetOrMessage(downstreamFilePath, destStartLine, destEndLine);


            if (upstreamCode == null) {
                upstreamCode = "该实体为下游新增，无上游代码";
            }
            if (downstreamCode == null) {
                downstreamCode = "该实体为下游删除，无下游代码";
            }

            // 构造返回结果
            Map<String, Object> result = Map.of(
                    "upstreamCode", upstreamCode,
                    "downstreamCode", downstreamCode
            );

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("处理请求时发生错误：" + e.getMessage());
        }
    }

    @PostMapping("/getRefactorCode")
    public ResponseEntity<Object> getRefactorCode(@RequestBody Map<String, Object> request) {
        String upProjectPath = (String) request.get("upProjectPath");
        String downProjectPath = (String) request.get("downProjectPath");
        System.out.println("getRefactorCode:upProjectPath");
        System.out.println("getRefactorCode:downProjectPath");
        System.out.println(upProjectPath);
        System.out.println(downProjectPath);

        Map<String, Object> entityData = (Map<String, Object>) request.get("entity");

        if (upProjectPath == null || downProjectPath == null || entityData == null) {
            return ResponseEntity.badRequest().body("请求参数不完整");
        }

        try {
            // 使用 ObjectMapper 将 JSON 数据映射为 Map
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> entity = objectMapper.convertValue(entityData, Map.class);

            // 提取 leftSideLocations 和 rightSideLocations
            List<Map<String, Object>> leftSideLocations = (List<Map<String, Object>>) entity.get("leftSideLocations");
            List<Map<String, Object>> rightSideLocations = (List<Map<String, Object>>) entity.get("rightSideLocations");

            // 提取代码片段
            List<String> beforeCode = new ArrayList<>();
            if (leftSideLocations != null && !leftSideLocations.isEmpty()) {
                for (Map<String, Object> location : leftSideLocations) {
                    String filePath = (String) location.get("filePath");
                    int startLine = (int) location.get("startLine");
                    int endLine = (int) location.get("endLine");

                    // 拼接完整文件路径
                    String fullFilePath = upProjectPath + File.separator + filePath;

                    // 提取代码片段
                    beforeCode.add(getCodeSnippetOrMessage(fullFilePath, startLine, endLine));
                }
            }

            List<String> afterCode = new ArrayList<>();
            if (rightSideLocations != null && !rightSideLocations.isEmpty()) {
                for (Map<String, Object> location : rightSideLocations) {
                    String filePath = (String) location.get("filePath");
                    int startLine = (int) location.get("startLine");
                    int endLine = (int) location.get("endLine");

                    // 拼接完整文件路径
                    String fullFilePath = downProjectPath + File.separator + filePath;

                    // 提取代码片段
                    afterCode.add(getCodeSnippetOrMessage(fullFilePath, startLine, endLine));
                }
            }

            // 构造返回结果
            Map<String, Object> result = Map.of(
                    "beforeCode", beforeCode,
                    "afterCode", afterCode
            );

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("处理请求时发生错误：" + e.getMessage());
        }
    }

    @PostMapping("/processArch")
    public ResponseEntity<Object> processArch(@RequestBody Map<String, Object> request) throws Exception {
        System.out.println("intoArch");

        // 获取当前工作目录并构建目标目录路径
        String currentDir = System.getProperty("user.dir");
        String guardingDirPath = currentDir + File.separator + "tools" + File.separator + "maq" + File.separator + "lib" + File.separator + "guarding";
        String archunitDirPath = currentDir + File.separator + "tools" + File.separator + "maq";
        String archOutPath = archunitDirPath + File.separator + "arch-out" + File.separator + "class.violations.csv";

        // 命令行语句
        String rank = (String) request.get("rank");
        String guardingCommand = "";
        if (rank.equals("1")) {
            guardingCommand = "guarding.exe --url " + (String) request.get("url");
        }else {
            guardingCommand = "guarding.exe --nl " + (String) request.get("nl");
        }
        String archunitCommand = "java -jar ./libs/archunit-1.4.0-SNAPSHOT.jar ./classes ./data/LineageOS-16.0/final_ownership.csv classes ./data/LineageOS-16.0/enre.json ./data/LineageOS-16.0/ignores.txt ./api.json";

        // 设置新的工作目录
        boolean success = setWorkingDirectory(archunitDirPath);
        if (success) {
            System.out.println("成功更改工作目录到: " + archunitDirPath);
        } else {
            System.out.println("无法更改工作目录");
        }

        // 运行 guarding
        ProcessBuilder processBuilder1 = new ProcessBuilder(guardingCommand);
        processBuilder1.directory(new File(guardingDirPath));
        processBuilder1.command(guardingCommand.split(" "));
        Process process1 = processBuilder1.start();
        int exitCode1 = process1.waitFor();
        System.out.println("Command executed with exit code: " + exitCode1);

        // 运行 archunit
        ProcessBuilder processBuilder2 = new ProcessBuilder(archunitCommand);
        processBuilder2.directory(new File(archunitDirPath));
        processBuilder2.command(archunitCommand.split(" "));
        Process process2 = processBuilder2.start();
        int exitCode2 = process2.waitFor();
        System.out.println("Command executed with exit code: " + exitCode2);

        return ResponseEntity.ok(Map.of(
                "message", "架构约束检查完成",
                "resultPath", archOutPath,
                "resultUrl", "/api/results/arch-violations"
        ));


    }

    private JsonNode extractVariablesJson(String filePath, ObjectMapper objectMapper) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("文件不存在: " + filePath);
        }

        // 使用 Jackson 的流式解析器
        JsonFactory jsonFactory = objectMapper.getFactory();
        try (JsonParser parser = jsonFactory.createParser(new FileInputStream(file))) {
            JsonToken token;
            while ((token = parser.nextToken()) != null) {
                if (JsonToken.FIELD_NAME.equals(token) && "variables".equals(parser.getCurrentName())) {
                    parser.nextToken(); // 跳到 "variables" 的值
                    return objectMapper.readTree(parser);
                }
            }
        }
        return null;
    }

    private JsonNode extractVariablesJsonIfPresent(String filePath, ObjectMapper objectMapper) {
        try {
            return extractVariablesJson(filePath, objectMapper);
        } catch (IOException e) {
            System.out.println("依赖分析文件不可用，将按源码相对路径回退: " + e.getMessage());
            return null;
        }
    }

    private String getCodeSnippet(String filePath, int startLine, int endLine) throws IOException {
        if (filePath == null || filePath.isBlank()) {
            throw new IOException("代码文件路径为空");
        }

        int normalizedStartLine = Math.max(1, startLine);
        int normalizedEndLine = Math.max(normalizedStartLine, endLine);
        Path resolvedPath = resolveSourcePath(filePath);
        if (resolvedPath != null) {
            try (BufferedReader reader = Files.newBufferedReader(resolvedPath, StandardCharsets.UTF_8)) {
                return readCodeSnippet(reader, normalizedStartLine, normalizedEndLine);
            }
        }

        String archivedSnippet = getBundledPmdSnippet(filePath, normalizedStartLine, normalizedEndLine);
        if (archivedSnippet != null) {
            return archivedSnippet;
        }

        throw new IOException("无法定位源文件: " + filePath);
    }

    private String getCodeSnippetOrMessage(String filePath, int startLine, int endLine) {
        try {
            return getCodeSnippet(filePath, startLine, endLine);
        } catch (IOException e) {
            return "// 当前环境未找到该版本的源文件\n// " + e.getMessage();
        }
    }

    private String readCodeSnippet(BufferedReader reader, int startLine, int endLine) throws IOException {
        StringBuilder codeSnippet = new StringBuilder();
        String line;
        int currentLine = 0;

        while ((line = reader.readLine()) != null) {
            currentLine++;
            if (currentLine >= startLine && currentLine <= endLine) {
                codeSnippet.append(line).append(System.lineSeparator());
            }
            if (currentLine > endLine) {
                break;
            }
        }

        if (codeSnippet.isEmpty()) {
            return "// 文件存在，但指定行范围没有可显示的内容（"
                    + startLine + "-" + endLine + "）";
        }
        return codeSnippet.toString();
    }

    private Path resolveSourcePath(String filePath) {
        try {
            Path requestedPath = Paths.get(filePath).normalize();
            if (Files.isRegularFile(requestedPath)) {
                return requestedPath;
            }
        } catch (InvalidPathException ignored) {
            // 继续尝试从项目内置源码和本机 AOSP 目录中定位。
        }

        String relativePath = extractKnownRelativePath(filePath);
        if (relativePath == null || relativePath.isBlank()) {
            return null;
        }

        List<Path> sourceRoots = new ArrayList<>();
        String configuredRoot = System.getenv("ARCHSENTINEL_SOURCE_ROOT");
        if (configuredRoot != null && !configuredRoot.isBlank()) {
            sourceRoots.add(Paths.get(configuredRoot));
        }

        Path backendRoot = findBackendRoot();
        sourceRoots.add(backendRoot.resolve("tools").resolve("UpFiles"));
        sourceRoots.add(backendRoot.resolve("tools").resolve("DownFiles"));
        sourceRoots.add(Paths.get("D:\\android15\\frameworks\\base"));

        String platformRelativePath = relativePath.replace('/', File.separatorChar);
        for (Path sourceRoot : sourceRoots) {
            Path candidate = sourceRoot.resolve(platformRelativePath).normalize();
            if (candidate.startsWith(sourceRoot.normalize()) && Files.isRegularFile(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private String extractKnownRelativePath(String filePath) {
        String normalized = filePath.replace('\\', '/');
        String lowerCasePath = normalized.toLowerCase();
        String[] markers = {
                "/frameworks/base/",
                "/android_frameworks_base/",
                "/pmd-main/"
        };
        for (String marker : markers) {
            int markerIndex = lowerCasePath.indexOf(marker);
            if (markerIndex >= 0) {
                return normalized.substring(markerIndex + marker.length());
            }
        }

        if (!Paths.get(normalized).isAbsolute()) {
            return normalized.replaceFirst("^/+", "");
        }
        return null;
    }

    private String getBundledPmdSnippet(String filePath, int startLine, int endLine) throws IOException {
        String normalized = filePath.replace('\\', '/');
        String lowerCasePath = normalized.toLowerCase();
        int markerIndex = lowerCasePath.indexOf("/pmd-main/");
        if (markerIndex < 0) {
            return null;
        }

        String entryName = "PMD-main/" + normalized.substring(markerIndex + "/pmd-main/".length());
        Path backendRoot = findBackendRoot();
        List<Path> archiveCandidates = List.of(
                backendRoot.resolve("tools").resolve("PMD-main.zip"),
                backendRoot.resolve("PMD-main.zip")
        );

        for (Path archivePath : archiveCandidates) {
            if (!Files.isRegularFile(archivePath)) {
                continue;
            }
            try (ZipFile zipFile = new ZipFile(archivePath.toFile(), StandardCharsets.UTF_8)) {
                ZipEntry entry = zipFile.getEntry(entryName);
                if (entry == null || entry.isDirectory()) {
                    continue;
                }
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(zipFile.getInputStream(entry), StandardCharsets.UTF_8))) {
                    return readCodeSnippet(reader, startLine, endLine);
                }
            }
        }
        return null;
    }

    private Path findBackendRoot() {
        Path currentPath = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
        Path candidate = currentPath;
        for (int i = 0; i < 4 && candidate != null; i++) {
            if (Files.isRegularFile(candidate.resolve("pom.xml"))) {
                return candidate;
            }
            candidate = candidate.getParent();
        }
        return currentPath;
    }

    // 创建目录（如果不存在）
    private void createDirectory(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (created) {
                System.out.println("文件夹创建成功: " + path);
            } else {
                System.out.println("文件夹创建失败: " + path);
            }
        } else {
            System.out.println("文件夹已存在: " + path);
        }
    }

    // 设置新的工作目录
    private boolean setWorkingDirectory(String newDir) {
        // 设置新的工作目录
        File dir = new File(newDir);
        if (dir.exists() && dir.isDirectory()) {
            // 更改工作目录
            System.setProperty("user.dir", dir.getAbsolutePath());
            return true;
        } else {
            System.out.println("目录不存在: " + newDir);
            return false;
        }
    }

}
