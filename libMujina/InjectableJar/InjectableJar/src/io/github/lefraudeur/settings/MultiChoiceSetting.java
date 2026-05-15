package io.github.lefraudeur.settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MultiChoiceSetting extends Setting<List<String>> {
    private final List<String> options;

    public MultiChoiceSetting(String name, List<String> defaultValues, String... options) {
        super(name, new ArrayList<>(defaultValues));
        this.options = Arrays.asList(options);
    }

    @Override
    public String getType() {
        return "multichoice";
    }

    public List<String> getOptions() {
        return options;
    }

    public boolean isSelected(String option) {
        return value.contains(option);
    }

    public void toggle(String option) {
        if (value.contains(option)) {
            value.remove(option);
        } else if (options.contains(option)) {
            value.add(option);
        }
    }
}
