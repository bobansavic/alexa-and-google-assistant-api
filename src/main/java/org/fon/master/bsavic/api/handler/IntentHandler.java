package org.fon.master.bsavic.api.handler;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Intent;
import com.amazon.ask.model.IntentConfirmationStatus;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Response;
import com.amazon.ask.request.Predicates;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.actions.api.*;
import com.google.actions.api.response.ResponseBuilder;
import com.google.common.base.Strings;
import org.fon.master.bsavic.api.model.*;
import org.fon.master.bsavic.api.model.google.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;

public abstract class IntentHandler extends DialogflowApp implements RequestHandler {
    private static final Logger log = LoggerFactory.getLogger(IntentHandler.class);
    private static final String ANNOTATIONS = "declaredAnnotations";

    private String intentName;
    private String intentNameContext;
    private Class<?> intentType;
    private Resolver resolver;
    private HandlerInput handlerInput;

    public IntentHandler(String intentName) {
        this.intentName = intentName;
        this.intentNameContext = intentName;
        this.intentType = resolveIntentType();
        log.info("Intent type set as [{}]", intentType.getName());
        updateRouting();
    }

    @Override
    public boolean canHandle(HandlerInput input) {
        return input.matches(Predicates.intentName(intentName));
    }



    @Override
    public Optional<Response> handle(HandlerInput input) {
        this.handlerInput = input;
        doSomething(null);

        if (!resolver.getSessionStorage().isEmpty()) {
            for (Map.Entry<String, Object> entry : resolver.getSessionStorage().entrySet()) {
                input.getAttributesManager().getSessionAttributes().put(entry.getKey(), entry.getValue());
            }
        }

        IntentRequest intentRequest = (IntentRequest) handlerInput.getRequestEnvelope().getRequest();
        IntentDeniedResponse intentDeniedResponse = resolver.getIntentDeniedResponse();
        if (intentRequest.getIntent().getConfirmationStatus() == IntentConfirmationStatus.DENIED
                && intentDeniedResponse != null) {
            return input.getResponseBuilder()
                    .withSpeech(intentDeniedResponse.getOutputSpeech())
                    .withShouldEndSession(intentDeniedResponse.isEndConversation())
                    .build();
        }
        String speechText = resolver.getOutputSpeech();
        com.amazon.ask.response.ResponseBuilder responseBuilder = input.getResponseBuilder()
                .withSpeech(speechText)
                .withShouldEndSession(resolver.isEndOfConversation());

        ElicitSlot slotToElicit = resolver.getSlotToElicit();
        if (slotToElicit != null) {
            responseBuilder.addElicitSlotDirective(slotToElicit.getSlot(), intentRequest.getIntent());
        }

        return responseBuilder
                .withSimpleCard("", speechText)
                .build();
    }

    public ActionResponse respond(ActionRequest request) {
        log.info("Executing intent \"{}\"...", request.getIntent());
        ResponseBuilder responseBuilderG = getResponseBuilder(request).add(resolver.getOutputSpeech());
        if (resolver.isEndOfConversation()) {
            responseBuilderG.endConversation();
        }
        if (!resolver.getSessionStorage().isEmpty()) {
            for (Map.Entry<String, Object> entry : resolver.getSessionStorage().entrySet()) {
                responseBuilderG.getUserStorage().put(entry.getKey(), entry.getValue());
            }
        }
        ActionResponse actionResponse = responseBuilderG.build();
        return actionResponse;
    }

