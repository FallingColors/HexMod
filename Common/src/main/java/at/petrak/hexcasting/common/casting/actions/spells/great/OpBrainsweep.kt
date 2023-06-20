package at.petrak.hexcasting.common.casting.actions.spells.great

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getMob
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapAlreadyBrainswept
import at.petrak.hexcasting.api.casting.mishaps.MishapBadBrainsweep
import at.petrak.hexcasting.api.casting.mishaps.MishapBadLocation
import at.petrak.hexcasting.api.mod.HexConfig
import at.petrak.hexcasting.api.mod.HexTags
import at.petrak.hexcasting.common.recipe.BrainsweepRecipe
import at.petrak.hexcasting.common.recipe.HexRecipeStuffRegistry
import at.petrak.hexcasting.ktxt.tellWitnessesThatIWasMurdered
import at.petrak.hexcasting.mixin.accessor.AccessorLivingEntity
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.core.BlockPos
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.npc.Villager
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.Vec3

object OpBrainsweep : SpellAction {
    override val argc = 2

    // this way you can hear the villager dying more : )
    override fun hasCastingSound(ctx: CastingEnvironment) = false

    override fun execute(
        args: List<Iota>,
        ctx: CastingEnvironment
    ): SpellAction.Result {
        val sacrifice = args.getMob(0, argc)
        val vecPos = args.getVec3(1, argc)
        val pos = BlockPos(vecPos)

        ctx.assertVecInRange(vecPos)
        ctx.assertEntityInRange(sacrifice)

        if (!ctx.canEditBlockAt(pos))
            throw MishapBadLocation(vecPos, "forbidden")

        if (sacrifice.type.`is`(HexTags.Entities.NO_BRAINSWEEPING))
            throw MishapBadBrainsweep(sacrifice, pos)

        if (IXplatAbstractions.INSTANCE.isBrainswept(sacrifice))
            throw MishapAlreadyBrainswept(sacrifice)

        val state = ctx.world.getBlockState(pos)

        val recman = ctx.world.recipeManager
        val recipes = recman.getAllRecipesFor(HexRecipeStuffRegistry.BRAINSWEEP_TYPE)
        val recipe = recipes.find { it.matches(state, sacrifice, ctx.world) }
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
        override fun cast(ctx: CastingEnvironment) {
            ctx.world.setBlockAndUpdate(pos, BrainsweepRecipe.copyProperties(state, recipe.result))

            IXplatAbstractions.INSTANCE.setBrainsweepAddlData(sacrifice)
            if (sacrifice is Villager && HexConfig.server().doVillagersTakeOffenseAtMindMurder()) {
                ctx.caster?.let { sacrifice.tellWitnessesThatIWasMurdered(it) }
            }

            val sound = (sacrifice as AccessorLivingEntity).`hex$getDeathSound`()
            if (sound != null)
                ctx.world.playSound(null, sacrifice, sound, SoundSource.AMBIENT, 0.8f, 1f)
            ctx.world.playSound(null, sacrifice, SoundEvents.PLAYER_LEVELUP, SoundSource.AMBIENT, 0.5f, 0.8f)
        }
    }
}
