package io.github.lefraudeur.events;

import io.github.lefraudeur.Main;
import io.github.lefraudeur.modules.Module;
import io.github.lefraudeur.utils.player.ChatUtils;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.client.render.RenderTickCounter;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import org.joml.Vector4f;

public class PostRender3DEvent extends PreRender3DEvent {
    public PostRender3DEvent(WorldRenderer worldRenderer, ObjectAllocator objectAllocator,
            RenderTickCounter renderTickCounter, boolean renderBlockOutline, Camera camera, Matrix4f projectionMatrix,
            Matrix4f positionMatrix, GpuBufferSlice gpuBufferSlice, Vector4f fogColor, boolean thickFog) {
        super(worldRenderer, objectAllocator, renderTickCounter, renderBlockOutline, camera, projectionMatrix,
                positionMatrix, gpuBufferSlice, fogColor, thickFog);
    }

    @Override
    public void dispatch() {
    }
}
