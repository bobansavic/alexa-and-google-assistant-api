package org.fon.master.bsavic.api.servlet;

import com.amazon.ask.Skill;
import com.amazon.ask.Skills;
import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.exception.AskSdkException;
import com.amazon.ask.model.LaunchRequest;
import com.amazon.ask.model.RequestEnvelope;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.ResponseEnvelope;
import com.amazon.ask.model.services.Serializer;
import com.amazon.ask.request.Predicates;
import com.amazon.ask.servlet.ServletConstants;
import com.amazon.ask.servlet.util.ServletUtils;
import com.amazon.ask.servlet.verifiers.*;
import com.amazon.ask.util.JacksonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import org.fon.master.bsavic.api.handler.ask.CancelAndStopIntentHandler;
import org.fon.master.bsavic.api.handler.ask.FallbackIntentHandler;
import org.fon.master.bsavic.api.handler.IntentHandler;
import org.fon.master.bsavic.api.handler.ask.NavigateHomeIntentHandler;
import org.fon.master.bsavic.api.model.ApiConstants;
import org.fon.master.bsavic.api.model.google.ActionsBuilderRequestInput;
import org.apache.commons.io.IOUtils;
import org.fon.master.bsavic.api.model.google.Intent;
import org.fon.master.bsavic.api.model.google.IntentParameterValue;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static com.amazon.ask.servlet.ServletConstants.DEFAULT_TOLERANCE_MILLIS;

