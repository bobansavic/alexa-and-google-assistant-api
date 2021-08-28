package org.fon.master.bsavic.api.model.google;

public class Slot {
    private SlotMode mode;
    private SlotStatus status;
    private Object value;
    private boolean updated;
    private Prompt prompt;

    public SlotMode getMode() {
        return mode;
    }

    public void setMode(SlotMode mode) {
        this.mode = mode;
    }

    public SlotStatus getStatus() {
        return status;
    }

    public void setStatus(SlotStatus status) {
        this.status = status;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public boolean isUpdated() {
        return updated;
    }

    public void setUpdated(boolean updated) {
        this.updated = updated;
    }

    public Prompt getPrompt() {
        return prompt;
    }

    public void setPrompt(Prompt prompt) {
        this.prompt = prompt;
    }
}
