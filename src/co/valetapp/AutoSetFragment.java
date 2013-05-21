package co.valetapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import co.valetapp.auto.AutoParkService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AutoSetFragment extends DynamicFragment {

    Spinner bluetoothSpinner;
    TextView bluetoothTextView;
    CheckBox bluetoothCheckBox, sensorCheckBox;
    SharedPreferences prefs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.auto_set_fragment, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prefs = getActivity().getSharedPreferences(Const.SHARED_PREFS_NAME, Context.MODE_PRIVATE);

        bluetoothSpinner = (Spinner) view.findViewById(R.id.bluetoothSpinner);
        bluetoothTextView = (TextView) view.findViewById(R.id.bluetoothTextView);

        bluetoothCheckBox = (CheckBox) view.findViewById(R.id.bluetoothCheckBox);
        bluetoothCheckBox.setChecked(prefs.getBoolean(Const.BLUETOOTH_ENABLED_KEY, false));
        bluetoothCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    prefs.edit().putBoolean(Const.BLUETOOTH_ENABLED_KEY, true).commit();
                }
                else {
                    prefs.edit().putBoolean(Const.BLUETOOTH_ENABLED_KEY, false).commit();
                }
            }
        });

        sensorCheckBox = (CheckBox) view.findViewById(R.id.sensorCheckBox);
        sensorCheckBox.setChecked(prefs.getBoolean(Const.PARKING_SENSOR_KEY, false));
        sensorCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    prefs.edit().putBoolean(Const.PARKING_SENSOR_KEY, true).commit();
                    Intent autoParkServiceIntent = new Intent(getActivity(), AutoParkService.class);
                    autoParkServiceIntent.setAction(AutoParkService.ACTION_START);
                    getActivity().startService(autoParkServiceIntent);
                }
                else {
                    prefs.edit().putBoolean(Const.PARKING_SENSOR_KEY, false).commit();
                    Intent autoParkServiceIntent = new Intent(getActivity(), AutoParkService.class);
                    autoParkServiceIntent.setAction(AutoParkService.ACTION_STOP);
                    getActivity().startService(autoParkServiceIntent);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        Set<BluetoothDevice> bondedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
        if (bondedDevices.size() == 0) {
            bluetoothTextView.setText(R.string.no_bluetooth);
            bluetoothSpinner.setVisibility(View.GONE);
            bluetoothCheckBox.setEnabled(false);

        } else {
            bluetoothTextView.setText(R.string.select_bluetooth);
            bluetoothSpinner.setVisibility(View.VISIBLE);
            bluetoothCheckBox.setEnabled(true);

            List<MyBluetoothDevice> myBluetoothDevices = new ArrayList<MyBluetoothDevice>(bondedDevices.size());
            for (BluetoothDevice bluetoothDevice : bondedDevices) {
                myBluetoothDevices.add(new MyBluetoothDevice(bluetoothDevice.getName(), bluetoothDevice.getAddress()));
            }

            ArrayAdapter<MyBluetoothDevice> adapter = new ArrayAdapter<MyBluetoothDevice>(getActivity(), R.layout.spinner_item, myBluetoothDevices);
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
            bluetoothSpinner.setAdapter(adapter);

            if (prefs.contains(Const.BLUETOOTH_KEY)) {
                String address = prefs.getString(Const.BLUETOOTH_KEY, "");
                int i = 0;
                for (MyBluetoothDevice myBluetoothDevice : myBluetoothDevices) {
                    if (myBluetoothDevice.address.equals(address)) {
                        bluetoothSpinner.setSelection(i);

                        break;
                    }

                    i++;
                }
            }
        }
    }

    void save() {
        MyBluetoothDevice myBluetoothDevice = (MyBluetoothDevice) bluetoothSpinner.getSelectedItem();
        if (myBluetoothDevice != null) {
            prefs.edit().putString(Const.BLUETOOTH_KEY, myBluetoothDevice.address).commit();
        }

    }

    private class MyBluetoothDevice {

        private final String name, address;

        private MyBluetoothDevice(String n, String a) {
            name = n;
            address = a;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
