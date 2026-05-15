package io.github.lefraudeur.modules;

import java.util.ArrayList;
import java.util.List;

public enum Category {

    COMBAT("Combat"),
    MOVEMENT("Movement"),
    PLAYER("Player"),
    RENDER("Render"),
    VISUAL("Visual"),
    CRYSTAL("Crystal"),
    MISC("Misc"),
    EXPLOIT("Exploit"),
    MACROS("Macros");

    private final String name;
    private final List<Module> modules = new ArrayList<>();

    Category(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void addModule(Module module) {
        modules.add(module);
    }

    public List<Module> getModules() {
        return modules;
    }

    @Override
    public String toString() {
        return name;
    }
}
