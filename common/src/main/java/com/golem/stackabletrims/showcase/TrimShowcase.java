package com.golem.stackabletrims.showcase;

import com.golem.stackabletrims.core.CoreServices;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;

/**
 * Builds armor stands wearing armor with stacked trims, for visually verifying rendering in-game.
 * Used by {@code /stackabletrims showcase} and by the Fabric client game test.
 */
public final class TrimShowcase {

    /** Entity tag applied to every stand spawned here, so they can be cleared again. */
    public static final String STAND_TAG = "stackabletrims_showcase";

    private TrimShowcase() {}

    /** One armor stand in the showcase row. */
    public record Entry(String label, Item[] armor, int trimCount, boolean enchanted, Integer dyeColor) {
        public static Entry of(String label, Item[] armor, int trimCount) {
            return new Entry(label, armor, trimCount, false, null);
        }
    }

    public static final Item[] DIAMOND = {Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS};
    public static final Item[] NETHERITE = {Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS};
    public static final Item[] IRON = {Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS};
    public static final Item[] GOLD = {Items.GOLDEN_HELMET, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.GOLDEN_BOOTS};
    public static final Item[] LEATHER = {Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS};

    private static final EquipmentSlot[] SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    /**
     * The default showcase row. Each entry exercises a different rendering path:
     * vanilla single trim, stacking, glint over stacked trims, per-armor trim overrides
     * (iron material on iron armor uses the darker palette), and dyed leather.
     */
    public static List<Entry> defaultEntries() {
        return List.of(
                Entry.of("No trim", DIAMOND, 0),
                Entry.of("1 trim (vanilla)", DIAMOND, 1),
                Entry.of("2 trims", DIAMOND, 2),
                Entry.of("3 trims", DIAMOND, 3),
                Entry.of("5 trims", DIAMOND, 5),
                new Entry("8 trims + glint", NETHERITE, 8, true, null),
                Entry.of("Iron: 3 trims (override)", IRON, 3),
                new Entry("Dyed leather: 3 trims", LEATHER, 3, false, DyeColor.PURPLE.getTextureDiffuseColor()),
                Entry.of("Gold: 12 trims", GOLD, 12)
        );
    }

    /** All trim patterns in the registry, sorted by id for deterministic output. */
    public static List<Holder<TrimPattern>> sortedPatterns(HolderLookup.Provider registries) {
        return registries.lookupOrThrow(Registries.TRIM_PATTERN).listElements()
                .sorted(Comparator.comparing(h -> h.key().identifier()))
                .map(h -> (Holder<TrimPattern>) h)
                .toList();
    }

    /** All trim materials in the registry, sorted by id for deterministic output. */
    public static List<Holder<TrimMaterial>> sortedMaterials(HolderLookup.Provider registries) {
        return registries.lookupOrThrow(Registries.TRIM_MATERIAL).listElements()
                .sorted(Comparator.comparing(h -> h.key().identifier()))
                .map(h -> (Holder<TrimMaterial>) h)
                .toList();
    }

    /**
     * Pick the {@code index}-th (pattern, material) pair. Walking both lists with different strides
     * keeps consecutive trims visually distinct so stacking is obvious.
     */
    public static ArmorTrim trimAt(HolderLookup.Provider registries, int index) {
        List<Holder<TrimPattern>> patterns = sortedPatterns(registries);
        List<Holder<TrimMaterial>> materials = sortedMaterials(registries);
        Holder<TrimPattern> pattern = patterns.get((index * 3) % patterns.size());
        Holder<TrimMaterial> material = materials.get((index * 5 + 1) % materials.size());
        return new ArmorTrim(material, pattern);
    }

