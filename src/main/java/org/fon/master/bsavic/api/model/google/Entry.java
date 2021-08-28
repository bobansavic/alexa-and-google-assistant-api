package org.fon.master.bsavic.api.model.google;

import java.util.List;

public class Entry {
    private String name;
    private List<String> synonyms;
    private EntryDisplay display;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(List<String> synonyms) {
        this.synonyms = synonyms;
    }

    public EntryDisplay getDisplay() {
        return display;
    }

    public void setDisplay(EntryDisplay display) {
        this.display = display;
    }
}
