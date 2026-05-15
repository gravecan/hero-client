package io.github.lefraudeur.settings;

public class KeybindSetting extends Setting<Integer> {
    public KeybindSetting(String name, Integer defaultValue) {
        super(name, defaultValue);
    }

    @Override
    public String getType() {
        return "keybind";
    }
}
