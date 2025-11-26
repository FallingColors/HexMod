package at.petrak.hexcasting.common.components;

import at.petrak.hexcasting.api.item.PigmentItem;
import net.minecraft.core.component.DataComponentType;

public record PigmentItemComponent(PigmentItem item) {
    public static final DataComponentType<PigmentItemComponent> COMPONENT_TYPE = DataComponentType.<PigmentItemComponent>builder()
            .build();
}
