# Changelog

## 3.0.0

- One codebase for every supported version, built with Stonecutter: Minecraft 1.20.1 (Fabric, Forge), 1.21–1.21.1,
  1.21.4, 1.21.5, 1.21.6–1.21.8, 1.21.9–1.21.10, 1.21.11, 26.1–26.1.2, 26.2 and 26.3 (Fabric, NeoForge).
- New: Minecraft 1.20.1 (Fabric and Forge), 1.21.4, 1.21.5, 1.21.6–1.21.8, 1.21.9–1.21.10 and 26.2.
- Fixed: 1.21.11 Fabric black-screening after the Mojang loading screen (#4). The armor renderer looked a vanilla class
  up by its development name, which does not exist in a normal Fabric install.
- Translations now load (they were packed outside `lang/`), with gamerule names for both the old
  (`maxTrimStack`) and new (`max_trim_stack`) rule ids.
- Releases are published to Modrinth and CurseForge automatically from a version tag.
