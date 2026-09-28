核心工具：PMD

版本：6.15.0

官方文档：https://pmd.sourceforge.io/pmd-6.15.0/index.html

# PMD简介

## 功能

PMD是一款可扩展的跨语言的静态代码检查工具，它可以发现普通的编程缺陷，例如未使用的变量、空的catch块、不必要的对象创建等。它还包含CPD检测器（copy-paste-detector），用于检测代码中的重复代码。

## 运行方式

### 插件方式

下载：File -> Settings -> Plugins -> Marketplace 搜索 “PMDPlugin” ，下载插件。 使用方法：在代码编辑框或Project 窗口的文件夹、包、文件右键，选择“Run PMD”->“Pre Defined”->“All”，对指定的文件夹、包、文件进行分析，分析结果在控制台输出。

### maven方式

在 pom.xml 中添加 reports 元素：

```XML
<project>
    ...
    <reporting>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-pmd-plugin</artifactId>
            </plugin>
        </plugins>
    </reporting>
    ...
</project>
```

如果需要在构建项目时自动运行 PMD，让构建过程中 PMD 发现了一些违规行为。

```XML
<project>
  ...
  <build>
      <plugins>
          <plugin>
              <groupId>org.apache.maven.plugins</groupId>
              <artifactId>maven-pmd-plugin</artifactId>
              <configuration>
                  <failOnViolation>true</failOnViolation> 
                  <!-- this is actually true by default, but can be disabled -->
                  <printFailingErrors>true</printFailingErrors>
              </configuration>
              <executions>
                  <execution>
                      <goals>
                          <goal>check</goal>
                      </goals>
                  </execution>
              </executions>
          </plugin>
      </plugins>
  </build>
  ...
</project>
```

指定规则集，需添加

```XML
<reporting>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-pmd-plugin</artifactId>
            <configuration>
                <rulesets>
                    <ruleset>/rulesets/java/braces.xml</ruleset>
                    <ruleset>/rulesets/java/naming.xml</ruleset>
                    <ruleset>d:\rulesets\strings.xml</ruleset>
                    <ruleset>http://localhost/design.xml</ruleset>
                </rulesets>
            </configuration>
        </plugin>
    </plugins>
</reporting>
```

启动增量分析

```XML
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-pmd-plugin</artifactId>
    <configuration>
        <analysisCache>true</analysisCache> <!-- enable incremental analysis -->
        <analysisCacheLocation>${project.build.directory}/pmd/pmd.cache</analysisCacheLocation> <!-- Optional: points to this location by default -->
    </configuration>
</plugin>
```

其他配置

```XML
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-pmd-plugin</artifactId>
    <configuration>
        <linkXRef>true</linkXRef>
        <sourceEncoding>ISO-8859-1</sourceEncoding>
        <minimumTokens>30</minimumTokens>
        <targetJdk>1.4</targetJdk>
    </configuration>
</plugin>
```

### **命令行方式** 

打包后的pmd为zip格式，位于pmd-dist\target目录下，打包后的目录结构如下：

