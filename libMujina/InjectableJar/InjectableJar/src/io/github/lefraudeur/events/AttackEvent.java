package io.github.lefraudeur.events;

import io.github.lefraudeur.Main;
import io.github.lefraudeur.modules.Module;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public class AttackEvent extends Event 
{
    private final PlayerEntity attacker;
    private final Entity target;
    public AttackEvent(PlayerEntity attacker, Entity target)
    {
        super();
        this.attacker = attacker;
        this.target = target;
    }

    @Override
    public void dispatch()
    {
        for (Module module : Main.modules)
            if (module.isEnabled())
                module.onAttackEvent(this);
    }

    public PlayerEntity getAttacker()
    {
        return attacker;
    }

    public Entity getTarget()
    {
        return target;
    }
}
