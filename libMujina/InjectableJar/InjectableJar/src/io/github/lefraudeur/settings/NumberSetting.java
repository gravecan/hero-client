package io.github.lefraudeur.settings;

public class NumberSetting extends Setting<Double> {
    private double min;
    private double max;
    private double increment;
    private boolean isFloat;

    public NumberSetting(String name, double value, double min, double max, double increment) {
        super(name, value);
        this.min = min;
        this.max = max;
        this.increment = increment;
        this.isFloat = increment % 1 != 0;
    }

    @Override
    public String getType() {
        return isFloat ? "float" : "int";
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public double getIncrement() {
        return increment;
    }
}
