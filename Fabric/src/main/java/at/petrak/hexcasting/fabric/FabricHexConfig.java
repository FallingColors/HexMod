package at.petrak.hexcasting.fabric;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.mod.HexConfig;
import at.petrak.hexcasting.common.loot.HexLootHandler;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import com.google.gson.GsonBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import me.shedaniel.autoconfig.serializer.PartitioningSerializer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

import java.util.List;

import static at.petrak.hexcasting.api.mod.HexConfig.anyMatchResLoc;
import static at.petrak.hexcasting.api.mod.HexConfig.noneMatch;

@Config(name = HexAPI.MOD_ID)
@Config.Gui.Background("minecraft:textures/block/calcite.png")
@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})

public class FabricHexConfig extends PartitioningSerializer.GlobalData {
    @ConfigEntry.Category("common")
    @ConfigEntry.Gui.TransitiveObject
    public final Common common = new Common();
    @ConfigEntry.Category("client")
    @ConfigEntry.Gui.TransitiveObject
    public final Client client = new Client();
    @ConfigEntry.Category("server")
    @ConfigEntry.Gui.TransitiveObject
    public final Server server = new Server();

    public static FabricHexConfig setup() {
        var gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
            .create();
        AutoConfig.register(FabricHexConfig.class, PartitioningSerializer.wrap((cfg, clazz) ->
            new GsonConfigSerializer<>(cfg, clazz, gson)));
        var instance = AutoConfig.getConfigHolder(FabricHexConfig.class).getConfig();

        HexConfig.setCommon(instance.common);
        // We care about the client only on the *physical* client ...
        if (IXplatAbstractions.INSTANCE.isPhysicalClient()) {
            HexConfig.setClient(instance.client);
        }
        // but we care about the server on the *logical* server
        // i believe this should Just Work without a guard? assuming we don't access it from the client ever
        HexConfig.setServer(instance.server);

        return instance;
    }

    @Config(name = "common")
    public static final class Common implements HexConfig.CommonConfigAccess, ConfigData {
        @ConfigEntry.Gui.Tooltip
        private long dustMediaAmount = DEFAULT_DUST_MEDIA_AMOUNT;
        @ConfigEntry.Gui.Tooltip
        private long shardMediaAmount = DEFAULT_SHARD_MEDIA_AMOUNT;
        @ConfigEntry.Gui.Tooltip
        private long chargedCrystalMediaAmount = DEFAULT_CHARGED_MEDIA_AMOUNT;
        @ConfigEntry.Gui.Tooltip
        private double mediaToHealthRate = DEFAULT_MEDIA_TO_HEALTH_RATE;

        @ConfigEntry.Gui.Tooltip
        private int cypherCooldown = DEFAULT_CYPHER_COOLDOWN;
        @ConfigEntry.Gui.Tooltip
        private int trinketCooldown = DEFAULT_TRINKET_COOLDOWN;
        @ConfigEntry.Gui.Tooltip
        private int artifactCooldown = DEFAULT_ARTIFACT_COOLDOWN;


        @Override
        public void validatePostLoad() throws ValidationException {
            this.dustMediaAmount = Math.max(this.dustMediaAmount, 0);
            this.shardMediaAmount = Math.max(this.shardMediaAmount, 0);
            this.chargedCrystalMediaAmount = Math.max(this.chargedCrystalMediaAmount, 0);
            this.mediaToHealthRate = Math.max(this.mediaToHealthRate, 0);
        }

        @Override
        public long dustMediaAmount() {
            return dustMediaAmount;
        }

        @Override
        public long shardMediaAmount() {
            return shardMediaAmount;
        }

        @Override
        public long chargedCrystalMediaAmount() {
            return chargedCrystalMediaAmount;
        }

        @Override
        public double mediaToHealthRate() {
            return mediaToHealthRate;
        }

        @Override
        public int cypherCooldown() {
            return cypherCooldown;
        }

        @Override
        public int trinketCooldown() {
            return trinketCooldown;
        }

        @Override
        public int artifactCooldown() {
            return artifactCooldown;
        }
    }

