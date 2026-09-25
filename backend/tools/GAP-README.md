# GAP（General Anti-Pattern detector）

GAP(General Anti-Pattern detector)  is a tool for detecting anti-patterns in the software system. 

## Supported Language

|  Language  | Maximum Version |
|:----------:|:---------------:|
|    Java    |       17        |

## Input

- `Project name` (Optional, if not specified, defaults to Example)
- `Dependency model file` (Required)
- `Rule files` (Seven files: MH-rule.yml, AWD-rule.yml, CD-rule.yml, CH-rule.yml, FE-rule.yml, SS-rule.yml, DC-rule.yml).

## Output

- **Seven** anti-pattern detection results in the directory named `project-gap-out`in the current working directory.
  - `projectName-AWD.json`
  - `projectName-CD.json`
  - `projectName-CH.json`
  - `projectName-DC.json`
  - `projectName-FE.json`
  - `projectName-MH.json`
  - `projectName-SS.json`


## Usage

### 1. Prepare the extraction of code entity dependencies or relationships json

Use [ENRE-Java](https://github.com/xjtu-enre/ENRE-java) tool and get the dependency model file.

### 2. Prepare the executable jar

The released jar of General  Anti-Pattern Detector is named as GAP-1.0.jar (in the jar directory)

### 3. Set up Java environment

    To execute GAP-1.0.jar, you should set up Java environment: at least JDK 11 version. 

### 4. CMD usage

**Attention** : The JAR file should be in the same working directory as the directory containing the rule files (i.e., the "**rule**" directory).

The usage command is:

```cmd
java -jar GAP-1.0.jar detect [-hV] [-awd=<AWDRuleFile>]
                                    [-cd=<CDRuleFile>]
                                    [-ch=<CHRuleFile>]
                                    -d=<dependencyModelFilePath>
                                    [-dc=<DCRuleFile>] [-fe=<FERuleFile>]
                                    [-mh=<MHRuleFile>] [-n=<projectName>]
                                    [-om=<outputMode>] [-ss=<SSRuleFile>]
```

#### Parameters

```bash
-awd, --AWDRule=<AWDRuleFile> Optional, Abstraction Without Decoupling (AWD) detection rule file path; if not specified, defaults to the AWD-rule.yml file in the directory named "rule" in the current working directory.

-cd, --CDRule=<CDRuleFile> Optional, Cyclic Dependency (CD) detection rule file path; if not specified, defaults to the CD-rule.yml file in the directory named "rule" in the current working directory.

-ch, --CHRule=<CHRuleFile> Optional, Cyclic Hierarchy (CH) detection rule file path; if not specified, defaults to the CH-rule.yml file in the directory named "rule" in the current working directory.

-d, --dependency=<dependencyModelFilePath> Required, dependency model result file path based on the ENRE-Java.
	  -dc, --DCRule=<DCRuleFile> Optional, Data Clumps(DC) detection rule file path; if not specified, defaults to the DC-rule.yml file in the directory named "rule" in the current working directory.
      -fe, --FERule=<FERuleFile> Optional, Feature Envy(FE) detection rule file path; if not specified, defaults to the FE-rule.yml file in the directory named "rule" in the current working directory.

-mh, --MHRule=<MHRuleFile> Optional, Multipath Hierarchy (MH) detection rule file path; if not specified, defaults to the MH-rule.yml file in the directory named "rule" in the current working directory.

-n, --name=<projectName> Optional, the project name for detection; if not specified, defaults to "Default".
    -om, --outputMode=<outputMode> Optional, anti-pattern output result mode, with the following three modes: detailed (full output mode), benchmark (dataset mode), simple (basic mode); if not specified, defaults to detailed.
	-ss, --SSRule=<SSRuleFile> Optional, Shotgun Surgery (SS) detection rule file path; if not specified, defaults to the SS-rule.yml file in the directory named "rule" in the current working directory.
```

#### Example

```bash
java -jar GAP-1.0.jar detect -n Example -d Example-out.json -om benchmark
```

## Function

    Output a list of anti-pattern categories and specific list in the software system.

## Anti Patterns Category

### Abstraction without Decoupling

#### definition

This anti-pattern describes a situation where a client class uses a service represented as an abstract type, but also a concrete implementation of this service.

### Cyclic Dependency

#### definition

This anti-pattern arises when two or more architecture components depend on each other directly or indirectly. For instance, if Module A depends on Module B, and Module B depends on Module A, there is a cyclic dependency. This can lead to difficulties during compilation, maintenance, and testing since it's unclear which module should be compiled or executed first.

### Cyclic Hierarchy

#### definition

This anti-pattern refers to the mistake of directly referencing a subtype from a supertype. Therefore, it implies that there are circular dependencies between the namespaces containing subtype and supertype.

### Data Clumps

#### definition

"Data Clumps" is an anti-pattern  that refers to a situation where groups of variables (or data elements) are frequently used together throughout the codebase. 

#### details

These variables tend to appear together in parameter lists, method signatures, or function calls. This anti-pattern that related data elements should be grouped into a single data structure, such as a class or a data object, rather than being scattered throughout the codebase.

The presence of data clumps indicates a lack of abstraction and can make the code less maintainable and readable. By creating a dedicated data structure to encapsulate related data elements, the code becomes more organized, easier to understand, and less prone to errors. This also enhances the reusability and maintainability of the codebase.

### Feature Envy

#### definition

`Feature Envy` is an anti-pattern that occurs when a particular module or class in a software system excessively uses methods or properties of another module or class. In other words, it's when one class seems more interested in the data or functionality of another class than in its own. 

#### details

This can indicate a design problem where responsibilities are not properly distributed between classes, leading to tighter coupling and reduced maintainability.

In this situation, it might be a better approach to move the code that's making use of another class's features into the class that owns those features. This can help improve encapsulation, reduce dependencies, and make the codebase more modular and readable.

### Multipath Hierarchy

#### definition

    This anti-pattern arises when there are multiple inheritance paths connecting subtypes with their super-types or a concrete class with their abstractions (abstract classes or interfaces)

### Shotgun Surgery

#### definition

`Shotgun Surgery` is an anti-pattern that refers to a situation where a single code change or a new feature requires modifications to be made in multiple places across the codebase. 

#### details

In essence, it's when a small change has a widespread impact and requires many individual changes to be made in various parts of the code. This can make maintenance and bug-fixing more complex and error-prone, as a change needs to be replicated in multiple places.

The term "shotgun surgery" suggests that you need to "shoot" changes all over the codebase, much like the spread of shotgun pellets. To address this issue, it's generally recommended to refactor the code to make it more modular and cohesive. By grouping related functionality together and reducing tight coupling, you can reduce the need for widespread changes and make the codebase more adaptable to future changes.
