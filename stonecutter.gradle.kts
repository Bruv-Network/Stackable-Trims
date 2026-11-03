plugins {
    id("dev.kikugie.stonecutter")
}

stonecutter active "26.3-fabric" /* [SC] DO NOT EDIT */

// Builds every node in one invocation.
tasks.register("chiseledBuild") {
    group = "build"
    description = "Builds every registered version/loader node."
    dependsOn(stonecutter.tasks.named("build"))
}

// Uploads every node's jar to Modrinth and CurseForge (gradle/publish.gradle.kts). Add -Ppublish.dryRun=true to build
// and log the uploads without making them.
tasks.register("publishAllMods") {
    group = "publishing"
    description = "Publishes every version/loader node to Modrinth and CurseForge."
    dependsOn(stonecutter.tasks.named("publishMods"))
}

stonecutter parameters {
    val loader = node.metadata.project.substringAfterLast('-')

    // Loader constants, used as `//? if fabric {` / `//? if forgelike {`.
    constants["fabric"] = loader == "fabric"
    constants["neoforge"] = loader == "neoforge"
    constants["forge"] = loader == "forge"
    constants["forgelike"] = loader == "neoforge" || loader == "forge"

    // Vanilla package moves. Each pair is injective in both directions (neither spelling exists in the other era), so
    // a string swap keeps these imports out of `//?` blocks. 26.3 is the source of truth (vcsVersion).
    replacements {
        string(current.parsed < "1.21.2") {
            replace("net.minecraft.world.item.equipment.trim.", "net.minecraft.world.item.armortrim.")
        }
        string(current.parsed < "1.21.11") {
            replace("import net.minecraft.world.level.gamerules.GameRules;", "import net.minecraft.world.level.GameRules;")
            // 1.21.11 renamed ResourceLocation to Identifier and moved RenderType into its own package. Dotted names
            // for imports, slashed ones for mixin descriptors.
            replace("net.minecraft.resources.Identifier", "net.minecraft.resources.ResourceLocation")
            replace("net/minecraft/resources/Identifier", "net/minecraft/resources/ResourceLocation")
            replace("Identifier.fromNamespaceAndPath(", "ResourceLocation.fromNamespaceAndPath(")
            replace("net.minecraft.client.renderer.rendertype.RenderType", "net.minecraft.client.renderer.RenderType")
            replace("net/minecraft/client/renderer/rendertype/RenderType", "net/minecraft/client/renderer/RenderType")
        }
    }
}
