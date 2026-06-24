package at.petrak.hexcasting.common.casting.actions.spells

import at.petrak.hexcasting.api.mod.HexConfig
import at.petrak.hexcasting.api.casting.*
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.core.BlockPos
import net.minecraft.core.particles.BlockParticleOption
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.core.registries.Registries
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.tags.BlockTags
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.item.FallingBlockEntity
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.Tier
import net.minecraft.world.item.Tiers
import net.minecraft.world.item.enchantment.Enchantments
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.FallingBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.Vec3
import kotlin.math.roundToLong

// https://github.com/VazkiiMods/Botania/blob/1.21.1-porting/Xplat/src/main/java/vazkii/botania/common/item/lens/WeightLens.java
object OpFallingBlock : SpellAction {
    override val argc = 1

    override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {
        val pos = args.getVec3(0, argc)
        env.assertVecInRange(pos)

        val centered = Vec3.atCenterOf(BlockPos.containing(pos))
        return SpellAction.Result(
            Spell(pos),
            (1.5 * MediaConstants.DUST_UNIT).roundToLong(),
            listOf(ParticleSpray.burst(centered, 1.0))
        )
    }

    private data class Spell(val v: Vec3) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            val pos = BlockPos.containing(v)

            val blockstate = env.world.getBlockState(pos)
            if (!env.canEditBlockAt(pos) || !IXplatAbstractions.INSTANCE.isBreakingAllowed(env.world, pos, blockstate, env.caster))
                return

            val tier = HexConfig.server().opBreakHarvestLevel()

            val stateBelow = env.world.getBlockState(pos.below())

            if ((
                        FallingBlock.isFree(stateBelow)
                                || !stateBelow.canOcclude()
                                || stateBelow.`is`(BlockTags.SLABS)
                        )
                && !blockstate.isAir
                && blockstate.getDestroySpeed(env.world, pos) >= 0f // fix being able to break bedrock &c
                && env.world.getBlockEntity(pos) == null
                && IXplatAbstractions.INSTANCE.isCorrectTierForDrops(tier, blockstate)
                && canSilkTouch(env.world, pos, blockstate, tier, env.castingEntity as? ServerPlayer)
            ) {
                val falling: FallingBlockEntity = FallingBlockEntity.fall(env.world, pos, blockstate)
                falling.time = 1
                env.world.sendParticles(
                    BlockParticleOption(ParticleTypes.FALLING_DUST, blockstate),
                    pos.x + 0.5,
                    pos.y + 0.5,
                    pos.z + 0.5,
                    10,
                    0.45,
                    0.45,
                    0.45,
                    5.0
                )
            }
        }

        fun canSilkTouch(level: ServerLevel, pos: BlockPos, state: BlockState, harvestTier: Tier, owner: Entity?): Boolean {
            val harvestToolStack: ItemStack = getHarvestToolStack(harvestTier, state)
            if (harvestToolStack.isEmpty) {
                return false
            }
            harvestToolStack.enchant(level.holderLookup(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH), 1)
            val drops: List<ItemStack> = Block.getDrops(state, level, pos, null, owner, harvestToolStack)
            val blockItem: Item = state.block.asItem()
            return drops.any { s -> s.item === blockItem }
        }

        companion object {
            fun getHarvestToolStack(harvestTier: Tier, state: BlockState): ItemStack {
                return getTool(harvestTier, state).copy()
            }

            private fun getTool(harvestTier: Tier, state: BlockState): ItemStack {
                if (harvestTier !in HARVEST_TOOLS_BY_LEVEL.keys) return ItemStack.EMPTY
                if (!state.requiresCorrectToolForDrops()) {
                    return HARVEST_TOOLS_BY_LEVEL[harvestTier]!![0]
                }
                for (tool in HARVEST_TOOLS_BY_LEVEL[harvestTier]!!) {
                    if (tool.isCorrectToolForDrops(state)) {
                        return tool
                    }
                }
                return ItemStack.EMPTY
            }

            private val HARVEST_TOOLS_BY_LEVEL: Map<Tier, List<ItemStack>> = mapOf(
                Tiers.WOOD to stacks(Items.WOODEN_PICKAXE, Items.WOODEN_AXE, Items.WOODEN_HOE, Items.WOODEN_SHOVEL),
                Tiers.STONE to stacks(Items.STONE_PICKAXE, Items.STONE_AXE, Items.STONE_HOE, Items.STONE_SHOVEL),
                Tiers.IRON to stacks(Items.IRON_PICKAXE, Items.IRON_AXE, Items.IRON_HOE, Items.IRON_SHOVEL),
                Tiers.DIAMOND to stacks(Items.DIAMOND_PICKAXE, Items.DIAMOND_AXE, Items.DIAMOND_HOE, Items.DIAMOND_SHOVEL),
                Tiers.NETHERITE to stacks(Items.NETHERITE_PICKAXE, Items.NETHERITE_AXE, Items.NETHERITE_HOE, Items.NETHERITE_SHOVEL)
            )

            private fun stacks(vararg items: Item): List<ItemStack> {
                return items.map { item -> ItemStack(item) }
            }
        }
    }
}