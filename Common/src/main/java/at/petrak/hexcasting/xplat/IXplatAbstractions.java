package at.petrak.hexcasting.xplat;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.common.command.PatternResLocArgument;
import at.petrak.hexcasting.common.network.IMessage;
import net.minecraft.commands.synchronization.ArgumentTypes;
import net.minecraft.commands.synchronization.EmptyArgumentSerializer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.ServiceLoader;
import java.util.stream.Collectors;

public interface IXplatAbstractions {
    Platform platform();

    boolean isPhysicalClient();

    void brainsweep(LivingEntity e);

    boolean isBrainswept(LivingEntity e);

    void sendPacketToPlayer(ServerPlayer target, IMessage packet);

    void sendPacketToServer(IMessage packet);

    default void init() {
        HexAPI.LOGGER.info("Hello Hexcasting! This is {}!", this.platform());

        ArgumentTypes.register(
            "hexcasting:pattern",
            PatternResLocArgument.class,
            new EmptyArgumentSerializer<>(PatternResLocArgument::id)
        );
    }


    IXplatAbstractions INSTANCE = find();

    private static IXplatAbstractions find() {
        var providers = ServiceLoader.load(IXplatAbstractions.class).stream().toList();
        if (providers.size() != 1) {
            var names = providers.stream().map(p -> p.type().getName()).collect(Collectors.joining(",", "[", "]"));
            throw new IllegalStateException(
                "There should be exactly one IXplatAbstractions implementation on the classpath. Found: " + names);
        } else {
            var provider = providers.get(0);
            HexAPI.LOGGER.debug("Instantiating xplat impl: " + provider.type().getName());
            return provider.get();
        }
    }
}
