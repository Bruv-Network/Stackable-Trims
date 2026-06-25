package com.golem.stackabletrims.mixin;

import com.golem.stackabletrims.component.StackableTrimsComponents;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;
import java.util.function.Function;

@Mixin(EquipmentLayerRenderer.class)
public class EquipmentLayerRendererMixin {

    // Shadow as raw Function to avoid referencing the package-private TrimSpriteKey type
    @SuppressWarnings("rawtypes")
    @Shadow
    @Final
    private Function trimSpriteLookup;

    // Cached MethodHandle for constructing the package-private TrimSpriteKey record
    @Unique
    private static final MethodHandle stackabletrims$trimSpriteKeyCtor;

    static {
        try {
            Class<?> clazz = Class.forName(
                    "net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer$TrimSpriteKey"
            );
            stackabletrims$trimSpriteKeyCtor = MethodHandles.privateLookupIn(clazz, MethodHandles.lookup())
                    .findConstructor(clazz, MethodType.methodType(
                            void.class, ArmorTrim.class,
                            EquipmentClientInfo.LayerType.class, ResourceKey.class
                    ));
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Stackable Trims: Failed to access TrimSpriteKey constructor", e);
        }
    }

    /**
     * Redirect vanilla's single-trim {@code submitModel} call (the last {@code submitModel} in
     * {@code renderLayers}, ordinal 2 after the base layer and foil calls).
     *
     * <p>Vanilla submits the trim through {@code submitNodeCollector.order(nextOrder++)}, i.e. a
     * collector ordered <em>after</em> all the base armor layers so the trim decal draws on top.
     * By redirecting at this exact call we reuse that correct ordering for every stacked trim,
     * instead of submitting them to the base-layer order bucket. The latter only happened to work
     * in the vanilla pipeline because the trims were submitted last within the shared bucket; under
     * Sodium, which reorders/batches a bucket by render type, the opaque armor would draw over the
     * trim decals and they would vanish.
     */
    @Redirect(
            method = "renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/resources/Identifier;II)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/OrderedSubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;IIILnet/minecraft/client/renderer/texture/TextureAtlasSprite;ILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V",
                    ordinal = 2)
    )
    private <S> void stackabletrims$redirectTrimSubmit(OrderedSubmitNodeCollector collector,
                                                       Model<? super S> model, S state,
                                                       PoseStack poseStack, RenderType renderType,
                                                       int light, int overlay, int tinted,
                                                       TextureAtlasSprite sprite, int outline,
                                                       ModelFeatureRenderer.CrumblingOverlay crumbling,
                                                       // enclosing renderLayers parameters (in order)
                                                       EquipmentClientInfo.LayerType layerType,
                                                       ResourceKey<EquipmentAsset> equipmentAssetId,
                                                       Model<? super S> enclosingModel, S enclosingState,
                                                       ItemStack itemStack) {
        List<ArmorTrim> stacked = itemStack.get(StackableTrimsComponents.STACKABLETRIMS);
        if (stacked == null || stacked.size() <= 1) {
            // No stacked trims — submit the single vanilla trim exactly as it would have.
            collector.submitModel(model, state, poseStack, renderType, light, overlay, tinted,
                    sprite, outline, crumbling);
            return;
        }

        // Render every stacked trim into the same (post base-layer) order bucket vanilla chose.
        for (ArmorTrim trim : stacked) {
            TextureAtlasSprite trimSprite = stackabletrims$lookupSprite(trim, layerType, equipmentAssetId);
            if (trimSprite == null) continue;

            RenderType trimRenderType = Sheets.armorTrimsSheet(trim.pattern().value().decal());
            collector.submitModel(model, state, poseStack, trimRenderType, light,
                    OverlayTexture.NO_OVERLAY, -1, trimSprite, outline, null);
        }
    }

    /**
     * Look up the trim sprite using the cached MethodHandle to construct the package-private TrimSpriteKey.
     */
    @Unique
    @SuppressWarnings("unchecked")
    private TextureAtlasSprite stackabletrims$lookupSprite(ArmorTrim trim,
                                                           EquipmentClientInfo.LayerType layerType,
                                                           ResourceKey<EquipmentAsset> equipmentAssetId) {
        try {
            Object key = stackabletrims$trimSpriteKeyCtor.invoke(trim, layerType, equipmentAssetId);
            return (TextureAtlasSprite) trimSpriteLookup.apply(key);
        } catch (Throwable e) {
            return null;
        }
    }
}
