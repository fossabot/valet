package co.valetapp;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import co.valetapp.auto.AutoParkService;

public class Tools {

    public static void park(Context context, Double latitude, Double longitude, boolean manual) {
        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putString(Const.LAT_KEY, Double.toString(latitude));
        editor.putString(Const.LONG_KEY, Double.toString(longitude));
        editor.putBoolean(Const.MANUAL_KEY, manual);
        editor.commit();

        if (manual) {
            Intent intent = new Intent(context, AutoParkService.class);
            intent.setAction(AutoParkService.ACTION_STOP);
            context.startService(intent);
        }
    }

    public static void unpark(Context context) {
        SharedPreferences prefs = getPrefs(context);
        SharedPreferences.Editor edit = prefs.edit();
        edit.remove(Const.LAT_KEY);
        edit.remove(Const.LONG_KEY);
        edit.remove(Const.TIME_KEY);
        edit.commit();

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(getAlarmIntent(context));

        if (prefs.getBoolean(Const.PARKING_SENSOR_KEY, false)) {
            Intent intent = new Intent(context, AutoParkService.class);
            intent.setAction(AutoParkService.ACTION_START);
            context.startService(intent);
        }

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
        }
        else {
            return false;
        }
    }

    public static boolean isTimed(Context context) {
        SharedPreferences prefs = getPrefs(context);
        if (prefs.contains(Const.TIME_KEY)) {
            return true;
        }
        else {
            return false;
        }
    }

    public static boolean isParkedAndTimed(Context context) {
        if (isParked(context) && isTimed(context)) {
            return true;
        }
        else {
            return false;
        }
    }

    public static boolean isParkedOrTimed(Context context) {
        if (isParked(context) || isTimed(context)) {
            return true;
        }
        else {
            return false;
        }
    }

    public static SharedPreferences getPrefs(Context context) {
        return  context.getSharedPreferences(Const.SHARED_PREFS_NAME, Activity.MODE_PRIVATE);
    }

    public static PendingIntent getAlarmIntent(Context context) {
        Intent i = new Intent(context, AlarmBroadcastReceiver.class);
        return PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
