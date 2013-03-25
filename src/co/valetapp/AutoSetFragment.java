package co.valetapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.HashSet;
import java.util.Set;

public class AutoSetFragment extends DynamicFragment {

    TableRow one, two;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.auto_set_fragment, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        one = (TableRow) view.findViewById(R.id.tableRowOne);
        two = (TableRow) view.findViewById(R.id.tableRowTwo);

        int count = 0;
        Set<BluetoothDevice> bondedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
        if (bondedDevices.size() == 0) {
            TextView tv = new TextView(getActivity());
            Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Gotham-Medium.ttf");
            tv.setTypeface(typeface);
            tv.setTextColor(Color.WHITE);
            tv.setText(R.string.no_bluetooth);
            one.addView(tv);

            // TODO make the TextView center in the TableRow
        }
        else {
            for (BluetoothDevice bluetoothDevice : bondedDevices) {

                CheckBox cb = new CheckBox(getActivity());
                Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Gotham-Medium.ttf");
                cb.setTypeface(typeface);
                cb.setTextColor(Color.WHITE);
                cb.setText(bluetoothDevice.getName());
                cb.setTag(bluetoothDevice.getAddress());

                SharedPreferences prefs = getActivity().getSharedPreferences(Const.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
                Set<String> addresses = prefs.getStringSet(Const.BLUETOOTH_KEY, null);
                if (addresses != null) {
                    for (String address : addresses) {
                        if (address.equals(cb.getTag())) {
                            cb.setChecked(true);
                        }
                    }
                }

                if (count % 2 == 0) {
                    one.addView(cb);

                } else {
                    two.addView(cb);
                }

                count++;
            }

        }
    }

    void save() {
        Set<String> addresses = new HashSet<String>();

        for (int i = 0; i < one.getChildCount(); i++) {
            addresses.add((String) one.getChildAt(i).getTag());
        }

        for (int i = 0; i < two.getChildCount(); i++) {
            addresses.add((String) two.getChildAt(i).getTag());
        }

        SharedPreferences.Editor editor = getActivity().getSharedPreferences(Const.SHARED_PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putStringSet(Const.BLUETOOTH_KEY, addresses).commit();
    }
}
