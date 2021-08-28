package org.fon.master.bsavic.api.model;

import com.google.common.base.Strings;
import org.fon.master.bsavic.api.model.google.AppRequestInput;

import java.util.*;

public class Resolver {

    private Object input;
    private String outputSpeech;
    private boolean endOfConversation = false;
    private Set<Slot> slots = new HashSet<>();
    private Map<String, Object> sessionStorage = new HashMap<>();
    private IntentDeniedResponse intentDeniedResponse;
    private ElicitSlot slotToElicit;

    public Resolver(Object input) {
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

    public Object getInput() {
        return input;
    }

    public Map<String, Object> getSessionStorage() {
        return sessionStorage;
    }

    public IntentDeniedResponse getIntentDeniedResponse() {
        return intentDeniedResponse;
    }

    public void setIntentDeniedResponse(IntentDeniedResponse intentDeniedResponse) {
        this.intentDeniedResponse = intentDeniedResponse;
    }

    public ElicitSlot getSlotToElicit() {
        return slotToElicit;
    }

    public void setSlotToElicit(String slot) {
        if (!Strings.isNullOrEmpty(slot)) {
            slotToElicit = new ElicitSlot(slot, extractIntentHandlerFromInput());
        }
    }

    private String extractIntentHandlerFromInput() {
        if (input instanceof AppRequestInput) {
            AppRequestInput in = (AppRequestInput) input;
            return in.getHandler().getName();
        } else {
            return null;
        }
    }
}
