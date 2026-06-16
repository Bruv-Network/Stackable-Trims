package com.golem.stackabletrims.fabric.client;

import com.golem.stackabletrims.StackableTrims;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class StackableTrimsFabricClient implements ClientModInitializer {
    
    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.level != null && client.getSingleplayerServer() != null) {
                StackableTrims.setGameRules(client.getSingleplayerServer().getGameRules());
            }
        });
        
        StackableTrims.LOGGER.info("Stackable Trims Fabric Client initialized");
    }
}