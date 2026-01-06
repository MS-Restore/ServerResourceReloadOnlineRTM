package fun.bm.serverresourcereloadonlinertm.util;

import com.google.common.base.Strings;
import com.mojang.logging.LogUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.NoSuchFileException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

public class ServerResourcePackUtil {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Pattern SHA1_PATTERN = Pattern.compile("^[a-fA-F0-9]{40}$");

    @Nullable
    public static Text parseResourcePackPrompt(String prompt) {
        if (!Strings.isNullOrEmpty(prompt)) {
            try {
                return Text.Serializer.fromJson(prompt);
            } catch (Exception exception) {
                LOGGER.warn("Failed to parse resource pack prompt '{}'", prompt, exception);
            }
        }

        return null;
    }

    public static MinecraftServer.ServerResourcePackProperties getServerResourcePackProperties(String url, String sha1, boolean required, String prompt) {
        if (url.isEmpty()) {
            return null;
        } else {
            String string;
            if (!sha1.isEmpty()) {
                string = sha1;

            } else {
                string = "";
            }

            if (string.isEmpty()) {
                LOGGER.warn("You specified a resource pack without providing a sha1 hash. Pack will be updated on the client only if you change the name of the pack.");
            } else if (!SHA1_PATTERN.matcher(string).matches()) {
                LOGGER.warn("Invalid sha1 for resource-pack-sha1");
            }

            Text text = parseResourcePackPrompt(prompt);
            return new MinecraftServer.ServerResourcePackProperties(url, string, required, text);
        }
    }

    @NotNull
    public static String downloadResourcePackAndCalculateSHA1(String url) throws NoSuchAlgorithmException, IOException {
        if (Strings.isNullOrEmpty(url)) {
            throw new NoSuchFileException(url);
        }
        MessageDigest sha1Digest = MessageDigest.getInstance("SHA-1");

        try (InputStream inputStream = new URL(url).openStream()) {
            byte[] buffer = new byte[8192];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                sha1Digest.update(buffer, 0, bytesRead);
            }
        } catch (Exception e) {
            throw new IOException("Failed to download resource pack from URL: " + url, e);
        }

        byte[] hashBytes = sha1Digest.digest();
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }

        return hexString.toString();
    }
}
