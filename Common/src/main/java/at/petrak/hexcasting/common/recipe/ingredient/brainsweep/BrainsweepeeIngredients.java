package at.petrak.hexcasting.common.recipe.ingredient.brainsweep;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.common.lib.HexRegistries;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
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

public class BrainsweepeeIngredients {
    public static final Codec<BrainsweepeeIngredient> TYPED_CODEC = Codec.lazyInitialized(() -> IXplatAbstractions.INSTANCE
            .getBrainsweepeeIngredientRegistry()
            .byNameCodec()
            .dispatch("type", BrainsweepeeIngredient::getType, BrainsweepeeIngredientType::codec));
    public static final StreamCodec<RegistryFriendlyByteBuf, BrainsweepeeIngredient> TYPED_STREAM_CODEC = ByteBufCodecs
            .registry(HexRegistries.BRAINSWEEPEE_INGREDIENT)
            .dispatch(BrainsweepeeIngredient::getType, BrainsweepeeIngredientType::streamCodec);

    public static final BrainsweepeeIngredientType<EntityTypeIngredient> ENTITY_TYPE = new EntityTypeIngredient.Type();
    public static final BrainsweepeeIngredientType<EntityTagIngredient> TAG = new EntityTagIngredient.Type();
    public static final BrainsweepeeIngredientType<VillagerIngredient> VILLAGER = new VillagerIngredient.Type();

    public static final BrainsweepeeIngredientType<? extends BrainsweepeeIngredient> NONE_TYPE = new BrainsweepeeIngredientType<>() {
        @Override
        public MapCodec<BrainsweepeeIngredient> codec() {
            return MapCodec.unit(NONE);
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, BrainsweepeeIngredient> streamCodec() {
            return StreamCodec.unit(NONE);
        }
    };

    public static final BrainsweepeeIngredient NONE = new BrainsweepeeIngredient() {

        @Override
        public BrainsweepeeIngredientType<?> getType() {
            return NONE_TYPE;
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
        public @Nullable Entity exampleEntity(Level level) {
            return null;
        }
    };

    public static void register(BiConsumer<BrainsweepeeIngredientType<?>, ResourceLocation> r) {
        r.accept(NONE_TYPE, HexAPI.modLoc("none"));
        r.accept(ENTITY_TYPE, HexAPI.modLoc("entity_type"));
        r.accept(TAG, HexAPI.modLoc("entity_tag"));
        r.accept(VILLAGER, HexAPI.modLoc("villager"));
    }

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
