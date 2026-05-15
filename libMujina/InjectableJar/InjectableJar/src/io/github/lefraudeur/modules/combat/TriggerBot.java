package io.github.lefraudeur.modules.combat;

import io.github.lefraudeur.events.PreTickEvent;
import io.github.lefraudeur.modules.Category;
import io.github.lefraudeur.modules.Info;
import io.github.lefraudeur.modules.Module;
import io.github.lefraudeur.settings.BooleanSetting;
import io.github.lefraudeur.settings.ModeSetting;
import io.github.lefraudeur.settings.NumberSetting;
import io.github.lefraudeur.settings.RangeSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import java.util.Random;

@Info(name = "TriggerBot", description = "Automatically attacks entities when looking at them", category = Category.COMBAT, key = Module.key_none)
public class TriggerBot extends Module {

    private final BooleanSetting onlyWeapons = addSetting(new BooleanSetting("Only Weapons", true));
    private final BooleanSetting ignoreWhileClicking = addSetting(new BooleanSetting("Ignore While Clicking", true));
    private final BooleanSetting requireSprint = addSetting(new BooleanSetting("Require Sprint", false));
    private final BooleanSetting checkShield = addSetting(new BooleanSetting("Check Shield", false));
    private final BooleanSetting focusMode = addSetting(new BooleanSetting("Focus Mode", false));
    private final RangeSetting cooldownRange = addSetting(
            new RangeSetting("Cooldown Range", 90.0, 92.0, 0.0, 100.0, 1.0));
    private final NumberSetting missChance = addSetting(new NumberSetting("Miss Chance", 0.0, 0.0, 75.0, 1.0));
    private final ModeSetting targetMode = addSetting(new ModeSetting("Target", "Players", "Players", "Mobs", "All"));
    private final BooleanSetting smartCrits = addSetting(new BooleanSetting("Smart Crits", true));
    private final BooleanSetting onlyCrits = addSetting(new BooleanSetting("Only Crits", false));

    private static final double MAX_HIT_RANGE = 3.0;
    private static final double FOCUS_RANGE = 6.0;
    private static final int MAX_CONSECUTIVE_MISSES = 3;
    private static final int MIN_HITS_BEFORE_MISS = 10;
    private static final double NEAR_MISS_RANGE = 3.6;
    private static final double MISS_ANGLE_THRESHOLD = 15.0;

    private Entity focusedTarget;
    private final Random random = new Random();
    private float targetCooldown = 0.90f;
    private int tickCounter = 0;
    private int consecutiveHits = 0;
    private int consecutiveMisses = 0;
    private Entity lastTarget = null;

    @Override
    public void onPreTickEvent(PreTickEvent event) {
        if (mc.player == null || mc.world == null)
            return;
        if (mc.currentScreen != null)
            return;
        if (!mc.isWindowFocused())
            return;

        tickCounter++;
        if (tickCounter % 10 == 0)
            updateTargetCooldown();

        if (focusMode.getValue() && focusedTarget != null && !focusedTarget.isAlive())
            focusedTarget = null;

        if (mc.player.isUsingItem() || mc.options.useKey.isPressed())
            return;
        if (onlyWeapons.getValue() && !isHoldingWeapon())
            return;
        if (ignoreWhileClicking.getValue() && mc.options.attackKey.isPressed())
            return;

        Entity directTarget = null;
        if (focusMode.getValue() && focusedTarget != null) {
            if (mc.player.distanceTo(focusedTarget) > FOCUS_RANGE) {
                focusedTarget = null;
            } else if (getCrosshairTarget() == focusedTarget) {
                directTarget = focusedTarget;
            }
        } else {
            directTarget = getCrosshairTarget();
        }

        if (directTarget != null && isValidTarget(directTarget)) {
            if (directTarget != lastTarget) {
                consecutiveHits = 0;
                consecutiveMisses = 0;
                lastTarget = directTarget;
            }

            if (HitSelect.shouldWait(directTarget))
                return;

            double distance = mc.player.distanceTo(directTarget);
            if (distance > MAX_HIT_RANGE)
                return;

            float cooldown = mc.player.getAttackCooldownProgress(0.0f);

            boolean canCrit = !mc.player.isOnGround() && mc.player.fallDistance > 0.0F
                    && !mc.player.isTouchingWater() && !mc.player.isInLava()
                    && !mc.player.isClimbing() && !mc.player.hasVehicle();

            boolean isStrictlyFalling = !mc.player.isOnGround() && mc.player.getVelocity().y < -0.05;
            boolean isRising = !mc.player.isOnGround() && mc.player.getVelocity().y > 0;

            if (smartCrits.getValue()) {
                if (canCrit) {
                    if (!isStrictlyFalling)
                        return;
                    if (cooldown < targetCooldown)
                        return;
                } else if (isRising) {
                    return;
                } else {
                    if (cooldown < targetCooldown)
                        return;
                }
            } else if (onlyCrits.getValue()) {
                if (!canCrit || !isStrictlyFalling)
                    return;
                if (cooldown < targetCooldown)
                    return;
            } else {
                if (cooldown < targetCooldown)
                    return;
            }

            if (checkShield.getValue() && directTarget instanceof PlayerEntity player && player.isBlocking())
                return;
            if (requireSprint.getValue() && !mc.player.isSprinting())
                return;

            performAttack(directTarget);

            if (!directTarget.isAlive() && focusMode.getValue()) {
                focusedTarget = null;
                return;
            }

            if (focusMode.getValue())
                focusedTarget = directTarget;
        } else {
            handleMissChance();
        }
    }

