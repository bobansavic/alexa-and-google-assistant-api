package org.fon.master.bsavic.api.model;

public class ElicitSlot {
    private String slot;
    private String intent;

    public ElicitSlot() {
    }

    public ElicitSlot(String slot, String intent) {
        this.slot = slot;
        this.intent = intent;
    }

    public String getSlot() {
        return slot;
    }

    public String getSlotWithPrefix() {
        return ApiConstants.ELICIT_SLOT_PREFIX + slot;
    }

    public void setSlot(String slot) {
        this.slot = slot;
    }

    public String getIntent() {
        return intent;
    }

    public void setIntent(String intent) {
        this.intent = intent;
    }
}
