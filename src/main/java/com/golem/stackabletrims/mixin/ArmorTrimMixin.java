package com.golem.stackabletrims.mixin;

//? if <1.20.5 {
/*import com.golem.stackabletrims.StackableTrims;
import com.golem.stackabletrims.core.CoreServices;
import com.golem.stackabletrims.core.TrimTooltipComposer;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/^* Before 1.20.5 vanilla writes the trim tooltip in one static helper: list every stacked trim there instead. ^/
@Mixin(ArmorTrim.class)
public class ArmorTrimMixin {

    @Inject(method = "appendUpgradeHoverText", at = @At("HEAD"), cancellable = true)
    private static void stackabletrims$appendStacked(ItemStack stack, RegistryAccess registries, List<Component> tooltip,
                                                     CallbackInfo ci) {
        if (StackableTrims.isBetterTrimTooltipsEnabled) {
            return;
        }
        List<ArmorTrim> trims = CoreServices.trims().getTrims(stack).orElse(List.of());
        if (trims.size() <= 1) {
            return;
        }
        tooltip.addAll(TrimTooltipComposer.lines(trims));
        ci.cancel();
    }
}
*///?} else {
/** Listed before 1.20.5; newer nodes compose the tooltip in ItemStackMixin. */
public class ArmorTrimMixin {
}
//?}
