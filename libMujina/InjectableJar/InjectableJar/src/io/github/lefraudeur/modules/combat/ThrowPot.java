package io.github.lefraudeur.modules.combat;

import io.github.lefraudeur.modules.Module;
import io.github.lefraudeur.modules.Info;
import io.github.lefraudeur.modules.Category;
import io.github.lefraudeur.events.PreTickEvent;
import io.github.lefraudeur.settings.BooleanSetting;
import io.github.lefraudeur.settings.NumberSetting;
import io.github.lefraudeur.settings.MultiChoiceSetting;
import io.github.lefraudeur.settings.KeybindSetting;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@Info(
    name = "ThrowPot",
    description = "Auto throw potions - GrimAC Post bypass",
    category = Category.MACROS,
    key = GLFW.GLFW_KEY_V
)
public class ThrowPot extends Module {

    private final NumberSetting minHealth = addSetting(new NumberSetting("Min Health", 10.0, 1.0, 20.0, 0.5));
    private final BooleanSetting doublePot = addSetting(new BooleanSetting("Double Pot", false));
    private final BooleanSetting switchBack = addSetting(new BooleanSetting("Switch Back", true));
    private final NumberSetting throwDelay = addSetting(new NumberSetting("Throw Delay", 150.0, 0.0, 500.0, 10.0));
    private final NumberSetting cooldown = addSetting(new NumberSetting("Cooldown", 500.0, 0.0, 3000.0, 50.0));
    private final NumberSetting slotSwitchDelay = addSetting(new NumberSetting("Slot Switch Delay", 50.0, 0.0, 150.0, 5.0));
    private final NumberSetting serverSyncDelay = addSetting(new NumberSetting("Server Sync Delay", 30.0, 0.0, 100.0, 5.0));
    private final BooleanSetting strictMode = addSetting(new BooleanSetting("Strict Mode", true));
    private final MultiChoiceSetting potionType = addSetting(new MultiChoiceSetting("Potion Type",
            new ArrayList<>(List.of("Health")), "Health", "Strength", "Speed"));
    private final KeybindSetting throwKey = addSetting(new KeybindSetting("Throw Key", GLFW.GLFW_KEY_V));

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
        if (now - lastUse < cooldown.getValue().longValue()) {
            keyHeld = true;
            return;
        }

        if (mc.player.getHealth() > minHealth.getValue().floatValue()) {
            keyHeld = true;
            return;
        }

        int potSlot = findPotionSlot();
        if (potSlot == -1) {
            keyHeld = true;
            return;
        }

        lastUse = now;
        keyHeld = true;
        throwing = true;

        throwPotions(potSlot);
    }

    private int getNextPotionSlot(int current) {
        for (int i = current + 1; i < 9; i++) {
            ItemStack s = mc.player.getInventory().getStack(i);
            if (isValidPotion(s)) return i;
        }
        for (int i = 0; i < current; i++) {
            ItemStack s = mc.player.getInventory().getStack(i);
            if (isValidPotion(s)) return i;
        }
        return -1;
    }

    private void throwPotions(int slot1) {
        new Thread(() -> {
            try {
                if (mc.player == null || mc.interactionManager == null) {
                    throwing = false;
                    return;
                }

                int originalSlot = getSelectedSlot();
                boolean doubleThrow = doublePot.getValue();
                boolean strict = strictMode.getValue();
                
                int slot2 = doubleThrow ? getNextPotionSlot(slot1) : -1;
                int[] slots = (doubleThrow && slot2 != -1) ? new int[]{slot1, slot2} : new int[]{slot1};

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
                        if (!isValidPotion(stack)) return;

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

    private int findPotionSlot() {
        int cur = getSelectedSlot();
        int best = -1, minD = 10;
        for (int i = 0; i < 9; i++) {
            ItemStack s = mc.player.getInventory().getStack(i);
            if (isValidPotion(s)) {
                int d = Math.abs(i - cur);
                if (d < minD) { 
                    minD = d; 
                    best = i; 
                }
            }
        }
        return best;
    }

    private boolean isValidPotion(ItemStack s) {
        if (s == null || s.isEmpty() || s.getItem() != Items.SPLASH_POTION) return false;
        PotionContentsComponent c = s.get(DataComponentTypes.POTION_CONTENTS);
        if (c == null || c.potion().isEmpty()) return false;
        RegistryEntry<Potion> p = c.potion().get();
        return p.value().getEffects().stream().anyMatch(e -> {
            boolean h = e.getEffectType().equals(net.minecraft.entity.effect.StatusEffects.INSTANT_HEALTH);
            boolean str = e.getEffectType().equals(net.minecraft.entity.effect.StatusEffects.STRENGTH);
            boolean sp = e.getEffectType().equals(net.minecraft.entity.effect.StatusEffects.SPEED);
            return (potionType.isSelected("Health") && h) ||
                   (potionType.isSelected("Strength") && str) ||
                   (potionType.isSelected("Speed") && sp);
        });
    }
}