package at.petrak.hex.datagen;

import at.petrak.hex.HexMod;
import at.petrak.hex.common.advancement.OvercastTrigger;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.critereon.*;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.function.Consumer;

public class Advancements extends AdvancementProvider {
    public static Advancement ROOT;
    public static Advancement ENLIGHTENMENT;

    public static final OvercastTrigger OVERCAST_TRIGGER = new OvercastTrigger();

    public Advancements(DataGenerator generatorIn, ExistingFileHelper fileHelperIn) {
        super(generatorIn, fileHelperIn);
    }

    @Override
    protected void registerAdvancements(Consumer<Advancement> consumer, ExistingFileHelper fileHelper) {
        ROOT = Advancement.Builder.advancement()
                // what an ergonomic design decision
                // i am so happy that data generators are the future
                .display(new DisplayInfo(new ItemStack(Items.BUDDING_AMETHYST),
                        new TranslatableComponent("advancement.hex:root"),
                        new TranslatableComponent("advancement.hex:root.desc"),
                        new ResourceLocation("minecraft", "textures/block/calcite.png"),
                        FrameType.TASK, true, true, false))
                // the only thing making this vaguely tolerable is the knowledge the json files are worse somehow
                .addCriterion("on_thingy", new TickTrigger.TriggerInstance(EntityPredicate.Composite.wrap(
                        EntityPredicate.Builder.entity()
                                .steppingOn(LocationPredicate.Builder.location()
                                        .setBlock(BlockPredicate.Builder.block()
                                                .of(Blocks.AMETHYST_BLOCK, Blocks.CALCITE)
                                                .build())
                                        .setY(MinMaxBounds.Doubles.between(-64.0, 30.0)).build())
                                .build())))
                .save(consumer, prefix("root")); // how the hell does one even read this

        ENLIGHTENMENT = Advancement.Builder.advancement()
                .display(new DisplayInfo(new ItemStack(Items.MUSIC_DISC_11),
                        new TranslatableComponent("advancement.hex:enlightenment"),
                        new TranslatableComponent("advancement.hex:enlightenment.desc"),
                        null,
                        FrameType.CHALLENGE, true, true, true))
                .addCriterion("health_used",
                        new OvercastTrigger.Instance(EntityPredicate.Composite.ANY,
                                MinMaxBounds.Ints.ANY,
                                // add a little bit of slop here
                                MinMaxBounds.Doubles.atLeast(17.95),
                                MinMaxBounds.Doubles.atMost(1.05)))
                .save(consumer, prefix("enlightenment"));

//        super.registerAdvancements(consumer, fileHelper);
    }

    public static void registerTriggers() {
        CriteriaTriggers.register(OVERCAST_TRIGGER);
    }

    protected static DisplayInfo simple(ItemLike icon, String name, FrameType frameType) {
        String expandedName = "advancement.hex:" + name;
        return new DisplayInfo(new ItemStack(icon.asItem()),
                new TranslatableComponent(expandedName),
                new TranslatableComponent(expandedName + ".desc"),
                null, frameType, true, true, false);
    }

    private static String prefix(String name) {
        return HexMod.MOD_ID + ":" + name;
    }


}
