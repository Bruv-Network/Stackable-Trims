package com.golem.stackabletrims.fabric;

import com.golem.stackabletrims.StackableTrims;
import com.golem.stackabletrims.component.StackableTrimsComponents;
import com.golem.stackabletrims.core.GameRuleTrimPolicyResolver;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.GameRules;

public class StackableTrimsFabric implements ModInitializer {
    public static boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public void onInitialize() {
        StackableTrims.init();

        //better-trim-tooltips compatibility
        StackableTrims.setIsBetterTrimTooltipsEnabled(FabricLoader.getInstance().isModLoaded("better-trim-tooltips"));
        StackableTrimsComponents.STACKABLETRIMS = Registry.register(
                BuiltInRegistries.DATA_COMPONENT_TYPE,
                ResourceLocation.fromNamespaceAndPath(StackableTrims.MOD_ID, "stackabletrims"),
                StackableTrimsComponents.createStackableTrimsComponent()
        );
        GameRuleTrimPolicyResolver.MAX_TRIM_STACK = GameRuleRegistry.register(
                "maxTrimStack",
                GameRules.Category.MISC,
                GameRuleFactory.createIntRule(32, 1, 100)
        );
        GameRuleTrimPolicyResolver.ALLOW_DUPLICATE_TRIMS = GameRuleRegistry.register(
                "allowDuplicateTrims",
                GameRules.Category.MISC,
                GameRuleFactory.createBooleanRule(false)
        );
        ServerTickEvents.END_SERVER_TICK.register(server ->
                StackableTrims.setGameRules(server.getGameRules())
        );
        StackableTrims.LOGGER.info("Stackable Trims Fabric initialized");
    }
}