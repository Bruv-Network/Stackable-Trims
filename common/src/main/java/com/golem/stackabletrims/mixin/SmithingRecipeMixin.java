package com.golem.stackabletrims.mixin;

import com.golem.stackabletrims.core.CoreServices;
import com.golem.stackabletrims.core.TrimPolicy;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.crafting.SmithingTrimRecipe;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SmithingRecipe.class)
public interface SmithingRecipeMixin {

    @Inject(
            method = "matches(Lnet/minecraft/world/item/crafting/SmithingRecipeInput;Lnet/minecraft/world/level/Level;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    default void denyWhenAtOrOverLimit(SmithingRecipeInput input, Level level, CallbackInfoReturnable<Boolean> cir) {
        if (!((Object) this instanceof SmithingTrimRecipe)) return;

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
}
