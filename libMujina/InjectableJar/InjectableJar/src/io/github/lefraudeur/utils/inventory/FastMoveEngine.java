package io.github.lefraudeur.utils.inventory;

import io.github.lefraudeur.modules.Module;
import io.github.lefraudeur.modules.macros.ItemScroller;
import io.github.lefraudeur.Main;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

import java.util.ArrayList;
import java.util.List;

public class FastMoveEngine {

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    private static ItemScroller getModule() {
        // Main.getModuleByClass returns Module, cast needed
        return (ItemScroller) Main.getModuleByClass(ItemScroller.class);
    }

    public static void moveSingleItem(ScreenHandler handler, Slot sourceSlot, boolean toPlayerInventory) {
        ItemScroller module = getModule();
        if (module == null || !module.isEnabled() || !module.canPerformAction()) {
            return;
        }

        if (sourceSlot == null || !sourceSlot.hasStack())
            return;

        ItemStack sourceStack = sourceSlot.getStack();
        if (sourceStack.isEmpty())
            return;

        List<Slot> destSlots = getDestinationSlots(handler, sourceSlot, toPlayerInventory);
        if (destSlots.isEmpty())
            return;

        Slot destSlot = SlotMatcher.findDestinationSlot(destSlots, sourceStack, true);
        if (destSlot == null)
            return;

        clickSlot(handler, sourceSlot.id, 0, SlotActionType.PICKUP);
        module.recordAction();

        clickSlot(handler, destSlot.id, 1, SlotActionType.PICKUP); // Right click to place one?
        // Wait, original logic logic:
        // pickup source (holds it)
        // pickup dest with button 1 (which might swap or place one?)
        // pickup source again?
        // Actually, to move single item efficiently usually you SHIFT+CLICK but that
        // moves Stack.
        // To move single item: Pickup stack, Right click dest (places 1), Place
        // remaining back.
        // The reference implementation does:
        // click source (0/PICKUP) -> pickup all
        // click dest (1/PICKUP) -> if holding, right click places 1.
        // click source (0/PICKUP) -> place rest back.
        module.recordAction();

        clickSlot(handler, sourceSlot.id, 0, SlotActionType.PICKUP);
        module.recordAction();
    }

    public static void moveStack(ScreenHandler handler, Slot sourceSlot) {
        ItemScroller module = getModule();
        if (module == null || !module.isEnabled() || !module.canPerformAction())
            return;

        if (sourceSlot == null || !sourceSlot.hasStack())
            return;

        clickSlot(handler, sourceSlot.id, 0, SlotActionType.QUICK_MOVE); // Shift-click
        module.recordAction();
    }

    public static void moveAll(ScreenHandler handler, Slot sourceSlot, boolean toPlayerInventory) {
        ItemScroller module = getModule();
        if (module == null || !module.isEnabled())
            return;

        List<Slot> sourceSlots = getSourceSlots(handler, sourceSlot, toPlayerInventory);

        for (Slot slot : sourceSlots) {
            if (!module.canPerformAction())
                break;

            if (slot.hasStack()) {
                clickSlot(handler, slot.id, 0, SlotActionType.QUICK_MOVE);
                module.recordAction();
            }
        }
    }

    public static void moveAllMatching(ScreenHandler handler, Slot sourceSlot, boolean toPlayerInventory) {
        ItemScroller module = getModule();
        if (module == null || !module.isEnabled())
            return;

        if (sourceSlot == null || !sourceSlot.hasStack())
            return;
        ItemStack targetStack = sourceSlot.getStack();

        List<Slot> sourceSlots = getSourceSlots(handler, sourceSlot, toPlayerInventory);

        for (Slot slot : sourceSlots) {
            if (!module.canPerformAction())
                break;

            if (SlotMatcher.slotContainsItem(slot, targetStack, true)) {
                clickSlot(handler, slot.id, 0, SlotActionType.QUICK_MOVE);
                module.recordAction();
            }
        }
    }

    public static boolean isPlayerInventorySlot(ScreenHandler handler, Slot slot) {
        if (slot == null || handler == null)
            return false;
        int totalSlots = handler.slots.size();
        int playerInventoryStart = totalSlots - 36; // Bottom inventory + hotbar
        return slot.id >= playerInventoryStart;
    }

    private static List<Slot> getDestinationSlots(ScreenHandler handler, Slot sourceSlot, boolean toPlayerInventory) {
        List<Slot> slots = new ArrayList<>();
        for (Slot slot : handler.slots) {
            boolean isPlayerSlot = isPlayerInventorySlot(handler, slot);
            if (toPlayerInventory == isPlayerSlot && slot != sourceSlot) {
                slots.add(slot);
            }
        }
        return slots;
    }

    private static List<Slot> getSourceSlots(ScreenHandler handler, Slot sourceSlot, boolean toPlayerInventory) {
        List<Slot> slots = new ArrayList<>();
        for (Slot slot : handler.slots) {
            boolean isPlayerSlot = isPlayerInventorySlot(handler, slot);
            // Move from same section (if moving all from chest, we want all form chest)
            if (toPlayerInventory != isPlayerSlot) {
                slots.add(slot);
            }
        }
        return slots;
    }

    public static void clickSlot(ScreenHandler handler, int slotId, int button, SlotActionType actionType) {
        if (mc.interactionManager == null || handler == null)
            return;
        try {
            mc.interactionManager.clickSlot(
                    handler.syncId,
                    slotId,
                    button,
                    actionType,
                    mc.player);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
