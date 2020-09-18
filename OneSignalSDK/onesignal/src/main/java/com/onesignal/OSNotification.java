/**
 * Modified MIT License
 *
 * Copyright 2016 OneSignal
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * 1. The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * 2. All copies of substantial portions of the Software may only be used in connection
 * with services provided by OneSignal.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.onesignal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.onesignal.GenerateNotification.BUNDLE_KEY_ACTION_ID;
import static com.onesignal.NotificationBundleProcessor.PUSH_ADDITIONAL_DATA_KEY;

/**
 * The notification the user received
 * <br/><br/>
 * {@link #androidNotificationId} - Android Notification ID assigned to the notification. Can be used to cancel or replace the notification
 * {@link #groupedNotifications} - If the notification is a summary notification for a group, this will contain
 * all notification payloads it was created from.
 */
public class OSNotification {

   private NotificationCompat.Extender notificationExtender;

   /**
    * Summary notifications grouped
    * Notification payload will have the most recent notification received.
    */
   @Nullable
   private List<OSNotification> groupedNotifications;

   /**
    * Android notification id. Can later be used to dismiss the notification programmatically.
    */
   private Integer androidNotificationId;

   private String notificationId;
   private String templateName;
   private String templateId;
   private String title;
   private String body;
   private JSONObject additionalData;
   private String smallIcon;
   private String largeIcon;
   private String bigPicture;
   private String smallIconAccentColor;
   private String launchURL;
   private String sound;
   private String ledColor;
   private int lockScreenVisibility = 1;
   private String groupKey;
   private String groupMessage;
   private List<ActionButton> actionButtons;
   private String fromProjectNumber;
   private BackgroundImageLayout backgroundImageLayout;
   private String collapseId;
   private int priority;
   private String rawPayload;

   protected OSNotification() {
   }

   public OSNotification(@NonNull JSONObject payload) {
      this(null, payload, 0);
   }

   public OSNotification(@Nullable List<OSNotification> groupedNotifications, @NonNull JSONObject payload) {
      this(groupedNotifications, payload, 0);
   }

   public OSNotification(@Nullable List<OSNotification> groupedNotifications, @NonNull JSONObject jsonPayload, Integer androidNotificationId) {
      initPayloadData(jsonPayload);
      this.groupedNotifications = groupedNotifications;
      this.androidNotificationId = androidNotificationId;
   }

   protected OSNotification(OSNotification notification) {
      this.notificationExtender = notification.notificationExtender;
      this.groupedNotifications = notification.groupedNotifications;
      this.androidNotificationId = notification.androidNotificationId;
      this.notificationId = notification.notificationId;
      this.templateName = notification.templateName;
      this.templateId = notification.templateId;
      this.title = notification.title;
      this.body = notification.body;
      this.additionalData = notification.additionalData;
      this.largeIcon = notification.largeIcon;
      this.bigPicture = notification.bigPicture;
      this.smallIconAccentColor = notification.smallIconAccentColor;
      this.launchURL = notification.launchURL;
      this.sound = notification.sound;
      this.ledColor = notification.ledColor;
      this.lockScreenVisibility = notification.lockScreenVisibility;
      this.groupKey = notification.groupKey;
      this.groupMessage = notification.groupMessage;
      this.actionButtons = notification.actionButtons;
      this.fromProjectNumber = notification.fromProjectNumber;
      this.backgroundImageLayout = notification.backgroundImageLayout;
      this.collapseId = notification.collapseId;
      this.priority = notification.priority;
      this.rawPayload = notification.rawPayload;
   }

   private void initPayloadData(JSONObject currentJsonPayload) {
      JSONObject customJson;
      try {
         customJson = NotificationBundleProcessor.getCustomJSONObject(currentJsonPayload);
      } catch (Throwable t) {
         OneSignal.Log(OneSignal.LOG_LEVEL.ERROR, "Error assigning OSNotificationReceivedEvent payload values!", t);
         return;
      }

      notificationId = customJson.optString("i");
      templateId = customJson.optString("ti");
      templateName = customJson.optString("tn");
      rawPayload = currentJsonPayload.toString();
      additionalData = customJson.optJSONObject(PUSH_ADDITIONAL_DATA_KEY);
      launchURL = customJson.optString("u", null);

      body = currentJsonPayload.optString("alert", null);
      title = currentJsonPayload.optString("title", null);
      smallIcon = currentJsonPayload.optString("sicon", null);
      bigPicture = currentJsonPayload.optString("bicon", null);
      largeIcon = currentJsonPayload.optString("licon", null);
      sound = currentJsonPayload.optString("sound", null);
      groupKey = currentJsonPayload.optString("grp", null);
      groupMessage = currentJsonPayload.optString("grp_msg", null);
      smallIconAccentColor = currentJsonPayload.optString("bgac", null);
      ledColor = currentJsonPayload.optString("ledc", null);
      String visibility = currentJsonPayload.optString("vis", null);
      if (visibility != null)
         lockScreenVisibility = Integer.parseInt(visibility);
      fromProjectNumber = currentJsonPayload.optString("from", null);
      priority = currentJsonPayload.optInt("pri", 0);
      String collapseKey = currentJsonPayload.optString("collapse_key", null);
      if (!"do_not_collapse".equals(collapseKey))
         collapseId = collapseKey;

      try {
         setActionButtons();
      } catch (Throwable t) {
         OneSignal.Log(OneSignal.LOG_LEVEL.ERROR, "Error assigning OSNotificationReceivedEvent.actionButtons values!", t);
      }

      try {
         setBackgroundImageLayout(currentJsonPayload);
      } catch (Throwable t) {
         OneSignal.Log(OneSignal.LOG_LEVEL.ERROR, "Error assigning OSNotificationReceivedEvent.backgroundImageLayout values!", t);
      }
   }