![img](https://nioxpkg1lwd.feishu.cn/space/api/box/stream/download/asynccode/?code=ZDkxZmU5NTk2MDk5Y2ZlOTcyMWIxNjJmY2Q5ZmJkYmFfVE1iNHNFR21rVHRQWWRkQWJ4QnpteTJzZjZ4Q3NvVGFfVG9rZW46VEl0bGJGUjVIb0s3WUp4MDR5SGNUQXBBblVGXzE3MzU5OTIzNDE6MTczNTk5NTk0MV9WNA)

bin目录下的脚本包含：

![img](https://nioxpkg1lwd.feishu.cn/space/api/box/stream/download/asynccode/?code=MjdmMDFhOTc4YjRkNjZiMTA5ZWNlNmVkN2MyYmM4NzZfR0VTeVlwSnFiUGMwOHMwY0dsRksxRDhTaUFodk5RQW5fVG9rZW46R2RjUWJ5YmhvbzJPcVZ4aVdNaWN3R1RRbndkXzE3MzU5OTIzNDE6MTczNTk5NTk0MV9WNA)

.bat结尾用于windows系统，run.sh用于linux系统

bgastviewer.bat可以查看ast

![img](https://nioxpkg1lwd.feishu.cn/space/api/box/stream/download/asynccode/?code=MmQ5MzcyZmY2Y2YxYWJmOTk5YmEyNGI5ZGQ0ZGY0YTBfRWFzczFwNEJ5blU2NWxqV2NCSkxyQ1F3MWFManhkeW1fVG9rZW46S0hremJyeU1pbzJON3B4MDFVcmNndmtnbkxmXzE3MzU5OTIzNDE6MTczNTk5NTk0MV9WNA)

cpd.bat用于检测重复代码，CPD 支持 Java、JSP、C、C++、C#、Fortran 和 PHP 等语言

有两个必需的参数：

- `--files <path>`：要分析的源路径。这可以是文件名、 目录或包含源代码的 jar 或 zip 文件。
- `--minimum-tokens <number>`：应报告为重复项的最小令牌长度。

例如：

```XML
cpd.bat --minimum-tokens 100 --files /home/me/src

  Found a 7 line (110 tokens) duplication in the following files:
  Starting at line 579 of /home/me/src/test/java/foo/FooTypeTest.java
  Starting at line 586 of /home/me/src/test/java/foo/FooTypeTest.java

          assertEquals(Boolean.TYPE, expressions.get(index++).getType());
          assertEquals(Boolean.TYPE, expressions.get(index++).getType());
          assertEquals(Boolean.TYPE, expressions.get(index++).getType());
          assertEquals(Boolean.TYPE, expressions.get(index++).getType());
          assertEquals(Boolean.TYPE, expressions.get(index++).getType());
          assertEquals(Boolean.TYPE, expressions.get(index++).getType());
          assertEquals(Boolean.TYPE, expressions.get(index++).getType());
```

cpdgui.bat检测重复代码的可视化界面

![img](https://nioxpkg1lwd.feishu.cn/space/api/box/stream/download/asynccode/?code=MTNiNDEwZWVhM2JiOTI0OWI2MTJmYTQ4ZjViZjRkMjVfM3pSeXZYenY1WkZlUFptQ3J1WlpFa05xY1MxQ1UxeFhfVG9rZW46T1MxV2JGMUMxb0VDcDl4WUdjcmNVM3NIbjliXzE3MzU5OTIzNDE6MTczNTk5NTk0MV9WNA)

designer.bat用于设计自定义规则

![img](https://nioxpkg1lwd.feishu.cn/space/api/box/stream/download/asynccode/?code=YTAyMmUyM2UwYTI3MzNhZTZmMTQwOGVlYTY0NGQxNGVfRzIyOHBVc1NrRVBFZUtyRUNtQ1J3aU1KcWc0TXFCRGxfVG9rZW46T2h0MmJJYlZIb3ZiZ2d4QVJweGMycjZhbjBjXzE3MzU5OTIzNDE6MTczNTk5NTk0MV9WNA)

pmd.bat用于启动规则检测，相关的参数如下：

| Option                                               | Description                                                  | Default value | Applies to |
| ---------------------------------------------------- | ------------------------------------------------------------ | ------------- | ---------- |
| `-rulesets <refs>` `-R <refs>`                       | **Required** Comma-separated list of ruleset or rule references. |               |            |
| `-dir <path>` `-d <path>`                            | **Required** Root directory for the analyzed sources.        |               |            |
| `-format <format>` `-f <format>`                     | Output format of the analysis report. The available formats are described [here](https://pmd.sourceforge.io/pmd-6.15.0/pmd_userdocs_cli_reference.html#available-report-formats). | `text`        |            |
|                                                      |                                                              |               |            |
| `-auxclasspath <cp>`                                 | Specifies the classpath for libraries used by the source code. This is used to resolve types in source files. Alternatively, a `file://` URL to a text file containing path elements on consecutive lines can be specified. |               | Java       |
| `-benchmark` `-b`                                    | Enables benchmark mode, which outputs a benchmark report upon completion. The report is sent to standard error. | `false`       |            |
| `-cache <filepath>`                                  | Specify the location of the cache file for incremental analysis. This should be the full path to the file, including the desired file name (not just the parent directory). If the file doesn't exist, it will be created on the first run. The file will be overwritten on each run with the most up-to-date rule violations. This can greatly improve analysis performance and is **highly recommended**. |               |            |
| `-debug` `-verbose` `-D` `-V`                        | Debug mode. Prints more log output.                          | `false`       |            |
| `-encoding <charset>` `-e <charset>`                 | Specifies the character set encoding of the source code files PMD is reading. The valid values are the standard character sets of `java.nio.charset.Charset`. | `UTF-8`       |            |
| `-failOnViolation <bool>` `--failOnViolation <bool>` | Specifies whether PMD exits with non-zero status if violations are found. By default PMD exits with status 4 if violations are found. Disable this feature with `-failOnViolation false` to exit with 0 instead and just output the report. | `true`        |            |
| `-filelist <filepath>`                               | Path to file containing a comma delimited list of files to analyze. If this is given, then you don't need to provide `-dir`. |               |            |
| `-ignorelist <filepath>`                             | Path to file containing a comma delimited list of files to ignore. This option can be combined with `-dir` and `-filelist`. This ignore list takes precedence over any files in the filelist. |               |            |
| `-help` `-h` `-H`                                    | Display help on usage.                                       | `false`       |            |
| `-language <lang>` `-l <lang>`                       | Specify the language PMD should use.                         |               |            |
| `-minimumpriority <num>` `-min <num>`                | Rule priority threshold; rules with lower priority than configured here won't be used. | `5`           |            |
| `-norulesetcompatibility`                            | Disables the ruleset compatibility filter. The filter is active by default and tries to automatically "fix" old ruleset files with old rule names | `false`       |            |
| `-no-cache`                                          | Explicitly disables incremental analysis. This switch turns off suggestions to use Incremental Analysis, and causes the `-cache` option to be discarded if it is provided. | `false`       |            |
| `-property <name>=<value>` `-P <name>=<value>`       | Specifies a property for the report renderer. The option can be specified several times. | `[]`          |            |
| `-reportfile <path>` `-r <path>`                     | Path to a file in which the report output will be sent. By default the report is printed on standard output. |               |            |
| `-shortnames`                                        | Prints shortened filenames in the report.                    | `false`       |            |
| `-showsuppressed`                                    | Causes the suppressed rule violations to be added to the report. | `false`       |            |
| `-stress` `-S`                                       | Performs a stress test.                                      | `false`       |            |
| `-suppressmarker <marker>`                           | Specifies the comment token that marks lines which PMD should ignore. | `NOPMD`       |            |
| `-threads <num>` `-t <num>`                          | Sets the number of threads used by PMD. Set threads to `0` to disable multi-threading processing. | `1`           |            |
| `-uri <uri>` `-u <uri>`                              | Database URI for sources. If this is given, then you don't need to provide `-dir`. |               | PLSQL      |
| `-version <version>` `-v <version>`                  | Specify the version of a language PMD should use.            |               |            |

例如：

```XML
-d
D:\liantong\CQM_Test\项目3_zqyyfjzone-master-master_1021_20231130\zqyyfjzone-master-master_1021_20231130
-f
xml
-R
D:\liantong\pmd-pmd_releases-6.15.0\pmd-java\src\main\resources\rulesets\java\CQM-HY.xml
-r
D:\liantong\CQM_Test\项目3_zqyyfjzone-master-master_1021_20231130\zqyyfjzone-master-master_1021_20231130.xml
-D
-t
0
```

run.sh用于linux系统，如：

```XML
run.sh pmd -d ../../../src/main/java/ -f text -R rulesets/java/quickstart.xml
```

## pmd的返回值

| Exit Status | Corresponding explanations                                   |
| ----------- | ------------------------------------------------------------ |
| 0           | Everything is fine, no violations found                      |
| 1           | Couldn't understand command-line parameters or PMD exited with an exception |
| 4           | At least one violation has been detected, unless `-failOnViolation false` is set. |

## 支持的语言

| **支持语言**               | **内置规则数量** | **新增规则**                    |
| -------------------------- | ---------------- | ------------------------------- |
| apex（Salesforce Apex)）   | 52               | --                              |
| jsp                        | 12               | --                              |
| plsql                      | 21               | --                              |
| vf(Salesforce VisualForce) | 2                | --                              |
| vm(Apache Velocity)        | 9                | --                              |
| java                       | 358              | 新增49条P3C规则和64条自定义规则 |
| ecmascript(JavaScript)     | 17               | --                              |
| xml（xml和xsl）            | 5                | --                              |

## 可得到的报告格式

| 格式            | 解释                                                         | 补充                                                         |
| --------------- | ------------------------------------------------------------ | ------------------------------------------------------------ |
| **codeclimate** | Renderer for Code Climate JSON format                        |                                                              |
| **csv**         | Comma-separated values tabular format                        | Properties:problem: Include problem column. Default: true.package: Include package column. Default: true.file: Include file column. Default: true.priority: Include priority column. Default: true.line: Include line column. Default: true.desc: Include description column. Default: true.ruleSet: Include Rule set column. Default: true.rule: Include Rule column. Default: true. |
| **emacs**       | GNU Emacs integration                                        |                                                              |
| **html**        | HTML format                                                  | Properties:linePrefix: Prefix for line number anchor in the source file.linkPrefix: Path to HTML source. |
| **ideaj**       | IntelliJ IDEA integration                                    | Properties:classAndMethodName: Class and method name, pass when processing a directory.`.method`sourcePath:fileName: |
| **summaryhtml** | Summary HTML format                                          | Properties:linePrefix: Prefix for line number anchor in the source file.linkPrefix: Path to HTML source. |
| **text**        | Text format                                                  |                                                              |
| **textcolor**   | Text format, with color support (requires ANSI console support, e.g. xterm, rxvt, etc.) | Properties:color: Enables colors with anything other than or . Default: yes.`false0` |
| **textpad**     | TextPad integration                                          |                                                              |
| **vbhtml**      | Vladimir Bossicard HTML format                               |                                                              |
| **xml**         | XML format                                                   | Properties:encoding: XML encoding format, defaults to UTF-8. |
| **xslt**        | XML with a XSL transformation applied                        | Properties:encoding: XML encoding format, defaults to UTF-8.xsltFilename: The XSLT file name. |
| **yahtml**      | Yet Another HTML format                                      | Properties:outputDir: Output directory.                      |