package co.valetapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

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

        wl.release();
    }
}
