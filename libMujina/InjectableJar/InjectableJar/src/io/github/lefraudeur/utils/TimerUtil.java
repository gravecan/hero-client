package io.github.lefraudeur.utils;

public class TimerUtil {
    private long lastMS;

    public TimerUtil() {
        reset();
    }

    public long getCurrentMS() {
        return System.currentTimeMillis();
    }

    public boolean hasReached(double milliseconds) {
        return (getCurrentMS() - lastMS) >= milliseconds;
    }

    public void reset() {
        lastMS = getCurrentMS();
    }

    public boolean delay(double milliseconds) {
        if (hasReached(milliseconds)) {
            reset();
            return true;
        }
        return false;
    }
}
