package com.golem.stackabletrims.fabric;

//? if fabric {
import com.golem.stackabletrims.StackableTrims;
import com.golem.stackabletrims.core.GameRuleTrimPolicyResolver;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
//? if >=1.20.5 {
import com.golem.stackabletrims.component.StackableTrimsComponents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
//?}
//? if >=1.21.11 {
import com.golem.stackabletrims.showcase.ShowcaseCommand;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.minecraft.world.level.gamerules.GameRuleCategory;
//?} else {
/*import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.world.level.gamerules.GameRules;
*///?}

public class StackableTrimsFabric implements ModInitializer {
    public static boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public void onInitialize() {
        StackableTrims.init();

        //better-trim-tooltips compatibility
        StackableTrims.setIsBetterTrimTooltipsEnabled(isModLoaded("better-trim-tooltips"));
        //? if >=1.20.5 {
        StackableTrimsComponents.STACKABLETRIMS = Registry.register(
                BuiltInRegistries.DATA_COMPONENT_TYPE,
                Identifier.fromNamespaceAndPath(StackableTrims.MOD_ID, "stackabletrims"),
                StackableTrimsComponents.createStackableTrimsComponent()
        );
        //?}
        //? if >=1.21.11 {
        GameRuleTrimPolicyResolver.MAX_TRIM_STACK = GameRuleBuilder.forInteger(GameRuleTrimPolicyResolver.DEFAULT_MAX_TRIM_STACK)
                .category(GameRuleCategory.MISC)
                .range(1, 100)
                .buildAndRegister(Identifier.withDefaultNamespace("max_trim_stack"));
        GameRuleTrimPolicyResolver.ALLOW_DUPLICATE_TRIMS = GameRuleBuilder.forBoolean(false)
                .category(GameRuleCategory.MISC)
                .buildAndRegister(Identifier.withDefaultNamespace("allow_duplicate_trims"));
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                ShowcaseCommand.register(dispatcher)
        );
        //?} else {
        /*GameRuleTrimPolicyResolver.MAX_TRIM_STACK = GameRuleRegistry.register(
                "maxTrimStack",
                GameRules.Category.MISC,
                GameRuleFactory.createIntRule(GameRuleTrimPolicyResolver.DEFAULT_MAX_TRIM_STACK, 1, 100)
        );
        GameRuleTrimPolicyResolver.ALLOW_DUPLICATE_TRIMS = GameRuleRegistry.register(
                "allowDuplicateTrims",
                GameRules.Category.MISC,
                GameRuleFactory.createBooleanRule(false)
        );
        *///?}
        ServerTickEvents.END_SERVER_TICK.register(StackableTrims::onServerTick);
        StackableTrims.LOGGER.info("Stackable Trims Fabric initialized");
    }
}
//?}
