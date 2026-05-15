package io.github.lefraudeur.modules.combat;

import io.github.lefraudeur.modules.Module;
import io.github.lefraudeur.modules.Info;
import io.github.lefraudeur.modules.Category;
import io.github.lefraudeur.events.PreTickEvent;
import io.github.lefraudeur.settings.NumberSetting;
import io.github.lefraudeur.settings.KeybindSetting;
import io.github.lefraudeur.settings.BooleanSetting;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Field;

@Info(
    name = "PearlKey",
    description = "Lanza perlas automáticamente al presionar la tecla",
    category = Category.MACROS,
    key = GLFW.GLFW_KEY_UNKNOWN
)
public class PearlKey extends Module {

    private final KeybindSetting throwKey = addSetting(new KeybindSetting("Tecla Lanzar", GLFW.GLFW_KEY_UNKNOWN));
    private final NumberSetting throwDelay = addSetting(new NumberSetting("Throw Delay", 500.0, 100.0, 5000.0, 50.0));
    private final NumberSetting slotSwitchDelay = addSetting(new NumberSetting("Slot Switch Delay", 50.0, 0.0, 150.0, 5.0));
    private final BooleanSetting switchBack = addSetting(new BooleanSetting("Switch Back", true));
    
    private static final long SERVER_SYNC_DELAY = 30;
    private static final boolean STRICT_MODE = true;

    private long lastThrowTime = 0;
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
        if (now - lastThrowTime < throwDelay.getValue().longValue()) {
            keyHeld = true;
            return;
        }

        int pearlSlot = findPearlSlot();
        if (pearlSlot == -1) {
            keyHeld = true;
            return;
        }

        // ✅ FIX: Cambiado de Items.ENDER_PEARL a new ItemStack(Items.ENDER_PEARL)
        if (mc.player.getItemCooldownManager().isCoolingDown(new ItemStack(Items.ENDER_PEARL))) {
            keyHeld = true;
            return;
        }

        lastThrowTime = now;
        keyHeld = true;
        throwing = true;

        throwPearl(pearlSlot);
    }

    private void throwPearl(int slot) {
        new Thread(() -> {
            try {
                if (mc.player == null || mc.interactionManager == null) {
                    throwing = false;
                    return;
                }

                int originalSlot = getSelectedSlot();

                if (slot != originalSlot) {
                    long now = System.currentTimeMillis();
                    long timeSinceSwitch = now - lastSlotSwitch;
                    long requiredDelay = slotSwitchDelay.getValue().longValue();
                    
                    if (requiredDelay > 0 && timeSinceSwitch < requiredDelay) {
                        Thread.sleep(requiredDelay - timeSinceSwitch);
                    }
                    
                    mc.execute(() -> setSelectedSlot(slot));
                    
                    lastSlotSwitch = System.currentTimeMillis();
                    lastSlot = slot;
                    
                    long switchDelay = slotSwitchDelay.getValue().longValue();
                    if (STRICT_MODE) switchDelay += 10;
                    if (switchDelay > 0) Thread.sleep(switchDelay);
                }

                long syncDelay = SERVER_SYNC_DELAY;
                if (STRICT_MODE && slot != originalSlot) syncDelay += 10;
                if (syncDelay > 0) Thread.sleep(syncDelay);

                mc.execute(() -> {
                    if (mc.player == null || mc.interactionManager == null) return;
                    
                    int verifySlot = getSelectedSlot();
                    if (STRICT_MODE && verifySlot != slot) {
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

                long finalWait = STRICT_MODE ? 100 : 75;
                if (finalWait > 0) Thread.sleep(finalWait);

                if (switchBack.getValue() && slot != originalSlot) {
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
        return 0;
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
        if (mc.player == null) return -1;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() == Items.ENDER_PEARL) {
                return i;
            }
        }
        return -1;
    }
}