public class SkillAndActionServlet extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(SkillAndActionServlet.class);

    private transient final Skill skill;
    private transient final List<SkillServletVerifier> verifiers;
    private transient final Serializer serializer = new JacksonSerializer();
    private String applicationName;

    private List<IntentHandler> handlers = new ArrayList<>();

    public SkillAndActionServlet(String applicationName, IntentHandler... handlers) {
        this.applicationName = applicationName;
        List<SkillServletVerifier> defaultVerifiers = new ArrayList<>();
        if (!ServletUtils.isRequestSignatureCheckSystemPropertyDisabled()) {
            defaultVerifiers.add(new SkillRequestSignatureVerifier());
        }
        Long timestampToleranceProperty = ServletUtils.getTimeStampToleranceSystemProperty();
        defaultVerifiers.add(new SkillRequestTimestampVerifier(timestampToleranceProperty != null
                ? timestampToleranceProperty : DEFAULT_TOLERANCE_MILLIS));
        log.info("Found {} intent handler(s) to register", handlers.length);
        if (handlers.length > 0) {
            for (IntentHandler handler : handlers) {
                log.info("{}", handler.getIntentName());
            }
        }
        this.skill = Skills.standard()
                // Add default handlers for built-in Alexa intents
                .addRequestHandlers(
                        new LaunchRequestHandler(),
                        new FallbackIntentHandler(),
                        new CancelAndStopIntentHandler(),
                        new NavigateHomeIntentHandler())
                // Add the rest of defined handlers
                .addRequestHandlers(handlers)
                .build();
        this.verifiers = defaultVerifiers;
        this.handlers.addAll(Arrays.asList(handlers));
    }

    /**
     * Handles a POST request. Based on the request parameters, invokes the right method on the
     *
     * @param request  the object that contains the request the client has made of the org.fon.master.bsavic.api.servlet
     * @param response object that contains the response the org.fon.master.bsavic.api.servlet sends to the client
     * @throws IOException if an input or output error is detected when the org.fon.master.bsavic.api.servlet handles the request
     */
    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
            throws IOException {
        byte[] serializedRequestEnvelope = IOUtils.toByteArray(request.getInputStream());
        String jsonString = IOUtils.toString(serializedRequestEnvelope, "UTF-8");
        JSONObject object = new JSONObject(jsonString);
        if (checkRequestOrigin(object) == 0) {
            log.info("Request came from Alexa");
            try {
            processAlexaRequest(serializedRequestEnvelope, request, response);
        } catch (SecurityException ex) {
            int statusCode = HttpServletResponse.SC_BAD_REQUEST;
            log.error("Incoming request failed verification {}", statusCode, ex);
            response.sendError(statusCode, ex.getMessage());
        } catch (AskSdkException ex) {
            int statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            log.error("Exception occurred in doPost, returning status code {}", statusCode, ex);
            response.sendError(statusCode, ex.getMessage());
            }
        } else if (checkRequestOrigin(object) == 1) {
            log.info("Request came from Google Assistant (Legacy)");
            // Do Google stuff
            try {
                processLegacyGoogleAssistantRequest(object, jsonString, request, response);
            } catch (InterruptedException | ExecutionException e) {
                log.error("Error building Google Assistant response", e);
            }

        } else if (checkRequestOrigin(object) == 11) {
            log.info("Request came from Google Assistant");
            try {
                processGoogleAssistantRequest(jsonString, request, response);
            } catch (Exception e) {
                log.error("Failed to marshal action request from Google Actions Builder", e);
                log.info(object.toString(4));
            }

        }
        else {
            log.info("Unsure where request came from!\n{}", object.toString(4));
        }
    }

    private void processGoogleAssistantRequest(String jsonString, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String responseText = "";
        ActionsBuilderRequestInput requestInput = new ObjectMapper().readValue(jsonString, ActionsBuilderRequestInput.class);
        String handlerName = requestInput.getHandler().getName();
        log.info("Detected handler: {}", handlerName);
        responseText = routeRequest(requestInput).invoke(requestInput, getHeadersMap(request));

        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(responseText);
        response.getWriter().flush();
    }

    private void processLegacyGoogleAssistantRequest(JSONObject object, String jsonString, HttpServletRequest request, HttpServletResponse response) throws ExecutionException, InterruptedException, IOException {
        JSONObject queryResultObject = object.getJSONObject("queryResult");
        JSONObject intentObject = queryResultObject.getJSONObject("intent");
        String intentName = intentObject.getString("displayName");

        String responseText = "";

        for (IntentHandler handler : handlers) {
            if (handler.getIntentNameContext().equals(intentName)) {
                responseText = handler.handleRequest(jsonString, getHeadersMap(request)).get();
            }
        }
        if (Strings.isNullOrEmpty(responseText)) {
            for (IntentHandler handler : handlers) {
                if (intentName.contains(handler.getIntentNameContext())) {
                    handler.updateRouting(handler.getClass(), intentName);
                    responseText = handler.handleRequest(jsonString, getHeadersMap(request)).get();
                }
            }
        }
        if (Strings.isNullOrEmpty(responseText)) {
            for (IntentHandler handler : handlers) {
                if (handler.getIntentNameContext().contains(intentName)) {
                    handler.updateRouting(handler.getClass(), intentName);
                    responseText = handler.handleRequest(jsonString, getHeadersMap(request)).get();
                }
            }
        }
        if (!Strings.isNullOrEmpty(responseText)) {
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(responseText);
            response.getWriter().flush();
        }
    }

    private void processAlexaRequest(byte[] serializedRequestEnvelope, HttpServletRequest request, HttpServletResponse response) throws IOException {
        final RequestEnvelope deserializedRequestEnvelope = serializer.deserialize(IOUtils.toString(
                serializedRequestEnvelope, ServletConstants.CHARACTER_ENCODING), RequestEnvelope.class);

        final AlexaHttpRequest alexaHttpRequest = new ServletRequest(request, serializedRequestEnvelope, deserializedRequestEnvelope);

        for (SkillServletVerifier verifier : verifiers) {
            verifier.verify(alexaHttpRequest);
        }

        ResponseEnvelope skillResponse = skill.invoke(deserializedRequestEnvelope);
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        if (skillResponse != null) {
            byte[] serializedResponse = serializer.serialize(skillResponse).getBytes(StandardCharsets.UTF_8);
            try (final OutputStream out = response.getOutputStream()) {
                response.setContentLength(serializedResponse.length);
                out.write(serializedResponse);
            }
        }
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        throw new NotSerializableException("Skill org.fon.master.bsavic.api.servlet is not serializable");
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        throw new NotSerializableException("Skill org.fon.master.bsavic.api.servlet is not serializable");
    }

    private int checkRequestOrigin(JSONObject jsonObject) {
        // 0 - Alexa
        // 1 - Google
        // 11 - Google Actions Builder
        if (jsonObject.has("request") &&
                jsonObject.has("session")
                && jsonObject.has("version")
                && !jsonObject.has("originalDetectIntentRequest")) {
            return 0;
        } else if (jsonObject.has("originalDetectIntentRequest") &&
                jsonObject.has("queryResult") &&
                jsonObject.has("responseId")) {
            return 1;
        } else if (jsonObject.has("handler")
                && jsonObject.has("intent")
                && jsonObject.has("scene")) {
            return 11;
        }
        else {
            return -1;
        }
    }

    private String getIntentName(String body) {
        // convert body to JSON
        JSONObject bodyJsonObject = new JSONObject(body);
        //get queryResult
        JSONObject queryResultObject = bodyJsonObject.getJSONObject("queryResult");
        //retrieve intent
        JSONObject intentObject = queryResultObject.getJSONObject("intent");
        //retrieve displayName
        return intentObject.getString("displayName");
    }

    private Map<String, String> getHeadersMap(HttpServletRequest request) {
        Map<String, String> headersMap = new HashMap<>();
        Enumeration<?> headerNamesEnumeration = request.getHeaderNames();
        while (headerNamesEnumeration.hasMoreElements()) {
            String key = (String) headerNamesEnumeration.nextElement();
            String value = request.getHeader(key);
            headersMap.put(key, value);
        }
        return headersMap;
    }

    private IntentHandler routeRequest(ActionsBuilderRequestInput requestInput) {
        String handlerRoutingQuery = null;
        String resolvedSlot = null;
        Map<String, Object> sessionStorage = requestInput.getSession().getParams();
        cleanUpSessionStorage(sessionStorage);
        for (Map.Entry<String, Object> entry : sessionStorage.entrySet()) {
            if (entry.getKey().contains(ApiConstants.ELICIT_SLOT_PREFIX)
                    && !entry.getKey().contains(ApiConstants.ELICIT_SLOT_RESOLVED_PREFIX)) {
                String slot = entry.getKey().substring(ApiConstants.ELICIT_SLOT_PREFIX.length());
                Intent intent = requestInput.getIntent();
                Object intentParam = intent.getParams().get(slot);
                if (intentParam == null || Strings.isNullOrEmpty(intentParam.toString())) {
                    requestInput.getIntent().getParams().put(slot, new IntentParameterValue(intent.getQuery()));
                    handlerRoutingQuery = entry.getValue().toString();
                    resolvedSlot = slot;

                    requestInput.getHandler().setName(handlerRoutingQuery);
                    requestInput.getIntent().setName(entry.getValue().toString());
                }
            }
        }
        if (Strings.isNullOrEmpty(handlerRoutingQuery)) {
            handlerRoutingQuery = requestInput.getHandler().getName();
        }
        if (!Strings.isNullOrEmpty(resolvedSlot)) {
            requestInput.getSession().getParams().remove(ApiConstants.ELICIT_SLOT_PREFIX + resolvedSlot);
            requestInput.getSession().getParams()
                    .put(ApiConstants.ELICIT_SLOT_RESOLVED_PREFIX + resolvedSlot, requestInput.getIntent().getQuery());
        }
        for (IntentHandler handler : handlers) {
            if (handler.getIntentNameContext().equals(handlerRoutingQuery)) {
                return handler;
            }
        }
        return null;
    }

    private void cleanUpSessionStorage(Map<String, Object> sessionStorage) {
        List<String> paramsToRemove = new ArrayList<>();
        for (String key : sessionStorage.keySet()) {
            if (key.contains(ApiConstants.ELICIT_SLOT_RESOLVED_PREFIX)) {
                String resolvedKey = key;
                String resolvedKeyClean = key.substring(ApiConstants.ELICIT_SLOT_RESOLVED_PREFIX.length());
                for (String unresolved : sessionStorage.keySet()) {
                    if (!unresolved.equals(resolvedKey) &&
                            (unresolved.contains(ApiConstants.ELICIT_SLOT_PREFIX)
                            && !unresolved.contains(ApiConstants.ELICIT_SLOT_RESOLVED_PREFIX))) {
                        if (unresolved.substring(ApiConstants.ELICIT_SLOT_PREFIX.length()).equals(resolvedKeyClean)) {
                            paramsToRemove.add(unresolved);
                        }
                    }
                }
            }
        }
        for (String param : paramsToRemove) {
            sessionStorage.remove(param);
        }
    }

    public SkillAndActionServlet setApplicationName(String applicationName) {
        this.applicationName = applicationName;
        return this;
    }

    private class LaunchRequestHandler implements RequestHandler {
        @Override
        public boolean canHandle(HandlerInput input) {
            return input.matches(Predicates.requestType(LaunchRequest.class));
        }

        @Override
        public Optional<Response> handle(HandlerInput input) {
            String speechText;
            if (Strings.isNullOrEmpty(applicationName)) {
                speechText = "Welcome, what can I help you with?";
            } else {
                speechText = "Welcome to " + applicationName + ", what can I help you with?";
            }
            return input.getResponseBuilder()
                    .withSpeech(speechText)
                    .withSimpleCard("Welcome", speechText)
                    .withReprompt(speechText)
                    .build();
        }
    }
}
