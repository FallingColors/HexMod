package at.petrak.hexcasting.api.mod;

import at.petrak.hexcasting.api.casting.ActionRegistryEntry;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class HexTags {
    public static final class Items {
        public static final TagKey<Item> EDIFIED_LOGS = create("edified_logs");
        public static final TagKey<Item> EDIFIED_PLANKS = create("edified_planks");
        public static final TagKey<Item> STAVES = create("staves");
        public static final TagKey<Item> PHIAL_BASE = create("phial_base");
        public static final TagKey<Item> GRANTS_ROOT_ADVANCEMENT = create("grants_root_advancement");
        public static final TagKey<Item> SEAL_MATERIALS = create("seal_materials");

        public static final TagKey<Item> IMPETI = create("impeti");
        public static final TagKey<Item> DIRECTRICES = create("directrices");
        public static final TagKey<Item> MINDFLAYED_CIRCLE_COMPONENTS = create("brainswept_circle_components");

        public static TagKey<Item> create(String name) {
            return create(modLoc(name));
        }

        public static TagKey<Item> create(ResourceLocation id) {
            return TagKey.create(Registries.ITEM, id);
        }
    }

    public static final class Blocks {
        public static final TagKey<Block> EDIFIED_LOGS = create("edified_logs");
        public static final TagKey<Block> EDIFIED_PLANKS = create("edified_planks");


        public static final TagKey<Block> IMPETI = create("impeti");
        public static final TagKey<Block> DIRECTRICES = create("directrices");
        public static final TagKey<Block> MINDFLAYED_CIRCLE_COMPONENTS = create("brainswept_circle_components");

        // Used to determine what blocks should be replaced with air by OpDestroyFluid
        public static final TagKey<Block> WATER_PLANTS = create("water_plants");

        public static final TagKey<Block> CHEAP_TO_BREAK_BLOCK = create("cheap_to_break_block");

        public static TagKey<Block> create(String name) {
            return TagKey.create(Registries.BLOCK, modLoc(name));
        }
    }

    public static final class Entities {
        public static final TagKey<EntityType<?>> STICKY_TELEPORTERS = create("sticky_teleporters");
        public static final TagKey<EntityType<?>> CANNOT_TELEPORT = create("cannot_teleport");

        public static final TagKey<EntityType<?>> NO_BRAINSWEEPING = create("cannot_brainsweep");

        public static TagKey<EntityType<?>> create(String name) {
            return TagKey.create(Registries.ENTITY_TYPE, modLoc(name));
        }
    }

    public static final class Actions {
        /**
         * Actions with this tag can't be used until the caster is enlightened and send the
         * "am I not skilled enough" message
         */
        public static final TagKey<ActionRegistryEntry> REQUIRES_ENLIGHTENMENT = create("requires_enlightenment");
        /**
         * Actions where the pattern is calculated per-world
         */
        public static final TagKey<ActionRegistryEntry> PER_WORLD_PATTERN = create("per_world_pattern");

        /**
         * Actions that can cause Blind Diversion
         */
        public static final TagKey<ActionRegistryEntry> CAN_START_ENLIGHTEN = create("can_start_enlighten");

        public static TagKey<ActionRegistryEntry> create(String name) {
            return TagKey.create(IXplatAbstractions.INSTANCE.getActionRegistry().key(), modLoc(name));
        }
    }
}
