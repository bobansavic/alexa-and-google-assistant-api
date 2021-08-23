package model;

import com.google.actions.api.ActionRequest;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Resolver {

    private ActionRequest input;
    private String outputSpeech;
    private boolean endOfConversation = false;
    Set<Slot> slots = new HashSet<>();
    Map<String, Object> sessionStorage = new HashMap<>();

    public Resolver(ActionRequest input) {
        this.input = input;
    }

    public String getOutputSpeech() {
        return outputSpeech;
    }

    public void setOutputSpeech(String outputSpeech) {
        this.outputSpeech = outputSpeech;
    }

    public boolean isEndOfConversation() {
        return endOfConversation;
    }

    public void setEndOfConversation(boolean endOfConversation) {
        this.endOfConversation = endOfConversation;
    }

    public void registerSlot(String slotName, String value) {
        slots.add(new Slot(slotName, value));
    }

    public Set<Slot> getSlots() {
        return slots;
    }

    public Slot getSlot(String slotName) {
        if (!slots.isEmpty()) {
            for (Slot slot : slots) {
                if (slot.getName().equals(slotName)) {
                    return slot;
                }
            }
        }
        return null;
    }

    public Integer getSlotAsInteger(String slotName) {
        if (!slots.isEmpty()) {
            for (Slot s : slots) {
                if (s.getName().equals(slotName)) {
                    double d = Double.parseDouble(s.getValue());
                    return (int) d;
                }
            }
        }
        return null;
    }

    public ActionRequest getInput() {
        return input;
    }

    public Map<String, Object> getSessionStorage() {
        return sessionStorage;
    }
}
