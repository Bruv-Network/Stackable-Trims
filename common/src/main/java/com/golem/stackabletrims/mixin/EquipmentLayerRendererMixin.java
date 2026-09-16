package com.golem.stackabletrims.mixin;

import com.golem.stackabletrims.component.StackableTrimsComponents;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.UvMapping;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.client.resources.palette.PalettedTextureManager;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(EquipmentLayerRenderer.class)
public class EquipmentLayerRendererMixin {

    /**
     * Wrap vanilla's single-trim {@code submitModel} call in {@code renderLayers}.
     *
     * <p>In 26.3 the call sites are: the base layer loop (ordinal 0), the trim (ordinal 1) and the
     * trimmed-armor glint (ordinal 2). Vanilla submits the trim through
     * {@code submitNodeCollector.order(nextOrder++)}, i.e. a collector ordered <em>after</em> all the
     * base armor layers so the trim decal draws on top. By wrapping this exact call we reuse that
     * ordering for every stacked trim, which keeps the decals from being drawn over by the opaque
     * armor under renderers that batch a bucket by render type (e.g. Sodium). {@code @WrapOperation}
     * (rather than {@code @Redirect}) chains with other mods and avoids MixinExtras' redirect
     * pre-processing, which broke on NeoForge.
     *
     * <p>26.3 replaced the armor trim texture atlas with {@link PalettedTextureManager}: each trim is
     * a base pattern texture with the material's palette swapped in at runtime. We resolve a
     * {@link PalettedTextureManager.Handle} per stacked trim the same way vanilla's private
     * {@code TrimTextureKey} does, honouring {@code EquipmentClientInfo.trimOverrides()}.
     */
    @WrapOperation(
            method = "renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/resources/Identifier;II)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/OrderedSubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;IIILnet/minecraft/client/renderer/texture/UvMapping;I)V",
                    ordinal = 1)
    )
    private <S> void stackabletrims$wrapTrimSubmit(OrderedSubmitNodeCollector collector,
                                                   Model<? super S> model, S state,
                                                   PoseStack poseStack, RenderType renderType,
                                                   int light, int overlay, int tinted,
                                                   UvMapping uvMapping, int outline,
                                                   Operation<Void> original,
                                                   @Local(argsOnly = true) EquipmentClientInfo.LayerType layerType,
                                                   @Local(argsOnly = true) ItemStack itemStack,
                                                   @Local EquipmentClientInfo equipmentInfo) {
        List<ArmorTrim> stacked = itemStack.get(StackableTrimsComponents.STACKABLETRIMS);
        if (stacked == null || stacked.size() <= 1) {
            // No stacked trims — submit the single vanilla trim exactly as it would have.
            original.call(collector, model, state, poseStack, renderType, light, overlay, tinted,
                    uvMapping, outline);
            return;
        }

        PalettedTextureManager palettedTextures = Minecraft.getInstance().getPalettedTextureManager();

        // Render every stacked trim into the same (post base-layer) order bucket vanilla chose,
        // in stacking order so later trims draw over earlier ones.
        for (ArmorTrim trim : stacked) {
            PalettedTextureManager.Handle handle =
                    stackabletrims$getOrPrepareTexture(trim, layerType, equipmentInfo, palettedTextures);
            RenderType trimRenderType = RenderTypes.armorTrim(handle.textureLocation(), trim.pattern().value().decal());
            collector.submitModel(model, state, poseStack, trimRenderType, light,
                    OverlayTexture.NO_OVERLAY, -1, handle, outline);
        }
    }

    /**
     * Mirror of vanilla {@code EquipmentLayerRenderer.TrimTextureKey#getOrPrepareTexture}.
     * {@link PalettedTextureManager#getOrPrepare} caches by (texture, palette), so this is cheap
     * after the first call for a given trim.
     */
    @Unique
    private static PalettedTextureManager.Handle stackabletrims$getOrPrepareTexture(ArmorTrim trim,
                                                                                    EquipmentClientInfo.LayerType layerType,
                                                                                    EquipmentClientInfo equipmentInfo,
                                                                                    PalettedTextureManager palettedTextures) {
        Identifier textureId = trim.pattern().value().assetId();
        Identifier paletteId = trim.material().value().paletteId();

        for (EquipmentClientInfo.TrimOverride override : equipmentInfo.trimOverrides()) {
            if (override.predicate().matches(trim)) {
                textureId = override.textureId().orElse(textureId);
                paletteId = override.paletteId().orElse(null);
                break;
            }
        }

        Identifier baseTexture = textureId.withPath(path -> layerType.trimAssetPrefix() + "/" + path);
        return paletteId == null
                ? stackabletrims$createTextureWithNoPalette(baseTexture)
                : palettedTextures.getOrPrepare(baseTexture, paletteId);
    }

    @Unique
    private static PalettedTextureManager.Handle stackabletrims$createTextureWithNoPalette(Identifier texture) {
        final Identifier textureLocation = texture.withPath(path -> "textures/" + path + ".png");
        return new PalettedTextureManager.Handle() {
            @Override
            public Identifier textureLocation() {
                return textureLocation;
            }

            @Override
            public float getU(float offset) {
                return offset;
            }

            @Override
            public float getV(float offset) {
                return offset;
            }
        };
    }
}
