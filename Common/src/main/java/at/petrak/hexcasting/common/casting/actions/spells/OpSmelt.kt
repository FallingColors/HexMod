package at.petrak.hexcasting.common.casting.actions.spells

import at.petrak.hexcasting.api.casting.*
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.EntityIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.Vec3Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.RecipeHolder
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.item.crafting.SingleRecipeInput
import net.minecraft.world.item.crafting.SmeltingRecipe
import net.minecraft.world.phys.Vec3
import java.util.*
import kotlin.math.roundToLong

object OpSmelt : SpellAction {
    override val argc = 1

    override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {
        when (val target = args[0]) {
            is EntityIota -> {
                val itemEntity = args.getItemEntity(env.world, 0, argc)
                env.assertEntityInRange(itemEntity)
                return SpellAction.Result(
                    ItemSpell(itemEntity),
                    (itemEntity.item.count * 0.75 * MediaConstants.DUST_UNIT).roundToLong(),
                    listOf(ParticleSpray.burst(itemEntity.position(), 1.0))
                )
            }
            is Vec3Iota -> {
                val pos = args.getBlockPos(0, argc)
                env.assertPosInRangeForEditing(pos)
                return SpellAction.Result(
                    BlockSpell(pos),
                    (0.75 * MediaConstants.DUST_UNIT).roundToLong(),
                    listOf(ParticleSpray.burst(Vec3.atCenterOf(pos), 1.0))
                )
            }
            else -> throw MishapInvalidIota.ofType(target, 0, "entity_or_vector")
        }
    }

    fun smeltResult(item: Item, env: CastingEnvironment): ItemStack? {
        val optional: Optional<RecipeHolder<SmeltingRecipe>> = env.world.recipeManager.getRecipeFor(
            RecipeType.SMELTING, SingleRecipeInput(ItemStack(item, 1)),
            env.world
        )

        if (!optional.isPresent) return null

        val result = optional.get().value.getResultItem(env.world.registryAccess()).copy()

        if (result.isEmpty) return null

        return result
    }

    private data class ItemSpell(val itemEntity: ItemEntity) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            val result = smeltResult(itemEntity.item.item, env) ?: return // cursed .item.item to map from ItemEntity to ItemLike to ItemStack

            result.count *= itemEntity.item.count

            env.world.addFreshEntity(ItemEntity(env.world, itemEntity.x, itemEntity.y, itemEntity.z, result.copy()))
            itemEntity.remove(Entity.RemovalReason.DISCARDED)
        }
    }

    private data class BlockSpell(val pos: BlockPos) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            if (!env.canEditBlockAt(pos)) return
            val blockState = env.world.getBlockState(pos)
            if (!IXplatAbstractions.INSTANCE.isBreakingAllowed(env.world, pos, blockState, env.castingEntity as? ServerPlayer)) return

            val itemStack = smeltResult(blockState.block.asItem(), env) ?: return

            if (itemStack.item is BlockItem) {
                env.world.setBlockAndUpdate(pos, (itemStack.item as BlockItem).block.defaultBlockState())

                if (itemStack.count > 1) {
                    itemStack.count -= 1
                    env.world.addFreshEntity(ItemEntity(env.world, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), itemStack.copy()))
                }
            } else {
                env.world.destroyBlock(pos, false, env.caster)
                env.world.addFreshEntity(ItemEntity(env.world, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), itemStack.copy()))
                // Send a block update, also copied from Ars Nouveau (this is all copied from Ars Nouveau)
                if (!env.world.isOutsideBuildHeight(pos))
                    env.world.sendBlockUpdated(pos, env.world.getBlockState(pos), env.world.getBlockState(pos), 3) // don't know how this works
            }
        }
    }
}