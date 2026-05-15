package io.github.lefraudeur;

import io.github.lefraudeur.modules.Category;
import io.github.lefraudeur.modules.Module;
import net.minecraft.client.MinecraftClient;
import org.jetbrains.annotations.Nullable;

import java.io.FileWriter;
import java.util.Arrays;
import java.util.Comparator;

public final class Main {
    public static MinecraftClient mc;
    public static Module[] modules = null;
    public final static String clientVersion = "1.0";

    private static final String LOG_PATH = "C:\\Users\\Public\\mujina_java_log.txt";
    private static final int SERVER_PORT = 6969;

    public static void main(String[] args) {
    }

    public static void init() {
        logInitStart();
        initializeMinecraft();
        initializeModules();
        startServer();
    }

    private static void logInitStart() {
        try (FileWriter fw = new FileWriter(LOG_PATH, true)) {
            fw.write("=== Java init() started ===\n");
            fw.write("Java version: " + System.getProperty("java.version") + "\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void initializeMinecraft() {
        try {
            mc = MinecraftClient.getInstance();
            Module.mc = mc;
            System.out.println("[INIT] MinecraftClient initialized");
        } catch (Throwable t) {
            System.err.println("[ERROR] Failed to get MinecraftClient: " + t.getMessage());
            logError(t);
        }
    }

    private static void initializeModules() {
        System.out.println("[INIT] Initializing modules...");
        java.util.List<Module> moduleList = new java.util.ArrayList<>();

        try {
            moduleList.add(new io.github.lefraudeur.modules.combat.TriggerBot());
        } catch (Throwable t) {
            logError(t);
        }
        try {
            moduleList.add(new io.github.lefraudeur.modules.combat.WTap());
        } catch (Throwable t) {
            logError(t);
        }
        try {
            moduleList.add(new io.github.lefraudeur.modules.combat.HitSelect());
        } catch (Throwable t) {
            logError(t);
        }
        try {
            moduleList.add(new io.github.lefraudeur.modules.combat.ThrowPot());
        } catch (Throwable t) {
            logError(t);
        }
        try {
            moduleList.add(new io.github.lefraudeur.modules.macros.KeyPearl());
        } catch (Throwable t) {
            logError(t);
        }
        try {
            moduleList.add(new io.github.lefraudeur.modules.macros.FastEXP());
        } catch (Throwable t) {
            logError(t);
        }
        try {
            moduleList.add(new io.github.lefraudeur.modules.macros.FastPot());
        } catch (Throwable t) {
            logError(t);
        }
        try {
            moduleList.add(new io.github.lefraudeur.modules.macros.ItemScroller());
        } catch (Throwable t) {
            logError(t);
        }
        try {
            moduleList.add(new io.github.lefraudeur.modules.movement.Sprint());
        } catch (Throwable t) {
            logError(t);
        }

        modules = moduleList.toArray(new Module[0]);
        System.out.println("[INIT] " + modules.length + " module(s) loaded");
    }

    private static void startServer() {
        try {
            System.out.println("[INIT] Starting SocketServer on port " + SERVER_PORT + "...");
            new io.github.lefraudeur.web.SocketServer(SERVER_PORT).start();
            System.out.println("[INIT] Server started successfully");
        } catch (Throwable t) {
            System.err.println("[FATAL] Server startup failed: " + t.toString());
            t.printStackTrace();
            logError(t);
        }
    }

    private static void logError(Throwable t) {
        try (FileWriter fw = new FileWriter(LOG_PATH, true)) {
            fw.write("[FATAL]: " + t.toString() + "\n");
        } catch (Exception ignored) {
        }
    }

    public static void shutdown() {
        if (modules == null)
            return;

        for (Module module : modules) {
            if (module.isEnabled()) {
                module.disable();
            }
        }
    }

    public static Module[] getEnabledModules() {
        if (modules == null)
            return new Module[0];

        return Arrays.stream(modules)
                .filter(Module::isEnabled)
                .toArray(Module[]::new);
    }

    public static Module[] getEnabledSortedModules() {
        return Arrays.stream(getEnabledModules())
                .sorted(Comparator.comparingInt((Module module) -> mc.textRenderer.getWidth(module.getName()))
                        .reversed())
                .toArray(Module[]::new);
    }

    @Nullable
    public static Module getModuleByClass(Class<? extends Module> moduleType) {
        if (modules == null)
            return null;

        for (Module module : modules) {
            if (module.getClass() == moduleType) {
                return module;
            }
        }
        return null;
    }

    @Nullable
    public static Module getModuleByName(String name) {
        if (modules == null || name == null)
            return null;

        for (Module module : modules) {
            if (module.getName().equalsIgnoreCase(name)) {
                return module;
            }
        }
        return null;
    }

    @Nullable
    public static Category getCategoryByName(String name) {
        if (modules == null || name == null)
            return null;

        for (Module module : modules) {
            if (module.getName().equalsIgnoreCase(name)) {
                return module.getCategory();
            }
        }
        return null;
    }
}