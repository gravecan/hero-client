package io.github.lefraudeur.events;

import io.github.lefraudeur.Main;
import io.github.lefraudeur.modules.Module;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;

import net.minecraft.client.render.RenderTickCounter;

public class PreRender2DEvent extends Event {
    private final InGameHud inGameHud;
    private final DrawContext drawContext;
    private final RenderTickCounter renderTickCounter;

    public PreRender2DEvent(InGameHud inGameHud, DrawContext drawContext, RenderTickCounter renderTickCounter) {
        this.inGameHud = inGameHud;
        this.drawContext = drawContext;
        this.renderTickCounter = renderTickCounter;
    }

    @Override
    public void dispatch() {
        for (Module module : Main.modules)
            if (module.isEnabled())
                module.onPreRender2DEvent(this);
    }

    public InGameHud getInGameHud() {
        return inGameHud;
    }

    public DrawContext getDrawContext() {
        return drawContext;
    }

    public float getTickDelta() {
        return 1.0f; 
    }
}
