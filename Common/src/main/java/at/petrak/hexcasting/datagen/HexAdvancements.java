package at.petrak.hexcasting.datagen;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.advancements.FailToCastGreatSpellTrigger;
import at.petrak.hexcasting.api.advancements.MinMaxLongs;
import at.petrak.hexcasting.api.advancements.OvercastTrigger;
import at.petrak.hexcasting.api.advancements.SpendMediaTrigger;
import at.petrak.hexcasting.api.misc.MediaConstants;
import at.petrak.hexcasting.api.mod.HexTags;
import at.petrak.hexcasting.common.items.ItemLoreFragment;
import at.petrak.hexcasting.common.lib.HexBlocks;
import at.petrak.hexcasting.common.lib.HexItems;
import at.petrak.paucal.api.datagen.PaucalAdvancementSubProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.function.Consumer;

public class HexAdvancements extends PaucalAdvancementSubProvider {
    public static final OvercastTrigger.Instance ENLIGHTEN =
        new OvercastTrigger.Instance(ContextAwarePredicate.ANY,
            MinMaxBounds.Ints.ANY,
            // add a little bit of slop here. use 80% or more health ...
            MinMaxBounds.Doubles.atLeast(0.8),
            // and be left with under 1 healthpoint (half a heart)
            // TODO this means if 80% of your health is less than half a heart, so if you have 2.5 hearts or
            //  less, you can't become enlightened.
            MinMaxBounds.Doubles.between(Double.MIN_NORMAL, 1.0));

    public HexAdvancements() {
        super(HexAPI.MOD_ID);
    }

    @Override
    public void generate(HolderLookup.Provider provider, Consumer<Advancement> consumer) {
        var root = Advancement.Builder.advancement()
            // what an ergonomic design decision
            // i am so happy that data generators are the future
            .display(new DisplayInfo(new ItemStack(Items.BUDDING_AMETHYST),
                Component.translatable("advancement.hexcasting:root"),
                Component.translatable("advancement.hexcasting:root.desc"),
                new ResourceLocation("minecraft", "textures/block/calcite.png"),
                FrameType.TASK, true, true, true))
            // the only thing making this vaguely tolerable is the knowledge the json files are worse somehow
            .addCriterion("has_charged_amethyst", InventoryChangeTrigger.TriggerInstance.hasItems(
                ItemPredicate.Builder.item().of(HexTags.Items.GRANTS_ROOT_ADVANCEMENT).build()))
            .save(consumer, prefix("root")); // how the hell does one even read this

        // weird names so we have alphabetical parity
        Advancement.Builder.advancement()
            .display(simpleDisplay(Items.GLISTERING_MELON_SLICE, "wasteful_cast", FrameType.TASK))
            .parent(root)
            .addCriterion("waste_amt", new SpendMediaTrigger.Instance(ContextAwarePredicate.ANY,
                MinMaxLongs.ANY,
                MinMaxLongs.atLeast(89 * MediaConstants.DUST_UNIT / 10)))
            .save(consumer, prefix("aaa_wasteful_cast"));
        Advancement.Builder.advancement()
            .display(simpleDisplay(HexItems.CHARGED_AMETHYST, "big_cast", FrameType.TASK))
            .parent(root)
            .addCriterion("cast_amt", new SpendMediaTrigger.Instance(ContextAwarePredicate.ANY,
                MinMaxLongs.atLeast(64 * MediaConstants.CRYSTAL_UNIT),
                MinMaxLongs.ANY))
            .save(consumer, prefix("aab_big_cast"));

        var impotence = Advancement.Builder.advancement()
            .display(simpleDisplay(Items.BLAZE_POWDER, "y_u_no_cast_angy", FrameType.TASK))
            .parent(root)
            .addCriterion("did_the_thing",
                new FailToCastGreatSpellTrigger.Instance(ContextAwarePredicate.ANY))
            .save(consumer, prefix("y_u_no_cast_angy"));

        var opened_eyes = Advancement.Builder.advancement()
            .display(simpleDisplay(Items.ENDER_EYE, "opened_eyes", FrameType.TASK))
            .parent(impotence)
            .addCriterion("health_used",
                new OvercastTrigger.Instance(ContextAwarePredicate.ANY,
                    MinMaxBounds.Ints.ANY,
                    MinMaxBounds.Doubles.ANY,
                    // you can't just kill yourself
                    MinMaxBounds.Doubles.atLeast(0.0)))
            .save(consumer, prefix("opened_eyes"));

        Advancement.Builder.advancement()
            .display(new DisplayInfo(new ItemStack(Items.MUSIC_DISC_11),
                Component.translatable("advancement.hexcasting:enlightenment"),
                Component.translatable("advancement.hexcasting:enlightenment.desc"),
                null,
                FrameType.CHALLENGE, true, true, true))
            .parent(opened_eyes)
            .addCriterion("health_used", ENLIGHTEN)
            .save(consumer, prefix("enlightenment"));

        var loreRoot = Advancement.Builder.advancement()
            .display(simpleDisplayWithBackground(HexBlocks.AKASHIC_LIGATURE, "lore", FrameType.GOAL,
                modLoc("textures/block/slate.png")))
            .addCriterion("used_item", new ConsumeItemTrigger.TriggerInstance(ContextAwarePredicate.ANY,
                ItemPredicate.Builder.item().of(HexItems.LORE_FRAGMENT).build()))
            .save(consumer, prefix("lore"));

        for (var advId : ItemLoreFragment.NAMES) {
            Advancement.Builder.advancement()
                .display(new DisplayInfo(new ItemStack(HexItems.LORE_FRAGMENT),
                    Component.translatable("advancement." + advId), Component.empty(),
                    null, FrameType.TASK, true, true, true))
                .parent(loreRoot)
                .addCriterion(ItemLoreFragment.CRITEREON_KEY, new ImpossibleTrigger.TriggerInstance())
                .save(consumer, advId.toString());
        }

//        super.registerAdvancements(consumer, fileHelper);
    }
}
