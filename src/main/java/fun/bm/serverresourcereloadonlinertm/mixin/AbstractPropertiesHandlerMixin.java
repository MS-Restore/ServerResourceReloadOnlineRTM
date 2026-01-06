package fun.bm.serverresourcereloadonlinertm.mixin;

import net.minecraft.server.dedicated.AbstractPropertiesHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

@Mixin(AbstractPropertiesHandler.class)
public class AbstractPropertiesHandlerMixin {
    @Final
    @Shadow
    protected Properties properties;

    @Inject(method = "saveProperties", at = @At("HEAD"), cancellable = true)
    public void saveProperties(Path path, CallbackInfo ci) throws IOException {
        ci.cancel();
        try {
            Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8);

            try {
                properties.store(writer, "Minecraft server properties");
            } catch (Throwable var6) {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (Throwable var5) {
                        var6.addSuppressed(var5);
                    }
                }

                throw var6;
            }

            if (writer != null) {
                writer.close();
            }
        } catch (IOException var7) {
            throw new IOException("Failed to store properties to file: " + path, var7);
        }
    }
}
