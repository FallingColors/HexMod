package at.petrak.hexcasting.common.casting.actions.spells

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getBlockPos
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapBadBlock
import at.petrak.hexcasting.api.casting.mishaps.MishapBadLocation
import at.petrak.hexcasting.api.casting.mishaps.MishapLackingHotbarItem
import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.mixin.accessor.AccessorBlockItem
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.particles.BlockParticleOption
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.core.registries.Registries
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.block.state.pattern.BlockInWorld
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.Vec3

object OpPlaceBlock : SpellAction {
    override val argc: Int
        get() = 1

    override fun execute(
        args: List<Iota>,
        env: CastingEnvironment
    ): SpellAction.Result {
        val pos = args.getBlockPos(0, argc)
        env.assertPosInRangeForEditing(pos)

        val blockHit = BlockHitResult(
            Vec3.atCenterOf(pos), env.castingEntity?.direction ?: Direction.NORTH, pos, false
        )
        val itemUseCtx = env
            .queryForMatchingStack { it.item is BlockItem }
            ?.let { UseOnContext(env.world, env.castingEntity as? ServerPlayer, env.otherHand, it, blockHit) }
            ?: throw MishapLackingHotbarItem.of("placeable")
        val placeContext = BlockPlaceContext(itemUseCtx)

        assertCanPlaceAt(env, pos, placeContext)

        return SpellAction.Result(
            Spell(pos),
            MediaConstants.DUST_UNIT / 8,
            listOf(ParticleSpray.cloud(Vec3.atCenterOf(pos), 1.0))
        )
    }

    fun assertCanPlaceAt(env: CastingEnvironment, pos: BlockPos, placeContext: BlockPlaceContext) {
        // stepping through all the checks that the spell performs
        val casterPlayer = env.castingEntity as? Player
        val stack = placeContext.itemInHand

        // XXX: this might have side effects from other mods, is it safe to call twice/call it here?
        if (!IXplatAbstractions.INSTANCE.isPlacingAllowed(env.world, pos, stack, casterPlayer))
            throw MishapBadLocation(Vec3.atCenterOf(pos), "forbidden")

        val worldState = env.world.getBlockState(pos)
        if (!worldState.canBeReplaced(placeContext))
            throw MishapBadBlock.of(pos, "replaceable")

        if (!env.withdrawItem({ItemStack.isSameItemSameTags(it, stack)}, 1, false)) {
            throw MishapLackingHotbarItem.of("placable")
        }

        // Begin checks from ItemStack.useOn()

        if (
            casterPlayer != null
            && !casterPlayer.abilities.mayBuild
            && !stack.hasAdventureModePlaceTagForBlock(
                env.world.registryAccess().registryOrThrow(Registries.BLOCK),
                BlockInWorld(env.world, pos, false)
            )
        ) {
            // Adventure mode check, assertPosInRangeForEditing should already catch this but just in case
            throw MishapBadLocation(Vec3.atCenterOf(pos), "forbidden")
        }

        val item = stack.item as BlockItem
        // Checks in BlockItem.useOn -> BlockItem.place
        if (!item.block.isEnabled(env.world.enabledFeatures())) {
            // XXX: ideally we never select this for placement at all
            throw MishapLackingHotbarItem.of("placable")
        }

        if (!placeContext.canPlace()) {
            throw MishapBadBlock.of(pos, "replacable")
        }

        val newPlacementContext = item.updatePlacementContext(placeContext)
            ?: throw MishapBadLocation(Vec3.atCenterOf(pos), "obstructed")
        // in vanilla, this only happens for wall-likes that are obstructed by entities
        //  or attempting to place op-only blocks. assume the latter doesn't happen, because
        //  we don't have a way to really differentiate

        val hasPlacementState = item.block.getStateForPlacement(newPlacementContext)
            ?.let { (item as AccessorBlockItem).`hex$canPlace`(newPlacementContext, it) }
            ?: false
        if (!hasPlacementState) {
            // likely has an entity blocking placement
            throw MishapBadLocation(Vec3.atCenterOf(pos), "obstructed")
        }

        // Checks in BlockItem.placeBlock -> ServerLevel.setBlock
        if (env.world.isOutsideBuildHeight(pos)) {
            // probably redundant but better safe than sorry
            throw MishapBadLocation(Vec3.atCenterOf(pos), "out_of_world")
        }

        if (env.world.isDebug) {
            // debug world type cannot have blocks edited
            throw MishapBadLocation(Vec3.atCenterOf(pos), "forbidden")
        }

        // Checks in LevelChunk.setBlockState shouldn't trip for OpPlaceBlock
        // checks: chunk fully air, blockstate identical to current
    }

    private data class Spell(val pos: BlockPos) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            val caster = env.castingEntity

            val blockHit = BlockHitResult(
                Vec3.atCenterOf(pos), caster?.direction ?: Direction.NORTH, pos, false
            )

            val bstate = env.world.getBlockState(pos)
            val placeeStack = env.queryForMatchingStack { it.item is BlockItem }
            if (placeeStack != null) {
                if (!IXplatAbstractions.INSTANCE.isPlacingAllowed(env.world, pos, placeeStack, caster as? ServerPlayer))
                    return

                if (!placeeStack.isEmpty) {
                    // https://github.com/VazkiiMods/Psi/blob/master/src/main/java/vazkii/psi/common/spell/trick/block/PieceTrickPlaceBlock.java#L143
                    val spoofedStack = placeeStack.copy()

                    // we temporarily give the player the stack, place it using mc code, then give them the old stack back.
                    spoofedStack.count = 1

                    val itemUseCtx =
                        UseOnContext(env.world, caster as? ServerPlayer, env.otherHand, spoofedStack, blockHit)
                    val placeContext = BlockPlaceContext(itemUseCtx)
                    if (bstate.canBeReplaced(placeContext)) {
                        if (env.withdrawItem({ ItemStack.isSameItemSameTags(it, placeeStack) }, 1, false)) {
                            val res = spoofedStack.useOn(placeContext)

                            if (res != InteractionResult.FAIL) {
                                env.withdrawItem({ ItemStack.isSameItemSameTags(it, placeeStack) }, 1, true)

                                env.world.playSound(
                                    caster as? ServerPlayer,
                                    pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(),
                                    bstate.soundType.placeSound, SoundSource.BLOCKS, 1.0f,
                                    1.0f + (Math.random() * 0.5 - 0.25).toFloat()
                                )
                                val particle = BlockParticleOption(ParticleTypes.BLOCK, bstate)
                                env.world.sendParticles(
                                    particle, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(),
                                    4, 0.1, 0.2, 0.1, 0.1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
