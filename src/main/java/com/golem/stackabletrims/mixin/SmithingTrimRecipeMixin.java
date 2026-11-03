package com.golem.stackabletrims.mixin;

import com.golem.stackabletrims.core.CoreServices;
import com.golem.stackabletrims.core.TrimPolicy;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SmithingTrimRecipe;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//? if >=1.21.5 {
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.item.equipment.trim.TrimPattern;
//?} else if >=1.20.5 {
/*import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import java.util.Optional;
*///?} else {
/*import net.minecraft.core.RegistryAccess;
import net.minecraft.world.Container;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.item.equipment.trim.TrimMaterials;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import net.minecraft.world.item.equipment.trim.TrimPatterns;
import java.util.Optional;
*///?}
//? if >=1.20.5 && <26.1
/*import net.minecraft.core.HolderLookup;*/
//? if <1.21.2 {
/*import net.minecraft.world.level.Level;
*///?}
//? if >=1.20.5 && <1.21.2
/*import net.minecraft.world.item.crafting.SmithingRecipeInput;*/

/**
 * Replaces vanilla's "set the one trim" with "append to the stack". Each era builds the trimmed item somewhere else:
 * {@code assemble} up to 1.21.1, the static {@code applyTrim} from 1.21.2. From 1.21.2 the stack-limit check lives in
 * {@link SmithingRecipeMixin} because {@code matches} moved to the {@code SmithingRecipe} interface.
 */
@Mixin(SmithingTrimRecipe.class)
public class SmithingTrimRecipeMixin {

    //? if >=1.21.5 {
    @Inject(
            method = "applyTrim",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;copyWithCount(I)Lnet/minecraft/world/item/ItemStack;"),
            cancellable = true
    )
    //? if >=26.1 {
    private static void handleStackableTrims(ItemStack base, ItemStack addition, Holder<TrimPattern> pattern,
    //?} else {
    /*private static void handleStackableTrims(HolderLookup.Provider registries, ItemStack base, ItemStack addition, Holder<TrimPattern> pattern,
    *///?}
                                             CallbackInfoReturnable<ItemStack> cir,
                                             @Local(ordinal = 1) ArmorTrim newTrim) {
        cir.setReturnValue(stackabletrims$append(base, newTrim));
    }
    //?} else if >=1.21.2 {
    /*@Inject(
            method = "applyTrim",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;copyWithCount(I)Lnet/minecraft/world/item/ItemStack;"),
            cancellable = true
    )
    private static void handleStackableTrims(HolderLookup.Provider registries, ItemStack base, ItemStack addition, ItemStack template,
                                             CallbackInfoReturnable<ItemStack> cir,
                                             @Local(ordinal = 0) Optional<Holder.Reference<TrimMaterial>> materialOpt,
                                             @Local(ordinal = 1) Optional<Holder.Reference<TrimPattern>> patternOpt) {
        cir.setReturnValue(stackabletrims$append(base, new ArmorTrim(materialOpt.get(), patternOpt.get())));
    }
    *///?} else if >=1.20.5 {
    /*@Inject(
            method = "matches(Lnet/minecraft/world/item/crafting/SmithingRecipeInput;Lnet/minecraft/world/level/Level;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void denyWhenAtOrOverLimit(SmithingRecipeInput input, Level level, CallbackInfoReturnable<Boolean> cir) {
        if (stackabletrims$atLimit(input.base(), level)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(
            method = "assemble(Lnet/minecraft/world/item/crafting/SmithingRecipeInput;Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/world/item/ItemStack;",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;get(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;"),
            cancellable = true
    )
    public void handleStackableTrims(SmithingRecipeInput input, HolderLookup.Provider registries,
                                     CallbackInfoReturnable<ItemStack> cir,
                                     @Local ItemStack baseStack,
                                     @Local(ordinal = 0) Optional<Holder.Reference<TrimMaterial>> materialOpt,
                                     @Local(ordinal = 1) Optional<Holder.Reference<TrimPattern>> patternOpt) {
        if (materialOpt.isEmpty() || patternOpt.isEmpty()) {
            return;
        }
        cir.setReturnValue(stackabletrims$append(baseStack, new ArmorTrim(materialOpt.get(), patternOpt.get())));
    }
    *///?} else {
    /*@Inject(method = "matches", at = @At("HEAD"), cancellable = true)
    private void denyWhenAtOrOverLimit(Container container, Level level, CallbackInfoReturnable<Boolean> cir) {
        if (stackabletrims$atLimit(container.getItem(1), level)) {
            cir.setReturnValue(false);
        }
    }

    // 1.20.1 builds the result in one place with no hook between reading and writing the trim, so the whole method
    // is replaced (vanilla: same template/base/addition slots, same holders).
    @Inject(method = "assemble", at = @At("HEAD"), cancellable = true)
    private void handleStackableTrims(Container container, RegistryAccess registries, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack baseStack = container.getItem(1);
        if (!baseStack.is(ItemTags.TRIMMABLE_ARMOR)) {
            return;
        }
        Optional<Holder.Reference<TrimMaterial>> material = TrimMaterials.getFromIngredient(registries, container.getItem(2));
        Optional<Holder.Reference<TrimPattern>> pattern = TrimPatterns.getFromTemplate(registries, container.getItem(0));
        if (material.isEmpty() || pattern.isEmpty()) {
            return;
        }
        cir.setReturnValue(stackabletrims$append(baseStack, new ArmorTrim(material.get(), pattern.get())));
    }
    *///?}

    //? if <1.21.2 {
    /*@Unique
    private static boolean stackabletrims$atLimit(ItemStack baseStack, Level level) {
        TrimPolicy policy = CoreServices.policyResolver().forLevel(level);
        return policy.getMaxStack() == 0 || CoreServices.trims().getTrimCount(baseStack) >= policy.getMaxStack();
    }
    *///?}

    /** The base with {@code newTrim} appended, or empty when the stack policy forbids it. */
    @Unique
    private static ItemStack stackabletrims$append(ItemStack base, ArmorTrim newTrim) {
        TrimPolicy policy = CoreServices.policyResolver().forLevel(null);
        if (!CoreServices.trims().canAdd(base, newTrim, policy)) {
            return ItemStack.EMPTY;
        }

        ItemStack result = base.copyWithCount(1);
        return CoreServices.trims().addTrim(result, newTrim, policy.getMaxStack()) ? result : ItemStack.EMPTY;
    }
}
