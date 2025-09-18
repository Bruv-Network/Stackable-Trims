package com.golem.stackabletrims.component;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.armortrim.ArmorTrim;

import java.util.List;

public class StackableTrimsComponents {

    public static DataComponentType<List<ArmorTrim>> STACKABLETRIMS;

    public static DataComponentType<List<ArmorTrim>> createStackableTrimsComponent() {
        return DataComponentType.<List<ArmorTrim>>builder()
                .persistent(Codec.list(ArmorTrim.CODEC))
                .networkSynchronized(ByteBufCodecs.collection(
                        java.util.ArrayList::new,
                        ArmorTrim.STREAM_CODEC
                ))
                .build();
    }
}