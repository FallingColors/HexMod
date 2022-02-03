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
import net.minecraft.world.InteractionHand
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.block.*
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.Vec3

object OpExtinguish : SpellOperator {
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
            // how many levels of "borrowed" code are we on now
            val todo = ArrayDeque<BlockPos>()
            val seen = HashSet<BlockPos>()
            todo.add(BlockPos(target))

            var successes = 0
            while (todo.isNotEmpty() && successes <= MAX_DESTROY_COUNT) {
                val here = todo.removeFirst()
                val distFromFocus =
                    ctx.caster.position().distanceToSqr(Vec3(here.x.toDouble(), here.y.toDouble(), here.z.toDouble()))
                val distFromTarget =
                    target.distanceTo(Vec3(here.x.toDouble(), here.y.toDouble(), here.z.toDouble())) // max distance to prevent runaway shenanigans
                if (distFromFocus < Operator.MAX_DISTANCE * Operator.MAX_DISTANCE && seen.add(here) && distFromTarget < 10) {
                    // never seen this pos in my life
                    val blockstate = ctx.world.getBlockState(here)
                    val success =
                    when (blockstate.block) {
                        is BaseFireBlock -> {ctx.world.setBlock(here, Blocks.AIR.defaultBlockState(), 3); true}
                        is CampfireBlock -> {if (blockstate.getValue(CampfireBlock.LIT)) { // check if campfire is lit before putting it out
                            val wilson = Items.WOODEN_SHOVEL // summon shovel from the ether to do our bidding
                            val hereVec = (Vec3(here.x.toDouble(), here.y.toDouble(), here.z.toDouble()))
                            wilson.useOn(UseOnContext(ctx.world, null, InteractionHand.MAIN_HAND, ItemStack(wilson.asItem()), BlockHitResult(hereVec, Direction.UP, here, false))); true}
                            else false}
                        is AbstractCandleBlock -> {
                            if (blockstate.getValue(AbstractCandleBlock.LIT)) { // same check for candles
                                AbstractCandleBlock.extinguish(null, blockstate, ctx.world, here); true}
                            else false}
                        is NetherPortalBlock -> {ctx.world.setBlock(here, Blocks.AIR.defaultBlockState(), 3); true}
                        else -> false
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
                    }
                    for (dir in Direction.values()) {
                        todo.add(here.relative(dir))
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