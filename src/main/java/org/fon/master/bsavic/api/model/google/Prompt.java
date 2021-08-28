package org.fon.master.bsavic.api.model.google;

import com.google.api.services.actions_fulfillment.v2.model.Suggestion;

import java.util.List;

public class Prompt {
    private boolean override;
    private Simple firstSimple;
    private Content content;
    private Simple lastSimple;
    private List<Suggestion> suggestions;
    private Link link;
//    private Canvas canvas;
//    private OrderUpdate orderUpdate;


    public boolean isOverride() {
        return override;
    }

    public void setOverride(boolean override) {
        this.override = override;
    }

    public Simple getFirstSimple() {
        return firstSimple;
    }

    public void setFirstSimple(Simple firstSimple) {
        this.firstSimple = firstSimple;
    }

    public Content getContent() {
        return content;
    }

    public void setContent(Content content) {
        this.content = content;
    }

    public Simple getLastSimple() {
        return lastSimple;
    }

    public void setLastSimple(Simple lastSimple) {
        this.lastSimple = lastSimple;
    }

    public List<Suggestion> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<Suggestion> suggestions) {
        this.suggestions = suggestions;
    }

    public Link getLink() {
        return link;
    }

    public void setLink(Link link) {
        this.link = link;
    }
}
