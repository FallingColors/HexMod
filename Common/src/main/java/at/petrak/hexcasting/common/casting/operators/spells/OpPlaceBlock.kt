package at.petrak.hexcasting.common.casting.operators.spells

import at.petrak.hexcasting.api.misc.ManaConstants
import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.mishaps.MishapBadBlock
import net.minecraft.core.BlockPos
import net.minecraft.core.particles.BlockParticleOption
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.Vec3

object OpPlaceBlock : SpellOperator {
    override val argc: Int
        get() = 1

    override fun execute(
        args: List<SpellDatum<*>>,
        ctx: CastingContext
    ): Triple<RenderedSpell, Int, List<ParticleSpray>>? {
        val target = args.getChecked<Vec3>(0, argc)
        ctx.assertVecInRange(target)

        val pos = BlockPos(target)

        if (!ctx.world.mayInteract(ctx.caster, pos))
            return null


        val blockHit = BlockHitResult(
            Vec3.ZERO, ctx.caster.direction, pos, false
        )
        val itemUseCtx = UseOnContext(ctx.caster, ctx.castingHand, blockHit)
        val placeContext = BlockPlaceContext(itemUseCtx)

        val worldState = ctx.world.getBlockState(pos)
        if (!worldState.canBeReplaced(placeContext))
            throw MishapBadBlock.of(pos, "replaceable")

        return Triple(
            Spell(target),
            ManaConstants.DUST_UNIT,
            listOf(ParticleSpray.Cloud(Vec3.atCenterOf(pos), 1.0))
        )
    }

    private data class Spell(val vec: Vec3) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            val pos = BlockPos(vec)

            if (!ctx.world.mayInteract(ctx.caster, pos))
                return

            val blockHit = BlockHitResult(
                Vec3.ZERO, ctx.caster.direction, pos, false
            )
            val itemUseCtx = UseOnContext(ctx.caster, ctx.castingHand, blockHit)
            val placeContext = BlockPlaceContext(itemUseCtx)

            val bstate = ctx.world.getBlockState(pos)
            if (bstate.canBeReplaced(placeContext)) {
                val placeeSlot = ctx.getOperativeSlot { it.item is BlockItem }
                if (placeeSlot != null) {
                    val placeeStack = ctx.caster.inventory.getItem(placeeSlot).copy()
                    if (!placeeStack.isEmpty) {
                        val placee = placeeStack.item as BlockItem
                        if (ctx.withdrawItem(placee, 1, false)) {
                            // https://github.com/VazkiiMods/Psi/blob/master/src/main/java/vazkii/psi/common/spell/trick/block/PieceTrickPlaceBlock.java#L143
                            // TODO do we need to add this back somehow
                            // val evt = BlockEvent.EntityPlaceEvent(
                            //     BlockSnapshot.create(ctx.world.dimension(), ctx.world, pos),
                            //     ctx.world.getBlockState(pos.above()),
                            //     ctx.caster
                            // )
                            // MinecraftForge.EVENT_BUS.post(evt)

                            // we temporarily give the player the stack, place it using mc code, then give them the old stack back.
                            val oldStack = ctx.caster.getItemInHand(ctx.castingHand)
                            val spoofedStack = placeeStack.copy()
                            spoofedStack.count = 1
                            ctx.caster.setItemInHand(ctx.castingHand, spoofedStack)

                            val res = spoofedStack.useOn(placeContext)

                            ctx.caster.setItemInHand(ctx.castingHand, oldStack)
                            if (res != InteractionResult.FAIL) {
                                ctx.withdrawItem(placee, 1, true)

                                ctx.world.playSound(
                                    ctx.caster,
                                    vec.x, vec.y, vec.z, bstate.soundType.placeSound, SoundSource.BLOCKS, 1.0f,
                                    1.0f + (Math.random() * 0.5 - 0.25).toFloat()
                                )
                                val particle = BlockParticleOption(ParticleTypes.BLOCK, bstate)
                                ctx.world.sendParticles(particle, vec.x, vec.y, vec.z, 4, 0.1, 0.2, 0.1, 0.1)
                            }
                        }
                    }
                }
            }
        }
    }
}
