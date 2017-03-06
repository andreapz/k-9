package com.tiscali.appmail.notification;


import com.tiscali.appmail.Account;
import com.tiscali.appmail.K9;
import com.tiscali.appmail.K9.NotificationQuickDelete;
import com.tiscali.appmail.R;

import android.app.PendingIntent;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.BigTextStyle;
import android.support.v4.app.NotificationCompat.Builder;


abstract class BaseNotifications {
    protected final Context context;
    protected final NotificationController controller;
    protected final NotificationActionCreator actionCreator;


    protected BaseNotifications(NotificationController controller,
            NotificationActionCreator actionCreator) {
        this.context = controller.getContext();
        this.controller = controller;
        this.actionCreator = actionCreator;
    }

    protected NotificationCompat.Builder createBigTextStyleNotification(Account account,
            NotificationHolder holder, int notificationId) {
        String accountName = controller.getAccountName(account);
        NotificationContent content = holder.content;
        String groupKey = NotificationGroupKeys.getGroupKey(account);

        NotificationCompat.Builder builder = createAndInitializeNotificationBuilder(account)
                .setTicker(content.summary).setGroup(groupKey).setContentTitle(content.sender)
                .setContentText(content.subject).setSubText(accountName);

        NotificationCompat.BigTextStyle style = createBigTextStyle(builder);
        style.bigText(content.preview);

        builder.setStyle(style);

        PendingIntent contentIntent = actionCreator
                .createViewMessagePendingIntent(content.messageReference, notificationId);
        builder.setContentIntent(contentIntent);

        return builder;
    }

    protected NotificationCompat.Builder createAndInitializeNotificationBuilder(Account account) {
        return controller.createNotificationBuilder().setSmallIcon(getNewMailNotificationIcon())
                .setColor(getColor()) // account.getChipColor()
                .setWhen(System.currentTimeMillis()).setAutoCancel(true);
    }

    private int getColor() {
        // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        // return ContextCompatApi23.getColor(context, id);
        // } else {
        return context.getResources().getColor(R.color.colorPrimary);
        // }
    }

    protected boolean isDeleteActionEnabled() {
        NotificationQuickDelete deleteOption = K9.getNotificationQuickDeleteBehaviour();
        return deleteOption == NotificationQuickDelete.ALWAYS
                || deleteOption == NotificationQuickDelete.FOR_SINGLE_MSG;
    }

    protected BigTextStyle createBigTextStyle(Builder builder) {
        return new BigTextStyle(builder);
    }

    private int getNewMailNotificationIcon() {
        return R.drawable.notification_icon_new_mail;
    }
}