   private void setActionButtons() throws Throwable {
      if (additionalData != null && additionalData.has("actionButtons")) {
         JSONArray jsonActionButtons = additionalData.getJSONArray("actionButtons");
         actionButtons = new ArrayList<>();

         for (int i = 0; i < jsonActionButtons.length(); i++) {
            JSONObject jsonActionButton = jsonActionButtons.getJSONObject(i);
            ActionButton actionButton = new ActionButton();
            actionButton.id = jsonActionButton.optString("id", null);
            actionButton.text = jsonActionButton.optString("text", null);
            actionButton.icon = jsonActionButton.optString("icon", null);
            actionButtons.add(actionButton);
         }
         additionalData.remove(BUNDLE_KEY_ACTION_ID);
         additionalData.remove("actionButtons");
      }
   }

   private void setBackgroundImageLayout(JSONObject currentJsonPayload) throws Throwable {
      String jsonStrBgImage = currentJsonPayload.optString("bg_img", null);
      if (jsonStrBgImage != null) {
         JSONObject jsonBgImage = new JSONObject(jsonStrBgImage);
         backgroundImageLayout = new BackgroundImageLayout();
         backgroundImageLayout.image = jsonBgImage.optString("img");
         backgroundImageLayout.titleTextColor = jsonBgImage.optString("tc");
         backgroundImageLayout.bodyTextColor = jsonBgImage.optString("bc");
      }
   }

   public OSMutableNotification mutableCopy() {
      return new OSMutableNotification(this);
   }

   public NotificationCompat.Extender getNotificationExtender() {
      return notificationExtender;
   }

   protected void setNotificationExtender(NotificationCompat.Extender notificationExtender) {
      this.notificationExtender = notificationExtender;
   }

   public Integer getAndroidNotificationId() {
      return androidNotificationId;
   }

   protected void setAndroidNotificationId(Integer androidNotificationId) {
      this.androidNotificationId = androidNotificationId;
   }

   @Nullable
   public List<OSNotification> getGroupedNotifications() {
      return groupedNotifications;
   }

   void setGroupedNotifications(@Nullable List<OSNotification> groupedNotifications) {
      this.groupedNotifications = groupedNotifications;
   }

   public String getNotificationId() {
      return notificationId;
   }

   void setNotificationId(String notificationId) {
      this.notificationId = notificationId;
   }

   public String getTemplateName() {
      return templateName;
   }

   void setTemplateName(String templateName) {
      this.templateName = templateName;
   }

   public String getTemplateId() {
      return templateId;
   }

   void setTemplateId(String templateId) {
      this.templateId = templateId;
   }

   public String getTitle() {
      return title;
   }

   void setTitle(String title) {
      this.title = title;
   }

   public String getBody() {
      return body;
   }

   void setBody(String body) {
      this.body = body;
   }

   public JSONObject getAdditionalData() {
      return additionalData;
   }

   void setAdditionalData(JSONObject additionalData) {
      this.additionalData = additionalData;
   }

   public String getSmallIcon() {
      return smallIcon;
   }

   void setSmallIcon(String smallIcon) {
      this.smallIcon = smallIcon;
   }

   public String getLargeIcon() {
      return largeIcon;
   }

   void setLargeIcon(String largeIcon) {
      this.largeIcon = largeIcon;
   }

   public String getBigPicture() {
      return bigPicture;
   }

   void setBigPicture(String bigPicture) {
      this.bigPicture = bigPicture;
   }

   public String getSmallIconAccentColor() {
      return smallIconAccentColor;
   }

   void setSmallIconAccentColor(String smallIconAccentColor) {
      this.smallIconAccentColor = smallIconAccentColor;
   }

   public String getLaunchURL() {
      return launchURL;
   }

   void setLaunchURL(String launchURL) {
      this.launchURL = launchURL;
   }

   public String getSound() {
      return sound;
   }

