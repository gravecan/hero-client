package io.github.lefraudeur.modules.macros;

import io.github.lefraudeur.events.PreRender2DEvent;
import io.github.lefraudeur.modules.Category;
import io.github.lefraudeur.modules.Info;
import io.github.lefraudeur.modules.Module;
import io.github.lefraudeur.settings.NumberSetting;
import io.github.lefraudeur.utils.ReflectionHelper;
import io.github.lefraudeur.utils.inventory.FastMoveEngine;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Info(name = "ItemScroller", description = "Rapidly move items in inventories by dragging", category = Category.MACROS, key = Module.key_none)
public class ItemScroller extends Module {

    private final NumberSetting delayMin = addSetting(new NumberSetting("Delay Min (ms)", 10.0, 0.0, 100.0, 1.0));
    private final NumberSetting delayMax = addSetting(new NumberSetting("Delay Max (ms)", 30.0, 0.0, 100.0, 1.0));

    private long lastActionTime = 0;
    private long currentActionDelay = 0;
    private boolean isDragging = false;
    private final Set<Integer> draggedSlots = new HashSet<>();

    @Override
    protected void onEnable() {
        resetState();
    }

    @Override
    protected void onDisable() {
        resetState();
    }

    private void resetState() {
        isDragging = false;
        draggedSlots.clear();
        lastActionTime = 0;
        updateRandomDelay();
    }

    public boolean canPerformAction() {
        return System.currentTimeMillis() - lastActionTime >= currentActionDelay;
    }

    public void recordAction() {
        lastActionTime = System.currentTimeMillis();
        updateRandomDelay();
    }

    private void updateRandomDelay() {
        double min = delayMin.getValue();
        double max = delayMax.getValue();
        if (min >= max) {
            currentActionDelay = (long) min;
        } else {
            currentActionDelay = (long) (min + ThreadLocalRandom.current().nextDouble() * (max - min));
        }
    }

    @Override
    public void onPreRender2DEvent(PreRender2DEvent event) {
        if (mc.currentScreen instanceof HandledScreen<?> screen) {
            handleScreenInteraction(screen);
        } else {
            if (isDragging)
                resetState();
        }
    }

    private void handleScreenInteraction(HandledScreen<?> screen) {
        long window = mc.getWindow().getHandle();
        boolean leftDown = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;

        if (!leftDown) {
            if (isDragging)
                resetState();
            return;
        }

        Slot focusedSlot = getFocusedSlot(screen);
        if (focusedSlot == null || !focusedSlot.hasStack())
            return;

        if (!isDragging)
            isDragging = true;

        if (draggedSlots.contains(focusedSlot.id))
            return;

        boolean shift = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS
                || GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
        boolean ctrl = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS
                || GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS;

        boolean toPlayerInv = !FastMoveEngine.isPlayerInventorySlot(screen.getScreenHandler(), focusedSlot);

        if (ctrl && shift) {
            FastMoveEngine.moveAll(screen.getScreenHandler(), focusedSlot, toPlayerInv);
            draggedSlots.add(focusedSlot.id);
        } else if (ctrl) {
            FastMoveEngine.moveAllMatching(screen.getScreenHandler(), focusedSlot, toPlayerInv);
            draggedSlots.add(focusedSlot.id);
        } else if (shift) {
            FastMoveEngine.moveStack(screen.getScreenHandler(), focusedSlot);
            draggedSlots.add(focusedSlot.id);
        }
    }

    private Slot getFocusedSlot(HandledScreen<?> screen) {
        try {
            Object obj = ReflectionHelper.getFieldValue(screen, "field_2787", "focusedSlot");
            if (obj instanceof Slot slot)
                return slot;
        } catch (Exception ignored) {
        }
        return null;
    }
}
