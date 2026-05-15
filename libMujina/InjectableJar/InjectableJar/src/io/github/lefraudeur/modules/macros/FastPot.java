package io.github.lefraudeur.modules.macros;

import io.github.lefraudeur.events.PreTickEvent;
import io.github.lefraudeur.modules.Category;
import io.github.lefraudeur.modules.Info;
import io.github.lefraudeur.modules.Module;
import io.github.lefraudeur.utils.ReflectionHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.item.SplashPotionItem;

@Info(name = "FastPot", description = "Makes potions throw instantly with no delay", category = Category.MACROS, key = Module.key_none)
public class FastPot extends Module {

    @Override
    public void onPreTickEvent(PreTickEvent event) {
        if (mc.player == null || mc.interactionManager == null) return;

        if (mc.player.isUsingItem()) return;

        ItemStack main = mc.player.getMainHandStack();
        if (isPotion(main)) {
            try {
                // Get current cooldown
                Object cooldownObj = ReflectionHelper.getFieldValue(mc, "field_1752", "itemUseCooldown");
                int currentCooldown = 0;
                if (cooldownObj instanceof Integer) {
                    currentCooldown = (int) cooldownObj;
                }

                if (currentCooldown > 1) {
                    ReflectionHelper.setFieldValue(mc, "field_1752", "itemUseCooldown", 1);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isPotion(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        return stack.getItem() instanceof SplashPotionItem || stack.getItem() instanceof PotionItem;
    }
}