   void setSound(String sound) {
      this.sound = sound;
   }

   public String getLedColor() {
      return ledColor;
   }

   void setLedColor(String ledColor) {
      this.ledColor = ledColor;
   }

   public int getLockScreenVisibility() {
      return lockScreenVisibility;
   }

   void setLockScreenVisibility(int lockScreenVisibility) {
      this.lockScreenVisibility = lockScreenVisibility;
   }

   public String getGroupKey() {
      return groupKey;
   }

   void setGroupKey(String groupKey) {
      this.groupKey = groupKey;
   }

   public String getGroupMessage() {
      return groupMessage;
   }

   void setGroupMessage(String groupMessage) {
      this.groupMessage = groupMessage;
   }

   public List<ActionButton> getActionButtons() {
      return actionButtons;
   }

   void setActionButtons(List<ActionButton> actionButtons) {
      this.actionButtons = actionButtons;
   }

   public String getFromProjectNumber() {
      return fromProjectNumber;
   }

   void setFromProjectNumber(String fromProjectNumber) {
      this.fromProjectNumber = fromProjectNumber;
   }

   public BackgroundImageLayout getBackgroundImageLayout() {
      return backgroundImageLayout;
   }

   void setBackgroundImageLayout(BackgroundImageLayout backgroundImageLayout) {
      this.backgroundImageLayout = backgroundImageLayout;
   }

   public String getCollapseId() {
      return collapseId;
   }

   void setCollapseId(String collapseId) {
      this.collapseId = collapseId;
   }

   public int getPriority() {
      return priority;
   }

   void setPriority(int priority) {
      this.priority = priority;
   }

   public String getRawPayload() {
      return rawPayload;
   }

   void setRawPayload(String rawPayload) {
      this.rawPayload = rawPayload;
   }

   public JSONObject toJSONObject() {
      JSONObject mainObj = new JSONObject();

      try {
         mainObj.put("androidNotificationId", androidNotificationId);

         if (groupedNotifications != null) {
            JSONArray payloadJsonArray = new JSONArray();
            for (OSNotification notification : groupedNotifications)
               payloadJsonArray.put(notification.toJSONObject());
            mainObj.put("groupedNotifications", payloadJsonArray);
         }

         JSONObject payload = new JSONObject();

         payload.put("notificationID", notificationId);
         payload.put("title", title);
         payload.put("body", body);
         payload.put("smallIcon", smallIcon);
         payload.put("largeIcon", largeIcon);
         payload.put("bigPicture", bigPicture);
         payload.put("smallIconAccentColor", smallIconAccentColor);
         payload.put("launchURL", launchURL);
         payload.put("sound", sound);
         payload.put("ledColor", ledColor);
         payload.put("lockScreenVisibility", lockScreenVisibility);
         payload.put("groupKey", groupKey);
         payload.put("groupMessage", groupMessage);
         payload.put("fromProjectNumber", fromProjectNumber);
         payload.put("collapseId", collapseId);
         payload.put("priority", priority);

         if (additionalData != null)
            payload.put("additionalData", additionalData);

         if (actionButtons != null) {
            JSONArray actionButtonJsonArray = new JSONArray();
            for (ActionButton actionButton : actionButtons) {
               actionButtonJsonArray.put(actionButton.toJSONObject());
            }
            payload.put("actionButtons", actionButtonJsonArray);
         }

         payload.put("rawPayload", rawPayload);

         mainObj.put("payload", payload);
      }
      catch(JSONException e) {
         e.printStackTrace();
      }

      return mainObj;
   }

   /**
    * List of action buttons on the notification. Part of {@link OSNotificationPayload}.
    */
   public static class ActionButton {
      private String id;
      private String text;
      private String icon;

      public ActionButton() {}

      public ActionButton(JSONObject jsonObject) {
         id = jsonObject.optString("id");
         text = jsonObject.optString("text");
         icon = jsonObject.optString("icon");
      }

      public ActionButton(String id, String text, String icon) {
         this.id = id;
         this.text = text;
         this.icon = icon;
      }

      public JSONObject toJSONObject() {
         JSONObject json = new JSONObject();
         try {
            json.put("id", id);
            json.put("text", text);
            json.put("icon", icon);
         }
         catch (Throwable t) {
            t.printStackTrace();
         }

         return json;
      }

      public String getId() {
         return id;
      }

      public String getText() {
         return text;
      }

      public String getIcon() {
         return icon;
      }
   }

   /**
    * If a background image was set, this object will be available. Part of {@link OSNotificationPayload}.
    */
   public static class BackgroundImageLayout {
      private String image;
      private String titleTextColor;
      private String bodyTextColor;

      public String getImage() {
         return image;
      }

      public String getTitleTextColor() {
         return titleTextColor;
      }

      public String getBodyTextColor() {
         return bodyTextColor;
      }
   }
}
