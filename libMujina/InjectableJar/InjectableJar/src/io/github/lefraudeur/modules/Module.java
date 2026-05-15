package io.github.lefraudeur.modules;

import io.github.lefraudeur.events.*;

import io.github.lefraudeur.utils.player.ChatUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.Packet;

import java.util.Objects;
import java.util.List;
import java.util.stream.Stream;

public abstract class Module {

    public static final int key_none = -1;
    public boolean canToggle; 
    private boolean enabled;
    private int keyBind;
    private final Category category;
    private final String name;
    private final String description;

    public static MinecraftClient mc;

    public Module() {
        final Info module = this.getClass().getAnnotation(Info.class);
        category = module.category();
        category.addModule(this);
        keyBind = module.key();
        name = module.name();
        description = module.description();
        enabled = false;
        canToggle = true;
    }

    public void enable() {
        if (enabled)
            return;
        enabled = true;
        onEnable();
    }

    public void disable() {
        if (!enabled)
            return;
        enabled = false;
        onDisable();
    }

    protected List<io.github.lefraudeur.settings.Setting<?>> settings = new java.util.ArrayList<>();

    protected <T extends io.github.lefraudeur.settings.Setting<?>> T addSetting(T setting) {
        settings.add(setting);
        return setting;
    }

    public List<io.github.lefraudeur.settings.Setting<?>> getSettings() {
        return settings;
    }

    public io.github.lefraudeur.settings.Setting<?> getSettingByName(String name) {
        for (io.github.lefraudeur.settings.Setting<?> setting : settings) {
            if (setting.getName().equalsIgnoreCase(name)) {
                return setting;
            }
        }
        return null;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Module getMe() {
        return this;
    }

    public void toggle() {
        this.enabled = !this.enabled;
        if (this.enabled) {
            onEnable();
        } else {
            onDisable();
        }
        io.github.lefraudeur.web.SocketServer.broadcast(io.github.lefraudeur.web.SocketServer.buildStateUpdate());
    }

    protected void onEnable() {
        
        
    }

    protected void onDisable() {
        
        
    }

    public void onPacketSendEvent(final PacketSendEvent event) {
    }

    public void onPreTickEvent(final PreTickEvent event) {
    }

    public void onPostTickEvent(final PostTickEvent event) {

    }

    public void onAttackEvent(final AttackEvent event) {
    }

    public void onPacketReceiveEvent(final PacketReceiveEvent event) {
    }

    public void onPreRender2DEvent(PreRender2DEvent event) {
    }

    public void onPostRender2DEvent(PostRender2DEvent event) {
    }

    public void onPreDoAttackEvent(PreDoAttackEvent event) {
    }

    public void onPostDoAttackEvent(PostDoAttackEvent event) {
    }

    public void onMidUpdateTargetedEntityEvent(MidUpdateTargetedEntityEvent event) {
    }

    public void onBlockCollisionEvent(BlockCollisionEvent event) {
    }

    public boolean isNull() {
        return (mc.player == null || mc.world == null);
    }

    public void send(final Packet<?> packetIn) 
    {
        if (packetIn == null) {
            return;
        }
        Objects.requireNonNull(mc.getNetworkHandler()).getConnection().send(packetIn);
    }

    public void message(String message) {
        ChatUtils.addChatMessage(message);
    }

    public int getKeyBind() {
        return keyBind;
    }

    public void setKey(final int bind) {
        this.keyBind = bind;
    }

    public int getDefaultKey() {
        return keyBind;
    }

    public String getName() {
        return name;
    }

    public Category getCategory() {
        return category;
    }

}
