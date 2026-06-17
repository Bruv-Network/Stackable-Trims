package com.golem.stackabletrims.fabric;

import com.golem.stackabletrims.StackableTrims;
import com.golem.stackabletrims.component.StackableTrimsComponents;
import com.golem.stackabletrims.core.GameRuleTrimPolicyResolver;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.gamerules.GameRuleCategory;

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
                Identifier.fromNamespaceAndPath(StackableTrims.MOD_ID, "stackabletrims"),
                StackableTrimsComponents.createStackableTrimsComponent()
        );
        GameRuleTrimPolicyResolver.MAX_TRIM_STACK = GameRuleBuilder.forInteger(32)
                .category(GameRuleCategory.MISC)
                .range(1, 100)
                .buildAndRegister(Identifier.withDefaultNamespace("maxTrimStack"));
        GameRuleTrimPolicyResolver.ALLOW_DUPLICATE_TRIMS = GameRuleBuilder.forBoolean(false)
                .category(GameRuleCategory.MISC)
                .buildAndRegister(Identifier.withDefaultNamespace("allowDuplicateTrims"));
        ServerTickEvents.END_SERVER_TICK.register(server ->
                StackableTrims.setGameRules(server.getGameRules())
        );
        StackableTrims.LOGGER.info("Stackable Trims Fabric initialized");
    }
}