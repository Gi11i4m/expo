package expo.modules.notifications.notifications.service;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.WeakHashMap;

import androidx.core.app.NotificationManagerCompat;
import expo.modules.notifications.notifications.NotificationManager;
import expo.modules.notifications.notifications.model.Notification;
import expo.modules.notifications.notifications.model.NotificationBehavior;
import expo.modules.notifications.notifications.presentation.builders.ExpoNotificationBuilder;

/**
 * A notification service using {@link ExpoNotificationBuilder} to build notifications.
 * Capable of presenting the notifications to the user.
 */
public class ExpoNotificationsService extends BaseNotificationsService {
  /**
   * A weak map of listeners -> reference. Used to check quickly whether given listener
   * is already registered and to iterate over when notifying of new token.
   */
  protected static WeakHashMap<NotificationManager, WeakReference<NotificationManager>> sListenersReferences = new WeakHashMap<>();

  /**
   * Used only by {@link NotificationManager} instances. If you look for a place to register
   * your listener, use {@link NotificationManager} singleton module.
   * <p>
   * Purposefully the argument is expected to be a {@link NotificationManager} and just a listener.
   * <p>
   * This class doesn't hold strong references to listeners, so you need to own your listeners.
   *
   * @param listener A listener instance to be informed of new push device tokens.
   */
  public static void addListener(NotificationManager listener) {
    // Checks whether this listener has already been registered
    if (!sListenersReferences.containsKey(listener)) {
      WeakReference<NotificationManager> listenerReference = new WeakReference<>(listener);
      sListenersReferences.put(listener, listenerReference);
    }
  }

  @Override
  protected void onNotificationReceived(Notification notification) {
    for (NotificationManager listener : getListeners()) {
      listener.onNotificationReceived(notification);
    }
  }

  @Override
  protected void onNotificationsDropped() {
    for (NotificationManager listener : getListeners()) {
      listener.onNotificationsDropped();
    }
  }

  /**
   * Callback called when the service is supposed to present a notification.
   *
   * @param notification Notification presented
   * @param behavior     Allowed notification behavior
   */
  @Override
  protected void onNotificationPresent(expo.modules.notifications.notifications.model.Notification notification, NotificationBehavior behavior) {
    String tag = notification.getNotificationRequest().getIdentifier();
    int id = 0;
    NotificationManagerCompat.from(this).notify(tag, id, getNotification(notification, behavior));
  }

  protected android.app.Notification getNotification(Notification notification, NotificationBehavior behavior) {
    return new ExpoNotificationBuilder(this)
        .setNotification(notification)
        .setAllowedBehavior(behavior)
        .build();
  }

  private Collection<NotificationManager> getListeners() {
    Collection<NotificationManager> listeners = new ArrayList<>();
    for (WeakReference<NotificationManager> reference : sListenersReferences.values()) {
      NotificationManager manager = reference.get();
      if (manager != null) {
        listeners.add(manager);
      }
    }
    return listeners;
  }
}
