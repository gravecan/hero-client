package io.github.lefraudeur.settings;

public class RangeSetting extends Setting<Double> {
    private double min;
    private double max;
    private double currentMin;
    private double increment;

    public RangeSetting(String name, double minVal, double maxVal, double minLimit, double maxLimit, double increment) {
        super(name, maxVal);
        this.currentMin = minVal;
        this.min = minLimit;
        this.max = maxLimit;
        this.increment = increment;
    }

    @Override
    public String getType() {
        return "range";
    }

    public double getMin() {
        return currentMin;
    }

    public double getMax() {
        return getValue();
    }

    public void setMin(double val) {
        this.currentMin = val;
    }

    public void setMax(double val) {
        setValue(val);
    }

    public double getLimitMin() {
        return min;
    }

    public double getLimitMax() {
        return max;
    }

    public double getIncrement() {
        return increment;
    }
}
