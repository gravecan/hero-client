package io.github.lefraudeur.modules.combat;

import io.github.lefraudeur.Main;
import io.github.lefraudeur.events.PreDoAttackEvent;
import io.github.lefraudeur.modules.Category;
import io.github.lefraudeur.modules.Info;
import io.github.lefraudeur.modules.Module;
import io.github.lefraudeur.settings.BooleanSetting;
import io.github.lefraudeur.settings.NumberSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;

@Info(name = "HitSelect", description = "Optimizes hits by waiting for the perfect moment/distance", category = Category.COMBAT, key = Module.key_none)
public class HitSelect extends Module {

    public static final NumberSetting range = new NumberSetting("Optimal Range", 2.9, 2.5, 3.0, 0.1);
    public static final BooleanSetting predictMovement = new BooleanSetting("Predict Movement", true);

    private final NumberSetting rangeSetting = addSetting(range);
    private final BooleanSetting predictSetting = addSetting(predictMovement);

    @Override
    public void onPreDoAttackEvent(PreDoAttackEvent event) {
        if (mc.crosshairTarget == null || mc.crosshairTarget.getType() != HitResult.Type.ENTITY)
            return;

        Entity target = ((EntityHitResult) mc.crosshairTarget).getEntity();
        if (shouldWait(target)) {
            event.cancel(true);
        }
    }

    public static boolean shouldWait(Entity target) {
        if (target == null || !(target instanceof LivingEntity))
            return false;

        HitSelect instance = (HitSelect) Main.getModuleByClass(HitSelect.class);
        if (instance == null || !instance.isEnabled())
            return false;

        double dist = mc.player.distanceTo(target);
        if (dist > range.getValue().doubleValue()) {
            if (predictMovement.getValue() && isTargetMovingAway(target)) {
                // If moving away, waiting might lose the hit entirely?
                // Usually HitSelect means "Wait until they come CLOSE or are in OPTIMAL range".
                // If they are far and moving away, we might want to hit NOW before they leave
                // range?
                // Actually, "Hit Select" often means "Don't hit at max range if you can get a
                // better combo by waiting".
                // But if they are fleeing, waiting is bad.
                // Correct logic: Wait if they are far BUT approaching (so we hit them
                // closer/better context).
                // OR if they are far and static?
                // Let's stick to the logic requested earlier: "si esta lejos (>2.8) y se esta
                // alejando [espera?]".
                // Wait, User request said: "hit select... si el target esta lejos (>2.8) y se
                // esta alejando, espera".
                // That sounds counter-intuitive (if leaving, hit now!).
                // Maybe "separating"? When separating, KB pushes them out of combo.
                // If we wait, they might stop or we get closer?
                // Actually, typical implementation: Cancel if cooldown < X OR if distance >
                // optimal AND moving away?
                // OR Cancel if distance > optimal AND NOT moving away (so we can get closer)?
                // Let's use the logic I wrote in TriggerBot which user liked:
                // "if (dist > range) { if (isMovingAway) return; }" -> WAIT if moving away?
                // Wait, `return` in TriggerBot meant "don't attack". So "Wait".
                // Meaning: If far and moving away, DON'T hit.
                // Why? To avoid dealing low KB at max range that pushes them out of reach?
                // Yes, 1.8 PvP mechanics often favor letting them come to you or get closer for
                // better combos.
                // So "If > 2.9 and Moving Away -> Cancel".
                return isTargetMovingAway(target);
            }
            return false;
        }
        return false;
    }

    private static boolean isTargetMovingAway(Entity target) {
        Vec3d playerPos = mc.player.getPos();
        Vec3d targetVel = target.getVelocity();
        Vec3d toTarget = target.getPos().subtract(playerPos).normalize();
        double dot = targetVel.normalize().dotProduct(toTarget);
        // dot > 0 means moving in same direction (away if we are behind, or they are
        // fleeing)
        return dot > 0.5;
    }
}
