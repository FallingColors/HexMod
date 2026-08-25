package at.petrak.hexcasting.common.items.armor;

import at.petrak.hexcasting.annotations.SoftImplement;
import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.item.VariantItem;
import at.petrak.hexcasting.client.model.HexModelLayers;
import at.petrak.hexcasting.client.model.HexRobesModel;
import at.petrak.hexcasting.common.items.ItemLens;
import at.petrak.hexcasting.common.lib.HexArmorMaterials;
import at.petrak.hexcasting.common.lib.HexAttributes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.jetbrains.annotations.Nullable;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

/**
 * To get the armor model in;
 * On forge: client item extension in ForgeHexClientInitializer (line 161)
 * On fabric: custom HexRobesRenderer set up from FabricHexClientInitializer (line 60)
 */
public class ItemRobes extends ArmorItem implements VariantItem {
    public final Type type;
    private @Nullable HexRobesModel[] models;

    public static ItemAttributeModifiers HOOD_MODIFIERS = ItemAttributeModifiers.builder()
        .add(HexAttributes.SCRY_SIGHT, ItemLens.SCRY_SIGHT, EquipmentSlotGroup.HEAD)
        .add(HexAttributes.GRID_ZOOM, ItemLens.GRID_ZOOM, EquipmentSlotGroup.HEAD)
        .add(Attributes.ARMOR, new AttributeModifier(
            modLoc("robes_hood_armor"), 3.0, AttributeModifier.Operation.ADD_VALUE
        ), EquipmentSlotGroup.HEAD)
        .build();

    public static ItemAttributeModifiers TUNIC_MODIFIERS = ItemAttributeModifiers.builder()
        .add(HexAttributes.MEDIA_CONSUMPTION_MODIFIER, new AttributeModifier(
            modLoc("robes_tunic_discount"), -0.1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE
        ), EquipmentSlotGroup.CHEST)
        .add(Attributes.ARMOR, new AttributeModifier(
            modLoc("robes_tunic_armor"), 7.0, AttributeModifier.Operation.ADD_VALUE
        ), EquipmentSlotGroup.CHEST)
        .build();

    public static ItemAttributeModifiers LEGS_MODIFIERS = ItemAttributeModifiers.builder()
        .add(HexAttributes.AMBIT_RADIUS, new AttributeModifier(
            modLoc("robes_legs_ambit"), 4.0, AttributeModifier.Operation.ADD_VALUE
        ), EquipmentSlotGroup.LEGS)
        .add(Attributes.ARMOR, new AttributeModifier(
            modLoc("robes_legs_armor"), 6.0, AttributeModifier.Operation.ADD_VALUE
        ), EquipmentSlotGroup.LEGS)
        .build();

    public static ItemAttributeModifiers BOOTS_MODIFIERS = ItemAttributeModifiers.builder()
        .add(HexAttributes.SENTINEL_RADIUS, new AttributeModifier(
            modLoc("robes_boots_sentinel_ambit"), 2.0, AttributeModifier.Operation.ADD_VALUE
        ), EquipmentSlotGroup.FEET)
        .add(Attributes.ARMOR, new AttributeModifier(
            modLoc("robes_boots_armor"), 2.0, AttributeModifier.Operation.ADD_VALUE
        ), EquipmentSlotGroup.FEET)
        .build();

    public ItemRobes(Type type, Properties properties) {
        super(HexArmorMaterials.ROBES, type, properties);
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
