package com.example.macky_chat_app;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class firebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        //work
        String notification_title = remoteMessage.getNotification().getTitle();
        String notification_message = remoteMessage.getNotification().getBody();
        String click_action = remoteMessage.getNotification().getClickAction();
        final String from_user_id = remoteMessage.getData().get("from_user_id");
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this,getString(R.string.default_notification_channel_id))
                .setSmallIcon(R.mipmap.ic_launcher).setContentTitle(notification_title)
                .setContentText(notification_message);
        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
            Intent result_intent = new Intent(click_action);
            result_intent.putExtra("from_user_id", from_user_id);
            PendingIntent resultPendingIntent = PendingIntent.getActivities(
                    this, 0, new Intent[]{result_intent}, PendingIntent.FLAG_UPDATE_CURRENT
            );
            builder.setContentIntent(resultPendingIntent);
        }
        int notification_id = (int) System.currentTimeMillis();
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(notification_id,builder.build());
    }
}
