package org.fon.master.bsavic.api.handler.ask;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;

import java.util.Optional;
import java.util.function.Predicate;

public class NavigateHomeIntentHandler implements RequestHandler {
    @Override
    public boolean canHandle(HandlerInput input) {
        return input.matches(Predicate.isEqual("AMAZON.NavigateHomeIntent"));
    }

    @Override
    public Optional<Response> handle(HandlerInput input) {
        String speechText = "Ok, how can I help you?";
        return input.getResponseBuilder()
                .withSpeech(speechText)
                .withSimpleCard("Welcome", speechText)
                .withShouldEndSession(false)
                .build();
    }
}