    public String invoke(AppRequestInput input, Map<String, String> headers) throws JsonProcessingException {
        doSomething(input);
        String speech = resolver.getOutputSpeech();

        AppRequestOutput requestOutput = new AppRequestOutput();
        requestOutput.setSession(input.getSession());
        requestOutput.setUser(input.getUser());
        requestOutput.setDevice(input.getDevice());
        requestOutput.setScene(input.getScene());
        requestOutput.setHome(input.getHome());

        Prompt prompt = new Prompt();
        Simple firstSimple = new Simple();
        firstSimple.setSpeech(speech);
        firstSimple.setText(speech);
        prompt.setFirstSimple(firstSimple);
        requestOutput.setPrompt(prompt);

        if (resolver.isEndOfConversation()) {
            requestOutput.getScene().setNext(new NextScene("actions.scene.END_CONVERSATION"));
            requestOutput.getUser().getParams().clear();
        }

        for (Map.Entry<String, Object> entry : resolver.getSessionStorage().entrySet()) {
            requestOutput.getUser().getParams().put(entry.getKey(), entry.getValue());
        }

        ElicitSlot slotToElicit = resolver.getSlotToElicit();
        if (slotToElicit != null) {
            requestOutput.getSession().getParams().put(slotToElicit.getSlotWithPrefix(), slotToElicit.getIntent());
        }

        return new ObjectMapper().writeValueAsString(requestOutput);
    }

    public abstract ActionResponse doSomething(Object input);

