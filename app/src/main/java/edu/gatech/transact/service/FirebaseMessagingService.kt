package edu.gatech.transact.service


import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import edu.gatech.transact.R
import edu.gatech.transact.views.HomeActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

const val channelName = "edu.gatech.transact"
@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class FirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        if (message.notification != null) {
            generateNotification(message.notification!!.title!!, message.notification!!.body!!)
        }

    }
    @SuppressLint("RemoteViewLayout")
    fun getRemoteView(title: String, message: String): RemoteViews {
        val remoteView = RemoteViews("edu.gatech.transact", R.layout.notification)

        remoteView.setTextViewText(R.id.notificationTitle, title)
        remoteView.setTextViewText(R.id.notificationText, message)
        remoteView.setImageViewResource(R.id.notificationLogo, R.drawable.ic_stat_name)
        return remoteView
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    fun generateNotification(title: String, message: String) {
        val intent = Intent(this, HomeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        var builder: NotificationCompat.Builder = NotificationCompat.Builder(applicationContext,
            R.string.default_notification_channel_id.toString())
            .setSmallIcon(R.drawable.ic_stat_name)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000))
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)

        builder = builder.setContent(getRemoteView(title, message))

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationChannelDefault = NotificationChannel(R.string.default_notification_channel_id.toString(),
            channelName, NotificationManager.IMPORTANCE_HIGH)

        notificationManager.createNotificationChannel(notificationChannelDefault)

        notificationManager.notify(0, builder.build())


    }

}