    @Config(name = "client")
    public static final class Client implements HexConfig.ClientConfigAccess, ConfigData {
        @ConfigEntry.Gui.Tooltip
        private boolean ctrlTogglesOffStrokeOrder = DEFAULT_CTRL_TOGGLES_OFF_STROKE_ORDER;
        @ConfigEntry.Gui.Tooltip
        private boolean invertSpellbookScrollDirection = DEFAULT_INVERT_SPELLBOOK_SCROLL;
        @ConfigEntry.Gui.Tooltip
        private boolean invertAbacusScrollDirection = DEFAULT_INVERT_SPELLBOOK_SCROLL;
        @ConfigEntry.Gui.Tooltip
        private double gridSnapThreshold = DEFAULT_GRID_SNAP_THRESHOLD;
        @ConfigEntry.Gui.Tooltip
        private boolean clickingTogglesDrawing = DEFAULT_CLICKING_TOGGLES_DRAWING;

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

        @Override
        public boolean clickingTogglesDrawing() {
             return clickingTogglesDrawing;
        }
    }

    @Config(name = "server")
    public static final class Server implements HexConfig.ServerConfigAccess, ConfigData {
        @ConfigEntry.BoundedDiscrete(min = 0, max = 4)
        @ConfigEntry.Gui.Tooltip
        private int opBreakHarvestLevel = DEFAULT_OP_BREAK_HARVEST_LEVEL;
        @ConfigEntry.Gui.Tooltip
        private int maxOpCount = DEFAULT_MAX_OP_COUNT;
        @ConfigEntry.Gui.Tooltip
        private int maxSpellCircleLength = DEFAULT_MAX_SPELL_CIRCLE_LENGTH;
        @ConfigEntry.Gui.Tooltip
        private List<String> actionDenyList = List.of();
        @ConfigEntry.Gui.Tooltip
        private List<String> circleActionDenyList = List.of();
        @ConfigEntry.Gui.Tooltip
        private boolean villagersOffendedByMindMurder = DEFAULT_VILLAGERS_DISLIKE_MIND_MURDER;
        @ConfigEntry.Gui.Tooltip
        private boolean doesTrueNameHaveAmbit = DEFAULT_TRUE_NAME_HAS_AMBIT;


        @ConfigEntry.Gui.Tooltip
        private List<String> tpDimDenylist = DEFAULT_DIM_TP_DENYLIST;

        // ModMenu bad and doesn't like java objects in here so we do stupid string parsing
        @ConfigEntry.Gui.Tooltip
        private List<String> scrollInjectionsRaw = HexLootHandler.DEFAULT_SCROLL_INJECTS
            .stream()
            .map(si -> si.injectee() + " " + si.countRange())
            .toList();
        @ConfigEntry.Gui.Excluded
        private transient Object2IntMap<ResourceLocation> scrollInjections;

        // TODO: hook this up to the config, change Jankery, test, also test scroll injects on fabric
        @ConfigEntry.Gui.Tooltip
        private List<ResourceLocation> loreInjections = HexLootHandler.DEFAULT_LORE_INJECTS;
        @ConfigEntry.Gui.Tooltip
        private double loreChance = HexLootHandler.DEFAULT_LORE_CHANCE;


        @Override
        public void validatePostLoad() throws ValidationException {
            this.maxOpCount = Math.max(this.maxOpCount, 0);
            this.maxSpellCircleLength = Math.max(this.maxSpellCircleLength, 4);

            this.scrollInjections = new Object2IntOpenHashMap<>();
            try {
                for (var auugh : this.scrollInjectionsRaw) {
                    String[] split = auugh.split(" ");
                    ResourceLocation loc = new ResourceLocation(split[0]);
                    int count = Integer.parseInt(split[1]);
                    this.scrollInjections.put(loc, count);
                }

            } catch (Exception e) {
                throw new ValidationException("Bad parsing of scroll injects", e);
            }

            this.loreChance = Mth.clamp(this.loreChance, 0.0, 1.0);
        }

        @Override
        public int opBreakHarvestLevelBecauseForgeThoughtItWasAGoodIdeaToImplementHarvestTiersUsingAnHonestToGodTopoSort() {
            return opBreakHarvestLevel;
        }

        @Override
        public int maxOpCount() {
            return maxOpCount;
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
            return noneMatch(tpDimDenylist, dimension.location());
        }

        @Override
        public boolean trueNameHasAmbit() {
            return doesTrueNameHaveAmbit;
        }

        /**
         * Returns -1 if none is found
         */
        public int scrollRangeForLootTable(ResourceLocation lootTable) {
            return this.scrollInjections.getOrDefault(lootTable, -1);
        }

        public boolean shouldInjectLore(ResourceLocation lootTable) {
            return anyMatchResLoc(this.loreInjections, lootTable);
        }

        public double getLoreChance() {
            return loreChance;
        }
    }
}
