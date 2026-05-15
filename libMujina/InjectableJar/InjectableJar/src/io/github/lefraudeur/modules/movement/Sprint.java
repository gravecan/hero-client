package io.github.lefraudeur.modules.movement;

import io.github.lefraudeur.events.PreTickEvent;
import io.github.lefraudeur.modules.Category;
import io.github.lefraudeur.modules.Info;
import io.github.lefraudeur.modules.Module;

@Info(name = "Sprint", description = "Automatically sprints when moving", category = Category.MOVEMENT, key = Module.key_none)
public class Sprint extends Module {

    @Override
    public void onPreTickEvent(PreTickEvent event) {
        if (mc.player == null) return;

        // Logic from reference:
        // float forwardSpeed = player.forwardSpeed; // We can check movementInput
        // boolean isSprinting = player.isSprinting();
        // boolean isSneaking = player.isSneaking();
        // boolean isSwimming = player.isSwimming();
        // boolean isUsingItem = player.isUsingItem();
        // boolean canSprint = forwardSpeed > 0 && !isSneaking && !isSwimming && !isUsingItem;

        // In 1.21/Fabric mappings might differ slightly but logic is same.
        // We can just set sprinting to true if moving forward.
        
        if (mc.player.forwardSpeed > 0 && !mc.player.isSneaking() && !mc.player.horizontalCollision && !mc.player.isUsingItem()) {
            mc.player.setSprinting(true);
        }
    }
    
    @Override
    protected void onDisable() {
        if (mc.player != null) {
            mc.player.setSprinting(false);
        }
    }
}
