package org.fon.master.bsavic.api.model.google;

import java.util.Map;

public class Intent {
    private String name;
    private Map<String, IntentParameterValue> params;
    private String query;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, IntentParameterValue> getParams() {
        return params;
    }

    public void setParams(Map<String, IntentParameterValue> params) {
        this.params = params;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}
