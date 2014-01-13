package co.valetapp.util;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.TypedValue;

import java.io.File;

import co.valetapp.R;
import co.valetapp.activity.ParkActivity;
import co.valetapp.receiver.AlarmBroadcastReceiver;
import co.valetapp.service.AutoParkService;

public class Tools {

    public static void park(Context context, Double latitude, Double longitude, boolean reliablyParked, boolean manuallyParked) {
        park(context, Double.toString(latitude), Double.toString(longitude), reliablyParked, manuallyParked);
    }

    public static void park(Context context, String latitude, String longitude, boolean reliablyParked, boolean manuallyParked) {
        SharedPreferences prefs = getPrefs(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Const.LAT_KEY, latitude);
        editor.putString(Const.LONG_KEY, longitude);
        editor.putBoolean(Const.RELIABLY_PARKED_KEY, reliablyParked);
        editor.putBoolean(Const.MANUALLY_PARKED_KEY, manuallyParked);
        editor.commit();

        if (reliablyParked) {
            Intent intent = new Intent(context, AutoParkService.class);
            intent.setAction(AutoParkService.ACTION_STOP);
            context.startService(intent);
        }

        boolean hasNotifications = prefs.getBoolean(Const.NOTIFICATIONS_KEY, false);
        if (!manuallyParked && hasNotifications) {
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(context)
                            .setAutoCancel(true)
                            .setSmallIcon(R.drawable.ic_stat_valet)
                            .setContentTitle(context.getString(R.string.notification_title))
                            .setContentText(context.getString(R.string.notification_text));
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
    }

    public static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(Const.SHARED_PREFS_NAME, Activity.MODE_PRIVATE);
    }

    public static void unpark(Context context) {
        deleteExternalStoragePublicPicture();

        SharedPreferences prefs = getPrefs(context);
        SharedPreferences.Editor edit = prefs.edit();
        edit.remove(Const.LAT_KEY);
        edit.remove(Const.LONG_KEY);
        edit.remove(Const.TIME_KEY);
        edit.remove(Const.NOTE_KEY);
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

    public static long getTime(Context context) {
        return context.getSharedPreferences(Const.SHARED_PREFS_NAME, Context.MODE_PRIVATE).getLong(Const.TIME_KEY, 0);
    }

    public static PendingIntent getAlarmIntent(Context context) {
        Intent i = new Intent(context, AlarmBroadcastReceiver.class);
        return PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static boolean isManuallyParked(Context context) {
        if (isParked(context)) {
            if (getPrefs(context).contains(Const.RELIABLY_PARKED_KEY)) {
                return true;
            }
            else {
                return false;
            }
        } else {
            return false;
        }
    }

    public static boolean isParked(Context context) {
        SharedPreferences prefs = getPrefs(context);
        return prefs.contains(Const.LAT_KEY) && prefs.contains(Const.LONG_KEY);
    }

    public static boolean isTimed(Context context) {
        SharedPreferences prefs = getPrefs(context);
        return prefs.contains(Const.TIME_KEY);
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

    public static float convertDpToPixels(Context context, int dp) {
        Resources r = context.getResources();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
    }
}
