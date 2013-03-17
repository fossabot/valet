package co.valetapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.widget.Toast;
import com.android.vending.billing.util.IabHelper;
import com.android.vending.billing.util.IabResult;
import com.android.vending.billing.util.Inventory;
import com.android.vending.billing.util.Purchase;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Settings extends PreferenceActivity {
    BluetoothAdapter bluetoothAdapter;
    CheckBoxPreference autoPark;
    boolean hasAutoPark, hasBluetooth;
    IabHelper mHelper;
    IabHelper.OnIabPurchaseFinishedListener purchaseFinishedListener
            = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            if (result.isFailure()) {
                Log.d(getLocalClassName(), "Error purchasing: " + result);

                return;
            } else if (purchase.getSku().equals(Const.SKU_AUTO_PARK)) {
                hasAutoPark = true;
                autoPark.setChecked(true);
            }
        }
    };
    IabHelper.QueryInventoryFinishedListener gotInventoryListener
            = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result,
                                             Inventory inventory) {

            if (result.isFailure()) {
                autoPark.setEnabled(false);
            } else {
                hasAutoPark = inventory.hasPurchase(Const.SKU_AUTO_PARK);
                autoPark.setEnabled(true);
                autoPark.setChecked(true);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        autoPark = (CheckBoxPreference) findPreference("pref_auto_park");
        autoPark.setEnabled(false);
        autoPark.setShouldDisableView(true);
        autoPark.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (hasAutoPark) {

                    return false;
                } else {
                    autoPark.setChecked(false);
                    mHelper.launchPurchaseFlow(Settings.this, Const.SKU_AUTO_PARK, 10001,
                            purchaseFinishedListener, "");

                    // TODO add developer payload and set up verification server

                    return true;
                }
            }
        });

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            autoPark.setEnabled(false);

        }

        String base64EncodedPublicKey = Const.KEY_1 +
                Const.KEY_2 +
                Const.KEY_3 +
                Const.KEY_4 +
                Const.KEY_5;

        mHelper = new IabHelper(this, base64EncodedPublicKey);

        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                    Log.d(getLocalClassName(), "Problem setting up In-app Billing: " + result);
                }

                mHelper.queryInventoryAsync(gotInventoryListener);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

       hasBluetooth = bluetoothAdapter.getState() == BluetoothAdapter.STATE_ON;

//        Set<BluetoothDevice> devices = bluetoothAdapter.getBondedDevices();
//        if (devices.size() > 0) {
//            bluetooth.setEnabled(true);
//
//            List<CharSequence> entries = new ArrayList<CharSequence>(devices.size());
//            List<CharSequence> values = new ArrayList<CharSequence>(devices.size());
//            for (BluetoothDevice device : devices) {
//                entries.add(device.getName());
//                values.add(device.getAddress());
//            }
//
//            autoParkPref.setEntries(entries.toArray(new CharSequence[entries.size()]));
//            autoParkPref.setEntryValues(values.toArray(new CharSequence[values.size()]));
//        } else {
//            bluetooth.setEnabled(false);
//        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mHelper != null) mHelper.dispose();
        mHelper = null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(getLocalClassName(), "onActivityResult(" + requestCode + "," + resultCode + "," + data);

        // Pass on the activity result to the helper for handling
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
            super.onActivityResult(requestCode, resultCode, data);
        } else {
            Log.d(getLocalClassName(), "onActivityResult handled by IABUtil.");
        }
    }
}
