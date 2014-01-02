package co.valetapp.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import co.valetapp.service.LocationService;
import co.valetapp.util.Const;

/**
 * Created by jophde on 6/11/13.
 */
public class DockBroadcastReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {

        int dockState = intent.getIntExtra(Intent.EXTRA_DOCK_STATE, -1);
        boolean isDocked = dockState != Intent.EXTRA_DOCK_STATE_UNDOCKED;

        SharedPreferences prefs = context.getSharedPreferences(Const.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        boolean isEnabled = prefs.getBoolean(Const.DOCK_KEY, false);

        if (!isDocked && isEnabled) {
            Intent locationServiceIntent = new Intent(context, LocationService.class);
            locationServiceIntent.putExtra(Const.RELIABLY_PARKED_KEY, true);
            context.startService(locationServiceIntent);
        }
    }
}
