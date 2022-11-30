package at.petrak.hexcasting.fabric;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.misc.ScrollQuantity;
import at.petrak.hexcasting.api.mod.HexConfig;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import me.shedaniel.autoconfig.serializer.PartitioningSerializer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

import java.util.List;

import static at.petrak.hexcasting.api.mod.HexConfig.anyMatch;
import static at.petrak.hexcasting.api.mod.HexConfig.noneMatch;

@Config(name = HexAPI.MOD_ID)
@Config.Gui.Background("minecraft:textures/block/calcite.png")
@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})

public class FabricHexConfig extends PartitioningSerializer.GlobalData {
    @ConfigEntry.Category("common")
    @ConfigEntry.Gui.TransitiveObject
    private final Common common = new Common();
    @ConfigEntry.Category("client")
    @ConfigEntry.Gui.TransitiveObject
    private final Client client = new Client();
    @ConfigEntry.Category("server")
    @ConfigEntry.Gui.TransitiveObject
    private final Server server = new Server();

    public static void setup() {
        AutoConfig.register(FabricHexConfig.class, PartitioningSerializer.wrap(JanksonConfigSerializer::new));
        var instance = AutoConfig.getConfigHolder(FabricHexConfig.class).getConfig();

        HexConfig.setCommon(instance.common);
        // We care about the client only on the *physical* client ...
        if (IXplatAbstractions.INSTANCE.isPhysicalClient()) {
            HexConfig.setClient(instance.client);
        }
        // but we care about the server on the *logical* server
        // i believe this should Just Work without a guard? assuming we don't access it from the client ever
        HexConfig.setServer(instance.server);
    }

    @Config(name = "common")
    private static final class Common implements HexConfig.CommonConfigAccess, ConfigData {
        @ConfigEntry.Gui.Tooltip
        private int dustMediaAmount = DEFAULT_DUST_MEDIA_AMOUNT;
        @ConfigEntry.Gui.Tooltip
        private int shardMediaAmount = DEFAULT_SHARD_MEDIA_AMOUNT;
        @ConfigEntry.Gui.Tooltip
        private int chargedCrystalMediaAmount = DEFAULT_CHARGED_MEDIA_AMOUNT;
        @ConfigEntry.Gui.Tooltip
        private double mediaToHealthRate = DEFAULT_MEDIA_TO_HEALTH_RATE;

        @Override
        public void validatePostLoad() throws ValidationException {
            this.dustMediaAmount = Math.max(this.dustMediaAmount, 0);
            this.shardMediaAmount = Math.max(this.shardMediaAmount, 0);
            this.chargedCrystalMediaAmount = Math.max(this.chargedCrystalMediaAmount, 0);
            this.mediaToHealthRate = Math.max(this.mediaToHealthRate, 0);
        }

        @Override
        public int dustMediaAmount() {
            return dustMediaAmount;
        }

        @Override
        public int shardMediaAmount() {
            return shardMediaAmount;
        }

        @Override
        public int chargedCrystalMediaAmount() {
            return chargedCrystalMediaAmount;
        }

        @Override
        public double mediaToHealthRate() {
            return mediaToHealthRate;
        }
    }

    @Config(name = "client")
    private static final class Client implements HexConfig.ClientConfigAccess, ConfigData {
        @ConfigEntry.Gui.Tooltip
        private boolean ctrlTogglesOffStrokeOrder = DEFAULT_CTRL_TOGGLES_OFF_STROKE_ORDER;
        @ConfigEntry.Gui.Tooltip
        private boolean invertSpellbookScrollDirection = DEFAULT_INVERT_SPELLBOOK_SCROLL;
        @ConfigEntry.Gui.Tooltip
        private boolean invertAbacusScrollDirection = DEFAULT_INVERT_SPELLBOOK_SCROLL;
        @ConfigEntry.Gui.Tooltip
        private double gridSnapThreshold = DEFAULT_GRID_SNAP_THRESHOLD;

