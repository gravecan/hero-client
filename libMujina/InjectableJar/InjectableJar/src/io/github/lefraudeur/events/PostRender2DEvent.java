package io.github.lefraudeur.events;

import io.github.lefraudeur.Main;
import io.github.lefraudeur.modules.Module;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;

import net.minecraft.client.render.RenderTickCounter;

public class PostRender2DEvent extends PreRender2DEvent 
{
    public PostRender2DEvent(InGameHud inGameHud, DrawContext drawContext, RenderTickCounter renderTickCounter) {
        super(inGameHud, drawContext, renderTickCounter);
    }

    @Override
    public void dispatch() {
        for (Module module : Main.modules)
            if (module.isEnabled())
                module.onPostRender2DEvent(this);
    }
}
