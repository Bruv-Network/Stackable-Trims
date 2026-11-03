plugins {
    id("java-library")
    id("net.neoforged.moddev.legacyforge") version "2.0.141"
}

fun prop(name: String): String = property(name) as String

val mcVersion = stonecutter.current.version
val javaVersion = prop("java_version").toInt()

group = prop("mod.group")
version = "${prop("mod.version")}+$mcVersion"
base.archivesName = "${prop("mod.archives")}-forge"

java {
    toolchain.languageVersion = JavaLanguageVersion.of(javaVersion)
    withSourcesJar()
}

legacyForge {
    version = prop("deps.forge")
    runs {
        configureEach {
            ideName = "Forge ${name.replaceFirstChar { it.uppercase() }} ($mcVersion)"
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
    compileOnly("org.spongepowered:mixin:0.8.5")
    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")
    // MixinExtras (@WrapOperation, @Local). Unlike Fabric Loader and NeoForge, Forge 1.20.1 does not bundle it, so it
    // is nested (Jar-in-Jar; FML picks the newest copy any mod nests) and put on the dev runtime. The common
    // annotation processor writes its injectors into the refmap.
    compileOnly("io.github.llamalad7:mixinextras-common:0.3.5")
    annotationProcessor("io.github.llamalad7:mixinextras-common:0.3.5")
    implementation("io.github.llamalad7:mixinextras-forge:0.5.5")
    jarJar("io.github.llamalad7:mixinextras-forge:[0.3.5,)") { version { prefer("0.5.5") } }
}

mixin {
    add(sourceSets["main"], "stackabletrims.refmap.json")
    config("stackabletrims.mixins.json")
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
        "forge_loader_range" to prop("deps.forge_loader_range"),
        "pack_format" to prop("pack_format"),
        "mixin_compatibility" to "JAVA_17",
        "mixins" to mixinList(
            true to "AnvilScreenHandlerMixin",
            true to "SmithingTrimRecipeMixin",
            true to "ArmorTrimMixin",
        ),
        "client_mixins" to mixinList(true to "HumanoidArmorLayerMixin"),
    )
    inputs.properties(props)
    filesMatching(listOf("META-INF/mods.toml", "pack.mcmeta", "*.mixins.json")) { expand(props) }
    exclude("fabric.mod.json", "META-INF/neoforge.mods.toml")
}

tasks.named("createMinecraftArtifacts") { dependsOn(tasks.named("stonecutterGenerate")) }
tasks.compileJava {
    dependsOn(tasks.named("stonecutterGenerate"))
    options.release = javaVersion
}
tasks.processResources { dependsOn(tasks.named("stonecutterGenerate")) }

tasks.jar {
    manifest.attributes["MixinConfigs"] = "stackabletrims.mixins.json"
    from(rootProject.file("LICENSE")) { rename { "${it}_${prop("mod.id")}" } }
}

apply(from = rootProject.file("gradle/publish.gradle.kts"))
