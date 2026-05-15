package io.github.lefraudeur.events;

public abstract class Event 
{
    private boolean cancelled = false;

    public abstract void dispatch();

    public boolean isCancelled()
    {
        return cancelled;
    }

    public void setCancelled(boolean cancelled)
    {
        this.cancelled = cancelled;
    }

    public void cancel() { this.setCancelled(true); }
}
