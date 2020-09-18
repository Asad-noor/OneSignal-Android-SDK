/**
 * Modified MIT License
 * <p>
 * Copyright 2020 OneSignal
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * 1. The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * 2. All copies of substantial portions of the Software may only be used in connection
 * with services provided by OneSignal.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.onesignal;


import android.content.Context;
import android.net.Uri;

import androidx.core.app.NotificationCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.SecureRandom;

public class OSNotificationGenerationJob {

    // Timeout in seconds before applying defaults
    private static final long SHOW_NOTIFICATION_TIMEOUT = 25 * 1_000L;

    private static final String TITLE_PAYLOAD_PARAM = "title";
    private static final String ALERT_PAYLOAD_PARAM = "alert";
    private static final String CUSTOM_PAYLOAD_PARAM = "custom";
    private static final String ADDITIONAL_DATA_PAYLOAD_PARAM = "a";

    private OSNotification notification;
    private Context context;
    private JSONObject jsonPayload;
    private boolean restoring;
    private boolean iamPreview;
    private boolean processed;

    private Long shownTimeStamp;

    private CharSequence overriddenBodyFromExtender;
    private CharSequence overriddenTitleFromExtender;
    private Uri overriddenSound;
    private Integer overriddenFlags;
    private Integer orgFlags;
    private Uri orgSound;

    private OverrideSettings overrideSettings;

    OSNotificationGenerationJob(Context context) {
        this.context = context;
    }

    public OSNotificationGenerationJob(Context context, JSONObject jsonPayload, OverrideSettings overrideSettings) {
        this.context = context;
        this.jsonPayload = jsonPayload;
        this.overrideSettings = overrideSettings;
    }

    String getApiNotificationId() {
        return OneSignal.getNotificationIdFromFCMJson(jsonPayload);
    }

    int getAndroidIdWithoutCreate() {
        if (overrideSettings == null || overrideSettings.getAndroidNotificationId() == null)
            return -1;

        return overrideSettings.getAndroidNotificationId();
    }

    Integer getAndroidId() {
        if (overrideSettings == null)
            overrideSettings = new OverrideSettings();

        if (overrideSettings.getAndroidNotificationId() == null) {
            int id = new SecureRandom().nextInt();
            overrideSettings.setAndroidNotificationId(id);
            notification.setAndroidNotificationId(id);
        }

        return overrideSettings.getAndroidNotificationId();
    }

    /**
     * Get the notification title from the payload
     */
    CharSequence getTitle() {
        if (overriddenTitleFromExtender != null)
            return overriddenTitleFromExtender;
        return jsonPayload.optString(TITLE_PAYLOAD_PARAM, null);
    }

    /**
     * Get the notification body from the payload
     */
    CharSequence getBody() {
        if (overriddenBodyFromExtender != null)
            return overriddenBodyFromExtender;
        return jsonPayload.optString(ALERT_PAYLOAD_PARAM, null);
    }

    /**
     * Get the notification additional data json from the payload
     */
    JSONObject getAdditionalData() {
        try {
            return new JSONObject(jsonPayload
                    .optString(CUSTOM_PAYLOAD_PARAM))
                    .getJSONObject(ADDITIONAL_DATA_PAYLOAD_PARAM);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return new JSONObject();
    }

    /**
     * If androidNotificationId is -1 then the notification is a silent one
     */
    boolean isNotificationToDisplay() {
        return getAndroidIdWithoutCreate() != -1;
    }

    boolean hasExtender() {
        return overrideSettings != null && overrideSettings.getExtender() != null;
    }

    void setAndroidIdWithoutOverriding(Integer id) {
        if (id == null)
            return;

        if (overrideSettings != null && overrideSettings.getAndroidNotificationId() != null)
            return;

        if (overrideSettings == null)
            overrideSettings = new OverrideSettings();

        overrideSettings.setAndroidNotificationId(id);
    }

    public OSNotification getNotification() {
        return notification;
    }

    public void setNotification(OSNotification notification) {
        this.notification = notification;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public JSONObject getJsonPayload() {
        return jsonPayload;
    }

    public void setJsonPayload(JSONObject jsonPayload) {
        this.jsonPayload = jsonPayload;
    }

    public boolean isRestoring() {
        return restoring;
    }

    public void setRestoring(boolean restoring) {
        this.restoring = restoring;
    }

    public boolean isIamPreview() {
        return iamPreview;
    }

    public void setIamPreview(boolean iamPreview) {
        this.iamPreview = iamPreview;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }

    public Long getShownTimeStamp() {
        return shownTimeStamp;
    }

    public void setShownTimeStamp(Long shownTimeStamp) {
        this.shownTimeStamp = shownTimeStamp;
    }

    public CharSequence getOverriddenBodyFromExtender() {
        return overriddenBodyFromExtender;
    }

    public void setOverriddenBodyFromExtender(CharSequence overriddenBodyFromExtender) {
        this.overriddenBodyFromExtender = overriddenBodyFromExtender;
    }

    public CharSequence getOverriddenTitleFromExtender() {
        return overriddenTitleFromExtender;
    }

    public void setOverriddenTitleFromExtender(CharSequence overriddenTitleFromExtender) {
        this.overriddenTitleFromExtender = overriddenTitleFromExtender;
    }

    public Uri getOverriddenSound() {
        return overriddenSound;
    }

    public void setOverriddenSound(Uri overriddenSound) {
        this.overriddenSound = overriddenSound;
    }

    public Integer getOverriddenFlags() {
        return overriddenFlags;
    }

    public void setOverriddenFlags(Integer overriddenFlags) {
        this.overriddenFlags = overriddenFlags;
    }

    public Integer getOrgFlags() {
        return orgFlags;
    }

    public void setOrgFlags(Integer orgFlags) {
        this.orgFlags = orgFlags;
    }

    public Uri getOrgSound() {
        return orgSound;
    }

    public void setOrgSound(Uri orgSound) {
        this.orgSound = orgSound;
    }

    public OverrideSettings getOverrideSettings() {
        return overrideSettings;
    }

    public void setOverrideSettings(OverrideSettings overrideSettings) {
        this.overrideSettings = overrideSettings;
    }

    public static class OverrideSettings {
        private NotificationCompat.Extender extender;
        private Integer androidNotificationId;

        // Note: Make sure future fields are nullable.
        // Possible future options
        //    int badgeCount;
        //   NotificationCompat.Extender summaryExtender;

        void override(OverrideSettings overrideSettings) {
            if (overrideSettings == null)
                return;

            if (overrideSettings.androidNotificationId != null)
                androidNotificationId = overrideSettings.androidNotificationId;

            if (overrideSettings.extender != null)
                extender = overrideSettings.extender;
        }

        public Integer getAndroidNotificationId() {
            return androidNotificationId;
        }

        public void setAndroidNotificationId(Integer androidNotificationId) {
            this.androidNotificationId = androidNotificationId;
        }

        public NotificationCompat.Extender getExtender() {
            return extender;
        }

        public void setExtender(NotificationCompat.Extender extender) {
            this.extender = extender;
        }

        @Override
        public String toString() {
            return "OverrideSettings{" +
                    "extender=" + extender +
                    ", androidNotificationId=" + androidNotificationId +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "OSNotificationGenerationJob{" +
                "jsonPayload=" + jsonPayload +
                ", isRestoring=" + restoring +
                ", isIamPreview=" + iamPreview +
                ", isProcessed=" + processed +
                ", shownTimeStamp=" + shownTimeStamp +
                ", overriddenBodyFromExtender=" + overriddenBodyFromExtender +
                ", overriddenTitleFromExtender=" + overriddenTitleFromExtender +
                ", overriddenSound=" + overriddenSound +
                ", overriddenFlags=" + overriddenFlags +
                ", orgFlags=" + orgFlags +
                ", orgSound=" + orgSound +
                ", overrideSettings=" + overrideSettings +
                '}';
    }
}
