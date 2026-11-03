package com.golem.stackabletrims.core;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
//? if >=1.20.5 {
import com.golem.stackabletrims.component.StackableTrimsComponents;
import net.minecraft.core.component.DataComponents;
//?} else {
/*import com.golem.stackabletrims.StackableTrims;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
*///?}

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Stores every trim of a stack in application order, and mirrors the newest one into vanilla's own trim slot so the
 * armor keeps one trim (and vanilla code keeps working) without the mod.
 */
public class DefaultTrimStackService implements TrimStackService {

    //? if <1.20.5 {
    /*/^* Before data components the stacked trims live next to vanilla's "Trim" compound. ^/
    private static final String STACKED_TAG = "StackableTrims";
    private static final String VANILLA_TAG = "Trim";
    *///?}

    @Override
    public Optional<List<ArmorTrim>> getTrims(ItemStack stack) {
        if (!stack.is(ItemTags.TRIMMABLE_ARMOR)) {
            return Optional.empty();
        }

        List<ArmorTrim> trims = readStacked(stack);
        if (trims.isEmpty()) {
            ArmorTrim singleTrim = readVanilla(stack);
            if (singleTrim != null) {
                return Optional.of(List.of(singleTrim));
            }
            return Optional.empty();
        }

        return Optional.of(trims);
    }

    @Override
    public boolean addTrim(ItemStack stack, ArmorTrim newTrim, int maxStack) {
        if (!stack.is(ItemTags.TRIMMABLE_ARMOR)) {
            return false;
        }

        List<ArmorTrim> currentTrims = getTrims(stack).orElse(List.of());
        if (currentTrims.size() >= maxStack) {
            return false;
        }

        List<ArmorTrim> newTrims = new ArrayList<>(currentTrims);
        newTrims.add(newTrim);
        write(stack, newTrims);
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
        List<ArmorTrim> currentTrims = readStacked(stack);
        if (currentTrims.isEmpty()) {
            // A plain vanilla trim (or nothing): removing any count clears it.
            if (count > 0) {
                write(stack, List.of());
            }
            return;
        }

        List<ArmorTrim> newTrims = new ArrayList<>(currentTrims);
        int toRemove = Math.min(count, newTrims.size());
        for (int i = 0; i < toRemove; i++) {
            newTrims.remove(newTrims.size() - 1);
        }
        write(stack, newTrims);
    }

    @Override
    public int getTrimCount(ItemStack stack) {
        return getTrims(stack).map(List::size).orElse(0);
    }

    @Override
    public boolean canAdd(ItemStack stack, ArmorTrim newTrim, TrimPolicy policy) {
        if (!stack.is(ItemTags.TRIMMABLE_ARMOR)) {
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

    //? if >=1.20.5 {
    private static List<ArmorTrim> readStacked(ItemStack stack) {
        List<ArmorTrim> trims = stack.get(StackableTrimsComponents.STACKABLETRIMS);
        return trims == null ? new ArrayList<>() : new ArrayList<>(trims);
    }

    private static ArmorTrim readVanilla(ItemStack stack) {
        return stack.get(DataComponents.TRIM);
    }

    private static void write(ItemStack stack, List<ArmorTrim> trims) {
        if (trims.isEmpty()) {
            stack.remove(StackableTrimsComponents.STACKABLETRIMS);
            stack.remove(DataComponents.TRIM);
        } else {
            stack.set(StackableTrimsComponents.STACKABLETRIMS, List.copyOf(trims));
            stack.set(DataComponents.TRIM, trims.get(trims.size() - 1));
        }
    }
    //?} else {
    /*private static List<ArmorTrim> readStacked(ItemStack stack) {
        List<ArmorTrim> trims = new ArrayList<>();
        CompoundTag tag = stack.getTag();
        RegistryAccess registries = StackableTrims.registries();
        if (tag == null || registries == null || !tag.contains(STACKED_TAG, Tag.TAG_LIST)) {
            return trims;
        }
        RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, registries);
        for (Tag element : tag.getList(STACKED_TAG, Tag.TAG_COMPOUND)) {
            ArmorTrim.CODEC.parse(ops, element).result().ifPresent(trims::add);
        }
        return trims;
    }

    private static ArmorTrim readVanilla(ItemStack stack) {
        RegistryAccess registries = StackableTrims.registries();
        return registries == null ? null : ArmorTrim.getTrim(registries, stack).orElse(null);
    }

    private static void write(ItemStack stack, List<ArmorTrim> trims) {
        if (trims.isEmpty()) {
            stack.removeTagKey(STACKED_TAG);
            stack.removeTagKey(VANILLA_TAG);
            return;
        }
        RegistryAccess registries = StackableTrims.registries();
        if (registries == null) {
            return;
        }
        RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, registries);
        ListTag list = new ListTag();
        for (ArmorTrim trim : trims) {
            ArmorTrim.CODEC.encodeStart(ops, trim).result().ifPresent(list::add);
        }
        stack.getOrCreateTag().put(STACKED_TAG, list);
        ArmorTrim.setTrim(registries, stack, trims.get(trims.size() - 1));
    }
    *///?}
}
