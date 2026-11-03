package com.golem.stackabletrims.forge;

//? if forge {
/*import com.golem.stackabletrims.StackableTrims;
import com.golem.stackabletrims.core.GameRuleTrimPolicyResolver;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(StackableTrims.MOD_ID)
public class StackableTrimsForge {

    public StackableTrimsForge() {
        StackableTrims.init();

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.addListener(this::onServerTick);

        //better-trim-tooltips compatibility
        StackableTrims.setIsBetterTrimTooltipsEnabled(isModLoaded("better-trim-tooltips"));

        if (FMLEnvironment.dist == Dist.CLIENT) {
            StackableTrims.setClientRegistries(StackableTrimsForgeClient::registries);
        }

        StackableTrims.LOGGER.info("Stackable Trims Forge initialized");
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
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
    }

    private void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            StackableTrims.onServerTick(event.getServer());
        }
    }

    public static boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }
}
*///?}
