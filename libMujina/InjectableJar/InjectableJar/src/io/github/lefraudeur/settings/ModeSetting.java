package io.github.lefraudeur.settings;

import java.util.Arrays;
import java.util.List;

public class ModeSetting extends Setting<Integer> {
    private List<String> modes;

    public ModeSetting(String name, String defaultMode, String... modes) {
        super(name, 0);
        this.modes = Arrays.asList(modes);
        this.value = this.modes.indexOf(defaultMode);
    }

    @Override
    public String getType() {
        return "mode";
    }

    public List<String> getModes() {
        return modes;
    }

    public String getMode() {
        return modes.get(value);
    }

    public void setMode(String mode) {
        if (modes.contains(mode)) {
            this.value = modes.indexOf(mode);
        }
    }

    public void cycle() {
        this.value = (this.value + 1) % modes.size();
    }
}
