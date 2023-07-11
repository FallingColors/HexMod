package at.petrak.hexcasting.api.casting.eval.env;

import at.petrak.hexcasting.api.casting.eval.CastResult;
import at.petrak.hexcasting.api.casting.eval.sideeffects.EvalSound;
import at.petrak.hexcasting.api.pigment.FrozenPigment;
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;

public class PackagedItemCastEnv extends PlayerBasedCastEnv {

    protected EvalSound sound = HexEvalSounds.NOTHING;

    public PackagedItemCastEnv(ServerPlayer caster, InteractionHand castingHand) {
        super(caster, castingHand);
    }

    @Override
    public void postExecution(CastResult result) {
        super.postExecution(result);
        // TODO: how do we know when to actually play this sound?
        this.sound = this.sound.greaterOf(result.getSound());
    }

    @Override
    public long extractMedia(long costLeft) {
        if (this.caster.isCreative())
            return 0;

        var casterStack = this.caster.getItemInHand(this.castingHand);
        var casterHexHolder = IXplatAbstractions.INSTANCE.findHexHolder(casterStack);
        var canCastFromInv = casterHexHolder.canDrawMediaFromInventory();

        var casterMediaHolder = IXplatAbstractions.INSTANCE.findMediaHolder(casterStack);

        // The contracts on the AD and on this function are different.
        // ADs return the amount extracted, this wants the amount left
        if (casterMediaHolder != null) {
            long extracted = casterMediaHolder.withdrawMedia((int) costLeft, false);
            costLeft -= extracted;
        }
        if (canCastFromInv && costLeft > 0) {
            costLeft = this.extractMediaFromInventory(costLeft, this.canOvercast());
        }

        return costLeft;
    }

    @Override
    public InteractionHand getCastingHand() {
        return this.castingHand;
    }

    @Override
    public FrozenPigment getPigment() {
        var casterStack = this.caster.getItemInHand(this.castingHand);
        var casterHexHolder = IXplatAbstractions.INSTANCE.findHexHolder(casterStack);
        var hexHolderPigment = casterHexHolder.getPigment();
        if (hexHolderPigment != null)
            return hexHolderPigment;
        return IXplatAbstractions.INSTANCE.getPigment(this.caster);
    }

    public EvalSound getSound() {
        return sound;
    }
}
