# Starlords 0.3.78 historical baseline

**Policy update:** old-save compatibility is optional and must not obstruct the overhaul. Normal builds check runtime validity and replace the repository's `jars/StarLords.jar`. The historical comparison below runs only when explicitly requested with `python tools/build.py verify --check-legacy`.

Captured from repository revision `d5cd42288ab7fd0b194266d51b206ed1f43edc8c`, before implementation changes to Java or gameplay data.

| Property | Original value |
| --- | --- |
| Mod ID | `starlords` |
| Declared game version | `0.98a-RC5` |
| Entry point | `starlords.plugins.LordsModPlugin` |
| Runtime archive | `jars/StarLords.jar` |
| Archive SHA-256 | `83ef185f96b81b1acc13d97ecfaff596544e2b004bdc123fc1ae016d3d02dcda` |
| Source files | 356 |
| Packaged classes | 408 |
| Class-file major version | 60 (Java 16) |
| Packaged classes without a corresponding source file | 0 |

The `SourceFile` attribute is used to associate compiled classes with source files. In particular, the package-private `CurrentAction_*` classes are declared in `DialogRule_currentAction.java`; a filename-only comparison incorrectly treats them as missing source.

`baseline-0.3.78.json` captures class names, superclasses, interfaces, fields, method descriptors, generic field signatures, enum ordering, compiler target, source hashes, and the source-level persistence identifiers found by the scanner.

## Optional historical comparisons

- Existing class names and superclass/interface relationships.
- Existing non-static, non-transient, non-synthetic fields, including private fields that XStream may persist. Removal, type/generic-type changes, or conversion to static/transient are rejected.
- Public/protected fields and methods, including visibility and static/instance changes.
- Existing enum constants and their order; new constants may be appended.
- Java class-file target and absence of preview bytecode.
- Existing literal XStream aliases (`x.alias`), recognized static string memory-key declarations, and literal `persistentData.put` keys.

The optional comparison is conservative and reports even removal of an unused old field. Those findings do not block ordinary builds or require migrations. Record a new-campaign requirement when a redesign needs one. Plugin registration and supported bytecode remain mandatory runtime checks, using the current metadata rather than requiring the original class names.

## Limits

These are structural regression checks, not proof of save compatibility or unchanged gameplay. The source scanner recognizes the existing declaration/call patterns; it is not a complete Java parser and does not discover dynamically assembled keys. Method bodies, initialization of newly added fields on old saves, custom serialization hooks, resources, and runtime behavior need separate review and testing. Source hashes record provenance, rather than prohibiting later edits.

The original JAR and a new compiler's output need not be byte-for-byte equal. The reproducibility check compares two fresh builds made with the same recorded inputs. It does not imply that the original source and runtime have identical behavior.

Campaign acceptance uses a new campaign and a save/reload within that build; older-version saves are optional. [TESTING.md](../TESTING.md) contains the short Console Commands/Nexerelin procedure. The later presentation phases add the context-specific scenarios listed in the accepted design.
