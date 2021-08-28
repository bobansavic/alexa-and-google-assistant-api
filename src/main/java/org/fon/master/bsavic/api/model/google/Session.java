package org.fon.master.bsavic.api.model.google;

import java.util.List;
import java.util.Map;

public class Session {
    private String id;
    Map<String, Object> params;
    List<TypeOverride> typeOverrides;
    private String languageCode;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public List<TypeOverride> getTypeOverrides() {
        return typeOverrides;
    }

    public void setTypeOverrides(List<TypeOverride> typeOverrides) {
        this.typeOverrides = typeOverrides;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }
}
