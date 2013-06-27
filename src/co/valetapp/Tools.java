package co.valetapp;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import co.valetapp.auto.AutoParkService;

import java.io.File;

public class Tools {

    public static void park(Context context, Double latitude, Double longitude, boolean manual) {
        SharedPreferences prefs = getPrefs(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Const.LAT_KEY, Double.toString(latitude));
        editor.putString(Const.LONG_KEY, Double.toString(longitude));
        editor.putBoolean(Const.MANUAL_KEY, manual);
        editor.commit();

        if (manual) {
            Intent intent = new Intent(context, AutoParkService.class);
            intent.setAction(AutoParkService.ACTION_STOP);
            context.startService(intent);
        }
        else {
            if (prefs.getBoolean(Const.NOTIFICATIONS_KEY, false)) {
                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(context)
                                .setAutoCancel(true)
                                .setSmallIcon(R.drawable.ic_stat_valet)
                                .setContentTitle(context.getString(R.string.notification_title))
                                .setContentText(context.getString(R.string.notification_text));
// Creates an explicit intent for an Activity in your app
                Intent resultIntent = new Intent(context, ParkActivity.class);

// The stack builder object will contain an artificial back stack for the
// started Activity.
// This ensures that navigating backward from the Activity leads out of
// your application to the Home screen.
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
// Adds the back stack for the Intent (but not the Intent itself)
                stackBuilder.addParentStack(ParkActivity.class);
// Adds the Intent that starts the Activity to the top of the stack
                stackBuilder.addNextIntent(resultIntent);
                PendingIntent resultPendingIntent =
                        stackBuilder.getPendingIntent(
                                0,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );
                mBuilder.setContentIntent(resultPendingIntent);
                NotificationManager mNotificationManager =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
                mNotificationManager.notify(0, mBuilder.build());
            }
        }

    }

    public static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(Const.SHARED_PREFS_NAME, Activity.MODE_PRIVATE);
    }

    public static void unpark(Context context) {
        SharedPreferences prefs = getPrefs(context);
        SharedPreferences.Editor edit = prefs.edit();
        edit.remove(Const.LAT_KEY);
        edit.remove(Const.LONG_KEY);
        edit.remove(Const.TIME_KEY);
        edit.remove(Const.IMAGE_KEY);
        edit.commit();

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(getAlarmIntent(context));

        if (prefs.getBoolean(Const.PARKING_SENSOR_KEY, false)) {
            Intent intent = new Intent(context, AutoParkService.class);
            intent.setAction(AutoParkService.ACTION_START);
            context.startService(intent);
        }

    }

    public static PendingIntent getAlarmIntent(Context context) {
        Intent i = new Intent(context, AlarmBroadcastReceiver.class);
        return PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static boolean isManuallyParked(Context context) {
        if (isParked(context)) {
            if (getPrefs(context).contains(Const.MANUAL_KEY)) {
                return true;
            }
        }

        return false;
    }


    public static boolean isParked(Context context) {
        SharedPreferences prefs = getPrefs(context);
        if (prefs.contains(Const.LAT_KEY) && prefs.contains(Const.LONG_KEY)) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isParkedAndTimed(Context context) {
        if (isParked(context) && isTimed(context)) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isTimed(Context context) {
        SharedPreferences prefs = getPrefs(context);
        if (prefs.contains(Const.TIME_KEY)) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isParkedOrTimed(Context context) {
        if (isParked(context) || isTimed(context)) {
            return true;
        } else {
            return false;
        }
    }

    public static File createExternalStoragePublicPicture() {
        File path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        path.mkdirs();
        if (path.isDirectory()) {
            return new File(path, "co.valetapp.jpg");
        } else {
            return null;
        }
    }

    public static void deleteExternalStoragePublicPicture() {
        File path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File file = new File(path, "co.valetapp.jpg");
        file.delete();
    }

    public static boolean hasExternalStoragePublicPicture() {
        File path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File file = new File(path, "co.valetapp.jpg");
        return file.exists();
    }
}
