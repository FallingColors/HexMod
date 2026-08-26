package at.petrak.hexcasting.common.lib;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import at.petrak.hexcasting.xplat.IXplatRegister;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class HexArmorMaterials {
    private static final IXplatRegister<ArmorMaterial> REGISTER = IXplatAbstractions.INSTANCE.createRegistar(Registries.ARMOR_MATERIAL);

    public static void register() {
        REGISTER.registerAll();
    }

    public static final Holder<ArmorMaterial> ROBES = REGISTER.registerHolder("robes", () ->
        new ArmorMaterial(
            Map.of(), // no defense here since it's specified with the other attributes in ItemRobes
            ArmorMaterials.GOLD.value().enchantmentValue(),
            SoundEvents.ARMOR_EQUIP_LEATHER,
            () -> Ingredient.of(HexItems.NEURAL_FIBER.get()),
            List.of(new ArmorMaterial.Layer(HexAPI.modLoc("robes"))),
            0, 0  // no toughness or knockback resistance
        ));
}
