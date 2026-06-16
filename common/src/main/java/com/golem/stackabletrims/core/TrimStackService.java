package com.golem.stackabletrims.core;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.trim.ArmorTrim;

import java.util.List;
import java.util.Optional;

public interface TrimStackService {
    Optional<List<ArmorTrim>> getTrims(ItemStack stack);
    boolean addTrim(ItemStack stack, ArmorTrim newTrim, int maxStack);
    Optional<ArmorTrim> getLast(ItemStack stack);
    boolean hasTrim(ItemStack stack, ArmorTrim trim);
    void removeTrims(ItemStack stack, int count);
    int getTrimCount(ItemStack stack);
    boolean canAdd(ItemStack stack, ArmorTrim newTrim, TrimPolicy policy);
}
