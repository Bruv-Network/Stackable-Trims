plugins {
    id("java-library")
    id("dev.kikugie.loom-back-compat")
}

fun prop(name: String): String = property(name) as String

val mcVersion = stonecutter.current.version
val javaVersion = prop("java_version").toInt()
// 26.1+ ships unobfuscated: Loom runs with remapping off, never creates the `mod*` configurations, and needs no
// mappings layer. Everything older is obfuscated and gets Mojang mappings layered in.
val obfuscated = mcVersion.substringBefore('.').toInt() < 26

group = prop("mod.group")
version = "${prop("mod.version")}+$mcVersion"
base.archivesName = "${prop("mod.archives")}-fabric"

java {
    toolchain.languageVersion = JavaLanguageVersion.of(javaVersion)
    withSourcesJar()
}

// Loom's configurations are referenced by name: Kotlin DSL emits no type-safe accessors for a build script that
// Stonecutter assigns under a non-default file name.
dependencies {
    "minecraft"("com.mojang:minecraft:${prop("deps.minecraft")}")
    if (obfuscated) "mappings"(loom.officialMojangMappings())
    val cfg = if (obfuscated) "modImplementation" else "implementation"
    cfg("net.fabricmc:fabric-loader:${prop("deps.fabric_loader")}")
    cfg("net.fabricmc.fabric-api:fabric-api:${prop("deps.fabric_api")}")
}

loom {
    runs {
        named("client") {
            client()
            configName = "Fabric Client ($mcVersion)"
            ideConfigGenerated(true)
            runDir = "runs/client"
        }
        named("server") {
            server()
            configName = "Fabric Server ($mcVersion)"
            ideConfigGenerated(true)
            runDir = "runs/server"
        }
    }
}

// Production smoke test, not part of `build`: `gradlew :<node>:prodClient` launches the real client the way players run
// it (intermediary names on obfuscated nodes, which a dev run never sees) with this node's release jar and Fabric API.
// -Pprod.mods=<jar>[,<jar>...] loads those jars instead of this build, e.g. a released version to compare against.
dependencies {
    "productionRuntimeMods"("net.fabricmc.fabric-api:fabric-api:${prop("deps.fabric_api")}")
}
tasks.register<net.fabricmc.loom.task.prod.ClientProductionRunTask>("prodClient") {
    runDir = layout.projectDirectory.dir("runs/prod-client")
    javaLauncher = javaToolchains.launcherFor { languageVersion = JavaLanguageVersion.of(javaVersion) }
    (findProperty("prod.mods") as String?)?.takeIf { it.isNotBlank() }?.let { jars ->
        mods.setFrom(configurations.named("productionRuntimeMods"), jars.split(',').map { file(it.trim()) })
    }
}

// Which mixins exist on this node (see the `//?` conditions in src/main/java/com/golem/stackabletrims/mixin).
fun mixinList(vararg entries: Pair<Boolean, String>) =
    entries.filter { it.first }.joinToString(", ") { "\"${it.second}\"" }

tasks.processResources {
    val props = mapOf(
        "mod_id" to prop("mod.id"),
        "mod_name" to prop("mod.name"),
        "mod_version" to version.toString(),
        "mod_author" to prop("mod.author"),
        "mod_license" to prop("mod.license"),
        "mod_description" to prop("mod.description"),
        "mod_homepage" to prop("mod.homepage"),
        "mod_sources" to prop("mod.sources"),
        "mod_issues" to prop("mod.issues"),
        "minecraft_range" to prop("deps.minecraft_range"),
        "fabric_loader" to prop("deps.fabric_loader_floor"),
        "java_version" to javaVersion.toString(),
        "mixin_compatibility" to if (javaVersion <= 17) "JAVA_17" else "JAVA_21",
        "mixins" to mixinList(
            true to "AnvilScreenHandlerMixin",
            true to "SmithingTrimRecipeMixin",
            stonecutter.eval(mcVersion, ">=1.21.2") to "SmithingRecipeMixin",
            stonecutter.eval(mcVersion, ">=1.20.5") to "ItemStackMixin",
            stonecutter.eval(mcVersion, "<1.20.5") to "ArmorTrimMixin",
        ),
        "client_mixins" to mixinList(
            stonecutter.eval(mcVersion, "<1.21.2") to "HumanoidArmorLayerMixin",
            stonecutter.eval(mcVersion, ">=1.21.4") to "EquipmentLayerRendererMixin",
        ),
    )
    inputs.properties(props)
    filesMatching(listOf("fabric.mod.json", "*.mixins.json")) { expand(props) }
    // Only Forge generates a refmap; naming an absent one leaves the jar pointing at a file it lacks.
    filesMatching("*.mixins.json") { filter { if (it.trimStart().startsWith("\"refmap\"")) null else it } }
    exclude("META-INF/neoforge.mods.toml", "META-INF/mods.toml", "pack.mcmeta")
}

// Stonecutter rewrites the shared src/ tree for the active node. Compilation and resource processing must wait for
// that rewrite, or they consume the previous node's source state.
tasks.compileJava { dependsOn(tasks.named("stonecutterGenerate")) }
tasks.processResources { dependsOn(tasks.named("stonecutterGenerate")) }

tasks.jar {
    from(rootProject.file("LICENSE")) { rename { "${it}_${prop("mod.id")}" } }
}

// Modrinth + CurseForge upload of this node's jar; run every node with the root `publishAllMods`.
apply(from = rootProject.file("gradle/publish.gradle.kts"))
