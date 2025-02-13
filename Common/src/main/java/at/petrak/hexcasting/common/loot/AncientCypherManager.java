package at.petrak.hexcasting.common.loot;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.PatternIota;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AncientCypherManager extends SimpleJsonResourceReloadListener {
    public static final AncientCypherManager INSTANCE = new AncientCypherManager();

    private Map<ResourceLocation, List<Iota>> data;

    private AncientCypherManager() {
        super(new Gson(), "loot_cyphers");
    }

    @Override
    protected Map<ResourceLocation, JsonElement> prepare(ResourceManager $$0, ProfilerFiller $$1) {
        return super.prepare($$0, $$1);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        Map<ResourceLocation, List<Iota>> data = new HashMap<>(map.size());
        for (var entry : map.entrySet()) {
            var key = entry.getKey();
            try {
                var value = entry.getValue().getAsJsonArray();
                var iotaList = new ArrayList<Iota>(value.size());
                for (var elem : value) {
                    var pattern = HexPattern.CODEC.parse(JsonOps.INSTANCE, elem).resultOrPartial(HexAPI.LOGGER::error).orElseThrow();
                    iotaList.add(new PatternIota(pattern));
                }
                data.put(key, iotaList);
            } catch (Exception e) {
                HexAPI.LOGGER.error("Error loading custom loot cypher {}: {}", key, e.getMessage());
            }
        }
        this.data = data;
    }

    public Pair<ResourceLocation, List<Iota>> randomHex(RandomSource rand) {
        var map = this.data;
        var entries = map.entrySet().stream().toList();
        var entry = entries.get(rand.nextInt(entries.size()));
        return Pair.of(entry.getKey(), entry.getValue());
    }
}
