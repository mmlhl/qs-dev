import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.printer.PrettyPrinter;
import com.github.javaparser.printer.configuration.PrettyPrinterConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.util.Properties;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Enhanced Java to BeanShell converter.
 * Features:
 * - Converts @BeanShellType annotated types to Object
 * - Extracts methods from @ScriptMethods annotated classes to root level
 * - Removes annotations (BeanShell doesn't support them)
 * - Recursively converts all Java files in subdirectories
 * - Outputs main.java and module files to dist/
 */
public class JavaToBeanShellConverter {

    // 类名映射表: 原始类名 -> 重命名后的类名
    private static final Map<String, String> classNameMap = new HashMap<>();
    
    // 全局实例映射表: 类名 -> 全局变量名 (用于 @GlobalInstance 注解的类)
    private static final Map<String, String> globalInstanceMap = new HashMap<>();
    
    private static final Set<String> BEANSHELL_TYPES = new HashSet<>();
    
    // 全局跟踪已加载的模块,避免重复 load
    private static final ThreadLocal<Set<String>> loadedModules = ThreadLocal.withInitial(HashSet::new);

    static {
        // Types that should be converted to Object
        BEANSHELL_TYPES.add("MessageData");
        BEANSHELL_TYPES.add("GroupInfo");
        BEANSHELL_TYPES.add("GroupMemberInfo");
        BEANSHELL_TYPES.add("ForbiddenInfo");
        BEANSHELL_TYPES.add("FriendInfo");
        
        // Java reflection types - BeanShell doesn't support these type declarations
        BEANSHELL_TYPES.add("Method");
        BEANSHELL_TYPES.add("Field");
        BEANSHELL_TYPES.add("Constructor");
        BEANSHELL_TYPES.add("Class");
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("Usage: java JavaToBeanShellConverter <script-package-dir>");
            System.exit(1);
        }

        String scriptPackageDir = args[0];
        Path scriptPath = Paths.get(scriptPackageDir);
        
        if (!Files.isDirectory(scriptPath)) {
            System.err.println("Error: " + scriptPackageDir + " is not a directory");
            System.exit(1);
        }
        
        // 清空已加载模块列表
        loadedModules.get().clear();
        // 清空类名映射表
        classNameMap.clear();
        // 清空全局实例映射表
        globalInstanceMap.clear();
        
        // 预先扫描整个 myscript 目录，收集所有类的映射关系
        collectAllClassNameMappings(scriptPath);
        
        // Find project root
        Path projectRoot = scriptPath;
        while (projectRoot != null && !Files.exists(projectRoot.resolve("build.gradle"))) {
            projectRoot = projectRoot.getParent();
        }
        if (projectRoot == null) {
            projectRoot = Paths.get(".");
        }
        
        // Get script name from @ScriptInfo annotation
        Path mainFile = scriptPath.resolve("Main.java");
        String scriptName = getScriptName(mainFile);
        
        // Create dist/scriptName directory structure
        Path distRoot = projectRoot.resolve("dist");
        Path scriptDir = distRoot.resolve(scriptName);
        
        // Delete existing script directory if exists (clean build)
        if (Files.exists(scriptDir)) {
            System.out.println("Cleaning existing script directory: " + scriptDir);
            deleteDirectory(scriptDir);
        }
        
        Files.createDirectories(scriptDir);
        
        // 先转换所有子目录(constants, utils等),再转换 Main.java
        // 这样可以确保依赖关系正确
        convertDirectory(scriptPath, scriptDir, projectRoot, scriptPath);
        
        // Convert Main.java to scriptName/main.java
        if (Files.exists(mainFile)) {
            String mainOutput = convertFile(mainFile, projectRoot, scriptPath, true);
            Path mainOutputPath = scriptDir.resolve("main.java");
            Files.writeString(mainOutputPath, mainOutput);
            System.out.println("Generated: " + mainOutputPath);
            
            // Generate script metadata files (desc.txt and info.prop) in scriptName folder
            generateScriptMetadata(mainFile, scriptDir, distRoot, scriptPath, scriptName);
            
            // Write source dir -> display name mapping for gradle deploy
            String sourceDirName = scriptPath.getFileName().toString();
            Path mappingFile = distRoot.resolve(".script-mapping");
            // Append to mapping file
            String mappingLine = sourceDirName + "=" + scriptName + "\n";
            java.nio.file.Files.writeString(mappingFile, mappingLine, 
                java.nio.file.StandardOpenOption.CREATE, 
                java.nio.file.StandardOpenOption.APPEND);
        } else {
            System.err.println("Warning: Main.java not found in " + scriptPackageDir);
        }
        
        System.out.println("\nConversion successful!");
        System.out.println("Script output: " + scriptDir);
    }

    /**
     * Recursively delete a directory and all its contents
     */
    private static void deleteDirectory(Path directory) throws IOException {
        if (Files.exists(directory)) {
            Files.walk(directory)
                .sorted(Comparator.reverseOrder())
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        System.err.println("Failed to delete: " + path + ": " + e.getMessage());
                    }
                });
        }
    }

    /**
     * Convert a method declaration to BeanShell format:
     * - Replace @BeanShellType types with Object
     * - Remove all annotations
     * - Remove 'static' modifier (BeanShell methods are instance methods)
     * - Convert messageHandler.method() to method()
     * - Keep method signature and body
     */
    private static String convertMethod(MethodDeclaration method, PrettyPrinter printer) {
        // Clone the method to avoid modifying the original AST
        MethodDeclaration converted = method.clone();

        // Remove all annotations
        converted.getAnnotations().clear();
        
        // Remove 'static' modifier
        converted.setStatic(false);

        // Convert return type if needed
        Type returnType = converted.getType();
        convertTypeToObject(returnType);

        // Convert parameter types if needed
        for (Parameter param : converted.getParameters()) {
            convertTypeToObject(param.getType());
        }
        
        // Convert local variable types in method body
        if (converted.getBody().isPresent()) {
            converted.getBody().get().findAll(com.github.javaparser.ast.expr.VariableDeclarationExpr.class).forEach(varDecl -> {
                convertTypeToObject(varDecl.getCommonType());
            });
        }
        
        // Convert method calls: ClassName.staticMethod() -> renamedClassName.staticMethod()
        // Also convert Globals.xxx -> xxx
        if (converted.getBody().isPresent()) {
            // 不再移除实例方法调用的前缀，因为类已保留定义
            
            // Convert Globals.xxx to xxx (global variables)
            // Convert ClassName.staticField to globalVarName.staticField for @GlobalInstance classes
            converted.getBody().get().findAll(FieldAccessExpr.class).forEach(fieldAccess -> {
                if (fieldAccess.getScope().isNameExpr()) {
                    String scopeName = fieldAccess.getScope().asNameExpr().getNameAsString();
                    if (scopeName.equals("Globals")) {
                        // Replace Globals.myUin with just myUin
                        fieldAccess.replace(fieldAccess.getNameAsExpression());
                    } else if (globalInstanceMap.containsKey(scopeName)) {
                        // Convert ClassName.staticField to globalVarName.staticField
                        // e.g. SilkAudioDecoder.lastSampleRate -> audioDecoder.lastSampleRate
                        String globalVarName = globalInstanceMap.get(scopeName);
                        fieldAccess.getScope().asNameExpr().setName(globalVarName);
                    }
                    // 常量类引用保持不变: MessageType.VOICE
                    // 因为已经自动实例化了 MessageType = new Constants_MessageType()
                }
            });
        }

        return printer.print(converted);
    }

    /**
     * Recursively convert all Java files in subdirectories
     */
    private static void convertDirectory(Path sourceDir, Path distDir, Path projectRoot, Path scriptRoot) throws IOException {
        Files.list(sourceDir)
            .filter(Files::isDirectory)
            .forEach(subDir -> {
                try {
                    String relativePath = scriptRoot.relativize(subDir).toString();
                    Path outputDir = distDir.resolve(relativePath);
                    Files.createDirectories(outputDir);
                    
                    // Convert all Java files in this directory (except Bridge classes)
                    Files.list(subDir)
                        .filter(file -> file.toString().endsWith(".java"))
                        .filter(file -> !file.getFileName().toString().endsWith("Bridge.java"))
                        .forEach(javaFile -> {
                            try {
                                String fileName = javaFile.getFileName().toString();
                                String content = convertFile(javaFile, projectRoot, scriptRoot, false);
                                // Only generate file if content is not null (not empty)
                                if (content != null) {
                                    Path outputFile = outputDir.resolve(fileName);
                                    Files.writeString(outputFile, content);
                                    System.out.println("Generated: " + outputFile);
                                } else {
                                    System.out.println("Skipped (empty): " + scriptRoot.relativize(javaFile));
                                }
                            } catch (IOException e) {
                                System.err.println("Error converting " + javaFile + ": " + e.getMessage());
                            }
                        });
                    
                    // Recursively process subdirectories
                    convertDirectory(subDir, distDir, projectRoot, scriptRoot);
                } catch (IOException e) {
                    System.err.println("Error processing directory " + subDir + ": " + e.getMessage());
                }
            });
    }

    /**
     * Convert a single Java file to BeanShell format
     * @param isMainFile Whether this is the Main.java file (only main.java should generate global instances)
     */
    private static String convertFile(Path javaFile, Path projectRoot, Path scriptRoot, boolean isMainFile) throws IOException {
        FileInputStream in = new FileInputStream(javaFile.toFile());
        
        // Parse the Java file
        ParserConfiguration parserConfig = new ParserConfiguration();
        ParseResult<CompilationUnit> parseResult = new JavaParser(parserConfig).parse(in);
        in.close();
        
        if (!parseResult.isSuccessful()) {
            throw new IOException("Failed to parse " + javaFile + ": " + 
                parseResult.getProblems().stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(", ")));
        }
        
        CompilationUnit cu = parseResult.getResult().get();

        // Configure printer
        PrettyPrinterConfiguration printerConfig = new PrettyPrinterConfiguration();
        printerConfig.setIndentSize(4);
        PrettyPrinter printer = new PrettyPrinter(printerConfig);

        StringBuilder beanShellCode = new StringBuilder();
        
        // 添加构建信息头
        String buildTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        beanShellCode.append("// ================================================\n");
        beanShellCode.append("// Generated BeanShell Script\n");
        beanShellCode.append("// Source: ").append(scriptRoot.relativize(javaFile)).append("\n");
        beanShellCode.append("// Build Time: ").append(buildTime).append("\n");
        beanShellCode.append("// DO NOT EDIT - Changes will be overwritten\n");
        beanShellCode.append("// ================================================\n\n");

        // 保留外部 import（非本项目的 import）
        List<String> externalImports = new ArrayList<>();
        cu.getImports().forEach(importDecl -> {
            String importName = importDecl.getNameAsString();
            // 跳过本项目的 import (me.mm.qs.*)
            if (importName.startsWith("me.mm.qs.")) {
                return;
            }
            // 保留外部 import (android.*, java.*, 等)
            if (importDecl.isStatic()) {
                externalImports.add("import static " + importName + (importDecl.isAsterisk() ? ".*" : "") + ";");
            } else {
                externalImports.add("import " + importName + (importDecl.isAsterisk() ? ".*" : "") + ";");
            }
        });
        if (!externalImports.isEmpty()) {
            for (String imp : externalImports) {
                beanShellCode.append(imp).append("\n");
            }
            beanShellCode.append("\n");
        }

        // 先递归收集所有依赖,添加到已加载列表
        collectAllDependencies(cu, scriptRoot, loadedModules.get());
        
        // 先收集所有常量类的映射关系（从依赖文件中）
        collectClassNameMappings(cu, scriptRoot);
        
        // STEP 1: Generate global instances FIRST (ONLY in main.java)
        // This ensures imported files can access these global variables
        if (isMainFile) {
            List<String> globalInstances = extractGlobalInstances(cu, scriptRoot);
            if (!globalInstances.isEmpty()) {
                for (String instanceStmt : globalInstances) {
                    beanShellCode.append(instanceStmt).append("\n");
                }
                beanShellCode.append("\n");
            }
        }
        
        // STEP 2: Extract and convert imports to load() calls
        List<String> loadStatements = extractLoadStatements(cu, projectRoot, scriptRoot);
        if (!loadStatements.isEmpty()) {
            for (String loadStmt : loadStatements) {
                beanShellCode.append(loadStmt).append("\n");
            }
            beanShellCode.append("\n");
        }

        // Process all classes in myscript
        cu.findAll(ClassOrInterfaceDeclaration.class).forEach(classDecl -> {
            boolean hasGlobalInstanceAnnotation = classDecl.getAnnotations().stream()
                    .anyMatch(anno -> anno.getNameAsString().equals("GlobalInstance"));
            boolean hasRootCodeAnnotation = classDecl.getAnnotations().stream()
                    .anyMatch(anno -> anno.getNameAsString().equals("RootCode"));

            // Handle @RootCode class: extract all methods' bodies to root level
            if (hasRootCodeAnnotation) {
                classDecl.getMethods().forEach(method -> {
                    // Extract method body statements directly
                    if (method.getBody().isPresent()) {
                        method.getBody().get().getStatements().forEach(stmt -> {
                            beanShellCode.append(printer.print(stmt)).append("\n");
                        });
                        beanShellCode.append("\n");
                    }
                });
                return; // Skip further processing for @RootCode classes
            }

            // Skip @GlobalInstance classes - they are already generated as global variables
            if (hasGlobalInstanceAnnotation) {
                return;
            }
            
            String originalClassName = classDecl.getNameAsString();
            boolean isMainClass = originalClassName.equals("Main") && isMainFile;
            
            if (isMainClass) {
                // Main 类：去掉大括号，保留所有内容（变量和方法）到根级别
                
                // 提取成员变量
                classDecl.getFields().forEach(field -> {
                    field.getVariables().forEach(var -> {
                        String varName = var.getNameAsString();
                        if (var.getInitializer().isPresent()) {
                            beanShellCode.append(varName).append(" = ");
                            beanShellCode.append(printer.print(var.getInitializer().get())).append(";\n");
                        } else {
                            beanShellCode.append(varName).append(";\n");
                        }
                    });
                });
                
                if (!classDecl.getFields().isEmpty()) {
                    beanShellCode.append("\n");
                }
                
                // 提取方法
                classDecl.getMethods().forEach(method -> {
                    boolean hasRootCode = method.getAnnotations().stream()
                            .anyMatch(anno -> anno.getNameAsString().equals("RootCode"));
                    
                    if (hasRootCode) {
                        // 只提取方法体内容
                        if (method.getBody().isPresent()) {
                            method.getBody().get().getStatements().forEach(stmt -> {
                                beanShellCode.append(printer.print(stmt)).append("\n");
                            });
                            beanShellCode.append("\n");
                        }
                    } else {
                        // 提取完整方法
                        String methodCode = convertMethod(method, printer);
                        beanShellCode.append(methodCode).append("\n\n");
                    }
                });
            } else {
                // 其他类：保留类定义并重命名为 路径_类名
                String relativePath = scriptRoot.relativize(javaFile.getParent()).toString();
                String uniqueClassName;
                if (relativePath.isEmpty() || relativePath.equals(".")) {
                    uniqueClassName = originalClassName;
                } else {
                    String pathPrefix = relativePath.replace(File.separatorChar, '_')
                        .replace('-', '_');
                    pathPrefix = pathPrefix.substring(0, 1).toUpperCase() + pathPrefix.substring(1);
                    uniqueClassName = pathPrefix + "_" + originalClassName;
                }
                
                // 记录类名映射（用于转换 new 语句）
                classNameMap.put(originalClassName, uniqueClassName);
                
                // 添加类注释
                if (classDecl.getJavadocComment().isPresent()) {
                    beanShellCode.append("/**").append(classDecl.getJavadocComment().get().getContent()).append("*/\n");
                }
                
                // 输出类定义
                beanShellCode.append("class ").append(uniqueClassName).append(" {\n");
                
                // 添加所有字段（静态和非静态）
                classDecl.getFields().forEach(field -> {
                    field.getVariables().forEach(var -> {
                        String varName = var.getNameAsString();
                        if (var.getInitializer().isPresent()) {
                            beanShellCode.append("    ").append(varName).append(" = ");
                            beanShellCode.append(printer.print(var.getInitializer().get())).append(";\n");
                        } else {
                            beanShellCode.append("    ").append(varName).append(";\n");
                        }
                    });
                });
                
                if (!classDecl.getFields().isEmpty()) {
                    beanShellCode.append("\n");
                }
                
                // 添加方法
                classDecl.getMethods().forEach(method -> {
                    boolean hasRootCode = method.getAnnotations().stream()
                            .anyMatch(anno -> anno.getNameAsString().equals("RootCode"));
                    
                    if (!hasRootCode) {
                        String methodCode = convertMethod(method, printer);
                        // 缩进方法代码
                        String[] lines = methodCode.split("\n");
                        for (String line : lines) {
                            if (!line.trim().isEmpty()) {
                                beanShellCode.append("    ").append(line).append("\n");
                            }
                        }
                        beanShellCode.append("\n");
                    }
                });
                
                beanShellCode.append("}\n\n");
            }
        });

        // 替换代码中的类名引用: new ClassName() -> new Prefix_ClassName()
        String result = beanShellCode.toString();
        result = replaceClassNameReferences(result);
        
        // Check if the result only contains header comments and whitespace
        // If so, mark it as empty (should not generate file)
        String contentWithoutHeader = result
            .replaceFirst("(?s)^// =+[\\s\\S]*?// =+\\s*", "")
            .trim();
        if (contentWithoutHeader.isEmpty()) {
            return null; // Signal that this file should not be generated
        }
        
        return result;
    }

    /**
     * 后处理生成的代码,将类成员访问转换为直接变量访问
     * 将 MessageType.VOICE 转换为 VOICE
     * 将 ChatType.PRIVATE 转换为 PRIVATE
     */
    private static String postProcessBeanShellCode(String code, Path projectRoot, Path scriptRoot) {
        // 分析 myscript 包下的常量类,收集常量名
        Set<String> constantClassNames = new HashSet<>();
        
        try (Stream<Path> paths = Files.walk(scriptRoot)) {
            paths.filter(Files::isRegularFile)
                 .filter(p -> p.toString().endsWith(".java"))
                 .filter(p -> !p.getFileName().toString().equals("Main.java"))
                 .forEach(javaFile -> {
                     try {
                         FileInputStream in = new FileInputStream(javaFile.toFile());
                         ParseResult<CompilationUnit> parseResult = new JavaParser(new ParserConfiguration()).parse(in);
                         in.close();
                         
                         if (parseResult.isSuccessful()) {
                             CompilationUnit cu = parseResult.getResult().get();
                             // 找到不带 @ScriptMethods 注解的类(常量类)
                             cu.findAll(ClassOrInterfaceDeclaration.class).forEach(classDecl -> {
                                 boolean hasScriptMethodsAnnotation = classDecl.getAnnotations().stream()
                                         .anyMatch(anno -> anno.getNameAsString().equals("ScriptMethods"));
                                 
                                 if (!hasScriptMethodsAnnotation) {
                                     // 这是一个常量类
                                     constantClassNames.add(classDecl.getNameAsString());
                                 }
                             });
                         }
                     } catch (IOException e) {
                         // 忽略
                     }
                 });
        } catch (IOException e) {
            // 忽略
        }
        
        // 替换 ClassName.CONSTANT 为 CONSTANT
        String processedCode = code;
        for (String className : constantClassNames) {
            // 使用正则表达式替换: ClassName.CONSTANT_NAME -> CONSTANT_NAME
            // \b确保全字匹配, [A-Z_][A-Z0-9_]* 匹配常量名(大写下划线)
            processedCode = processedCode.replaceAll(
                "\\b" + className + "\\.([A-Z_][A-Z0-9_]*)",
                "$1"
            );
        }
        
        return processedCode;
    }

    /**
     * 递归收集文件的所有间接依赖,添加到已加载列表
     * 注意:不收集当前文件的直接导入,只收集它们的依赖(间接依赖)
     * 
     * 更新:由于BeanShell类定义可以重复加载,暂时禁用此逻辑
     * 让每个文件都加载其直接依赖
     */
    private static void collectAllDependencies(CompilationUnit cu, Path scriptRoot, Set<String> loaded) {
        // 暂时禁用间接依赖收集,让每个文件都加载其直接导入
        // 这样可以避免 Main 中直接使用的 MessageType 找不到的问题
        return;
    }
    
    /**
     * 递归收集传递依赖
     */
    private static void collectTransitiveDependencies(String filePath, Path scriptRoot, Set<String> loaded) {
        Path file = scriptRoot.resolve(filePath.replace('/', File.separatorChar) + ".java");
        if (Files.exists(file)) {
            try {
                FileInputStream in = new FileInputStream(file.toFile());
                ParseResult<CompilationUnit> parseResult = new JavaParser(new ParserConfiguration()).parse(in);
                in.close();
                
                if (parseResult.isSuccessful()) {
                    CompilationUnit cu = parseResult.getResult().get();
                    cu.getImports().forEach(importDecl -> {
                        String importName = importDecl.getNameAsString();
                        if (!importDecl.isStatic() && importName.startsWith("me.mm.qs.myscript.")) {
                            String relativePath = importName.substring("me.mm.qs.myscript.".length());
                            String depFilePath = relativePath.replace('.', '/');
                            
                            if (!loaded.contains(depFilePath)) {
                                loaded.add(depFilePath);
                                // 继续递归
                                collectTransitiveDependencies(depFilePath, scriptRoot, loaded);
                            }
                        }
                    });
                }
            } catch (IOException e) {
                // 忽略错误
            }
        }
    }

    /**
     * Extract imports from script package and convert to load() statements
     * Skip files with @GlobalInstance annotation (they are defined in main.java)
     * 
     * 注意:由于BeanShell类定义可以重复加载,我们不再使用loaded列表
     * 每个文件都加载其直接依赖,让BeanShell自己处理重复
     */
    private static List<String> extractLoadStatements(CompilationUnit cu, Path projectRoot, Path scriptRoot) {
        List<String> loadStatements = new ArrayList<>();
        
        // 从 Main.java 获取脚本根目录的包名前缀 (e.g., "me.mm.qs.scripts.voice_converter")
        // 这样所有文件的 import 都相对于脚本根目录解析，而不是当前文件的 package
        String scriptPackagePrefix = "";
        Path mainFile = scriptRoot.resolve("Main.java");
        if (Files.exists(mainFile)) {
            try {
                FileInputStream mainIn = new FileInputStream(mainFile.toFile());
                ParseResult<CompilationUnit> mainParseResult = new JavaParser(new ParserConfiguration()).parse(mainIn);
                mainIn.close();
                if (mainParseResult.isSuccessful() && mainParseResult.getResult().get().getPackageDeclaration().isPresent()) {
                    scriptPackagePrefix = mainParseResult.getResult().get().getPackageDeclaration().get().getNameAsString() + ".";
                }
            } catch (IOException e) {
                // Fall back to current file's package
                if (cu.getPackageDeclaration().isPresent()) {
                    scriptPackagePrefix = cu.getPackageDeclaration().get().getNameAsString() + ".";
                }
            }
        } else if (cu.getPackageDeclaration().isPresent()) {
            // Fallback: 从当前文件的 package 推断（移除子包部分）
            String currentPackage = cu.getPackageDeclaration().get().getNameAsString();
            // 尝试找到脚本根包名（去掉 .utils, .constants 等子包）
            String[] parts = currentPackage.split("\\.");
            if (parts.length > 0) {
                // 假设脚本根包名是最后一个不是常见子包名的部分
                StringBuilder rootPackage = new StringBuilder();
                for (int i = 0; i < parts.length; i++) {
                    if (i > 0) rootPackage.append(".");
                    rootPackage.append(parts[i]);
                    // 如果遇到 scripts.xxx 后的第一个部分，就停止
                    if (i >= 3 && parts[i-1].equals("scripts")) {
                        break;
                    }
                }
                scriptPackagePrefix = rootPackage.toString() + ".";
            }
        }
        final String packagePrefix = scriptPackagePrefix;
        
        cu.getImports().forEach(importDecl -> {
            String importName = importDecl.getNameAsString();
            // 跳过 Globals 的静态导入 (这些会变成全局变量)
            if (importDecl.isStatic() && importName.startsWith("me.mm.qs.script.Globals")) {
                return;
            }
            // 检查是否来自当前脚本包 (非静态导入)
            if (!importDecl.isStatic() && !packagePrefix.isEmpty() && importName.startsWith(packagePrefix)) {
                // 提取相对路径: me.mm.qs.scripts.voice_converter.utils.MessageHandler -> utils/MessageHandler
                String relativePath = importName.substring(packagePrefix.length());
                String filePath = relativePath.replace('.', '/');
                Path javaFile = scriptRoot.resolve(filePath + ".java");
                
                // Check if the file has @GlobalInstance annotation - skip if it does
                if (Files.exists(javaFile)) {
                    try {
                        FileInputStream in = new FileInputStream(javaFile.toFile());
                        ParseResult<CompilationUnit> parseResult = new JavaParser(new ParserConfiguration()).parse(in);
                        in.close();
                        
                        if (parseResult.isSuccessful()) {
                            CompilationUnit importedCu = parseResult.getResult().get();
                            boolean hasGlobalInstance = importedCu.findAll(ClassOrInterfaceDeclaration.class).stream()
                                .anyMatch(classDecl -> classDecl.getAnnotations().stream()
                                    .anyMatch(anno -> anno.getNameAsString().equals("GlobalInstance")));
                            
                            // Skip files with @GlobalInstance - they are already in main.java
                            if (hasGlobalInstance) {
                                return;
                            }
                        }
                    } catch (IOException e) {
                        // Ignore and generate load statement anyway
                    }
                }
                
                // Generate load statement for non-@GlobalInstance files
                String loadCall = String.format("load(appPath + \"/%s.java\");", filePath);
                loadStatements.add(loadCall);
            }
        });
        
        return loadStatements;
    }
    
    /**
     * Scan ALL Java files in scriptRoot and generate global instance initialization for @GlobalInstance annotated classes
     * Returns list of class definitions and instance initialization statements
     * e.g. "class AudioDecoderState { ... }\nAudioDecoderState = new AudioDecoderState();"
     * Also populates globalInstanceMap for static field access conversion
     * 
     * IMPORTANT: Global instances are generated BEFORE load statements,
     * so imported files can access these global variables
     */
    private static List<String> extractGlobalInstances(CompilationUnit cu, Path scriptRoot) {
        List<String> instanceStatements = new ArrayList<>();
        PrettyPrinterConfiguration printerConfig = new PrettyPrinterConfiguration();
        printerConfig.setIndentSize(4);
        PrettyPrinter printer = new PrettyPrinter(printerConfig);
        
        // Scan entire scriptRoot directory for @GlobalInstance classes
        try {
            Files.walk(scriptRoot)
                .filter(path -> path.toString().endsWith(".java"))
                .forEach(javaFile -> {
                    try {
                        FileInputStream in = new FileInputStream(javaFile.toFile());
                        ParseResult<CompilationUnit> parseResult = new JavaParser(new ParserConfiguration()).parse(in);
                        in.close();
                        
                        if (parseResult.isSuccessful()) {
                            CompilationUnit importedCu = parseResult.getResult().get();
                            // Find classes with @GlobalInstance annotation
                            importedCu.findAll(ClassOrInterfaceDeclaration.class).forEach(classDecl -> {
                                classDecl.getAnnotations().stream()
                                    .filter(anno -> anno.getNameAsString().equals("GlobalInstance"))
                                    .findFirst()
                                    .ifPresent(globalInstanceAnno -> {
                                        String className = classDecl.getNameAsString();
                                        
                                        // Check if this class also has @ScriptMethods annotation
                                        boolean hasScriptMethods = classDecl.getAnnotations().stream()
                                            .anyMatch(anno -> anno.getNameAsString().equals("ScriptMethods"));
                                        
                                        // For constant classes (without @ScriptMethods), use renamed class name
                                        // For data classes (with @ScriptMethods), use original class name
                                        String varName = className;
                                        String actualClassName = className;
                                        
                                        if (!hasScriptMethods) {
                                            // This is a constant class, will be renamed as Prefix_ClassName
                                            String classRelativePath = scriptRoot.relativize(javaFile.getParent()).toString();
                                            if (!classRelativePath.isEmpty() && !classRelativePath.equals(".")) {
                                                String pathPrefix = classRelativePath.replace(File.separatorChar, '_')
                                                    .replace('-', '_');
                                                pathPrefix = pathPrefix.substring(0, 1).toUpperCase() + pathPrefix.substring(1);
                                                actualClassName = pathPrefix + "_" + className;
                                                // For constant classes, use renamed class name as variable name
                                                varName = actualClassName;
                                            }
                                        }
                                        
                                        // Store mapping: ClassName -> renamedClassName (for reference conversion)
                                        globalInstanceMap.put(className, varName);
                                        
                                        // Generate class definition with static fields
                                        StringBuilder classCode = new StringBuilder();
                                        
                                        // Always generate class definition for @GlobalInstance classes
                                        // (both @ScriptMethods and constant classes)
                                        classCode.append("class ").append(actualClassName).append(" {\n");
                                        
                                        // Add static fields (without 'static' keyword in BeanShell)
                                        classDecl.getFields().forEach(field -> {
                                            if (field.isStatic() && field.isPublic()) {
                                                field.getVariables().forEach(var -> {
                                                    String fieldName = var.getNameAsString();
                                                    var.getInitializer().ifPresent(init -> {
                                                        classCode.append("    ").append(fieldName).append(" = ");
                                                        classCode.append(printer.print(init)).append(";\n");
                                                    });
                                                });
                                            }
                                        });
                                        
                                        classCode.append("}\n");
                                        
                                        // Generate instance statement: VarName = new ActualClassName();
                                        String instanceStmt = classCode.toString() + varName + " = new " + actualClassName + "();";
                                        instanceStatements.add(instanceStmt);
                                    });
                            });
                        }
                    } catch (IOException e) {
                        // Ignore errors
                    }
                });
        } catch (IOException e) {
            System.err.println("Error scanning for @GlobalInstance classes: " + e.getMessage());
        }
        
        return instanceStatements;
    }

    /**
     * Generate script metadata files (desc.txt and info.prop)
     */
    private static void generateScriptMetadata(Path mainFile, Path scriptDir, Path distRoot, Path scriptRoot, String scriptName) throws IOException {
        FileInputStream in = new FileInputStream(mainFile.toFile());
        ParseResult<CompilationUnit> parseResult = new JavaParser(new ParserConfiguration()).parse(in);
        in.close();
        
        if (!parseResult.isSuccessful()) {
            System.err.println("Warning: Failed to parse Main.java for metadata extraction");
            return;
        }
        
        CompilationUnit cu = parseResult.getResult().get();
        
        // Find class with @ScriptInfo annotation
        for (ClassOrInterfaceDeclaration classDecl : cu.findAll(ClassOrInterfaceDeclaration.class)) {
            for (AnnotationExpr annotation : classDecl.getAnnotations()) {
                if (annotation.getNameAsString().equals("ScriptInfo")) {
                    generateMetadataFiles(annotation, scriptDir, distRoot, scriptRoot);
                    return;
                }
            }
        }
        
        System.out.println("Warning: No @ScriptInfo annotation found, skipping metadata generation");
    }
    
    /**
     * Get script name from @ScriptInfo annotation
     */
    private static String getScriptName(Path mainFile) throws IOException {
        if (!Files.exists(mainFile)) {
            return "UnnamedScript";
        }
        
        FileInputStream in = new FileInputStream(mainFile.toFile());
        ParseResult<CompilationUnit> parseResult = new JavaParser(new ParserConfiguration()).parse(in);
        in.close();
        
        if (!parseResult.isSuccessful()) {
            return "UnnamedScript";
        }
        
        CompilationUnit cu = parseResult.getResult().get();
        for (ClassOrInterfaceDeclaration classDecl : cu.findAll(ClassOrInterfaceDeclaration.class)) {
            for (AnnotationExpr annotation : classDecl.getAnnotations()) {
                if (annotation.getNameAsString().equals("ScriptInfo")) {
                    String name = getAnnotationValue(annotation, "name", "UnnamedScript");
                    return name;
                }
            }
        }
        
        return "UnnamedScript";
    }
    
    /**
     * Generate desc.txt and info.prop from @ScriptInfo annotation
     */
    private static void generateMetadataFiles(AnnotationExpr scriptInfo, Path scriptDir, Path distRoot, Path scriptRoot) throws IOException {
        String name = getAnnotationValue(scriptInfo, "name", "Untitled Script");
        String author = getAnnotationValue(scriptInfo, "author", "Unknown");
        String version = getAnnotationValue(scriptInfo, "version", "1.0");
        String description = getAnnotationValue(scriptInfo, "description", "");
        String tags = getAnnotationValue(scriptInfo, "tags", "功能扩展");
        String idFromAnnotation = getAnnotationValue(scriptInfo, "id", "");
        
        // Generate or load script ID from source script folder (each script has its own ID)
        Path scriptIdFile = scriptRoot.resolve(".script-id");
        String scriptId;
        
        if (Files.exists(scriptIdFile)) {
            // Load existing ID from source directory
            scriptId = Files.readString(scriptIdFile).trim();
            System.out.println("Using existing script ID: " + scriptId);
        } else {
            // Generate new ID (32-char hex string like MD5)
            if (!idFromAnnotation.isEmpty()) {
                scriptId = idFromAnnotation;
            } else {
                scriptId = generateScriptId(name, author);
            }
            // Save to source directory so it persists across builds
            Files.writeString(scriptIdFile, scriptId);
            System.out.println("Generated new script ID: " + scriptId);
        }
        
        // Generate desc.txt in script folder
        Path descFile = scriptDir.resolve("desc.txt");
        Files.writeString(descFile, description);
        System.out.println("Generated: " + descFile);
        
        // Generate info.prop in script folder
        Path infoPropFile = scriptDir.resolve("info.prop");
        try (FileWriter writer = new FileWriter(infoPropFile.toFile())) {
            // Write properties in specific order
            writer.write("name=" + name + "\n");
            writer.write("type=1\n");
            writer.write("version=" + version + "\n");
            writer.write("author=" + author + "\n");
            writer.write("id=" + scriptId + "\n");
            writer.write("date=" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-d")) + "\n");
            writer.write("tags=" + tags + "\n");
        }
        System.out.println("Generated: " + infoPropFile);
    }
    
    /**
     * Extract annotation value
     */
    private static String getAnnotationValue(AnnotationExpr annotation, String key, String defaultValue) {
        if (annotation.isSingleMemberAnnotationExpr()) {
            return annotation.asSingleMemberAnnotationExpr()
                    .getMemberValue()
                    .toString()
                    .replaceAll("^\"|\"$", "");
        }
        
        if (annotation.isNormalAnnotationExpr()) {
            for (MemberValuePair pair : annotation.asNormalAnnotationExpr().getPairs()) {
                if (pair.getNameAsString().equals(key)) {
                    String value = pair.getValue().toString();
                    
                    // Handle concatenated strings: "text1" + "text2" + ...
                    if (value.contains(" + ")) {
                        // Split by + and clean each part
                        String[] parts = value.split("\\s*\\+\\s*");
                        StringBuilder result = new StringBuilder();
                        for (String part : parts) {
                            String cleaned = part.trim().replaceAll("^\"|\"$", "");
                            result.append(cleaned);
                        }
                        value = result.toString();
                    } else {
                        // Single string, just remove quotes
                        value = value.replaceAll("^\"|\"$", "");
                    }
                    
                    // Handle escape sequences
                    value = value.replace("\\n", "\n");
                    value = value.replace("\\t", "\t");
                    
                    return value;
                }
            }
        }
        
        return defaultValue;
    }

    /**
     * Generate a unique 32-character script ID (MD5-like format)
     * Based on script name, author, and timestamp to ensure uniqueness
     */
    private static String generateScriptId(String name, String author) {
        try {
            // Combine multiple sources to ensure uniqueness:
            // 1. Script name and author (for consistency)
            // 2. Current timestamp (for uniqueness)
            // 3. Random UUID (for collision prevention)
            String seed = name + "|" + author + "|" + 
                         System.currentTimeMillis() + "|" + 
                         UUID.randomUUID().toString();
            
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(seed.getBytes("UTF-8"));
            
            // Convert to 32-char hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException | java.io.UnsupportedEncodingException e) {
            // Fallback to UUID-based ID if MD5 is not available
            return UUID.randomUUID().toString().replace("-", "");
        }
    }

    /**
     * 收集导入文件中的类名映射（已由 collectAllClassNameMappings 替代）
     */
    private static void collectClassNameMappings(CompilationUnit cu, Path scriptRoot) {
        // 已由 collectAllClassNameMappings 在启动时预先收集，此处无需操作
    }
    
    /**
     * 扫描整个 myscript 目录，收集所有需要重命名的类
     * 包括普通类和 @GlobalInstance 类
     * 跳过 Main 类
     */
    private static void collectAllClassNameMappings(Path scriptRoot) {
        try {
            Files.walk(scriptRoot)
                .filter(path -> path.toString().endsWith(".java"))
                .forEach(javaFile -> {
                    try {
                        FileInputStream in = new FileInputStream(javaFile.toFile());
                        ParseResult<CompilationUnit> parseResult = new JavaParser(new ParserConfiguration()).parse(in);
                        in.close();
                        
                        if (parseResult.isSuccessful()) {
                            CompilationUnit cu = parseResult.getResult().get();
                            cu.findAll(ClassOrInterfaceDeclaration.class).forEach(classDecl -> {
                                String className = classDecl.getNameAsString();
                                
                                // 跳过 Main 类
                                if (className.equals("Main")) {
                                    return;
                                }
                                
                                boolean hasGlobalInstance = classDecl.getAnnotations().stream()
                                    .anyMatch(anno -> anno.getNameAsString().equals("GlobalInstance"));
                                
                                // 获取文件相对于 scriptRoot 的路径
                                String relativePath = scriptRoot.relativize(javaFile.getParent()).toString();
                                
                                // 处理 @GlobalInstance 类 - 添加到 globalInstanceMap
                                if (hasGlobalInstance) {
                                    String actualClassName = className;
                                    String varName = className;
                                    
                                    // 如果在子目录中，需要重命名
                                    if (!relativePath.isEmpty() && !relativePath.equals(".")) {
                                        String pathPrefix = relativePath.replace(File.separatorChar, '_')
                                            .replace('-', '_');
                                        pathPrefix = pathPrefix.substring(0, 1).toUpperCase() + pathPrefix.substring(1);
                                        actualClassName = pathPrefix + "_" + className;
                                        varName = actualClassName;
                                    }
                                    
                                    globalInstanceMap.put(className, varName);
                                    return;
                                }
                                
                                // 处理普通类 - 只有在子目录中的类才需要重命名
                                if (!relativePath.isEmpty() && !relativePath.equals(".")) {
                                    String pathPrefix = relativePath.replace(File.separatorChar, '_')
                                        .replace('-', '_');
                                    pathPrefix = pathPrefix.substring(0, 1).toUpperCase() + pathPrefix.substring(1);
                                    String uniqueClassName = pathPrefix + "_" + className;
                                    classNameMap.put(className, uniqueClassName);
                                }
                            });
                        }
                    } catch (IOException e) {
                        // 忽略错误
                    }
                });
        } catch (IOException e) {
            System.err.println("Error scanning for class name mappings: " + e.getMessage());
        }
    }
    
    /**
     * 替换代码中的类名引用
     * 例如: new MyMessageType() -> new Constants_MyMessageType()
     * 例如: MyMessageType type = ... -> type = ... (移除类型声明)
     * 例如: ArrayList<MyMessageType> list = ... -> list = ...
     */
    private static String replaceClassNameReferences(String code) {
        String result = code;
        
        // 合并 classNameMap 和 globalInstanceMap 进行替换
        Map<String, String> allMappings = new HashMap<>();
        allMappings.putAll(classNameMap);
        allMappings.putAll(globalInstanceMap);
        
        for (Map.Entry<String, String> entry : allMappings.entrySet()) {
            String originalName = entry.getKey();
            String mappedName = entry.getValue();
            
            // 1. 替换 new ClassName() 为 new MappedName()
            result = result.replaceAll(
                "\\bnew\\s+" + originalName + "\\s*\\(",
                "new " + mappedName + "("
            );
            
            // 2. 替换静态字段访问: ClassName.fieldName -> MappedName.fieldName
            // 使用负向预查确保不匹配字符串中的路径（如 "/utils/ClassName.java"）
            result = result.replaceAll(
                "(?<![/\"'])\\b" + originalName + "\\.",
                mappedName + "."
            );
            
            // 3. 替换变量声明: ClassName varName = 为 varName =
            // 匹配: ClassName varName =
            result = result.replaceAll(
                "\\b" + originalName + "\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\s*=",
                "$1 ="
            );
            
            // 4. 替换泛型中的类名: ArrayList<ClassName> -> ArrayList
            // 然后在下面统一移除泛型声明
            result = result.replaceAll(
                "<\\s*" + originalName + "\\s*>",
                ""
            );
        }
        
        // 5. 额外处理：移除所有含有映射类名的泛型声明
        for (String mappedName : allMappings.values()) {
            result = result.replaceAll(
                "<\\s*" + mappedName + "\\s*>",
                ""
            );
        }
        
        return result;
    }

    /**
     * Convert a Type to Object if it's a BeanShell type
     */
    private static void convertTypeToObject(Type type) {
        if (type.isClassOrInterfaceType()) {
            ClassOrInterfaceType classType = type.asClassOrInterfaceType();
            String typeName = classType.getNameAsString();
            
            // Check if this is a BeanShell type
            if (BEANSHELL_TYPES.contains(typeName)) {
                classType.setName("Object");
                classType.setTypeArguments((com.github.javaparser.ast.NodeList) null);
            }
            
            // Handle generic type arguments (e.g., ArrayList<MessageData>)
            if (classType.getTypeArguments().isPresent()) {
                classType.getTypeArguments().get().forEach(typeArg -> {
                    if (typeArg.isClassOrInterfaceType()) {
                        ClassOrInterfaceType argType = typeArg.asClassOrInterfaceType();
                        if (BEANSHELL_TYPES.contains(argType.getNameAsString())) {
                            argType.setName("Object");
                            argType.setTypeArguments((com.github.javaparser.ast.NodeList) null);
                        }
                    }
                });
            }
        }
    }
}