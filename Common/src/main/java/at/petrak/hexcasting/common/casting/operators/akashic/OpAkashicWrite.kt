package at.petrak.hexcasting.common.casting.operators.akashic

import at.petrak.hexcasting.api.misc.ManaConstants
import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.math.HexPattern
import at.petrak.hexcasting.api.spell.mishaps.MishapNoAkashicRecord
import at.petrak.hexcasting.api.spell.mishaps.MishapOthersName
import at.petrak.hexcasting.common.blocks.akashic.BlockEntityAkashicRecord
import at.petrak.hexcasting.common.lib.HexSounds
import net.minecraft.sounds.SoundSource

object OpAkashicWrite : SpellAction {
    override val argc = 3

    override val isGreat = true
    override val alwaysProcessGreatSpell = false
    override val causesBlindDiversion = false

    override fun execute(
        args: List<Iota>,
        ctx: CastingContext
    ): Triple<RenderedSpell, Int, List<ParticleSpray>> {
        val pos = args.getBlockPos(0, argc)
        val key = args.getPattern(1, argc)
        val datum = args.get(2)

        ctx.assertVecInRange(pos)

        val tile = ctx.world.getBlockEntity(pos)
        if (tile !is BlockEntityAkashicRecord) {
            throw MishapNoAkashicRecord(pos)
        }

        val trueName = MishapOthersName.getTrueNameFromDatum(datum, ctx.caster)
        if (trueName != null)
            throw MishapOthersName(trueName)

        return Triple(
            Spell(tile, key, datum),
            ManaConstants.DUST_UNIT,
            listOf()
        )
    }

    private data class Spell(val record: BlockEntityAkashicRecord, val key: HexPattern, val datum: Iota) :
        RenderedSpell {
        override fun cast(ctx: CastingContext) {
            record.addNewDatum(key, datum)

            ctx.world.playSound(
                null, record.blockPos, HexSounds.SCROLL_SCRIBBLE, SoundSource.BLOCKS,
                1f, 0.8f
            )

            // val colorizer = HexPlayerDataHelper.getColorizer(ctx.caster)
            // val normal = record.blockState.getValue(BlockAkashicBookshelf.FACING).normal
            // ParticleSpray(
            //     Vec3.atCenterOf(record.blockPos), Vec3.atBottomCenterOf(normal),
            //     0.5, Math.PI / 4, 10
            // ).sprayParticles(ctx.world, colorizer)
        }
    }
}
