package org.fon.master.bsavic.api.model;

public class IntentDeniedResponse {
    private String outputSpeech;
    private boolean endConversation;

    public IntentDeniedResponse(String outputSpeech, boolean endConversation) {
        this.outputSpeech = outputSpeech;
        this.endConversation = endConversation;
    }

    public String getOutputSpeech() {
        return outputSpeech;
    }

    public boolean isEndConversation() {
        return endConversation;
    }
}
