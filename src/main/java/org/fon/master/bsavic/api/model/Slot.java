package org.fon.master.bsavic.api.model;

public class Slot {
    private String name;
    private String value;

    public Slot(String name) {
        this.name = name;
    }

    public Slot(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public Integer getValueAsInteger() {
        double d = Double.parseDouble(value);
        return (int) d;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
