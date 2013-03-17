package co.valetapp.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;
import co.valetapp.Const;
import co.valetapp.R;

import java.util.Set;

public class BluetoothBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = context.getSharedPreferences(Const.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        if (prefs.contains(Const.BLUETOOTH_KEY)) {
            if (intent.getAction().equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Set<String> addresses = prefs.getStringSet(Const.BLUETOOTH_KEY, null);
                if (addresses != null) {
                    for (String address : addresses) {
                        if (address.equals(device.getAddress())) {
                            context.startService(new Intent(context, LocationService.class));

                            break;
                        }
                    }
                }
            }
        }
    }

}
