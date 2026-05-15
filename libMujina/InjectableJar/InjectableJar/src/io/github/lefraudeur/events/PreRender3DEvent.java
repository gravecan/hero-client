package io.github.lefraudeur.events;

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

public class PreRender3DEvent extends Event {
    private WorldRenderer worldRenderer;
    private ObjectAllocator objectAllocator;
    private RenderTickCounter renderTickCounter;
    private boolean renderBlockOutline;
    private Camera camera;
    private Matrix4f projectionMatrix;
    private Matrix4f positionMatrix;
    private GpuBufferSlice gpuBufferSlice;
    private Vector4f fogColor;
    private boolean thickFog;

    public PreRender3DEvent(WorldRenderer worldRenderer, ObjectAllocator objectAllocator,
            RenderTickCounter renderTickCounter, boolean renderBlockOutline, Camera camera, Matrix4f projectionMatrix,
            Matrix4f positionMatrix, GpuBufferSlice gpuBufferSlice, Vector4f fogColor, boolean thickFog) {
        this.worldRenderer = worldRenderer;
        this.objectAllocator = objectAllocator;
        this.renderTickCounter = renderTickCounter;
        this.renderBlockOutline = renderBlockOutline;
        this.camera = camera;
        this.projectionMatrix = projectionMatrix;
        this.positionMatrix = positionMatrix;
        this.gpuBufferSlice = gpuBufferSlice;
        this.fogColor = fogColor;
        this.thickFog = thickFog;
    }

    @Override
    public void dispatch() {
        
        
    }

    public float getTickDelta() {
        return 1.0f; 
    }
}