    void updateRouting() {
        try {
            Method[] methods = intentType.getDeclaredMethods();
            for (Method m : methods) {
                if (m.isAnnotationPresent(ForIntent.class)
                        && m.getName().equals("doSomething")
                        && !m.getDeclaredAnnotation(ForIntent.class).value().equals(intentName)) {
                    log.info("Updating Google Dialogflow routing for intent \"{}\"", intentName);
                    m.setAccessible(true);
                    Class<?> superclass = m.getClass().getSuperclass();
                    Field declaredField = superclass.getDeclaredField(ANNOTATIONS);
                    declaredField.setAccessible(true);
                    Map<Class<? extends Annotation>, Annotation> map = (Map<Class<? extends Annotation>, Annotation>) declaredField.get(m);
                    ForIntentCustom custom = new ForIntentCustom(intentName);
                    map.put(ForIntent.class, custom);
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void updateRouting(Class<? extends IntentHandler> type, String value) {
        try {
            Method[] methods = type.getDeclaredMethods();
            for (Method m : methods) {
                if (m.isAnnotationPresent(ForIntent.class)) {
                    log.info("Updating Google Dialogflow routing for followup intent \"{}\"", value);
                    ForIntent forIntent = m.getDeclaredAnnotation(ForIntent.class);
                    System.out.println("ForIntent initial value = " + forIntent.value());
                    m.setAccessible(true);
                    Class<?> superclass = m.getClass().getSuperclass();
                    Field declaredField = superclass.getDeclaredField(ANNOTATIONS);
                    declaredField.setAccessible(true);
                    Map<Class<? extends Annotation>, Annotation> map = (Map<Class<? extends Annotation>, Annotation>) declaredField.get(m);
                    ForIntentCustom custom = new ForIntentCustom(value);
                    map.put(ForIntent.class, custom);
                    intentNameContext = value;
                    log.info("Updated intent name context to \"{}\"", intentNameContext);
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public String getIntentName() {
        return intentName;
    }

    public void setResolver(Resolver resolver) {
        this.resolver = resolver;
    }

    public ActionResponse processResponse(Resolver resolver) {
        this.resolver = resolver;
        if (resolver.getInput() != null && resolver.getInput() instanceof ActionRequest) {
            return respond((ActionRequest) resolver.getInput());
        }
        return null;
    }

    public Logger getLogger() {
        return log;
    }

    public String getIntentNameContext() {
        return intentNameContext;
    }

    public void setIntentNameContext(String intentNameContext) {
        this.intentNameContext = intentNameContext;
    }

    public void registerSlots(Resolver resolver, org.fon.master.bsavic.api.model.Slot... slots) {
        if (resolver.getInput() != null && resolver.getInput() instanceof ActionRequest) {
            ActionRequest input = (ActionRequest) resolver.getInput();
            // Get slots Google style
            for (org.fon.master.bsavic.api.model.Slot s : slots) {
                Object parameterValue = input.getParameter(s.getName());
                if (parameterValue != null && !Strings.isNullOrEmpty(parameterValue.toString())) {
                    resolver.registerSlot(s.getName(), parameterValue.toString());
                }
            }
        } else if (resolver.getInput() != null && resolver.getInput() instanceof AppRequestInput) {
            AppRequestInput input = (AppRequestInput) resolver.getInput();
            Map<String, IntentParameterValue> inputSlots = input.getIntent().getParams();
            // Get slots Google Actions Builder style
            for (org.fon.master.bsavic.api.model.Slot s : slots) {
                IntentParameterValue slotValue = inputSlots.get(s.getName());
                if (slotValue == null) {
                    slotValue = inputSlots.get(s.getName().toLowerCase());
                }
                if (slotValue == null) {
                    Object resolvedSessionSlotValue = input.getSession().getParams()
                            .get(ApiConstants.ELICIT_SLOT_RESOLVED_PREFIX + s.getName());
                    if (resolvedSessionSlotValue == null) {
                        resolvedSessionSlotValue = input.getSession().getParams()
                                .get(ApiConstants.ELICIT_SLOT_RESOLVED_PREFIX + s.getName().toLowerCase());
                        if (resolvedSessionSlotValue != null) {
                            slotValue = new IntentParameterValue(resolvedSessionSlotValue.toString());
                        }
                    } else {
                        slotValue = new IntentParameterValue(resolvedSessionSlotValue.toString());
                    }
                }
                if (slotValue != null && !Strings.isNullOrEmpty(slotValue.getResolved())) {
                    resolver.registerSlot(s.getName(), slotValue.getResolved());
                }
            }
        } else if (handlerInput != null) {
            // Get slots Alexa style
            IntentRequest intentRequest = (IntentRequest) handlerInput.getRequestEnvelope().getRequest();
            Map<String, com.amazon.ask.model.Slot> askSlots = intentRequest.getIntent().getSlots();
            for (org.fon.master.bsavic.api.model.Slot s : slots) {
                com.amazon.ask.model.Slot askSlot = askSlots.get(s.getName());
                if (askSlot != null && !Strings.isNullOrEmpty(askSlot.getValue())) {
                    String value = askSlots.get(s.getName()).getValue();
                    resolver.registerSlot(s.getName(), value);
                }
            }
        }
    }

    public void getSessionStorage(Resolver resolver) {
        if (resolver.getInput() != null && resolver.getInput() instanceof ActionRequest) {
            ActionRequest input = (ActionRequest) resolver.getInput();
            Map<String, Object> userStorage = input.getUserStorage();
            for (Map.Entry<String, Object> entry : userStorage.entrySet()) {
                resolver.getSessionStorage().put(entry.getKey(), entry.getValue());
            }
        } else if (resolver.getInput() != null && resolver.getInput() instanceof AppRequestInput) {
            AppRequestInput input = (AppRequestInput) resolver.getInput();
            Map<String, Object> sessionParams = input.getUser().getParams();
            // Get slots Google Actions Builder style
            for (Map.Entry<String, Object> entry : sessionParams.entrySet()) {
                resolver.getSessionStorage().put(entry.getKey(), entry.getValue());
            }
        }else if (handlerInput != null) {
            Map<String, Object> sessionStorage = handlerInput.getAttributesManager().getSessionAttributes();
            if (sessionStorage != null) {
                for (Map.Entry<String, Object> entry : sessionStorage.entrySet()) {
                    resolver.getSessionStorage().put(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    private Class<?> resolveIntentType() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        for (int i = 0; i < stackTraceElements.length; i++) {
            StackTraceElement element = stackTraceElements[i];
            if (!element.getClassName().equals(IntentHandler.class.getName()) && element.getClassName().indexOf("java.lang.Thread") != 0) {
                try {
                    Class<?> clazz = Class.forName(element.getClassName());
                    if (IntentHandler.class.isAssignableFrom(clazz)) {
                        return clazz;
                    }
                } catch (ClassNotFoundException e) {
                    log.error("Class \"{}\" not found.", element.getClassName(), e);
                }
            }
        }
        return null;
    }
}
