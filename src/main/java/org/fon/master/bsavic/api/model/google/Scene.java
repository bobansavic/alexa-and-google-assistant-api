package org.fon.master.bsavic.api.model.google;

import java.util.Map;

public class Scene {
    private String name;
    private SlotFillingStatus slotFillingStatus;
    private Map<String, Slot> slots;
    private NextScene next;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SlotFillingStatus getSlotFillingStatus() {
        return slotFillingStatus;
    }

    public void setSlotFillingStatus(SlotFillingStatus slotFillingStatus) {
        this.slotFillingStatus = slotFillingStatus;
    }

    public Map<String, Slot> getSlots() {
        return slots;
    }

    public void setSlots(Map<String, Slot> slots) {
        this.slots = slots;
    }

    public NextScene getNext() {
        return next;
    }

    public void setNext(NextScene next) {
        this.next = next;
    }
}
