package at.petrak.hexcasting.interop;

import at.petrak.hexcasting.common.lib.HexItems;
import at.petrak.hexcasting.interop.accessories.AccessoriesApiInterop;
import at.petrak.hexcasting.interop.accessories.LensAccessoryRenderer;
import at.petrak.hexcasting.interop.inline.InlineHex;
import at.petrak.hexcasting.interop.inline.InlineHexClient;
import at.petrak.hexcasting.interop.pehkui.PehkuiInterop;
import at.petrak.hexcasting.xplat.IClientXplatAbstractions;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import at.petrak.hexcasting.xplat.Platform;
import io.wispforest.accessories.api.AccessoriesAPI;
import io.wispforest.accessories.api.client.AccessoriesRendererRegistry;
import vazkii.patchouli.api.PatchouliAPI;

import java.util.List;

public class HexInterop {
    public static final String PATCHOULI_ANY_INTEROP_FLAG = "hexcasting:any_interop";

    public static final String PEHKUI_ID = "pehkui";
    public static final String ACCESSORIES_ID = "accessories";

    public static void init() {
        initPatchouli();

        IXplatAbstractions xplat = IXplatAbstractions.INSTANCE;
        if (xplat.isModPresent(PEHKUI_ID)) {
            PehkuiInterop.init();
        }
        if (xplat.isModPresent(ACCESSORIES_ID)) {
            AccessoriesApiInterop.init();
        }

        xplat.initPlatformSpecific();

        InlineHex.init();
    }

    public static void clientInit() {
        InlineHexClient.init();

        IXplatAbstractions xplat = IXplatAbstractions.INSTANCE;
        if (xplat.isModPresent(ACCESSORIES_ID)) {
            AccessoriesApiInterop.clientInit();
        }

        xplat.initPlatformSpecific();
    }

    private static void initPatchouli() {
        var integrations = List.of(PEHKUI_ID);

        var anyInterop = false;
        for (var id : integrations) {
            if (IXplatAbstractions.INSTANCE.isModPresent(id)) {
                anyInterop = true;
                break;
            }
        }

        if (!anyInterop) {
            List<String> platformSpecificIntegrations;

            Platform platform = IXplatAbstractions.INSTANCE.platform();
            if (platform == Platform.FORGE) {
                platformSpecificIntegrations = List.of();
            } else if (platform == Platform.FABRIC) {
                platformSpecificIntegrations = List.of();
            } else {
                throw new UnsupportedOperationException();
            }

            for (var id : platformSpecificIntegrations) {
                if (IXplatAbstractions.INSTANCE.isModPresent(id)) {
                    anyInterop = true;
                    break;
                }
            }
        }

        if (anyInterop) {
            PatchouliAPI.get().setConfigFlag(PATCHOULI_ANY_INTEROP_FLAG, true);
        }
    }
}
