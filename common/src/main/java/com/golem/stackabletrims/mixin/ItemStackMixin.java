package com.golem.stackabletrims.mixin;

import com.golem.stackabletrims.StackableTrims;
import com.golem.stackabletrims.core.TrimTooltipComposer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ItemStack.class)
public class ItemStackMixin {

    @Inject(method = "getTooltipLines", at = @At("RETURN"))
    private void addStackableTrimTooltips(Item.TooltipContext tooltipContext, Player player, TooltipFlag tooltipFlag, CallbackInfoReturnable<List<Component>> cir) {
        ItemStack stack = (ItemStack) (Object) this;

        if (stack.has(DataComponents.TRIM)) {
            List<Component> tooltip = cir.getReturnValue();
            if (!StackableTrims.isBetterTrimTooltipsEnabled) {
                TrimTooltipComposer.compose(stack, tooltip);
            }
        }
    }
}