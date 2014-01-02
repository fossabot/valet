package co.valetapp.receiver;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import co.valetapp.R;
import co.valetapp.activity.ParkActivity;
import co.valetapp.util.Const;
import co.valetapp.util.Tools;

/**
 * User: jophde
 * Date: 3/24/13
 * Time: 11:29 AM
 */
public class AlarmBroadcastReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, Const.TAG);
        wl.acquire();

        Intent i = new Intent(context, ParkActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.setAction(Const.ACTION_ALARM);
        context.startActivity(i);

        boolean hasNotifications = Tools.getPrefs(context).getBoolean(Const.NOTIFICATIONS_KEY, false);
        if (hasNotifications) {
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(context)
                            .setAutoCancel(true)
                            .setSmallIcon(R.drawable.ic_stat_valet)
                            .setContentTitle(context.getString(R.string.notification_alarm_title))
                            .setContentText(context.getString(R.string.notification_alarm_text));
            Intent resultIntent = new Intent(context, ParkActivity.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addParentStack(ParkActivity.class);
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder.setContentIntent(resultPendingIntent);
            NotificationManager mNotificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            mNotificationManager.notify(0, mBuilder.build());

        }

        wl.release();
    }
}
