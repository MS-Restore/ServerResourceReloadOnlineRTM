package fun.bm.serverresourcereloadonlinertm.mixin;

import fun.bm.serverresourcereloadonlinertm.data.ResourceHolder;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.ServerPropertiesHandler;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.Properties;

@Mixin(ServerPropertiesHandler.class)
public class ServerPropertiesHandlerMixin {
    @Final
    @Shadow
    @Mutable
    public Optional<MinecraftServer.ServerResourcePackProperties> serverResourcePackProperties;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void init(Properties properties, CallbackInfo ci) {
        ServerTickEvents.START_SERVER_TICK.register(server -> tick());
    }

    @Unique
    public void tick() {
        if (ResourceHolder.isInUpdate) {
            serverResourcePackProperties = Optional.ofNullable(ResourceHolder.awaitUpdate);
            ResourceHolder.save();
            ResourceHolder.awaitUpdate = null;
            ResourceHolder.isInUpdate = false;
        }
    }
}
