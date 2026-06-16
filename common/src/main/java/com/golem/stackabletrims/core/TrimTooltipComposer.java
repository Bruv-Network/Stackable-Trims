package com.golem.stackabletrims.core;

import net.minecraft.ChatFormatting;
import net.minecraft.util.Util;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.trim.ArmorTrim;

import java.util.ArrayList;
import java.util.List;

public final class TrimTooltipComposer {

    private static final Component UPGRADE_TEXT = Component.translatable(
            Util.makeDescriptionId("item", Identifier.withDefaultNamespace("smithing_template.upgrade"))
    ).withStyle(ChatFormatting.GRAY);
    private static final String UPGRADE_KEY = Util.makeDescriptionId("item", Identifier.withDefaultNamespace("smithing_template.upgrade"));

    private TrimTooltipComposer() {}

    public static void compose(ItemStack stack, List<Component> tooltip) {
        CoreServices.trims().getTrims(stack).ifPresent(trims -> {
            if (trims.isEmpty()) return;

            int insertIndex = -1;
            for (int i = 0; i < tooltip.size(); i++) {
                Component c = tooltip.get(i);
                if (c.getContents() instanceof TranslatableContents tc && UPGRADE_KEY.equals(tc.getKey())) {
                    insertIndex = i;
                    tooltip.remove(i);
                    if (i < tooltip.size()) tooltip.remove(i);
                    if (i < tooltip.size()) tooltip.remove(i);
                    break;
                }
            }

            List<Component> newLines = new ArrayList<>();
            newLines.add(UPGRADE_TEXT);
            for (ArmorTrim trim : trims) {
                newLines.add(CommonComponents.space().append(trim.pattern().value().copyWithStyle(trim.material())));
                newLines.add(CommonComponents.space().append(trim.material().value().description()));
            }

            if (insertIndex >= 0) tooltip.addAll(insertIndex, newLines);
            else tooltip.addAll(newLines);
        });
    }
}
