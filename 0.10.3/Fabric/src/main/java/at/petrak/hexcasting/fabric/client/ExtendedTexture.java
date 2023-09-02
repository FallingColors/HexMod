package at.petrak.hexcasting.fabric.client;

// https://github.com/VazkiiMods/Botania/blob/db85d778ab23f44c11181209319066d1f04a9e3d/Fabric/src/main/java/vazkii/botania/fabric/client/ExtendedTexture.java
public interface ExtendedTexture {
    void setFilterSave(boolean bilinear, boolean mipmap);

    void restoreLastFilter();
}
