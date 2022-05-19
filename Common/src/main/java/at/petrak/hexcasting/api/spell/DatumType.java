package at.petrak.hexcasting.api.spell;

import net.minecraft.util.StringRepresentable;

public enum DatumType implements StringRepresentable {
    EMPTY("empty"),
    ENTITY("entity"),
    WIDGET("widget"),
    LIST("list"),
    PATTERN("pattern"),
    DOUBLE("double"),
    VEC("vec"),
    OTHER("other");

    public final String serializedName;

    DatumType(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return this.serializedName;
    }
}
