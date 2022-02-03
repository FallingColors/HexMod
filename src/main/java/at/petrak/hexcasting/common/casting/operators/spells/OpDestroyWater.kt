package at.petrak.hexcasting.common.casting.operators.spells

import at.petrak.hexcasting.api.Operator
import at.petrak.hexcasting.api.Operator.Companion.getChecked
import at.petrak.hexcasting.api.RenderedSpell
import at.petrak.hexcasting.api.SpellDatum
import at.petrak.hexcasting.api.SpellOperator
import at.petrak.hexcasting.common.casting.CastingContext
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.BucketPickup
import net.minecraft.world.level.block.LiquidBlock
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.material.Fluids
import net.minecraft.world.level.material.Material
import net.minecraft.world.phys.Vec3

object OpDestroyWater : SpellOperator {
    override val argc = 1
    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): Triple<RenderedSpell, Int, List<Vec3>> {
        val target = args.getChecked<Vec3>(0)
        ctx.assertVecInRange(target)

        return Triple(
            Spell(target),
            200_000,
            listOf(target)
        )
    }

    const val MAX_DESTROY_COUNT = 1024

    private data class Spell(val target: Vec3) : RenderedSpell {

        override fun cast(ctx: CastingContext) {
            // SpongeBlock.java
            val todo = ArrayDeque<BlockPos>()
            val seen = HashSet<BlockPos>()
            todo.add(BlockPos(target))
            for (dir in Direction.values()) { // a little extra range on the initial cast to make it feel more intuitive
                todo.add(BlockPos(target).relative(dir))
            }

            var successes = 0
            while (todo.isNotEmpty() && successes <= MAX_DESTROY_COUNT) {
                val here = todo.removeFirst()
                val distFromFocus =
                    ctx.caster.position().distanceToSqr(Vec3(here.x.toDouble(), here.y.toDouble(), here.z.toDouble()))
                if (distFromFocus < Operator.MAX_DISTANCE * Operator.MAX_DISTANCE && seen.add(here)) {
                    // never seen this pos in my life
                    val fluid = ctx.world.getFluidState(here)
                    if (fluid != Fluids.EMPTY.defaultFluidState()) {
                        val blockstate = ctx.world.getBlockState(here)
                        val material = blockstate.material
                        val success =
                            if (blockstate.block is BucketPickup && !(blockstate.block as BucketPickup).pickupBlock(
                                    ctx.world,
                                    here,
                                    blockstate
                                ).isEmpty
                            ) {
                                true
                            } else if (blockstate.block is LiquidBlock) {
                                ctx.world.setBlock(here, Blocks.AIR.defaultBlockState(), 3)
                                true
                            } else if (material == Material.WATER_PLANT || material == Material.REPLACEABLE_WATER_PLANT) {
                                val blockentity: BlockEntity? =
                                    if (blockstate.hasBlockEntity()) ctx.world.getBlockEntity(here) else null
                                Block.dropResources(blockstate, ctx.world, here, blockentity)
                                ctx.world.setBlock(here, Blocks.AIR.defaultBlockState(), 3)
                                true
                            } else {
                                false
                            }

                        if (success) {
                            ctx.world.sendParticles(
                                ParticleTypes.SMOKE,
                                here.x + 0.5 + Math.random() * 0.4 - 0.2,
                                here.y + 0.5 + Math.random() * 0.4 - 0.2,
                                here.z + 0.5 + Math.random() * 0.4 - 0.2,
                                2,
                                0.0,
                                0.05,
                                0.0,
                                0.0
                            )
                            successes++
                            for (dir in Direction.values()) {
                                todo.add(here.relative(dir))
                            }
                        }
                    }
                }
            }

            if (successes > 0) {
                ctx.world.playSound(
                    null,
                    target.x,
                    target.y,
                    target.z,
                    SoundEvents.FIRE_EXTINGUISH,
                    SoundSource.BLOCKS,
                    1.0f,
                    0.95f
                )
            }
        }
    }
}