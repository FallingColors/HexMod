package at.petrak.hexcasting.datagen;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.advancements.FailToCastGreatSpellTrigger;
import at.petrak.hexcasting.api.advancements.OvercastTrigger;
import at.petrak.hexcasting.api.advancements.SpendManaTrigger;
import at.petrak.hexcasting.api.misc.ManaConstants;
import at.petrak.hexcasting.common.items.ItemLoreFragment;
import at.petrak.hexcasting.common.lib.HexBlocks;
import at.petrak.hexcasting.common.lib.HexItems;
import at.petrak.paucal.api.datagen.PaucalAdvancementProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.critereon.*;
import net.minecraft.data.DataGenerator;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.function.Consumer;

public class HexAdvancements extends PaucalAdvancementProvider {
    public HexAdvancements(DataGenerator generatorIn) {
        super(generatorIn, HexAPI.MOD_ID);
    }

    @Override
    protected void makeAdvancements(Consumer<Advancement> consumer) {
        var root = Advancement.Builder.advancement()
            // what an ergonomic design decision
            // i am so happy that data generators are the future
            .display(new DisplayInfo(new ItemStack(Items.BUDDING_AMETHYST),
                new TranslatableComponent("advancement.hexcasting:root"),
                new TranslatableComponent("advancement.hexcasting:root.desc"),
                new ResourceLocation("minecraft", "textures/block/calcite.png"),
                FrameType.TASK, true, true, true))
            // the only thing making this vaguely tolerable is the knowledge the json files are worse somehow
            .addCriterion("has_charged_amethyst",
                InventoryChangeTrigger.TriggerInstance.hasItems(HexItems.CHARGED_AMETHYST))
            .save(consumer, prefix("root")); // how the hell does one even read this

        // weird names so we have alphabetical parity
        Advancement.Builder.advancement()
            .display(simpleDisplay(Items.GLISTERING_MELON_SLICE, "wasteful_cast", FrameType.TASK))
            .parent(root)
            .addCriterion("waste_amt", new SpendManaTrigger.Instance(EntityPredicate.Composite.ANY,
                MinMaxBounds.Ints.ANY,
                MinMaxBounds.Ints.atLeast(89 * ManaConstants.DUST_UNIT / 10)))
            .save(consumer, prefix("aaa_wasteful_cast"));
        Advancement.Builder.advancement()
            .display(simpleDisplay(HexItems.CHARGED_AMETHYST, "big_cast", FrameType.TASK))
            .parent(root)
            .addCriterion("cast_amt", new SpendManaTrigger.Instance(EntityPredicate.Composite.ANY,
                MinMaxBounds.Ints.atLeast(64 * ManaConstants.CRYSTAL_UNIT),
                MinMaxBounds.Ints.ANY))
            .save(consumer, prefix("aab_big_cast"));

        var impotence = Advancement.Builder.advancement()
            .display(simpleDisplay(Items.BLAZE_POWDER, "y_u_no_cast_angy", FrameType.TASK))
            .parent(root)
            .addCriterion("did_the_thing",
                new FailToCastGreatSpellTrigger.Instance(EntityPredicate.Composite.ANY))
            .save(consumer, prefix("y_u_no_cast_angy"));

        var opened_eyes = Advancement.Builder.advancement()
            .display(simpleDisplay(Items.ENDER_EYE, "opened_eyes", FrameType.TASK))
            .parent(impotence)
            .addCriterion("health_used",
                new OvercastTrigger.Instance(EntityPredicate.Composite.ANY,
                    MinMaxBounds.Ints.ANY,
                    MinMaxBounds.Doubles.ANY,
                    // you can't just kill yourself
                    MinMaxBounds.Doubles.atLeast(0.1)))
            .save(consumer, prefix("opened_eyes"));

        Advancement.Builder.advancement()
            .display(new DisplayInfo(new ItemStack(Items.MUSIC_DISC_11),
                new TranslatableComponent("advancement.hexcasting:enlightenment"),
                new TranslatableComponent("advancement.hexcasting:enlightenment.desc"),
                null,
                FrameType.CHALLENGE, true, true, true))
            .parent(opened_eyes)
            .addCriterion("health_used",
                new OvercastTrigger.Instance(EntityPredicate.Composite.ANY,
                    MinMaxBounds.Ints.ANY,
                    // add a little bit of slop here
                    MinMaxBounds.Doubles.atLeast(0.8),
                    MinMaxBounds.Doubles.between(0.1, 2.05)))
            .save(consumer, prefix("enlightenment"));

        var loreRoot = Advancement.Builder.advancement()
            .display(simpleDisplayWithBackground(HexBlocks.AKASHIC_LIGATURE, "lore", FrameType.GOAL,
                modLoc("textures/block/slate.png")))
            .addCriterion("used_item", new ConsumeItemTrigger.TriggerInstance(EntityPredicate.Composite.ANY,
                ItemPredicate.Builder.item().of(HexItems.LORE_FRAGMENT).build()))
            .save(consumer, prefix("lore"));

        for (var advId : ItemLoreFragment.NAMES) {
            Advancement.Builder.advancement()
                .display(new DisplayInfo(new ItemStack(HexItems.LORE_FRAGMENT),
                    new TranslatableComponent("advancement." + advId), TextComponent.EMPTY,
                    null, FrameType.TASK, true, true, true))
                .parent(loreRoot)
                .addCriterion(ItemLoreFragment.CRITEREON_KEY, new ImpossibleTrigger.TriggerInstance())
                .save(consumer, advId.toString());
        }

//        super.registerAdvancements(consumer, fileHelper);
    }
}
