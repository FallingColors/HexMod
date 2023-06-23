package at.petrak.hexcasting.interop;

import at.petrak.hexcasting.interop.pehkui.PehkuiInterop;
import at.petrak.hexcasting.xplat.IClientXplatAbstractions;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import at.petrak.hexcasting.xplat.Platform;
import vazkii.patchouli.api.PatchouliAPI;

import java.util.List;

public class HexInterop {
    public static final String PATCHOULI_ANY_INTEROP_FLAG = "hexcasting:any_interop";

    public static final String PEHKUI_ID = "pehkui";

    public static final class Forge {
        public static final String CURIOS_API_ID = "curios";
    }

    public static final class Fabric {
        public static final String TRINKETS_API_ID = "trinkets";
    }

    public static void init() {
        initPatchouli();

        IXplatAbstractions xplat = IXplatAbstractions.INSTANCE;
        if (xplat.isModPresent(PEHKUI_ID)) {
            PehkuiInterop.init();
        }

        xplat.initPlatformSpecific();
    }

    public static void clientInit() {
        IClientXplatAbstractions.INSTANCE.initPlatformSpecific();
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

        PatchouliAPI.get().setConfigFlag(PATCHOULI_ANY_INTEROP_FLAG, anyInterop);
    }
}
