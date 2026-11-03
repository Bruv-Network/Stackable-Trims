package com.golem.stackabletrims.neoforge;

//? if neoforge {
/*import com.golem.stackabletrims.StackableTrims;
import com.golem.stackabletrims.component.StackableTrimsComponents;
import com.golem.stackabletrims.core.GameRuleTrimPolicyResolver;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.level.gamerules.GameRules;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
//? if >=1.21.11 {
import com.golem.stackabletrims.showcase.ShowcaseCommand;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
//?}

import java.util.List;

@Mod(StackableTrims.MOD_ID)
public class StackableTrimsNeoForge {

    private static final DeferredRegister<DataComponentType<?>> COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, StackableTrims.MOD_ID);

    private static final DeferredHolder<DataComponentType<?>, DataComponentType<List<ArmorTrim>>> STACKABLETRIMS_COMPONENT =
            COMPONENTS.register("stackabletrims", StackableTrimsComponents::createStackableTrimsComponent);

    public StackableTrimsNeoForge(IEventBus modEventBus) {
        StackableTrims.init();

        COMPONENTS.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        NeoForge.EVENT_BUS.addListener(this::onServerTick);
        //? if >=1.21.11 {
        modEventBus.addListener(this::onRegister);
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
        //?}

        //better-trim-tooltips compatibility
        StackableTrims.setIsBetterTrimTooltipsEnabled(isModLoaded("better-trim-tooltips"));

        StackableTrims.LOGGER.info("Stackable Trims NeoForge initialized");
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        StackableTrimsComponents.STACKABLETRIMS = STACKABLETRIMS_COMPONENT.get();
        //? if <1.21.11 {
        /^event.enqueueWork(() -> {
            GameRuleTrimPolicyResolver.MAX_TRIM_STACK = GameRules.register(
                    "maxTrimStack",
                    GameRules.Category.MISC,
                    GameRules.IntegerValue.create(GameRuleTrimPolicyResolver.DEFAULT_MAX_TRIM_STACK)
            );
            GameRuleTrimPolicyResolver.ALLOW_DUPLICATE_TRIMS = GameRules.register(
                    "allowDuplicateTrims",
                    GameRules.Category.MISC,
                    GameRules.BooleanValue.create(false)
            );
        });
        ^///?}
    }

    //? if >=1.21.11 {
    private void onRegister(RegisterEvent event) {
        if (event.getRegistryKey().equals(Registries.GAME_RULE)) {
            GameRuleTrimPolicyResolver.MAX_TRIM_STACK = GameRules.registerInteger(
                    "max_trim_stack",
                    GameRuleCategory.MISC,
                    GameRuleTrimPolicyResolver.DEFAULT_MAX_TRIM_STACK, 1, 100
            );
            GameRuleTrimPolicyResolver.ALLOW_DUPLICATE_TRIMS = GameRules.registerBoolean(
                    "allow_duplicate_trims",
                    GameRuleCategory.MISC,
                    false
            );
        }
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        ShowcaseCommand.register(event.getDispatcher());
    }
    //?}

    private void onServerTick(ServerTickEvent.Post event) {
        StackableTrims.onServerTick(event.getServer());
    }

    public static boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }
}
*///?}
