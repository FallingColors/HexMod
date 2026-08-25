package at.petrak.hexcasting.common.items.armor;

import at.petrak.hexcasting.annotations.SoftImplement;
import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.item.VariantItem;
import at.petrak.hexcasting.client.model.HexModelLayers;
import at.petrak.hexcasting.client.model.HexRobesModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * To get the armor model in;
 * On forge: client item extension in ForgeHexClientInitializer (line 161)
 * On fabric: custom HexRobesRenderer set up from FabricHexClientInitializer (line 60)
 */
public class ItemRobes extends ArmorItem implements VariantItem {
    public final Type type;
    private @Nullable HexRobesModel[] models;

    public ItemRobes(Type type, Properties properties) {
        super(Holder.direct(HexAPI.instance().robesMaterial()), type, properties);
        this.type = type;
    }

    public static HexRobesModel[] provideArmorModelsForSlot(EquipmentSlot slot) {
        EntityModelSet models = Minecraft.getInstance().getEntityModels();
        return new HexRobesModel[] {
            new HexRobesModel(models.bakeLayer(HexModelLayers.ROBES_0), slot),
            new HexRobesModel(models.bakeLayer(HexModelLayers.ROBES_1), slot),
            new HexRobesModel(models.bakeLayer(HexModelLayers.ROBES_2), slot)
        };
    }

    public HexRobesModel[] getArmorModels() {
        if (models == null) models = provideArmorModelsForSlot(getEquipmentSlot());
        return models;
    }

    @SoftImplement("IItemExtension")
    public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, ArmorMaterial.Layer layer, boolean innerModel) {
        return HexAPI.modLoc("textures/armor/robes"+getVariant(stack)+".png");
    }

    @Override
    public Component getName(ItemStack pStack) {
        var descID = this.getDescriptionId(pStack);
        var robesItem = (ItemRobes) pStack.getItem();
        if (robesItem.type == Type.LEGGINGS) {
            return Component.translatable(descID + "." + robesItem.getVariant(pStack));
        } else {
            return Component.translatable(descID);
        }
    }

    @Override
    public int numVariants() { return 3; }
}
