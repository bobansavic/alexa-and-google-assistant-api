package org.fon.master.bsavic.api.handler;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
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
import org.fon.master.bsavic.api.model.Slot;
import org.fon.master.bsavic.api.model.google.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public abstract class IntentHandler extends DialogflowApp implements RequestHandler {
    private static final Logger log = LoggerFactory.getLogger(IntentHandler.class);
    private static final String ANNOTATIONS = "declaredAnnotations";

    private String intentName;
    private String intentNameContext;
    private Class<?> intentType;
    private ApiRequestResolver resolver;
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
        process(null);

        if (!resolver.getSessionStorage().isEmpty()) {
            for (Map.Entry<String, Object> entry : resolver.getSessionStorage().entrySet()) {
                input.getAttributesManager().getSessionAttributes().put(entry.getKey(), entry.getValue());
            }
        }

        IntentRequest intentRequest = (IntentRequest) handlerInput.getRequestEnvelope().getRequest();
        com.amazon.ask.model.Intent updatedIntent = intentRequest.getIntent();
        IntentDeniedResponse intentDeniedResponse = resolver.getIntentDeniedResponse();
        if (intentDeniedResponse != null
                && intentRequest.getIntent().getConfirmationStatus() == IntentConfirmationStatus.DENIED) {
            return input.getResponseBuilder()
                    .withSpeech(intentDeniedResponse.getOutputSpeech())
                    .withShouldEndSession(intentDeniedResponse.isEndConversation())
                    .build();
        }
        String speechText = resolver.getOutputSpeech();
        com.amazon.ask.response.ResponseBuilder responseBuilder = input.getResponseBuilder()
                .withShouldEndSession(resolver.isEndOfConversation())
                .withSpeech(speechText);

        ElicitSlot slotToElicit = resolver.getSlotToElicit();
        if (slotToElicit != null) {
            log.info("Adding ElicitSlotDirective for slot [{}] with updated intent [{}].", slotToElicit.getSlot(), updatedIntent.getName());
            responseBuilder.addElicitSlotDirective(slotToElicit.getSlot(), updatedIntent);
        }

        return responseBuilder
//                .withSimpleCard("", speechText)
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

    public String invoke(ActionsBuilderRequestInput input, Map<String, String> headers) throws JsonProcessingException {
        process(input);
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
        }

        for (Map.Entry<String, Object> entry : resolver.getSessionStorage().entrySet()) {
            requestOutput.getSession().getParams().put(entry.getKey(), entry.getValue());
        }

        ElicitSlot slotToElicit = resolver.getSlotToElicit();
        if (slotToElicit != null) {
            requestOutput.getSession().getParams().put(slotToElicit.getSlotWithPrefix(), slotToElicit.getIntent());
        }

        if (!Strings.isNullOrEmpty(resolver.getNextScene())) {
            requestOutput.getScene().setNext(new NextScene(resolver.getNextScene()));
        }

        return new ObjectMapper().writeValueAsString(requestOutput);
    }

    public abstract ActionResponse process(Object input);

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

    @SuppressWarnings("unchecked")
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

    public void setResolver(ApiRequestResolver resolver) {
        this.resolver = resolver;
    }

    public ActionResponse processResponse(ApiRequestResolver resolver) {
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

    public Slot getSlotForInput(Object input, String slotName) {
        if (input != null && resolver.getInput() instanceof ActionRequest) {
            ActionRequest actionRequestInput = (ActionRequest) resolver.getInput();
            // Get slots Google DialogFlow style
            Object parameterValue = actionRequestInput.getParameter(slotName);
            if (parameterValue != null && !Strings.isNullOrEmpty(parameterValue.toString())) {
                return new Slot(slotName, parameterValue.toString());
            } else {
                return null;
            }
        } else if (input != null && resolver.getInput() instanceof ActionsBuilderRequestInput) {
            ActionsBuilderRequestInput actionsBuilderInput = (ActionsBuilderRequestInput) resolver.getInput();
            Map<String, IntentParameterValue> inputSlots = actionsBuilderInput.getIntent().getParams();
            Map<String, org.fon.master.bsavic.api.model.google.Slot> sceneSlots = actionsBuilderInput.getScene().getSlots();
            // Get slots Google Actions Builder style
            IntentParameterValue slotValue;
            // First try to fill the slot via user inputs for elicited slots
            Object resolvedSessionSlotValue = actionsBuilderInput.getSession().getParams()
                    .get(ApiConstants.ELICIT_SLOT_RESOLVED_PREFIX + slotName);
            if (resolvedSessionSlotValue == null) {
                resolvedSessionSlotValue = actionsBuilderInput.getSession().getParams()
                        .get(ApiConstants.ELICIT_SLOT_RESOLVED_PREFIX + slotName.toLowerCase());
                if (resolvedSessionSlotValue != null) {
                    slotValue = new IntentParameterValue(resolvedSessionSlotValue.toString());
                } else {
                    // Try to fill the slot via intent parameters/slots
                    slotValue = inputSlots.get(slotName);
                    if (slotValue == null) {
                        slotValue = inputSlots.get(slotName.toLowerCase());
                    }
                }
            } else {
                slotValue = new IntentParameterValue(resolvedSessionSlotValue.toString());
            }

            if (slotValue != null && !Strings.isNullOrEmpty(slotValue.getResolved())) {
                return new Slot(slotName, slotValue.getResolved());
            } else {
                return null;
            }
        } else if (handlerInput != null) {
            // Get slots Alexa style
            IntentRequest intentRequest = (IntentRequest) handlerInput.getRequestEnvelope().getRequest();
            Map<String, com.amazon.ask.model.Slot> askSlots = intentRequest.getIntent().getSlots();
            com.amazon.ask.model.Slot askSlot = askSlots.get(slotName);
            if (askSlot != null && !Strings.isNullOrEmpty(askSlot.getValue())) {
                String value = askSlots.get(slotName).getValue();
                return new Slot(slotName, value);
            } else {
                return null;
            }
        }
        return null;
    }

    public void resolveSessionStorage(ApiRequestResolver resolver) {
        if (resolver.getInput() != null && resolver.getInput() instanceof ActionRequest) {
            ActionRequest input = (ActionRequest) resolver.getInput();
            Map<String, Object> userStorage = input.getUserStorage();
            for (Map.Entry<String, Object> entry : userStorage.entrySet()) {
                resolver.getSessionStorage().put(entry.getKey(), entry.getValue());
            }
        } else if (resolver.getInput() != null && resolver.getInput() instanceof ActionsBuilderRequestInput) {
            ActionsBuilderRequestInput input = (ActionsBuilderRequestInput) resolver.getInput();
            Map<String, Object> sessionParams = input.getSession().getParams();
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

    public class ApiRequestResolver {
        private Object input;
        private String outputSpeech;
        private boolean endOfConversation = false;
        private Map<String, Object> sessionStorage = new HashMap<>();
        private IntentDeniedResponse intentDeniedResponse;
        private ElicitSlot slotToElicit;
        private String nextScene;

        public ApiRequestResolver(Object input) {
            resolver = this;
            this.input = input;
            resolveSessionStorage(this);
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

        public Slot getSlot(String slotName) {
            return getSlotForInput(this.input, slotName);
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

        public String getNextScene() {
            return nextScene;
        }

        public void setNextScene(String nextScene) {
            this.nextScene = nextScene;
        }

        private String extractIntentHandlerFromInput() {
            if (input instanceof ActionsBuilderRequestInput) {
                ActionsBuilderRequestInput in = (ActionsBuilderRequestInput) input;
                return in.getHandler().getName();
            } else {
                return null;
            }
        }
    }
}
