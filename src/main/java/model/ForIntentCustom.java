package model;

import com.google.actions.api.ForIntent;

import java.lang.annotation.Annotation;

public class ForIntentCustom implements ForIntent {
    private String value;

    public ForIntentCustom(String value) {
        this.value = value;
    }

    @Override
    public String value() {
        return value;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return ForIntentCustom.class;
    }
}
