package co.valetapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import co.valetapp.auto.AutoParkService;

public class BootBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
           SharedPreferences  prefs = context.getSharedPreferences(Const.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
           if (prefs.contains(Const.TIME_KEY)) {
               AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
               am.set(AlarmManager.RTC_WAKEUP, prefs.getLong(Const.TIME_KEY, 0), Tools.getAlarmIntent(context));
           }

           if (!Tools.isManuallyParked(context) && prefs.getBoolean(Const.PARKING_SENSOR_KEY, false)) {
               Intent autoParkServiceIntent = new Intent(context, AutoParkService.class);
               autoParkServiceIntent.setAction(AutoParkService.ACTION_START);
               context.startService(autoParkServiceIntent);
           }
        }
    }
}
