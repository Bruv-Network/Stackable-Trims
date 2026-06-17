package com.golem.stackabletrims.mixin;

import com.golem.stackabletrims.component.StackableTrimsComponents;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
     * Thread-local context to pass stacked trim data between the redirect and inject.
     */
    @Unique
    private static final ThreadLocal<RenderContext> stackabletrims$context = new ThreadLocal<>();

    /**
     * Redirect the vanilla TRIM component read to suppress vanilla's single-trim rendering
     * when stacked trims are present.
     */
    @Redirect(
            method = "renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/resources/Identifier;II)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;get(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;")
    )
    private <T> T stackabletrims$redirectTrimGet(ItemStack stack, DataComponentType<T> componentType,
                                                  EquipmentClientInfo.LayerType layerType,
                                                  ResourceKey<EquipmentAsset> equipmentAssetId,
                                                  Model<?> model, Object state,
                                                  ItemStack itemStack,
                                                  PoseStack poseStack,
                                                  SubmitNodeCollector collector,
                                                  int light,
                                                  net.minecraft.resources.Identifier playerTexture,
                                                  int dyeColor,
                                                  int orderStart) {
        // Only intercept TRIM reads, pass through anything else
        if (componentType != DataComponents.TRIM) {
            return stack.get(componentType);
        }

        List<ArmorTrim> stacked = stack.get(StackableTrimsComponents.STACKABLETRIMS);
        if (stacked != null && stacked.size() > 1) {
            // Store context for the inject to render all stacked trims
            stackabletrims$context.set(new RenderContext(
                    stacked, layerType, equipmentAssetId, model, state,
                    poseStack, collector, light, orderStart
            ));
            // Return null to suppress vanilla's single-trim rendering
            return null;
        }

        // No stacked trims — let vanilla handle it normally
        stackabletrims$context.remove();
        return stack.get(componentType);
    }

    /**
     * After the method finishes, render all stacked trims if we suppressed vanilla's render.
     */
    @Inject(
            method = "renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/resources/Identifier;II)V",
            at = @At("RETURN")
    )
    private <S> void stackabletrims$renderStackedTrims(EquipmentClientInfo.LayerType layerType,
                                                        ResourceKey<EquipmentAsset> equipmentAssetId,
                                                        Model<? super S> model, S state,
                                                        ItemStack itemStack,
                                                        PoseStack poseStack,
                                                        SubmitNodeCollector collector,
                                                        int light,
                                                        net.minecraft.resources.Identifier playerTexture,
                                                        int dyeColor,
                                                        int orderStart,
                                                        CallbackInfo ci) {
        RenderContext ctx = stackabletrims$context.get();
        if (ctx == null) return;
        stackabletrims$context.remove();

        // Don't render trims on baby models (vanilla skips this)
        if (layerType == EquipmentClientInfo.LayerType.HUMANOID_BABY) return;

        int order = orderStart;
        for (ArmorTrim trim : ctx.trims) {
            TextureAtlasSprite sprite = stackabletrims$lookupSprite(trim, ctx.layerType, ctx.equipmentAssetId);
            if (sprite == null) continue;

            RenderType renderType = Sheets.armorTrimsSheet(trim.pattern().value().decal());

            OrderedSubmitNodeCollector orderedCollector = collector.order(order++);
            stackabletrims$submitTrimModel(orderedCollector, model, state, poseStack, renderType,
                    light, sprite, dyeColor);
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

    /**
     * Type-safe wrapper to call submitModel with proper generic inference.
     */
    @Unique
    @SuppressWarnings("unchecked")
    private static <S> void stackabletrims$submitTrimModel(OrderedSubmitNodeCollector orderedCollector,
                                                            Model<? super S> model, S state,
                                                            PoseStack poseStack, RenderType renderType,
                                                            int light, TextureAtlasSprite sprite,
                                                            int dyeColor) {
        orderedCollector.submitModel(model, state, poseStack, renderType,
                light, OverlayTexture.NO_OVERLAY, -1, sprite, dyeColor, null);
    }

    @Unique
    private record RenderContext(
            List<ArmorTrim> trims,
            EquipmentClientInfo.LayerType layerType,
            ResourceKey<EquipmentAsset> equipmentAssetId,
            Model<?> model,
            Object state,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            int light,
            int orderStart
    ) {}
}
