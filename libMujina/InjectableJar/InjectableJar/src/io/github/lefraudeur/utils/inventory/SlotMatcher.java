package io.github.lefraudeur.utils.inventory;

import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class SlotMatcher {

    public static boolean isSameItem(ItemStack stack1, ItemStack stack2, boolean matchNBT) {
        if (stack1.isEmpty() || stack2.isEmpty()) {
            return false;
        }

        if (!stack1.isOf(stack2.getItem())) {
            return false;
        }

        if (matchNBT) {
            // 1.21+ uses DataComponentTypes, but standard method is areEqual for strict check
            // or we can use custom logic. For standard usage:
            return ItemStack.areItemsEqual(stack1, stack2); 
            // Note: in 1.21 areItemsEqual checks item types. areEqual checks everything (components/nbt).
            // references used 'areItemsAndComponentsEqual' which suggests 1.21
        }

        return true;
    }

    public static boolean slotContainsItem(Slot slot, ItemStack targetStack, boolean matchNBT) {
        if (slot == null || !slot.hasStack()) {
            return false;
        }
        return isSameItem(slot.getStack(), targetStack, matchNBT);
    }

    public static boolean canInsertIntoSlot(Slot slot, ItemStack stack) {
        if (slot == null) {
            return false;
        }
        return slot.canInsert(stack);
    }
    
    public static Slot findDestinationSlot(java.util.List<Slot> slots, ItemStack sourceStack, boolean matchNBT) {
        if (sourceStack.isEmpty()) {
            return null;
        }
        
        Slot emptySlot = null;
        
        for (Slot slot : slots) {
            if (!canInsertIntoSlot(slot, sourceStack)) {
                continue;
            }
            
            if (slot.hasStack()) {
                ItemStack slotStack = slot.getStack();
                if (isSameItem(slotStack, sourceStack, matchNBT)) {
                    // Check if stack is not full
                    if (slotStack.getCount() < slotStack.getMaxCount()) {
                        return slot;
                    }
                }
            } else {
                if (emptySlot == null) {
                    emptySlot = slot;
                }
            }
        }
        return emptySlot;
    }
}
