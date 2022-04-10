package at.petrak.hexcasting.common.network;

import at.petrak.hexcasting.client.gui.GuiSpellcasting;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public record GuiOpener(MsgOpenSpellGuiAck msg) {
	@OnlyIn(Dist.CLIENT)
<<<<<<< HEAD
	public void open() {
=======
	public void openGui() {
>>>>>>> aae172093c4a71951f424ef3c6f21c5924cb72a7
		var mc = Minecraft.getInstance();
		mc.setScreen(new GuiSpellcasting(msg.hand(), msg.patterns(), msg.components()));
	}
}
