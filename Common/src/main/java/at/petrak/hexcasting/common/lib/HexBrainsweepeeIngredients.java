package at.petrak.hexcasting.common.lib;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.common.recipe.ingredient.brainsweep.*;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import at.petrak.hexcasting.xplat.IXplatRegister;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class HexBrainsweepeeIngredients {
    private static final IXplatRegister<BrainsweepeeIngredientType<?>> REGISTER = IXplatAbstractions.INSTANCE.createRegistar(HexRegistries.BRAINSWEEPEE_INGREDIENT);

    public static void register() {
        REGISTER.registerAll();
    }

    public static final Codec<BrainsweepeeIngredient> TYPED_CODEC = Codec.lazyInitialized(() -> IXplatAbstractions.INSTANCE
            .getBrainsweepeeIngredientRegistry()
            .byNameCodec()
            .dispatch("type", BrainsweepeeIngredient::getType, BrainsweepeeIngredientType::codec));
    public static final StreamCodec<RegistryFriendlyByteBuf, BrainsweepeeIngredient> TYPED_STREAM_CODEC = ByteBufCodecs
            .registry(HexRegistries.BRAINSWEEPEE_INGREDIENT)
            .dispatch(BrainsweepeeIngredient::getType, BrainsweepeeIngredientType::streamCodec);

    public static final Supplier<BrainsweepeeIngredientType<EntityTypeIngredient>> ENTITY_TYPE = REGISTER.register("entity_type", EntityTypeIngredient.Type::new);
    public static final Supplier<BrainsweepeeIngredientType<EntityTagIngredient>> TAG = REGISTER.register("entity_tag", EntityTagIngredient.Type::new);
    public static final Supplier<BrainsweepeeIngredientType<VillagerIngredient>> VILLAGER = REGISTER.register("villager", VillagerIngredient.Type::new);

    public static final Supplier<BrainsweepeeIngredientType<? extends BrainsweepeeIngredient>> NONE_TYPE = REGISTER.register("none", () -> new BrainsweepeeIngredientType<>() {
        @Override
        public MapCodec<BrainsweepeeIngredient> codec() {
            return MapCodec.unit(NONE);
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, BrainsweepeeIngredient> streamCodec() {
            return StreamCodec.unit(NONE);
        }
    });

    public static final BrainsweepeeIngredient NONE = new BrainsweepeeIngredient() {

        @Override
        public BrainsweepeeIngredientType<?> getType() {
            return NONE_TYPE.get();
        }

        @Override
        public boolean test(Entity entity, ServerLevel level) {
            return false;
        }

        @Override
        public Component getName() {
            return Component.literal("none");
        }

        @Override
        public List<Component> getTooltip(boolean advanced) {
            return List.of();
        }

        @Override
        public String getSomeKindOfReasonableIDForEmi() {
            return "none";
        }

        @Override
        public List<Entity> exampleEntities(Level level) {
            return List.of();
        }
    };

    public static BrainsweepeeIngredient of(EntityType<?> entityType) {
        return new EntityTypeIngredient(entityType);
    }

    public static BrainsweepeeIngredient of(TagKey<EntityType<?>> tagKey) {
        return new EntityTagIngredient(tagKey);
    }

    public static BrainsweepeeIngredient of(
        @Nullable VillagerProfession profession,
        @Nullable VillagerType biome,
        int minLevel
    ) {
        return new VillagerIngredient(profession, biome, minLevel);
    }
}
