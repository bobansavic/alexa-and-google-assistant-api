package org.fon.master.bsavic.api.model.google;

import com.google.api.services.actions_fulfillment.v2.model.Entitlement;

import java.util.List;

public class PackageEntitlements {
    private String packageName;
    private List<Entitlement> entitlements;

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public List<Entitlement> getEntitlements() {
        return entitlements;
    }

    public void setEntitlements(List<Entitlement> entitlements) {
        this.entitlements = entitlements;
    }
}
