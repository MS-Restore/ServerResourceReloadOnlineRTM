package fun.bm.serverresourcereloadonlinertm.data;

import com.mojang.logging.LogUtils;
import fun.bm.serverresourcereloadonlinertm.util.ServerResourcePackUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.ServerPropertiesLoader;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.Optional;

public class ResourceHolder {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static ServerPropertiesLoader propertiesLoader;
    public static boolean isInUpdate = false;
    public static MinecraftServer.ServerResourcePackProperties awaitUpdate = null;

    public static void setPropertiesLoader(ServerPropertiesLoader newPropertiesLoader) {
        propertiesLoader = newPropertiesLoader;
    }

    public static void save() {
        propertiesLoader.store();
    }

    public static Optional<MinecraftServer.ServerResourcePackProperties> handleResourcePackUpdate(String url, boolean force) throws NoSuchAlgorithmException, IOException {
        return handleResourcePackUpdate(url, null, force);
    }

    public static Optional<MinecraftServer.ServerResourcePackProperties> handleResourcePackUpdate(@Nullable String prompt) throws NoSuchAlgorithmException, IOException {
        Optional<MinecraftServer.ServerResourcePackProperties> srpp = getResourcePackProperties();
        String url = srpp.isPresent() ? srpp.get().url() : "";
        boolean force = srpp.map(MinecraftServer.ServerResourcePackProperties::isRequired).orElse(false);
        return handleResourcePackUpdate(url, prompt, force);
    }

    public static Optional<MinecraftServer.ServerResourcePackProperties> handleResourcePackUpdate(String url, @Nullable String prompt, boolean force) throws NoSuchAlgorithmException, IOException {
        Optional<MinecraftServer.ServerResourcePackProperties> srpp = getResourcePackProperties();
        boolean exists = srpp.isPresent();
        if (prompt == null) {
            Text promptOrigin = null;
            if (exists) promptOrigin = srpp.get().prompt();
            prompt = promptOrigin == null ? "" : promptOrigin.getString();
        } else if (!prompt.isEmpty() && !prompt.startsWith("\"") && !prompt.endsWith("\"")) {
            prompt = "\"" + prompt + "\"";
        }

        String sha1 = exists ? srpp.get().hash() : "";

        boolean shouldUpdateSha1 = !exists || !Objects.equals(srpp.get().url(), url) || sha1.isEmpty();

        if (shouldUpdateSha1) {
            try {
                sha1 = ServerResourcePackUtil.downloadResourcePackAndCalculateSHA1(url);
            } catch (Exception e) {
                LOGGER.error("Failed to download resource pack and calculate SHA1 from URL: {}", url, e);
                // for safety reasons, we don't want to update the resource pack if the download failed
                throw e;
            }
        }

        setResourcePackProperties(ServerResourcePackUtil.getServerResourcePackProperties(url, sha1, force, prompt));
        return getResourcePackProperties();
    }

    public static void setResourcePackProperties(MinecraftServer.ServerResourcePackProperties newResource) {
        awaitUpdate = newResource;
        isInUpdate = true;
    }

    public static Optional<MinecraftServer.ServerResourcePackProperties> getResourcePackProperties() {
        return isInUpdate ? Optional.ofNullable(awaitUpdate) : propertiesLoader.getPropertiesHandler().serverResourcePackProperties;
    }
}
