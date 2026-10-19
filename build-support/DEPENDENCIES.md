# Phase 1 dependency provenance

The verified build uses Oracle JDK **17.0.3.1**, Python **3.10**, and game libraries from the user's **Starsector 0.98a-RC8** installation. The game version was read from `com.fs.starfarer.Version.versionOnly` in `starfarer_obf.jar`. Starlords declares `0.98a-RC5`; no metadata change was required to compile against RC8.

The exact ordered classpath and individual JAR hashes are in [`build.lock.json`](../build.lock.json). Local paths are in the ignored `build.local.json`. This records a tested build combination, not a new minimum-version declaration for players.

The optional integration mods were not installed in the supplied game directory. The following official release downloads were obtained for compilation only, under the ignored `build/dependencies/` directory:

| Dependency | Version | Official artifact | JAR used |
| --- | --- | --- | --- |
| Nexerelin | 0.12.2d | [Release ZIP](https://github.com/Histidine91/Nexerelin/releases/download/v0.12.2d/Nexerelin_0.12.2d.zip) | `Nexerelin/jars/ExerelinCore.jar` |
| LunaLib | 2.0.5 | [Release ZIP](https://github.com/Lukas22041/LunaLib/releases/download/2.0.5/LunaLib.zip) | `LunaLib/jars/LunaLib.jar` |
| Console Commands | 4.0.9 | [Release ZIP](https://github.com/LazyWizard/console-commands/releases/download/4.0.9/ConsoleCommands.zip) | `Console Commands/jars/lw_Console.jar` |
| Second in Command | 2.0.0 | [Release ZIP](https://github.com/Lukas22041/SecondInCommand/releases/download/2.0.0/Second-in-Command.zip) | `SecondInCommand/jars/SecondInCommand.jar` |
| JetBrains annotations | 20.1.0 | [Maven Central JAR](https://repo.maven.apache.org/maven2/org/jetbrains/annotations/20.1.0/annotations-20.1.0.jar) | `annotations-20.1.0.jar` |
| Lombok | 1.18.34 | [Maven Central JAR](https://repo.maven.apache.org/maven2/org/projectlombok/lombok/1.18.34/lombok-1.18.34.jar) | `lombok-1.18.34.jar` (classpath and processor path) |

Release ZIP SHA-256 values:

```text
Nexerelin 0.12.2d   b95acb9ef5a21db286053b94eecafa627f662855a776498da92f332f96f1f1e5
LunaLib 2.0.5      9e179141c140d552145d36dbe8e5ff700bab755b0dee3637421c760d666e8a98
Console 4.0.9      9726c5746f648c08e49f5526dfe805bdf2e13f2a64bb5b0b718e1f4503982173
SecondInCommand    82f0b1e2efe4921e7ea39e16495d6544437aca5ecc51f5b8265988084bac5028
```

For another checkout, extract these releases into a development dependency directory and point the local configuration at the listed JARs. The pinned local build includes all 19 JARs supplied in `starsector-core/`, then the four integration JARs in the table order, annotations, and Lombok. The game JARs come from an existing game installation. No dependencies are copied into the rebuilt Starlords artifact.

The runtime requirements of those optional mods are separate from this compile classpath. A campaign using them needs their normal full installation and dependencies; the extracted development JARs alone are not a playable mod installation.
