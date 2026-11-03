package com.golem.stackabletrims.core;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.trim.ArmorTrim;

import java.util.ArrayList;
import java.util.List;

public final class TrimTooltipComposer {

    /** Vanilla's "Upgrade:" heading above the trim lines. */
    private static final String UPGRADE_KEY = "item.minecraft.smithing_template.upgrade";

    private TrimTooltipComposer() {}

    /** Replaces vanilla's single-trim lines in {@code tooltip} with one pattern/material pair per stacked trim. */
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

            List<Component> newLines = lines(trims);
            if (insertIndex >= 0) tooltip.addAll(insertIndex, newLines);
            else tooltip.addAll(newLines);
        });
    }

    /** The heading plus a pattern and a material line per trim, as vanilla draws a single trim. */
    public static List<Component> lines(List<ArmorTrim> trims) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable(UPGRADE_KEY).withStyle(ChatFormatting.GRAY));
        for (ArmorTrim trim : trims) {
            lines.add(CommonComponents.space().append(trim.pattern().value().copyWithStyle(trim.material())));
            lines.add(CommonComponents.space().append(trim.material().value().description()));
        }
        return lines;
    }
}
