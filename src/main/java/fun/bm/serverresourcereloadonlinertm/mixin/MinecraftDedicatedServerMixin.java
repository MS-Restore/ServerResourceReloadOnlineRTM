package fun.bm.serverresourcereloadonlinertm.mixin;

import com.mojang.datafixers.DataFixer;
import fun.bm.serverresourcereloadonlinertm.data.ResourceHolder;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.SaveLoader;
import net.minecraft.server.WorldGenerationProgressListenerFactory;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.server.dedicated.ServerPropertiesLoader;
import net.minecraft.util.ApiServices;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(MinecraftDedicatedServer.class)
public class MinecraftDedicatedServerMixin {
    @Inject(method = "<init>", at = @At("RETURN"))
    public void init(Thread serverThread, LevelStorage.Session session, ResourcePackManager dataPackManager, SaveLoader saveLoader, ServerPropertiesLoader propertiesLoader, DataFixer dataFixer, ApiServices apiServices, WorldGenerationProgressListenerFactory worldGenerationProgressListenerFactory, CallbackInfo ci) {
        ResourceHolder.setPropertiesLoader(propertiesLoader);
    }

    @Inject(method = "getResourcePackProperties", at = @At("HEAD"), cancellable = true)
    public void getResourcePackProperties(CallbackInfoReturnable<Optional<MinecraftServer.ServerResourcePackProperties>> cir) {
        cir.cancel();
        Optional<MinecraftServer.ServerResourcePackProperties> resourcePackProperties = ResourceHolder.getResourcePackProperties();
        cir.setReturnValue(resourcePackProperties);
    }
}
