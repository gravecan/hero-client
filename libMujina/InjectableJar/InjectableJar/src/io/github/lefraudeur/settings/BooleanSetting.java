package io.github.lefraudeur.settings;

public class BooleanSetting extends Setting<Boolean> {
    public BooleanSetting(String name, boolean value) {
        super(name, value);
    }

    @Override
    public String getType() {
        return "bool";
    }

    public void toggle() {
        this.value = !this.value;
    }
}
