package org.fon.master.bsavic.api.handler.ask;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import com.amazon.ask.request.Predicates;

import java.util.Optional;

public class CancelAndStopIntentHandler implements RequestHandler {
    @Override
    public boolean canHandle(HandlerInput input) {
        return input.matches(Predicates.intentName("AMAZON.CancelIntent"))
                || input.matches(Predicates.intentName("AMAZON.StopIntent"));
    }

    @Override
    public Optional<Response> handle(HandlerInput input) {
        String speechText = "Ok, goodbye.";
        return input.getResponseBuilder()
                .withSpeech(speechText)
                .withSimpleCard("Stop", speechText)
                .withShouldEndSession(true)
                .build();
    }
}
