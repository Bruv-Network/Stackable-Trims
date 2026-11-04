plugins {
    id("java-library")
    id("net.neoforged.moddev") version "2.0.141"
}

fun prop(name: String): String = property(name) as String

val mcVersion = stonecutter.current.version
val javaVersion = prop("java_version").toInt()

group = prop("mod.group")
version = "${prop("mod.version")}+$mcVersion"
base.archivesName = "${prop("mod.archives")}-neoforge"

java {
    toolchain.languageVersion = JavaLanguageVersion.of(javaVersion)
    withSourcesJar()
}

neoForge {
    enable {
        version = prop("deps.neoforge")
        // NeoForm's decompile-and-recompile pipeline fails to recompile 26.2+ (HolderSet access error), so those
        // nodes use the binary pipeline, as MDG does on CI. Older nodes keep the recompiled jar for its parameter names.
        isDisableRecompilation = stonecutter.eval(mcVersion, ">=26.2")
    }

    runs {
        configureEach {
            ideName = "NeoForge ${name.replaceFirstChar { it.uppercase() }} ($mcVersion)"
        }
        register("client") {
            client()
            gameDirectory = file("runs/client")
        }
        register("server") {
            server()
            gameDirectory = file("runs/server")
        }
    }

    mods {
        register(prop("mod.id")) { sourceSet(sourceSets["main"]) }
    }
}

dependencies {
    // MixinExtras (@WrapOperation, @Local): NeoForge bundles it at runtime on every line we target, so compile
    // against the oldest API we use and ship nothing.
    compileOnly("io.github.llamalad7:mixinextras-common:0.3.5")
}

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
        "mod_issues" to prop("mod.issues"),
        "minecraft_range" to prop("deps.minecraft_range"),
        "neoforge_range" to prop("deps.neoforge_range"),
        // NeoForge 26.2 deprecated logoFile and stops at a warning screen on every launch for mods that use it.
        "icon_key" to if (stonecutter.eval(mcVersion, ">=26.2")) "iconFile" else "logoFile",
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
    filesMatching(listOf("META-INF/neoforge.mods.toml", "*.mixins.json")) { expand(props) }
    filesMatching("*.mixins.json") { filter { if (it.trimStart().startsWith("\"refmap\"")) null else it } }
    exclude("fabric.mod.json", "META-INF/mods.toml", "pack.mcmeta")
}

// Stonecutter writes this project's sources from the root src/ tree; nothing may read them before that.
tasks.named("createMinecraftArtifacts") { dependsOn(tasks.named("stonecutterGenerate")) }
tasks.compileJava { dependsOn(tasks.named("stonecutterGenerate")) }
tasks.processResources { dependsOn(tasks.named("stonecutterGenerate")) }

tasks.jar {
    from(rootProject.file("LICENSE")) { rename { "${it}_${prop("mod.id")}" } }
}

apply(from = rootProject.file("gradle/publish.gradle.kts"))
