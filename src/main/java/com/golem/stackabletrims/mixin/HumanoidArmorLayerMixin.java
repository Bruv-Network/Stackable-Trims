package com.golem.stackabletrims.mixin;

//? if <1.21.2 {
/*import com.golem.stackabletrims.core.CoreServices;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//? if forgelike {
/^import net.minecraft.client.model.Model;
^///?}
//? if >=1.20.5
import net.minecraft.core.Holder;

import java.util.List;

/^*
 * Up to 1.21.1 armor is drawn by {@code HumanoidArmorLayer}, which renders the one vanilla trim through
 * {@code renderTrim}. The first {@code renderTrim} call of an armor piece is turned into one call per stacked trim, in
 * application order so the newest trim draws on top. Forge and NeoForge route the call through their own
 * {@code Model} overload, so that is the one wrapped there.
 ^/
@Mixin(HumanoidArmorLayer.class)
public abstract class HumanoidArmorLayerMixin<T extends LivingEntity, M extends HumanoidModel<T>, A extends HumanoidModel<T>>
        extends RenderLayer<T, M> {

    /^* The armor piece being drawn: renderTrim itself only sees its trim. ^/
    @Unique
    private ItemStack stackabletrims$stack = ItemStack.EMPTY;
    @Unique
    private boolean stackabletrims$rendering;

    protected HumanoidArmorLayerMixin(RenderLayerParent<T, M> parent) {
        super(parent);
    }

    // NeoForge splits renderArmorPiece into two overloads; the one with the extra floats reads the stack.
    @ModifyExpressionValue(
            //? if neoforge {
            /^method = "renderArmorPiece(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;ILnet/minecraft/client/model/HumanoidModel;FFFFFF)V",
            ^///?} else {
            method = "renderArmorPiece",
            //?}
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;getItemBySlot(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/item/ItemStack;")
    )
    private ItemStack stackabletrims$captureStack(ItemStack stack) {
        this.stackabletrims$stack = stack;
        return stack;
    }

    //? if forgelike && >=1.20.5 {
    /^@Inject(method = "renderTrim(Lnet/minecraft/core/Holder;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/item/armortrim/ArmorTrim;Lnet/minecraft/client/model/Model;Z)V",
            at = @At("HEAD"), cancellable = true)
    private void stackabletrims$renderStacked(Holder<ArmorMaterial> material, PoseStack poseStack, MultiBufferSource buffers,
                                              int light, ArmorTrim trim, Model model, boolean innerTexture, CallbackInfo ci) {
    ^///?} else if forgelike {
    /^// Forge's own overload: it has no SRG name, so it is not remapped.
    @Inject(method = "renderTrim(Lnet/minecraft/world/item/ArmorMaterial;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/item/armortrim/ArmorTrim;Lnet/minecraft/client/model/Model;Z)V",
            at = @At("HEAD"), cancellable = true, remap = false)
    private void stackabletrims$renderStacked(ArmorMaterial material, PoseStack poseStack, MultiBufferSource buffers,
                                              int light, ArmorTrim trim, Model model, boolean innerTexture, CallbackInfo ci) {
    ^///?} else if >=1.20.5 {
    @Inject(method = "renderTrim", at = @At("HEAD"), cancellable = true)
    private void stackabletrims$renderStacked(Holder<ArmorMaterial> material, PoseStack poseStack, MultiBufferSource buffers,
                                              int light, ArmorTrim trim, A model, boolean innerTexture, CallbackInfo ci) {
    //?} else {
    /^@Inject(method = "renderTrim", at = @At("HEAD"), cancellable = true)
    private void stackabletrims$renderStacked(ArmorMaterial material, PoseStack poseStack, MultiBufferSource buffers,
                                              int light, ArmorTrim trim, A model, boolean innerTexture, CallbackInfo ci) {
    ^///?}
        if (this.stackabletrims$rendering) {
            return;
        }
        List<ArmorTrim> trims = CoreServices.trims().getTrims(this.stackabletrims$stack).orElse(List.of());
        if (trims.size() <= 1) {
            return;
        }
        this.stackabletrims$rendering = true;
        try {
            for (ArmorTrim stacked : trims) {
                renderTrim(material, poseStack, buffers, light, stacked, model, innerTexture);
            }
        } finally {
            this.stackabletrims$rendering = false;
        }
        ci.cancel();
    }

    //? if forgelike && >=1.20.5 {
    /^@Shadow
    protected abstract void renderTrim(Holder<ArmorMaterial> material, PoseStack poseStack, MultiBufferSource buffers,
                                       int light, ArmorTrim trim, Model model, boolean innerTexture);
    ^///?} else if forgelike {
    /^@Shadow(remap = false)
    protected abstract void renderTrim(ArmorMaterial material, PoseStack poseStack, MultiBufferSource buffers,
                                       int light, ArmorTrim trim, Model model, boolean innerTexture);
    ^///?} else if >=1.20.5 {
    @Shadow
    protected abstract void renderTrim(Holder<ArmorMaterial> material, PoseStack poseStack, MultiBufferSource buffers,
                                       int light, ArmorTrim trim, A model, boolean innerTexture);
    //?} else {
    /^@Shadow
    protected abstract void renderTrim(ArmorMaterial material, PoseStack poseStack, MultiBufferSource buffers,
                                       int light, ArmorTrim trim, A model, boolean innerTexture);
    ^///?}
}
*///?} else {
/** Listed up to 1.21.1; from 1.21.2 armor is drawn by EquipmentLayerRenderer (EquipmentLayerRendererMixin). */
public class HumanoidArmorLayerMixin {
}
//?}
