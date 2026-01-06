package fun.bm.serverresourcereloadonlinertm.mixin;

import fun.bm.serverresourcereloadonlinertm.data.ResourceHolder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.AbstractPropertiesHandler;
import net.minecraft.server.dedicated.ServerPropertiesHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;
import java.util.Properties;

@Mixin(AbstractPropertiesHandler.class)
public class AbstractPropertiesHandlerMixin {
    @Final
    @Shadow
    protected Properties properties;

    @Unique
    private boolean firstLoad = true;

    @Inject(method = "saveProperties", at = @At("HEAD"))
    public void saveProperties(Path path, CallbackInfo ci) {
        if (firstLoad) {
            firstLoad = false;
            return;
        }
        if (((AbstractPropertiesHandler) (Object) this) instanceof ServerPropertiesHandler) {
            MinecraftServer.ServerResourcePackProperties srpp = ResourceHolder.getResourcePackProperties().orElse(null);
            if (srpp == null) {
                srpp = new MinecraftServer.ServerResourcePackProperties("", "", false, null);
            }
            this.properties.setProperty("resource-pack", srpp.url());
            this.properties.setProperty("resource-pack-sha1", srpp.hash());
            this.properties.setProperty("resource-pack-prompt", srpp.prompt() == null ? "" : "\"" + srpp.prompt().getString() + "\"");
            this.properties.setProperty("require-resource-pack", srpp.isRequired() ? "true" : "false");
        }
    }
}
