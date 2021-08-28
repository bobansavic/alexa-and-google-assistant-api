package org.fon.master.bsavic.api.model.google;

import java.util.List;
import java.util.Map;

public class User {
    private String locale;
    private Map<String, Object> params;
    private AccountLinkingStatus accountLinkingStatus;
    private UserVerificationStatus verificationStatus;
    private String lastSeenTime;
    private Engagement engagement;
    private List<PackageEntitlements> packageEntitlements;
    private List<Permission> permissions;
    private Object gaiamint;

    public Object getGaiamint() {
        return gaiamint;
    }

    public void setGaiamint(Object gaiamint) {
        this.gaiamint = gaiamint;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public AccountLinkingStatus getAccountLinkingStatus() {
        return accountLinkingStatus;
    }

    public void setAccountLinkingStatus(AccountLinkingStatus accountLinkingStatus) {
        this.accountLinkingStatus = accountLinkingStatus;
    }

    public UserVerificationStatus getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(UserVerificationStatus verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public String getLastSeenTime() {
        return lastSeenTime;
    }

    public void setLastSeenTime(String lastSeenTime) {
        this.lastSeenTime = lastSeenTime;
    }

    public Engagement getEngagement() {
        return engagement;
    }

    public void setEngagement(Engagement engagement) {
        this.engagement = engagement;
    }

    public List<PackageEntitlements> getPackageEntitlements() {
        return packageEntitlements;
    }

    public void setPackageEntitlements(List<PackageEntitlements> packageEntitlements) {
        this.packageEntitlements = packageEntitlements;
    }

    public List<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<Permission> permissions) {
        this.permissions = permissions;
    }
}
