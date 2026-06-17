package com.golem.stackabletrims.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.golem.stackabletrims.core.CoreServices;
import com.golem.stackabletrims.core.TrimPolicy;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import net.minecraft.world.item.crafting.SmithingTrimRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SmithingTrimRecipe.class)
public class SmithingTrimRecipeMixin {

    @Inject(
            method = "applyTrim",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;copyWithCount(I)Lnet/minecraft/world/item/ItemStack;"),
            cancellable = true
    )
    private static void handleStackableTrims(ItemStack base, ItemStack addition, Holder<TrimPattern> pattern,
                                             CallbackInfoReturnable<ItemStack> cir,
                                             @Local(ordinal = 1) ArmorTrim newTrim) {

        TrimPolicy policy = CoreServices.policyResolver().forLevel(null);
        if (!CoreServices.trims().canAdd(base, newTrim, policy)) {
            cir.setReturnValue(ItemStack.EMPTY);
            return;
        }

        ItemStack result = base.copyWithCount(1);
        if (CoreServices.trims().addTrim(result, newTrim, policy.getMaxStack())) {
            cir.setReturnValue(result);
        } else {
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }
}