package at.petrak.hexcasting.common.network;

import at.petrak.hexcasting.client.gui.GuiSpellcasting;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public record GuiOpener(MsgOpenSpellGuiAck msg) {
    @OnlyIn(Dist.CLIENT)
    public void openGui() {
        var mc = Minecraft.getInstance();
        mc.setScreen(new GuiSpellcasting(msg.hand(), msg.patterns(), msg.components()));
    }
}