        @Override
        public void validatePostLoad() throws ValidationException {
            this.gridSnapThreshold = Mth.clamp(this.gridSnapThreshold, 0.5, 1.0);
        }

        @Override
        public boolean ctrlTogglesOffStrokeOrder() {
            return ctrlTogglesOffStrokeOrder;
        }

        @Override
        public boolean invertSpellbookScrollDirection() {
            return invertSpellbookScrollDirection;
        }

        @Override
        public boolean invertAbacusScrollDirection() {
            return invertAbacusScrollDirection;
        }

        @Override
        public double gridSnapThreshold() {
            return gridSnapThreshold;
        }
    }

    @Config(name = "server")
    private static final class Server implements HexConfig.ServerConfigAccess, ConfigData {
        @ConfigEntry.BoundedDiscrete(min = 0, max = 4)
        @ConfigEntry.Gui.Tooltip
        private int opBreakHarvestLevel = DEFAULT_OP_BREAK_HARVEST_LEVEL;
        @ConfigEntry.Gui.Tooltip
        private int maxRecurseDepth = DEFAULT_MAX_RECURSE_DEPTH;
        @ConfigEntry.Gui.Tooltip
        private int maxSpellCircleLength = DEFAULT_MAX_SPELL_CIRCLE_LENGTH;
        @ConfigEntry.Gui.Tooltip
        private List<String> actionDenyList = List.of();
        @ConfigEntry.Gui.Tooltip
        private List<String> circleActionDenyList = List.of();
        @ConfigEntry.Gui.Tooltip
        private boolean villagersOffendedByMindMurder = DEFAULT_VILLAGERS_DISLIKE_MIND_MURDER;

        @ConfigEntry.Gui.Tooltip
        private List<String> tpDimDenylist = DEFAULT_DIM_TP_DENYLIST;

        @ConfigEntry.Gui.Tooltip
        private List<String> fewScrollTables = DEFAULT_FEW_SCROLL_TABLES;
        @ConfigEntry.Gui.Tooltip
        private List<String> someScrollTables = DEFAULT_SOME_SCROLL_TABLES;
        @ConfigEntry.Gui.Tooltip
        private List<String> manyScrollTables = DEFAULT_MANY_SCROLL_TABLES;

        @Override
        public void validatePostLoad() throws ValidationException {
            this.maxRecurseDepth = Math.max(this.maxRecurseDepth, 0);
            this.maxSpellCircleLength = Math.max(this.maxSpellCircleLength, 4);
        }

        @Override
        public int opBreakHarvestLevelBecauseForgeThoughtItWasAGoodIdeaToImplementHarvestTiersUsingAnHonestToGodTopoSort() {
            return opBreakHarvestLevel;
        }

        @Override
        public int maxRecurseDepth() {
            return maxRecurseDepth;
        }

        @Override
        public int maxSpellCircleLength() {
            return maxSpellCircleLength;
        }

        @Override
        public boolean isActionAllowed(ResourceLocation actionID) {
            return noneMatch(actionDenyList, actionID);
        }

        @Override
        public boolean isActionAllowedInCircles(ResourceLocation actionID) {
            return noneMatch(circleActionDenyList, actionID);
        }

        @Override
        public boolean doVillagersTakeOffenseAtMindMurder() {
            return villagersOffendedByMindMurder;
        }

        @Override
        public boolean canTeleportInThisDimension(ResourceKey<Level> dimension) {
            return noneMatch(manyScrollTables, dimension.location());
        }

        @Override
        public ScrollQuantity scrollsForLootTable(ResourceLocation lootTable) {
            if (anyMatch(fewScrollTables, lootTable)) {
                return ScrollQuantity.FEW;
            } else if (anyMatch(someScrollTables, lootTable)) {
                return ScrollQuantity.SOME;
            } else if (anyMatch(manyScrollTables, lootTable)) {
                return ScrollQuantity.MANY;
            }
            return ScrollQuantity.NONE;
        }
    }
}
