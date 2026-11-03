package com.golem.stackabletrims.forge;

//? if forge {
/*import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;

/^* Client-only half of {@link StackableTrimsForge}: only referenced on the physical client. ^/
public final class StackableTrimsForgeClient {

    private StackableTrimsForgeClient() {}

    public static RegistryAccess registries() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.level == null ? null : minecraft.level.registryAccess();
    }
}
*///?}
