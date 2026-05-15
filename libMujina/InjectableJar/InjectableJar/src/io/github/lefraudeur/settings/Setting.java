package io.github.lefraudeur.settings;

public abstract class Setting<T> {
    protected String name;
    protected T value;
    protected boolean hidden = false;

    public Setting(String name, T value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public boolean isHidden() {
        return hidden;
    }

    public abstract String getType(); 
}
