package team.creative.ambientsounds;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.creative.ambientsounds.engine.AmbientEngine;
import team.creative.ambientsounds.engine.AmbientTickHandler;
import team.creative.creativecore.CreativeCore;
import team.creative.creativecore.ICreativeLoader;
import team.creative.creativecore.client.ClientLoader;
import team.creative.creativecore.client.CreativeCoreClient;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Mod(value = AmbientSounds.MODID)
public class AmbientSounds implements ClientLoader {

    public static final Logger LOGGER = LogManager.getLogger(AmbientSounds.MODID);
    public static final String MODID = "ambientsounds";
    public static final String MOD_NAME = "AmbientSounds";
    public static final AmbientSoundsConfig CONFIG = new AmbientSoundsConfig();

    public static AmbientTickHandler TICK_HANDLER;

    private static final @NotNull Executor RELOAD_THREAD_POOL_EXECUTOR = Executors.newFixedThreadPool(
            1, new ThreadFactoryBuilder().setNameFormat(String.format("%s Reloader", MOD_NAME)).build());

    public AmbientSounds() {
        ICreativeLoader loader = CreativeCore.loader();
        loader.registerClient(this);
    }

    public static void scheduleReload() {
        TICK_HANDLER.scheduleReload();
    }

    public static void reloadAsync() {
        CompletableFuture.runAsync(AmbientSounds::reload, RELOAD_THREAD_POOL_EXECUTOR);
    }

    private static void reload() {
        if (TICK_HANDLER.engine != null)
            TICK_HANDLER.engine.stopEngine();
        if (TICK_HANDLER.environment != null)
            TICK_HANDLER.environment.reload();
        TICK_HANDLER.setEngine(AmbientEngine.loadAmbientEngine(TICK_HANDLER.soundEngine));
    }

    @Override
    public void onInitializeClient() {
        ICreativeLoader loader = CreativeCore.loader();

        TICK_HANDLER = new AmbientTickHandler();
        loader.registerClientTick(TICK_HANDLER::onTick);
        loader.registerClientRenderGui(TICK_HANDLER::onRender);
        loader.registerLoadLevel(TICK_HANDLER::loadLevel);

        loader.registerClientStarted(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            ReloadableResourceManager reloadableResourceManager = (ReloadableResourceManager) minecraft.getResourceManager();

            reloadableResourceManager.registerReloadListener(new SimplePreparableReloadListener<Void>() {
                @SuppressWarnings("NullableProblems")
                @Override
                protected @Nullable Void prepare(@NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profilerFiller) {
                    AmbientSounds.reloadAsync();
                    return null;
                }

                @Override
                protected void apply(@Nullable Void object, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profilerFiller) {
                    // NO-OP
                }
            });
        });

        CreativeCoreClient.registerClientConfig(MODID);
    }

    @Override
    public <T> void registerClientCommands(CommandDispatcher<T> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<T>literal("ambient-debug").executes(x -> {
            TICK_HANDLER.showDebugInfo = !TICK_HANDLER.showDebugInfo;
            return Command.SINGLE_SUCCESS;
        }));
        dispatcher.register(LiteralArgumentBuilder.<T>literal("ambient-reload").executes(x -> {
            AmbientSounds.reloadAsync();
            return Command.SINGLE_SUCCESS;
        }));
    }

}
