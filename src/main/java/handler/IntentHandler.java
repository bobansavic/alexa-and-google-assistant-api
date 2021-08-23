package handler;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.Slot;
import com.amazon.ask.request.Predicates;
import com.google.actions.api.ActionRequest;
import com.google.actions.api.ActionResponse;
import com.google.actions.api.DialogflowApp;
import com.google.actions.api.ForIntent;
import com.google.actions.api.response.ResponseBuilder;
import com.google.common.base.Strings;
import model.ForIntentCustom;
import model.Resolver;
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
        String speechText = resolver.getOutputSpeech();
        return input.getResponseBuilder()
                .withSpeech(speechText)
                .withShouldEndSession(resolver.isEndOfConversation())
//                .withSimpleCard("HelloWorld", speechText)
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

    public abstract ActionResponse doSomething(ActionRequest input);

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
        if (resolver.getInput() != null) {
            return respond(resolver.getInput());
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

    public void registerSlots(Resolver resolver, Slot... slots) {
        if (resolver.getInput() != null) {
            // Get slots Google style
            for (Slot s : slots) {
                Object parameterValue = resolver.getInput().getParameter(s.getName());
                if (parameterValue != null && !Strings.isNullOrEmpty(parameterValue.toString())) {
                    resolver.registerSlot(s.getName(), parameterValue.toString());
                }
            }
        } else {
            // Get slots Alexa style
            IntentRequest intentRequest = (IntentRequest) handlerInput.getRequestEnvelope().getRequest();
            Map<String, com.amazon.ask.model.Slot> askSlots = intentRequest.getIntent().getSlots();
            for (Slot s : slots) {
                com.amazon.ask.model.Slot askSlot = askSlots.get(s.getName());
                if (askSlot != null && !Strings.isNullOrEmpty(askSlot.getValue())) {
                    String value = askSlots.get(s.getName()).getValue();
                    resolver.registerSlot(s.getName(), value);
                }
            }
        }
    }

    void getSessionStorage(Resolver resolver) {
        if (resolver.getInput() != null) {
            Map<String, Object> userStorage = resolver.getInput().getUserStorage();
            for (Map.Entry<String, Object> entry : userStorage.entrySet()) {
                resolver.getSessionStorage().put(entry.getKey(), entry.getValue());
            }
        } else {
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
