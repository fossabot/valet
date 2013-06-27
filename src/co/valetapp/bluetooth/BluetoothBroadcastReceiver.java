package co.valetapp.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import co.valetapp.Const;
import co.valetapp.LocationService;

public class BluetoothBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = context.getSharedPreferences(Const.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        if (prefs.contains(Const.BLUETOOTH_KEY) && prefs.getBoolean(Const.BLUETOOTH_ENABLED_KEY, false)) {
            if (intent.getAction().equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (prefs.contains(Const.BLUETOOTH_KEY)) {
                    String address = prefs.getString(Const.BLUETOOTH_KEY, "");
                    if (address.equals(device.getAddress())) {
                        Intent locationServiceIntent = new Intent(context, LocationService.class);
                        context.startService(locationServiceIntent);
                    }
                }
            }
        }
    }

}