    /** Build one armor piece with {@code trimCount} stacked trims applied via the mod's service. */
    public static ItemStack buildPiece(HolderLookup.Provider registries, Item item, int trimCount,
                                       boolean enchanted, Integer dyeColor) {
        ItemStack stack = new ItemStack(item);
        for (int i = 0; i < trimCount; i++) {
            CoreServices.trims().addTrim(stack, trimAt(registries, i), Integer.MAX_VALUE);
        }
        if (enchanted) {
            stack.enchant(registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.PROTECTION), 1);
        }
        if (dyeColor != null) {
            stack.set(DataComponents.DYED_COLOR, new DyedItemColor(dyeColor));
        }
        return stack;
    }

    /** Build a full armor set for the given entry. */
    public static ItemStack[] buildSet(HolderLookup.Provider registries, Entry entry) {
        ItemStack[] set = new ItemStack[4];
        for (int i = 0; i < 4; i++) {
            set[i] = buildPiece(registries, entry.armor()[i], entry.trimCount(), entry.enchanted(), entry.dyeColor());
        }
        return set;
    }

    /** Equip an armor stand with the full set. */
    public static void equip(ArmorStand stand, ItemStack[] set) {
        for (int i = 0; i < 4; i++) {
            stand.setItemSlot(SLOTS[i], set[i]);
        }
    }

    /** Spawn a single tagged, named armor stand at {@code pos} facing {@code yaw}. */
    public static ArmorStand spawnStand(ServerLevel level, Vec3 pos, float yaw, Component name) {
        ArmorStand stand = new ArmorStand(level, pos.x, pos.y, pos.z);
        stand.snapTo(pos.x, pos.y, pos.z, yaw, 0.0F);
        stand.setYBodyRot(yaw);
        stand.setYHeadRot(yaw);
        stand.setNoGravity(true);
        stand.setShowArms(true);
        stand.addTag(STAND_TAG);
        if (name != null) {
            stand.setCustomName(name);
            stand.setCustomNameVisible(true);
        }
        level.addFreshEntity(stand);
        return stand;
    }

    /**
     * Spawn the whole showcase row centred {@code distance} blocks in front of {@code origin}
     * (looking along {@code viewerYaw}), spaced {@code spacing} apart, all facing the viewer.
     *
     * @return number of stands spawned
     */
    public static int spawnRow(ServerLevel level, Vec3 origin, float viewerYaw, double distance, double spacing,
                               List<Entry> entries) {
        // Minecraft yaw: 0 = +Z (south), 90 = -X (west).
        double rad = Math.toRadians(viewerYaw);
        Vec3 forward = new Vec3(-Math.sin(rad), 0, Math.cos(rad));
        Vec3 right = new Vec3(-forward.z, 0, forward.x);
        Vec3 center = origin.add(forward.scale(distance));
        float standYaw = viewerYaw + 180.0F;

        int n = entries.size();
        for (int i = 0; i < n; i++) {
            double offset = (i - (n - 1) / 2.0) * spacing;
            Vec3 pos = center.add(right.scale(offset));
            Entry entry = entries.get(i);
            ArmorStand stand = spawnStand(level, pos, standYaw, Component.literal(entry.label()));
            equip(stand, buildSet(level.registryAccess(), entry));
        }
        return n;
    }

    /** Remove every stand previously spawned by the showcase in this level. */
    public static int clear(ServerLevel level) {
        int removed = 0;
        for (ArmorStand stand : level.getEntities(EntityTypes.ARMOR_STAND,
                e -> e.entityTags().contains(STAND_TAG))) {
            stand.discard();
            removed++;
        }
        return removed;
    }

    /** Short human-readable description of a trim, e.g. "sentry/quartz". */
    public static String describe(ArmorTrim trim) {
        Identifier p = trim.pattern().unwrapKey().map(k -> k.identifier()).orElse(Identifier.withDefaultNamespace("?"));
        Identifier m = trim.material().unwrapKey().map(k -> k.identifier()).orElse(Identifier.withDefaultNamespace("?"));
        return p.getPath() + "/" + m.getPath();
    }
}
