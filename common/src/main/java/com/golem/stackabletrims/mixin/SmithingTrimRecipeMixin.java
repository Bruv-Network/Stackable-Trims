package com.golem.stackabletrims.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.golem.stackabletrims.core.CoreServices;
import com.golem.stackabletrims.core.TrimPolicy;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.crafting.SmithingTrimRecipe;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(SmithingTrimRecipe.class)
public class SmithingTrimRecipeMixin {

    @Inject(
            method = "matches(Lnet/minecraft/world/item/crafting/SmithingRecipeInput;Lnet/minecraft/world/level/Level;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void denyWhenAtOrOverLimit(SmithingRecipeInput input, Level level, CallbackInfoReturnable<Boolean> cir) {
        TrimPolicy policy = CoreServices.policyResolver().forLevel(level);
        if (policy.getMaxStack() == 0) {
            cir.setReturnValue(false);
            return;
        }

        ItemStack baseStack = input.base();
        if (CoreServices.trims().getTrimCount(baseStack) >= policy.getMaxStack()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(
            method = "assemble(Lnet/minecraft/world/item/crafting/SmithingRecipeInput;)Lnet/minecraft/world/item/ItemStack;",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;get(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;"),
            cancellable = true
    )
    public void handleStackableTrims(SmithingRecipeInput input,
                                     CallbackInfoReturnable<ItemStack> cir,
                                     @Local ItemStack baseStack,
                                     @Local(ordinal = 0) Optional<Holder.Reference<TrimMaterial>> materialOpt,
                                     @Local(ordinal = 1) Optional<Holder.Reference<TrimPattern>> patternOpt) {

        if (materialOpt.isEmpty() || patternOpt.isEmpty()) {
            return;
        }

        ArmorTrim newTrim = new ArmorTrim(materialOpt.get(), patternOpt.get());
        TrimPolicy policy = CoreServices.policyResolver().forLevel(null);
        if (!CoreServices.trims().canAdd(baseStack, newTrim, policy)) {
            cir.setReturnValue(ItemStack.EMPTY);
            return;
        }

        ItemStack result = baseStack.copyWithCount(1);
        if (CoreServices.trims().addTrim(result, newTrim, policy.getMaxStack())) {
            cir.setReturnValue(result);
        } else {
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }
}