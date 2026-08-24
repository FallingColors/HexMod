package at.petrak.hexcasting.common.items.armor;

import at.petrak.hexcasting.annotations.SoftImplement;
import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.client.model.HexModelLayers;
import at.petrak.hexcasting.client.model.HexRobesModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * To get the armor model in;
 * On forge: cursed self-mixin
 * On fabric: hook in ClientInit
 */
public class ItemRobes extends ArmorItem {
    public final ArmorItem.Type type;
    private @Nullable HexRobesModel model;

    public ItemRobes(ArmorItem.Type type, Properties properties) {
        super(Holder.direct(HexAPI.instance().robesMaterial()), type, properties);
        this.type = type;
    }

    // TODO: return a collection holding all the variants for the given slot
    public static HexRobesModel provideArmorModelForSlot(EquipmentSlot slot) {
        EntityModelSet models = Minecraft.getInstance().getEntityModels();
        ModelPart root = models.bakeLayer(HexModelLayers.ROBES);
        return new HexRobesModel(root, slot);
    }

    public HexRobesModel getArmorModel() {
        if (model == null) model = provideArmorModelForSlot(getEquipmentSlot());
        return model;
    }

    @SoftImplement("IItemExtension")
    public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, ArmorMaterial.Layer layer, boolean innerModel) {
        // TODO: detect variant based on itemstack data
        return HexAPI.modLoc("textures/armor/robes1.png");
    }
}
