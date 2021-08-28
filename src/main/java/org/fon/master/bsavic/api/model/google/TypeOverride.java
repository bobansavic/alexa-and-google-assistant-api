package org.fon.master.bsavic.api.model.google;

public class TypeOverride {
    private String name;
    private TypeOverrideMode mode;
    private SynonymType synonym;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TypeOverrideMode getMode() {
        return mode;
    }

    public void setMode(TypeOverrideMode mode) {
        this.mode = mode;
    }

    public SynonymType getSynonym() {
        return synonym;
    }

    public void setSynonym(SynonymType synonym) {
        this.synonym = synonym;
    }
}
