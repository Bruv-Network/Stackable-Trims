pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/") { name = "Fabric" }
        maven("https://maven.neoforged.net/releases/") { name = "NeoForged" }
        maven("https://maven.kikugie.dev/releases") { name = "KikuGie" }
        maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("dev.kikugie.stonecutter") version "0.9.8"
    // Picks the Fabric Loom variant per node: `fabric-loom-remap` for the obfuscated era (<26.1), plain
    // `fabric-loom` for the unobfuscated one (26.1+), same as Golem-Core.
    id("dev.kikugie.loom-back-compat") version "0.4.2"
}

stonecutter {
    create(rootProject) {
        // One node per (minecraft version, loader) pair. The node name carries the loader suffix, so the logical
        // version used by `//? if >=1.21.4` conditions is given separately: otherwise Stonecutter parses "-neoforge"
        // as a pre-release tag and orders it below the release.
        fun match(version: String, vararg loaders: String) = loaders.forEach {
            version("$version-$it", version).buildscript = "build.$it.gradle.kts"
        }

        match("26.3", "fabric", "neoforge")
        match("26.2", "fabric", "neoforge")
        match("26.1", "fabric", "neoforge")
        match("1.21.11", "fabric", "neoforge")
        match("1.21.10", "fabric", "neoforge")
        match("1.21.8", "fabric", "neoforge")
        match("1.21.5", "fabric", "neoforge")
        match("1.21.4", "fabric", "neoforge")
        match("1.21.1", "fabric", "neoforge")
        match("1.20.1", "fabric", "forge")

        vcsVersion = "26.3-fabric"
    }
}

rootProject.name = "Stackable-Trims"
