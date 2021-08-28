package org.fon.master.bsavic.api.model.google;

import java.util.List;

public class Engagement {
    private List<IntentSubscription> pushNotificationIntents;
    private List<IntentSubscription> dailyUpdateIntents;

    public List<IntentSubscription> getPushNotificationIntents() {
        return pushNotificationIntents;
    }

    public void setPushNotificationIntents(List<IntentSubscription> pushNotificationIntents) {
        this.pushNotificationIntents = pushNotificationIntents;
    }

    public List<IntentSubscription> getDailyUpdateIntents() {
        return dailyUpdateIntents;
    }

    public void setDailyUpdateIntents(List<IntentSubscription> dailyUpdateIntents) {
        this.dailyUpdateIntents = dailyUpdateIntents;
    }
}
