package at.petrak.hexcasting.common.casting.actions.spells.great

import at.petrak.hexcasting.api.HexAPI
import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getMob
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.Mishap
import at.petrak.hexcasting.api.casting.mishaps.MishapAlreadyBrainswept
import at.petrak.hexcasting.api.casting.mishaps.MishapBadBrainsweep
import at.petrak.hexcasting.api.casting.mishaps.MishapBadLocation
import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.api.mod.HexConfig
import at.petrak.hexcasting.api.mod.HexTags
import at.petrak.hexcasting.common.lib.HexDamageTypes
import at.petrak.hexcasting.common.lib.HexMobEffects
import at.petrak.hexcasting.common.recipe.BrainsweepRecipe
import at.petrak.hexcasting.common.recipe.HexRecipeStuffRegistry
import at.petrak.hexcasting.ktxt.tellWitnessesThatIWasMurdered
import at.petrak.hexcasting.mixin.accessor.AccessorLivingEntity
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.core.BlockPos
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.npc.Villager
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.Vec3

object OpBrainsweep : SpellAction {
    override val argc = 2

    // this way you can hear the villager dying more : )
    override fun hasCastingSound(env: CastingEnvironment) = false

    override fun execute(
            args: List<Iota>,
            env: CastingEnvironment
    ): SpellAction.Result {
        val sacrifice = args.getMob(env.world, 0, argc)
        val vecPos = args.getVec3(1, argc)
        val pos = BlockPos.containing(vecPos)

        env.assertVecInRange(vecPos)
        env.assertEntityInRange(sacrifice)

        if (!env.canEditBlockAt(pos))
            throw MishapBadLocation(vecPos, "forbidden")

        if (sacrifice.type.`is`(HexTags.Entities.NO_BRAINSWEEPING))
            throw MishapBadBrainsweep(sacrifice, pos)

        if (IXplatAbstractions.INSTANCE.isBrainswept(sacrifice))
            throw MishapAlreadyBrainswept(sacrifice)

        // special behavior for crystallization
        if (vecPos == sacrifice.eyePosition && sacrifice.hasEffect(HexMobEffects.ENLARGE_GRID)) {
            return SpellAction.Result(
                AltSpell(sacrifice, pos),
                MediaConstants.CRYSTAL_UNIT * 10,
                listOf(ParticleSpray.burst(Vec3.atCenterOf(pos), 1.0))
            )
        }

        val state = env.world.getBlockState(pos)

        val recman = env.world.recipeManager
        val recipes = recman.getAllRecipesFor(HexRecipeStuffRegistry.BRAINSWEEP_TYPE.get())
        val recipe = recipes.asSequence()
            .map { it.value }
            .find { it.matches(state, sacrifice, env.world) }
            ?: throw MishapBadBrainsweep(sacrifice, pos)

        return SpellAction.Result(
            Spell(pos, state, sacrifice, recipe),
            recipe.mediaCost,
            listOf(ParticleSpray.cloud(sacrifice.position(), 1.0), ParticleSpray.burst(Vec3.atCenterOf(pos), 0.3, 100))
        )
    }

    private data class Spell(
        val pos: BlockPos,
        val state: BlockState,
        val sacrifice: Mob,
        val recipe: BrainsweepRecipe
    ) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            env.world.setBlockAndUpdate(pos, BrainsweepRecipe.copyProperties(state, recipe.result))

            HexAPI.instance().brainsweep(sacrifice)

            if (sacrifice is Villager && HexConfig.server().doVillagersTakeOffenseAtMindMurder()) {
                env.castingEntity?.let { sacrifice.tellWitnessesThatIWasMurdered(it) }
            }

            val sound = (sacrifice as AccessorLivingEntity).`hex$getDeathSound`()
            if (sound != null)
                env.world.playSound(null, sacrifice, sound, SoundSource.AMBIENT, 0.8f, 1f)
            env.world.playSound(null, sacrifice, SoundEvents.PLAYER_LEVELUP, SoundSource.AMBIENT, 0.5f, 0.8f)
        }
    }

    private data class AltSpell(val sacrifice: Mob, val pos: BlockPos) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            val amount = sacrifice.health * 0.1F
            Mishap.trulyHurt(sacrifice, sacrifice.damageSources().source(HexDamageTypes.OVERCAST), amount)

            val blockStateId = Block.getId(Blocks.AMETHYST_BLOCK.defaultBlockState())
            env.world.levelEvent(2001, pos, blockStateId)
            env.world.levelEvent(2001, pos.below(), blockStateId)

            sacrifice.removeEffect(HexMobEffects.ENLARGE_GRID)
            sacrifice.addEffect(MobEffectInstance(HexMobEffects.CRYSTALLIZED, -1))

            HexAPI.instance().brainsweep(sacrifice)

            if (sacrifice is Villager && HexConfig.server().doVillagersTakeOffenseAtMindMurder()) {
                env.castingEntity?.let { sacrifice.tellWitnessesThatIWasMurdered(it) }
            }

            env.world.playSound(null, sacrifice, SoundEvents.ZOMBIE_VILLAGER_CURE, SoundSource.AMBIENT, 0.8f, 1f)
        }
    }
}
