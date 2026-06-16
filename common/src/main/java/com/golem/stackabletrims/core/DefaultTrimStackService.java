package com.golem.stackabletrims.core;

import com.golem.stackabletrims.component.StackableTrimsComponents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.trim.ArmorTrim;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DefaultTrimStackService implements TrimStackService {

    @Override
    public Optional<List<ArmorTrim>> getTrims(ItemStack stack) {
        if (!stack.is(ItemTags.TRIMMABLE_ARMOR)) {
            return Optional.empty();
        }

        List<ArmorTrim> trims = stack.get(StackableTrimsComponents.STACKABLETRIMS);
        if (trims == null || trims.isEmpty()) {
            ArmorTrim singleTrim = stack.get(DataComponents.TRIM);
            if (singleTrim != null) {
                return Optional.of(List.of(singleTrim));
            }
            return Optional.empty();
        }

        return Optional.of(new ArrayList<>(trims));
    }

    @Override
    public boolean addTrim(ItemStack stack, ArmorTrim newTrim, int maxStack) {
        if (!stack.is(ItemTags.TRIMMABLE_ARMOR)) {
            return false;
        }

        List<ArmorTrim> currentTrims = stack.get(StackableTrimsComponents.STACKABLETRIMS);
        if (currentTrims == null || currentTrims.isEmpty()) {
            ArmorTrim singleTrim = stack.get(DataComponents.TRIM);
            currentTrims = singleTrim != null ? new ArrayList<>(List.of(singleTrim)) : new ArrayList<>();
        }

        if (currentTrims.size() >= maxStack) {
            return false;
        }

        List<ArmorTrim> newTrims = new ArrayList<>(currentTrims);
        newTrims.add(newTrim);

        stack.set(StackableTrimsComponents.STACKABLETRIMS, newTrims);
        stack.set(DataComponents.TRIM, newTrim);
        return true;
    }

    @Override
    public Optional<ArmorTrim> getLast(ItemStack stack) {
        return getTrims(stack)
                .filter(trims -> !trims.isEmpty())
                .map(trims -> trims.get(trims.size() - 1));
    }

    @Override
    public boolean hasTrim(ItemStack stack, ArmorTrim trim) {
        return getTrims(stack)
                .map(trims -> trims.stream().anyMatch(t -> t.equals(trim)))
                .orElse(false);
    }

    @Override
    public void removeTrims(ItemStack stack, int count) {
        List<ArmorTrim> currentTrims = stack.getOrDefault(
                StackableTrimsComponents.STACKABLETRIMS,
                new ArrayList<>()
        );

        if (currentTrims.isEmpty()) {
            return;
        }

        List<ArmorTrim> newTrims = new ArrayList<>(currentTrims);
        int toRemove = Math.min(count, newTrims.size());
        for (int i = 0; i < toRemove; i++) {
            if (!newTrims.isEmpty()) {
                newTrims.removeLast();
            }
        }

        if (newTrims.isEmpty()) {
            stack.remove(StackableTrimsComponents.STACKABLETRIMS);
            stack.remove(DataComponents.TRIM);
        } else {
            stack.set(StackableTrimsComponents.STACKABLETRIMS, newTrims);
            stack.set(DataComponents.TRIM, newTrims.getLast());
        }
    }

    @Override
    public int getTrimCount(ItemStack stack) {
        return getTrims(stack).map(List::size).orElse(0);
    }

    @Override
    public boolean canAdd(ItemStack stack, ArmorTrim newTrim, TrimPolicy policy) {
        if (!stack.is(net.minecraft.tags.ItemTags.TRIMMABLE_ARMOR)) {
            return false;
        }
        int max = policy.getMaxStack();
        if (max == 0) {
            return false;
        }
        if (getTrimCount(stack) >= max) {
            return false;
        }
        return policy.allowDuplicates() || !hasTrim(stack, newTrim);
    }
}
