package com.golem.stackabletrims.mixin;

import com.golem.stackabletrims.core.TrimRemoval;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AnvilMenu.class)
public abstract class AnvilScreenHandlerMixin extends ItemCombinerMenu {

    protected AnvilScreenHandlerMixin(@Nullable MenuType<?> type, int syncId, Inventory playerInventory, ContainerLevelAccess context, ItemCombinerMenuSlotDefinition slotDefinition) {
        super(type, syncId, playerInventory, context, slotDefinition);
    }

    @Inject(method = "createResult", at = @At("HEAD"), cancellable = true)
    public void injectUpdateResult(CallbackInfo ci) {
        ItemStack armorStack = this.inputSlots.getItem(0);
        ItemStack toolStack = this.inputSlots.getItem(1);

        this.resultSlots.setItem(0, ItemStack.EMPTY);

        if (TrimRemoval.canRemoveTrims(armorStack, toolStack)) {
            ItemStack result = TrimRemoval.getRemovalResult(armorStack, toolStack);
            this.resultSlots.setItem(0, result);
            this.broadcastChanges();
            ci.cancel();
        }
    }

    @Inject(method = "onTake", at = @At("HEAD"), cancellable = true)
    public void injectOnTakeOutput(Player player, ItemStack stack, CallbackInfo ci) {
        ItemStack armorStack = this.inputSlots.getItem(0);
        ItemStack toolStack = this.inputSlots.getItem(1);

        if (TrimRemoval.canRemoveTrims(armorStack, toolStack)) {
            int flintCost = TrimRemoval.getFlintCost(armorStack, toolStack);

            toolStack.shrink(flintCost);
            armorStack.shrink(1);

            this.access.execute((world, pos) -> {
                BlockState blockState = world.getBlockState(pos);
                if (!player.getAbilities().instabuild && blockState.is(BlockTags.ANVIL) && player.getRandom().nextFloat() < 0.12F) {
                    BlockState blockState2 = AnvilBlock.damage(blockState);
                    if (blockState2 == null) {
                        world.removeBlock(pos, false);
                        world.levelEvent(1029, pos, 0);
                    } else {
                        world.setBlockAndUpdate(pos, blockState2);
                        world.levelEvent(1030, pos, 0);
                    }
                } else {
                    world.levelEvent(1030, pos, 0);
                }
            });

            ci.cancel();
        }
    }

    @Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true)
    public void canTakeOutput(Player player, boolean present, CallbackInfoReturnable<Boolean> cir) {
        ItemStack armorStack = this.inputSlots.getItem(0);
        ItemStack toolStack = this.inputSlots.getItem(1);

        if (TrimRemoval.canRemoveTrims(armorStack, toolStack)) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }
}