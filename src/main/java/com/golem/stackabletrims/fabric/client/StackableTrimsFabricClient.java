package com.golem.stackabletrims.fabric.client;

//? if fabric {
import com.golem.stackabletrims.StackableTrims;
import com.golem.stackabletrims.core.GameRuleTrimPolicyResolver;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
//? if <1.20.5
/*import net.minecraft.client.Minecraft;*/

public class StackableTrimsFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.level != null && client.getSingleplayerServer() != null) {
                StackableTrims.setGameRules(GameRuleTrimPolicyResolver.rulesOf(client.getSingleplayerServer()));
            }
        });
        //? if <1.20.5 {
        /*StackableTrims.setClientRegistries(() ->
                Minecraft.getInstance().level == null ? null : Minecraft.getInstance().level.registryAccess());
        *///?}

        StackableTrims.LOGGER.info("Stackable Trims Fabric Client initialized");
    }
}
//?}
