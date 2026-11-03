# Stackable Trims

You know how you can have multiple patterns on banners? Well now you can customize your armor just as much too!
The mod allows you to put multiple trims on your armor at the same time instead of 1 at a time. You can customize the limit using the gamerule `maxTrimStack` up to 1.21.10 and `max_trim_stack` from 1.21.11, which is 32 by default, but can go up to 100 (which is kind of overboard).

To remove trims, use flint in an anvil.

It even works with modded trims and materials!!!

![Fancy](https://media.forgecdn.net/attachments/679/491/2023-06-17_03.png)

So go out and make your armor look fabulous!

Please report any issues!!!

## Supported versions

| Minecraft | Loaders |
|---|---|
| 1.20.1 | Fabric, Forge |
| 1.21–1.21.1, 1.21.4, 1.21.5, 1.21.6–1.21.8, 1.21.9–1.21.10, 1.21.11 | Fabric, NeoForge |
| 26.1–26.1.2, 26.2, 26.3 | Fabric, NeoForge |

## Building

One source tree serves every version through [Stonecutter](https://stonecutter.kikugie.dev/): each
`versions/<minecraft>-<loader>` node builds its own jar from `src/`, with `//? if` comments selecting
version- and loader-specific code. The source is kept in the 26.3-fabric state (`vcsVersion`).

```bash
./gradlew chiseledBuild                      # every node; jars land in versions/*/build/libs
./gradlew :1.21.1-neoforge:build             # one node
./gradlew :1.21.11-fabric:prodClient         # launch a real (production) Fabric client with the built jar
./gradlew :1.21.1-neoforge:runClient         # NeoForge/Forge dev client
```

Gradle runs on Java 21; the nodes compile with Java 17 (1.20.1), 21 (1.21.x) and 25 (26.x) toolchains.

## Releasing

Pushing a tag that matches `mod.version` in `gradle.properties` runs `.github/workflows/release.yml`, which builds every
node and uploads the jars to Modrinth and CurseForge and to a GitHub release:

```bash
git tag -a v3.0.0 -m "changelog"   # v3.0.0-beta.1 / -alpha.1 publish as beta / alpha
git push origin v3.0.0
```

The changelog is the `## <version>` section of `CHANGELOG.md` (else the tag message). The workflow can also be run
from the Actions tab, with a dry-run option. It needs the repository secrets `MODRINTH_TOKEN` and `CURSEFORGE_TOKEN`.
Every push to `multiversion` that touches the build also publishes the jars as a GitHub pre-release
(`.github/workflows/prerelease.yml`).
