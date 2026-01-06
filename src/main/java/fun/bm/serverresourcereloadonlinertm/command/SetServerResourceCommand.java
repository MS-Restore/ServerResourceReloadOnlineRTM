package fun.bm.serverresourcereloadonlinertm.command;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import fun.bm.serverresourcereloadonlinertm.data.ResourceHolder;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class SetServerResourceCommand {
    protected static final String name = "setserverresource";

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                    CommandManager
                            .literal(name)
                            .requires(source -> source.hasPermissionLevel(4))
                            .then(
                                    CommandManager
                                            .argument("force", BoolArgumentType.bool())
                                            .then(
                                                    CommandManager
                                                            .argument("url", StringArgumentType.greedyString())
                                                            .executes(ctx -> {
                                                                String url = StringArgumentType.getString(ctx, "url");
                                                                ServerCommandSource source = ctx.getSource();
                                                                if (Objects.equals(url.toLowerCase(), "disable")) {
                                                                    ResourceHolder.setResourcePackProperties(null);
                                                                    source.sendMessage(Text.literal("已禁用服务器资源包"));
                                                                    return 1;
                                                                }
                                                                boolean forceEnable = BoolArgumentType.getBool(ctx, "force");
                                                                source.sendMessage(Text.literal("已开始应用所提供url的资源包，请稍候"));
                                                                CompletableFuture.runAsync(() -> {
                                                                    Optional<MinecraftServer.ServerResourcePackProperties> resourcePackProperties;
                                                                    try {
                                                                        resourcePackProperties = ResourceHolder.handleResourcePackUpdate(url, forceEnable);
                                                                    } catch (Exception e) {
                                                                        source.sendError(Text.literal("无法应用资源包url更新").styled(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(e.getMessage())))));
                                                                        return;
                                                                    }
                                                                    PlayerManager pm = source.getServer().getPlayerManager();
                                                                    pm.broadcast(Text.literal("已应用所提供url的资源包，后面加入的玩家将使用以下信息加载资源包"), false);
                                                                    sendResourceInfo(resourcePackProperties, pm);
                                                                });
                                                                return 1;
                                                            })
                                            )
                            )
                            .then(
                                    CommandManager
                                            .argument("prompt", StringArgumentType.greedyString())
                                            .executes(ctx -> {
                                                        String promptNew = StringArgumentType.getString(ctx, "prompt");
                                                        ServerCommandSource source = ctx.getSource();
                                                        source.sendMessage(Text.literal("已开始应用所提供prompt，请稍候"));
                                                        CompletableFuture.runAsync(() -> {
                                                            Optional<MinecraftServer.ServerResourcePackProperties> resourcePackProperties;
                                                            try {
                                                                resourcePackProperties = ResourceHolder.handleResourcePackUpdate(promptNew);
                                                            } catch (Exception e) {
                                                                source.sendError(Text.literal("无法应用资源包prompt更新").styled(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(e.getMessage())))));
                                                                return;
                                                            }
                                                            PlayerManager pm = source.getServer().getPlayerManager();
                                                            pm.broadcast(Text.literal("已应用所提供prompt，后面加入的玩家将使用以下信息加载资源包"), false);
                                                            sendResourceInfo(resourcePackProperties, pm);
                                                        });
                                                        return 1;
                                                    }
                                            )
                            )
            );
        });
    }

    private static void sendResourceInfo(Optional<MinecraftServer.ServerResourcePackProperties> resourcePackProperties, PlayerManager pm) {
        if (resourcePackProperties.isPresent()) {
            String urlRet = resourcePackProperties.get().url();
            String hash = resourcePackProperties.get().hash();
            Text prompt = resourcePackProperties.get().prompt();
            boolean required = resourcePackProperties.get().isRequired();
            pm.broadcast(Text.literal("资源包地址: " + urlRet), false);
            pm.broadcast(Text.literal("资源包哈希: " + hash), false);
            pm.broadcast(Text.literal("资源包提示: " + (prompt == null ? "未提供" : prompt.getString())), false);
            pm.broadcast(Text.literal("是否强制要求加载: " + (required ? "是" : "否")), false);
        } else {
            pm.broadcast(Text.literal("当前未设置加载的资源包"), false);
        }
    }
}

