package io.github.lefraudeur.modules.macros;

import io.github.lefraudeur.modules.Module;
import io.github.lefraudeur.modules.Info;
import io.github.lefraudeur.modules.Category;
import io.github.lefraudeur.events.PreTickEvent;
import io.github.lefraudeur.settings.BooleanSetting;
import io.github.lefraudeur.settings.NumberSetting;
import io.github.lefraudeur.settings.KeybindSetting;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Field;

@Info(
    name = "KeyPearl",
    description = "Automatically throws ender pearls when hotkey is pressed (ThrowPot style)",
    category = Category.MACROS,
    key = Module.key_none
)
public class KeyPearl extends Module {

    private final BooleanSetting switchBack = addSetting(new BooleanSetting("Switch Back", true));
    private final BooleanSetting strictMode = addSetting(new BooleanSetting("Strict Mode", true));
    private final NumberSetting throwDelay = addSetting(new NumberSetting("Throw Delay", 0.0, 0.0, 500.0, 10.0));
    private final NumberSetting cooldown = addSetting(new NumberSetting("Cooldown", 1000.0, 0.0, 3000.0, 50.0));
    private final NumberSetting slotSwitchDelay = addSetting(new NumberSetting("Slot Switch Delay", 50.0, 0.0, 150.0, 5.0));
    private final NumberSetting serverSyncDelay = addSetting(new NumberSetting("Server Sync Delay", 30.0, 0.0, 100.0, 5.0));
    private final KeybindSetting throwKey = addSetting(new KeybindSetting("Throw Key", GLFW.GLFW_KEY_Z));

    private long lastUse = 0;
    private boolean keyHeld = false;
    private boolean throwing = false;
    private long lastSlotSwitch = 0;
    private int lastSlot = -1;

    @Override
    public void onPreTickEvent(PreTickEvent event) {
        if (isNull() || !isEnabled() || mc.currentScreen != null) return;
        if (mc.player == null || mc.interactionManager == null) return;

        int key = throwKey.getValue();
        boolean pressed = key != key_none && GLFW.glfwGetKey(mc.getWindow().getHandle(), key) == GLFW.GLFW_PRESS;

        if (!pressed) {
            keyHeld = false;
            return;
        }

        if (keyHeld || throwing) return;

        long now = System.currentTimeMillis();
        // Cooldown interno del módulo
        if (now - lastUse < cooldown.getValue().longValue()) {
            keyHeld = true;
            return;
        }
        
        // Verificar cooldown del item (Ender Pearl)
        if (mc.player.getItemCooldownManager().isCoolingDown(new ItemStack(Items.ENDER_PEARL))) {
             keyHeld = true;
             return;
        }

        int pearlSlot = findPearlSlot();
        if (pearlSlot == -1) {
            keyHeld = true;
            return;
        }

        lastUse = now;
        keyHeld = true;
        throwing = true;

        throwPearl(pearlSlot);
    }

    private void throwPearl(int slot1) {
        new Thread(() -> {
            try {
                if (mc.player == null || mc.interactionManager == null) {
                    throwing = false;
                    return;
                }

                int originalSlot = getSelectedSlot();
                boolean strict = strictMode.getValue();
                
                // Keep the loop structure for 100% fidelity to the snippet, 
                // but only for one slot (Ender Pearl doesn't double throw)
                int[] slots = new int[]{slot1};

                for (int i = 0; i < slots.length; i++) {
                    final int slot = slots[i];
                    
                    if (mc.player == null || mc.interactionManager == null) break;

                    int currentSlot = getSelectedSlot();
                    
                    if (slot != currentSlot) {
                        long now = System.currentTimeMillis();
                        long timeSinceSwitch = now - lastSlotSwitch;
                        long requiredDelay = slotSwitchDelay.getValue().longValue();
                        
                        if (requiredDelay > 0 && timeSinceSwitch < requiredDelay) {
                            Thread.sleep(requiredDelay - timeSinceSwitch);
                        }
                        
                        mc.execute(() -> {
                            setSelectedSlot(slot);
                        });
                        
                        lastSlotSwitch = System.currentTimeMillis();
                        lastSlot = slot;
                        
                        long switchDelay = slotSwitchDelay.getValue().longValue();
                        if (strict) switchDelay += 10;
                        if (switchDelay > 0) Thread.sleep(switchDelay);
                    }

                    long syncDelay = serverSyncDelay.getValue().longValue();
                    if (strict && slot != currentSlot) syncDelay += 10;
                    if (syncDelay > 0) Thread.sleep(syncDelay);

                    mc.execute(() -> {
                        if (mc.player == null || mc.interactionManager == null) return;
                        
                        int verifySlot = getSelectedSlot();
                        if (strict && verifySlot != slot) {
                            return;
                        }
                        
                        ItemStack stack = mc.player.getStackInHand(Hand.MAIN_HAND);
                        if (stack.getItem() != Items.ENDER_PEARL) return;

                        try {
                            ActionResult result = mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                            
                            if (result.isAccepted()) {
                                mc.player.swingHand(Hand.MAIN_HAND, true);
                            }
                            
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                    
                    if (i < slots.length - 1) {
                        // This block won't run for single slot, but kept for structure
                        long delay = throwDelay.getValue().longValue();
                        if (strict) delay += 20;
                        if (delay > 0) Thread.sleep(delay);
                    }
                }

                long finalWait = strict ? 100 : 75;
                if (finalWait > 0) Thread.sleep(finalWait);

                if (switchBack.getValue()) {
                    int currentSlot = getSelectedSlot();
                    if (originalSlot != currentSlot) {
                        long now = System.currentTimeMillis();
                        long timeSinceSwitch = now - lastSlotSwitch;
                        long requiredDelay = slotSwitchDelay.getValue().longValue();
                        
                        if (requiredDelay > 0 && timeSinceSwitch < requiredDelay) {
                            Thread.sleep(requiredDelay - timeSinceSwitch);
                        }
                        
                        mc.execute(() -> setSelectedSlot(originalSlot));
                        lastSlotSwitch = System.currentTimeMillis();
                        lastSlot = originalSlot;
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                throwing = false;
            }
        }).start();
    }

    private int getSelectedSlot() {
        try {
            Object inv = mc.player.getInventory();
            for (String n : new String[]{"selectedSlot", "l", "field_7545", "f_36061_"}) {
                try {
                    Field f = inv.getClass().getDeclaredField(n);
                    f.setAccessible(true);
                    return f.getInt(inv);
                } catch (NoSuchFieldException ignored) {}
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0; // Fallback
    }

    private void setSelectedSlot(int slot) {
        try {
            Object inv = mc.player.getInventory();
            for (String n : new String[]{"selectedSlot", "l", "field_7545", "f_36061_"}) {
                try {
                    Field f = inv.getClass().getDeclaredField(n);
                    f.setAccessible(true);
                    f.setInt(inv, slot);
                    return;
                } catch (NoSuchFieldException ignored) {}
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int findPearlSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() == Items.ENDER_PEARL) {
                return i;
            }
        }
        return -1;
    }
}
