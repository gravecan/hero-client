package io.github.lefraudeur.modules.macros;

import io.github.lefraudeur.events.PreTickEvent;
import io.github.lefraudeur.modules.Category;
import io.github.lefraudeur.modules.Info;
import io.github.lefraudeur.modules.Module;
import io.github.lefraudeur.settings.NumberSetting;
import io.github.lefraudeur.utils.ReflectionHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.lwjgl.glfw.GLFW;

@Info(name = "FastEXP", description = "Just Throw Exp Fast", category = Category.MACROS, key = Module.key_none)
public class FastEXP extends Module {

    private final NumberSetting chance = addSetting(new NumberSetting("Chance %", 100.0, 0.0, 100.0, 1.0));

    @Override
    public void onPreTickEvent(PreTickEvent event) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (mc.currentScreen != null) return;

        ItemStack heldItem = mc.player.getMainHandStack();
        if (heldItem.isEmpty() || heldItem.getItem() != Items.EXPERIENCE_BOTTLE) return;

        // Check for Right Click
        if (GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_2) != GLFW.GLFW_PRESS) {
            return;
        }

        if (Math.random() * 100 < chance.getValue()) {
            try {
                // Set itemUseCooldown to 0
                // Field names for 1.21: itemUseCooldown, field_1752, cooldown
                ReflectionHelper.setFieldValue(mc, "field_1752", "itemUseCooldown", 0);
            } catch (Exception e) {
                // Try 'cooldown' if above fails, though ReflectionHelper handles multiple names if passed correctly.
                // Since ReflectionHelper takes one obf and one deobf, we might rely on it or just try another call if needed.
                // We'll trust ReflectionHelper's loop or simple try-catch block here is enough for now.
                e.printStackTrace();
            }
        }
    }
}