    private void handleMissChance() {
        double chance = missChance.getValue().doubleValue();
        if (chance <= 0)
            return;
        if (consecutiveHits < MIN_HITS_BEFORE_MISS)
            return;
        if (consecutiveMisses >= MAX_CONSECUTIVE_MISSES)
            return;

        float cooldown = mc.player.getAttackCooldownProgress(0.0f);
        if (cooldown < targetCooldown)
            return;

        Entity bestEntity = getEntityNearCrosshair();
        if (bestEntity == null)
            return;
        if (bestEntity != lastTarget)
            return;
        if (bestEntity.distanceTo(mc.player) > NEAR_MISS_RANGE)
            return;

        Vec3d lookVec = mc.player.getRotationVec(1.0F).normalize();
        Vec3d toEntityVec = bestEntity.getEyePos().subtract(mc.player.getEyePos()).normalize();
        double dot = lookVec.dotProduct(toEntityVec);
        double angle = Math.toDegrees(Math.acos(Math.min(1.0, Math.max(-1.0, dot))));

        if (angle > MISS_ANGLE_THRESHOLD)
            return;

        if (random.nextDouble() * 100 < chance) {
            mc.player.swingHand(Hand.MAIN_HAND);
            mc.player.resetLastAttackedTicks();
            consecutiveMisses++;
            updateTargetCooldown();
        }
    }

    private Entity getEntityNearCrosshair() {
        if (mc.world == null || mc.player == null)
            return null;

        Entity bestEntity = null;
        double smallestAngle = Double.MAX_VALUE;

        Vec3d lookVec = mc.player.getRotationVec(1.0F).normalize();
        Vec3d eyePos = mc.player.getEyePos();

        for (Entity entity : mc.world.getEntities()) {
            if (!isValidTarget(entity))
                continue;
            if (entity == mc.player)
                continue;
            if (mc.player.distanceTo(entity) > NEAR_MISS_RANGE)
                continue;

            Vec3d toEntityVec = entity.getEyePos().subtract(eyePos).normalize();
            double dot = lookVec.dotProduct(toEntityVec);
            double angle = Math.toDegrees(Math.acos(Math.min(1.0, Math.max(-1.0, dot))));

            if (angle < smallestAngle) {
                smallestAngle = angle;
                bestEntity = entity;
            }
        }
        return bestEntity;
    }

    private boolean isValidTarget(Entity entity) {
        if (!(entity instanceof LivingEntity living))
            return false;
        if (entity == mc.player)
            return false;
        if (!entity.isAlive())
            return false;
        if (living.getHealth() <= 0)
            return false;
        String mode = targetMode.getMode();
        if (mode.equals("All"))
            return true;
        if (mode.equals("Players"))
            return entity instanceof PlayerEntity;
        if (mode.equals("Mobs"))
            return entity instanceof MobEntity || !(entity instanceof PlayerEntity);
        return false;
    }

    private void updateTargetCooldown() {
        float min = (float) cooldownRange.getMin() / 100.0f;
        float max = (float) cooldownRange.getMax() / 100.0f;

        int minPercent = (int) (min * 100);
        int maxPercent = (int) (max * 100);
        int range = maxPercent - minPercent + 1;
        if (range <= 0)
            range = 1;

        targetCooldown = (minPercent + random.nextInt(range)) / 100.0f;
    }

    private void performAttack(Entity target) {
        if (mc.player.getAttackCooldownProgress(0.5f) < targetCooldown)
            return;

        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
        mc.player.resetLastAttackedTicks();
        consecutiveHits++;
        consecutiveMisses = 0;
    }

    private boolean isHoldingWeapon() {
        if (mc.player.getMainHandStack().isEmpty())
            return false;
        String name = mc.player.getMainHandStack().getItem().getTranslationKey().toLowerCase();
        return name.contains("sword") || name.contains("axe") || name.contains("trident") || name.contains("mace");
    }

    private Entity getCrosshairTarget() {
        if (!(mc.crosshairTarget instanceof EntityHitResult hit))
            return null;
        if (hit.getType() != HitResult.Type.ENTITY)
            return null;
        Entity entity = hit.getEntity();
        if (entity == mc.player || !entity.isAlive() || !(entity instanceof LivingEntity))
            return null;
        return entity;
    }
}