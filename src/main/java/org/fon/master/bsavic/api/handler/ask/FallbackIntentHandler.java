package org.fon.master.bsavic.api.handler.ask;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import com.amazon.ask.request.Predicates;

import java.util.Optional;

public class FallbackIntentHandler implements RequestHandler {
    @Override
    public boolean canHandle(HandlerInput input) {
        return input.matches(Predicates.intentName("AMAZON.FallbackIntent"));
    }

    @Override
    public Optional<Response> handle(HandlerInput input) {
        String speechText = "Sorry, I don't know that. You can try saying help!";
        return input.getResponseBuilder()
                .withSpeech(speechText)
                .withSimpleCard("Help", speechText)
                .withReprompt(speechText)
                .build();
    }
}
