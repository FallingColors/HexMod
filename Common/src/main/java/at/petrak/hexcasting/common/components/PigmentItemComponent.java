package at.petrak.hexcasting.common.components;

import at.petrak.hexcasting.api.item.PigmentItem;
import net.minecraft.core.component.DataComponentType;

public record PigmentItemComponent(PigmentItem item) {
    public static final DataComponentType<ItemMediaHolderComponent> COMPONENT_TYPE = DataComponentType.<ItemMediaHolderComponent>builder()
            .build();
}
