package org.fon.master.bsavic.api.model.google;

public class IntentParameterValue {
    private String original;
    private String resolved;
    private Object value;

    public IntentParameterValue() {
    }

    public IntentParameterValue(String original, String resolved, Object value) {
        this.original = original;
        this.resolved = resolved;
        this.value = value;
    }

    public IntentParameterValue(String value) {
        this.original = value;
        this.resolved = value;
        this.value = value;
    }

    public String getOriginal() {
        return original;
    }

    public void setOriginal(String original) {
        this.original = original;
    }

    public String getResolved() {
        return resolved;
    }

    public void setResolved(String resolved) {
        this.resolved = resolved;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
