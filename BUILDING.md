# Building the immersion fork

Every successful build replaces `jars/StarLords.jar`, making this repository folder ready to copy into Starsector's `mods` directory. It also creates `build/Starlords_immersionPatches.zip`, a clean installable package containing the runtime JAR and resources. See [TESTING.md](TESTING.md) for the short in-game check.

Old-save compatibility is optional during the overhaul. It must not obstruct the accepted design; a phase may require a new campaign. Saving and reloading a campaign created with the same build should still work.

## Requirements

- Python 3.10 or newer; only its standard library is used.
- A full JDK 17. Compilation targets Java 16 (`--release 16`), matching the class-file level of the supplied Starlords 0.3.78 JAR. The compiler and target are deliberately different.
- A licensed Starsector 0.98a installation containing `starsector-core/starfarer.api.jar` and its supporting libraries.
- Nexerelin, LunaLib, Console Commands, and Second in Command JARs. Their APIs are needed to compile existing integration classes even though those mods are optional at runtime.
- JetBrains annotations and Lombok. Lombok must also be on the annotation-processor path; its generated getters/setters and exception handling are required by the source.

The build does not download dependencies or install anything into Starsector. Game and third-party classes are not bundled in the output.

The [verified dependency versions and official download sources](build-support/DEPENDENCIES.md) describe the Phase 1 build environment.

## Local configuration

Copy `build.example.json` to `build.local.json` and replace the example paths. JSON accepts forward slashes in Windows paths. Paths may be absolute or relative to the configuration file; wildcard matches are sorted. Use one version of each dependency, and exclude old copies of Starlords itself.

The local configuration and generated `build/` directory are ignored by Git. `build.lock.json` records the exact compiler, compiler options, processor, ordered dependency filenames and SHA-256 hashes without recording personal paths. It is intended to be versioned.

From the repository root:

```powershell
python tools/build.py doctor
python tools/build.py lock
python tools/build.py reproduce
```

Run `lock` when deliberately selecting or changing dependencies. Do not refresh it merely to silence a mismatch: review which input changed. Subsequent builds normally need only:

```powershell
python tools/build.py build
```

`doctor` diagnoses missing/ambiguous API providers, an unsuitable JDK, or an invalid annotation-processor configuration. `build` requires the local compiler and dependencies to match the lock. `reproduce` performs two clean source builds in separate temporary directories and requires identical output hashes. This proves repeatability with the pinned inputs, not equivalence with an unknown upstream toolchain.

## Outputs and checks

- `jars/StarLords.jar`: current runtime, replaced automatically after a successful build.
- `build/Starlords_immersionPatches.zip`: extract into `Starsector/mods`; includes the enclosing mod folder, data, graphics, optional sounds, runtime JAR, metadata, license, testing instructions, and presentation authoring notes.
- `build/StarLords.jar`: freshly compiled mod classes, checked for runtime validity.
- `build/StarLords.reproduce.jar`: second artifact from `reproduce`.
- `build/build-report.json`: runtime and package hashes, validation/reproducibility results, source fingerprints, build-tool fingerprints, and locked inputs.
- `build/javac.log`: compiler diagnostics, including on a failed build.

Both `build` and `reproduce` update the repository runtime JAR. With `reproduce`, both compilations and the hash comparison must succeed before publication. Compilation or verification failures leave the last successful runtime JAR in place. Each output file is staged and replaced atomically to avoid partial writes. A failed invocation can leave intermediate artifacts in `build/`; check the exit status and successful report before using them.

Copy the whole repository folder, or use the clean ZIP to leave out `.git`, source, tools, local paths, and downloaded compile dependencies. The ZIP always contains the freshly compiled JAR, even if the previous repository JAR was older. Keep only one installed copy of the mod. Git retains the original runtime history; no manual JAR replacement is needed.

Every archive has a fixed entry order, timestamp, permissions, and storage mode. Only Starlords packages and the mod's own rule commands (`LordsOpenCommCmd`, `StarlordsRankGreeting`) are allowed in the output.

Check an existing artifact without compilation dependencies:

```powershell
python tools/build.py verify --jar jars/StarLords.jar
python tools/build.py verify --jar build/StarLords.jar
```

Normal verification checks archive integrity, permitted packages, the current plugin entry point, JAR registration, and supported bytecode. It does not require old classes, field layouts, enum ordering, aliases, or memory keys to survive a redesign. An optional historical comparison is available when useful:

```powershell
python tools/build.py verify --check-legacy
```

This diagnostic is never part of the build gate. The [baseline notes](build-support/README.md) describe its limits.

## Regression tests

```powershell
$env:STARLORDS_TEST_JDK = 'C:/path/to/jdk-17'
python -m unittest discover -s tools/tests -v
```

The tests use real compilation of small independent Java fixtures. They cover deterministic output, runtime validation, optional historical diagnostics, packaging of the fresh JAR, successful runtime replacement, and preservation of the working runtime after compilation or second-build failure. Historical baseline tests read the original JAR from Git instead of requiring the working JAR to remain unchanged. These tests do not simulate Starsector or substitute fake APIs for a full mod build. Compiler tests are skipped if no compiler is available; a complete verification run must have no skips.

The Phase 2 suite also compiles the full production source against the local game libraries. Controlled game-interface proxies exercise the actual title/text resolvers, JSON loader, dialogue rendering pipeline and proposal summaries. These checks cover commissions, defection, multiple faction titles, missing optional actors, literal token handling, hint/highlight inserts, and effect execution order. Data comparisons verify that the neutral rewrite preserves branch conditions/effects, roster gameplay fields, settings values and command IDs. This is not an in-game UI or campaign simulation; follow [TESTING.md](TESTING.md) for that validation. The API tests require `build.local.json`.

## Updating the baseline

`build-support/baseline-0.3.78.json` is a historical reference, not a constraint on future implementations. The capture command refuses to overwrite it. Keep it for comparisons; document when a phase requires a new campaign instead of spending effort on unnecessary legacy migrations.
