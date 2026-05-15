package io.github.lefraudeur.modules.combat;

import io.github.lefraudeur.events.AttackEvent;
import io.github.lefraudeur.events.PreTickEvent;
import io.github.lefraudeur.modules.Category;
import io.github.lefraudeur.modules.Info;
import io.github.lefraudeur.modules.Module;
import io.github.lefraudeur.settings.NumberSetting;

@Info(name = "WTap", description = "Automatically resets sprint after attacking for extra knockback", category = Category.COMBAT, key = Module.key_none)
public class WTap extends Module {

    private final NumberSetting delay = addSetting(new NumberSetting("Delay (Ticks)", 2.0, 1.0, 5.0, 1.0));

    private int state = 0; // 0: Idle, 1: Release W, 2: Wait to Press W
    private int counter = 0;

    @Override
    public void onAttackEvent(AttackEvent event) {
        if (event.getAttacker() != mc.player)
            return;

        // Only W-Tap if sprinting and moving forward
        if (mc.player.isSprinting() && mc.options.forwardKey.isPressed()) {
            // Optional: Don't W-Tap if critical hit (already high KB) or in air?
            // Common logic: Always reset sprint for server-side sprint packet reset.
            // But some prefer to keep momentum in air.
            // We'll stick to simple logic: reset if sprinting.
            state = 1;
            counter = 0;
        }
    }

    @Override
    public void onPreTickEvent(PreTickEvent event) {
        if (state == 0)
            return;

        counter++;

        if (state == 1) {
            // First tick after attack: Release W
            mc.options.forwardKey.setPressed(false);
            mc.options.sprintKey.setPressed(false); // Ensure sprint is cancelled

            // Wait for delay
            if (counter >= delay.getValue().intValue()) {
                state = 2;
                counter = 0;
            }
        } else if (state == 2) {
            // Delay passed: Press W again
            mc.options.forwardKey.setPressed(true);
            mc.options.sprintKey.setPressed(true);

            // Reset state
            state = 0;
            counter = 0;
        }
    }
}
