package com.tiscali.appmail.service;

import java.util.Map;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.tiscali.appmail.R;
import com.tiscali.appmail.activity.NavigationDrawerActivity;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;


/**
 * Created by thomascastangia on 09/02/17.
 */
public class TiscaliAppFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FBMessagingService";
    public static final String TOKEN_VERIFY_BROADCAST = "token_broadcast";

    public static final String FIREBASE_OTP_TOKEN = "FIREBASE_OTP_TOKEN";
    public static final String NOTIFICATION_SECTION = "t";
    public static final String NOTIFICATION_URL = "v";
    public static final String NOTIFICATION_MESSAGE = "m";
    public static final String NOTIFICATION_SECTION_SYSTEM = "s";
    public static final String NOTIFICATION_SECTION_NEWS = "n";
    public static final String NOTIFICATION_SECTION_VIDEO = "v";
    public static final String NOTIFICATION_SECTION_OFFERS = "o";

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {


        // TODO: Handle FCM messages here.

        Log.d(TAG, "From: " + remoteMessage.getFrom());


        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
        }


        if (remoteMessage.getData().size() > 0) {
            if (remoteMessage.getData().get(NOTIFICATION_SECTION) != null && remoteMessage.getData()
                    .get(NOTIFICATION_SECTION).equals(NOTIFICATION_SECTION_SYSTEM)) {
                Intent intent = new Intent(TOKEN_VERIFY_BROADCAST);
                String otp = remoteMessage.getData().get(NOTIFICATION_URL);
                if (otp != null) {
                    intent.putExtra(FIREBASE_OTP_TOKEN, otp);
                }
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            } else {
                sendNotification(remoteMessage.getData());
            }

        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param data FCM data received.
     */
    private void sendNotification(Map<String, String> data) {
        Intent intent = new Intent(this, NavigationDrawerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        String section = data.get(NOTIFICATION_SECTION);
        String url = data.get(NOTIFICATION_URL);
        String message = data.get(NOTIFICATION_MESSAGE);
        intent.putExtra(NOTIFICATION_SECTION, section);
        intent.putExtra(NOTIFICATION_URL, url);
        // end test
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        android.support.v4.app.NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.notification_icon_new_mail)
                        .setContentTitle(message).setContentText(url).setAutoCancel(true)
                        .setSound(defaultSoundUri).setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
