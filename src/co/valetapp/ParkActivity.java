package co.valetapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;
import co.valetapp.BarFragment.BarItem;
import co.valetapp.InfoFragment.GeoCoderAsyncTask;
import co.valetapp.bluetooth.BluetoothActivity;
import com.android.vending.billing.util.IabHelper;
import com.android.vending.billing.util.IabResult;
import com.android.vending.billing.util.Inventory;
import com.android.vending.billing.util.Purchase;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ObjectAnimator;
import com.parse.Parse;
import com.parse.ParseObject;

import java.util.List;

public class ParkActivity extends FragmentActivity
        implements LocationListener, OnMarkerClickListener, OnInfoWindowClickListener {
    SharedPreferences prefs;
    LocationManager locationManager;
    Location vehicleLocation;
    float bestAccuracy;
    Criteria criteria;
    ObjectAnimator titleAnimator;
    TextView titleTextView;
    InfoFragment infoFragment;
    GoogleMap googleMap;
    Marker vehicleMarker;
    GeoCoderAsyncTask geoCoderAsyncTask;
    State state;
    IabHelper iabHelper;
    IabHelper.OnIabPurchaseFinishedListener purchaseFinishedListener
            = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            if (result.isFailure()) {
                Log.e(getLocalClassName(), "Error purchasing: " + result);

                return;
            } else if (purchase.getSku().equals(Const.SKU_AUTO_PARK)) {
                hasAutoPark = true;
            }

            iabHelper.dispose();
        }
    };
    IabHelper.QueryInventoryFinishedListener gotInventoryListener
            = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result,
                                             Inventory inventory) {

            if (result.isFailure()) {
                hasAutoPark = false;
            } else {
                hasAutoPark = inventory.hasPurchase(Const.SKU_AUTO_PARK);
            }

            setInitialState();

            iabHelper.dispose();
        }
    };


    boolean hasAutoPark;
    boolean hasBilling;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Parse.initialize(this, "Rk1aoK66rLulnNtaALeL6PhQcGEDkmiudGItreof", "zcG1VzOhhxkQofbYaGNqbHC0BHKbw6myuNkZDeuq");

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setCostAllowed(false);

        prefs = getSharedPreferences(Const.SHARED_PREFS_NAME, MODE_PRIVATE);

        setContentView(R.layout.park_activity);

        int statusCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (statusCode != ConnectionResult.SUCCESS) {
            GooglePlayServicesUtil.getErrorDialog(statusCode, this,
                    Const.REQUEST_CODE_GOOGLE_PLAY,
                    new DialogInterface.OnCancelListener() {

                        @Override
                        public void onCancel(DialogInterface dialog) {
                            finish();
                        }
                    });

            return; // The user's device does not support Google Maps v2.
        }

        titleTextView = (TextView) findViewById(R.id.title_ftv);
        titleTextView.setVisibility(View.INVISIBLE);
        titleTextView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent(ParkActivity.this, AboutActivity.class));
            }
        });

        googleMap = getMapFragment().getMap();
        googleMap.setOnMarkerClickListener(this);
        googleMap.setOnInfoWindowClickListener(this);

        infoFragment = getInfoFragment();

        titleAnimator = ObjectAnimator.ofFloat(titleTextView, "alpha", 0f, 1f);
        titleAnimator.setDuration(3 * 1000);
        titleAnimator.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);

                titleTextView.setVisibility(View.VISIBLE);
                hideMap();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                showMap();

                setState(State.PARKING);
            }
        });


        hideMap();


        iabHelper = new IabHelper(this, Const.IAB_KEY);
        iabHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    Log.e(getLocalClassName(), "Problem setting up In-app Billing: " + result);

                    hasBilling = false;

                    setInitialState();
                }
                else {
                    hasBilling = true;
                    iabHelper.queryInventoryAsync(gotInventoryListener);
                }
            }
        });
    }

    private void setInitialState() {
        if (prefs.contains(Const.LAT_KEY) && prefs.contains(Const.LONG_KEY)) {
            titleTextView.setVisibility(View.VISIBLE);

            showMap();

            if (prefs.contains(Const.TIME_KEY)) {
                setState(State.TIMED);
            } else {
                setState(State.PARKED);
            }
        } else {
            titleAnimator.start();
        }
    }

    private void saveData() {
        ParseObject park = new ParseObject("Park");
        park.put("latitude", Double.parseDouble(prefs.getString(Const.LAT_KEY, "")));
        park.put("longitude", Double.parseDouble(prefs.getString(Const.LONG_KEY, "")));
        park.put("time", prefs.getLong(Const.TIME_KEY, 0));
        park.saveInBackground();
    }

    private void hideMap() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.hide(getMapFragment());
        ft.hide(getInfoFragment());
        ft.commit();
    }

    private void showMap() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.show(getMapFragment());
        ft.show(getInfoFragment());
        ft.commit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        iabHelper.dispose();
    }

    void setState(State state) {

        DynamicFragment dynamicFragment = null;
        BarFragment barFragment = new BarFragment();

        boolean addToBackStack = false;
        boolean keepDynamicFragment = false;

        switch (state) {
            case PARKING:
                clearBackStack();

                locationManager.removeUpdates(this);
                vehicleMarker = null;
                googleMap.setMyLocationEnabled(false);
                googleMap.clear();

                infoFragment.clear();

                barFragment.setItems(BarItem.LOCATING);

                getVehicleLocation();

                break;
            case LOCATED:
                if (hasBilling) {
                    barFragment.setItems(BarItem.PARK, BarItem.AUTO);
                }
                else {
                    barFragment.setItems(BarItem.PARK);
                }

                break;
            case PARKED:
                clearBackStack();

                locationManager.removeUpdates(this);

                googleMap.clear();

                barFragment.setItems(BarItem.FIND, BarItem.SCHEDULE, BarItem.RESET, BarItem.SHARE);

                showVehicle();

                break;
            case SCHEDULE:
                addToBackStack = true;

                barFragment.setItems(BarItem.TIMER, BarItem.ALARM);

                break;
            case UNSCHEDULE:
                addToBackStack = true;
                keepDynamicFragment = true;

                barFragment.setItems(BarItem.UNSCHEDULE);

                break;
            case TIMER:
                addToBackStack = true;
                dynamicFragment = new TimerFragment();

                barFragment.setItems(BarItem.SET);

                break;
            case ALARM:
                addToBackStack = true;
                dynamicFragment = new AlarmFragment();

                barFragment.setItems(BarItem.SET);

                break;
            case TIMED:
                clearBackStack();

                dynamicFragment = new TimedFragment();

                barFragment.setItems(BarItem.FIND, BarItem.SCHEDULE, BarItem.RESET, BarItem.SHARE);

                showVehicle();

                break;
            case CONFIRM:
                addToBackStack = true;

                barFragment.setItems(BarItem.CONFIRM);

                break;

            case AUTO_INFO:
                addToBackStack = true;

                dynamicFragment = new AutoInfoFragment();

                barFragment.setItems(BarItem.BLUETOOTH);

                break;

            case AUTO_SET:
                addToBackStack = true;

                dynamicFragment = new AutoSetFragment();

                barFragment.setItems(BarItem.AUTO_SET, BarItem.AUTO_INFO);

                break;
        }

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.replace(R.id.bar_fl, barFragment);
        if (dynamicFragment != null) {
            ft.replace(R.id.dynamic_fl, dynamicFragment);
        } else {
            if (!keepDynamicFragment && hasDynamicFragment()) {
                ft.remove(getDynamicFragment());
            }
        }

        if (addToBackStack) {
            ft.addToBackStack(null);
        }

        ft.commit();

        this.state = state;
    }

    void getVehicleLocation() {
        List<String> providers = locationManager.getProviders(criteria, true);
        if (providers != null) {
            Location newestLocation = null;
            for (String provider : providers) {
                Location location = locationManager.getLastKnownLocation(provider);
                if (location != null) {
                    if (newestLocation == null) {
                        newestLocation = location;
                    } else {
                        if (location.getTime() > newestLocation.getTime()) {
                            newestLocation = location;
                        }
                    }
                }

                locationManager.requestLocationUpdates(provider, 0, 0, this);
            }

            if (newestLocation != null) {
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                        new LatLng(newestLocation.getLatitude(), newestLocation.getLongitude()), Const.ZOOM_LEVEL);
                googleMap.animateCamera(cameraUpdate);
            }
        } else {
            LocationDialogFragment dialog = new LocationDialogFragment();
            dialog.show(getSupportFragmentManager(), LocationDialogFragment.class.getName());
        }
    }

    void getUserLocation() {

        List<String> providers = locationManager.getProviders(criteria, true);
        if (providers != null) {
            for (String provider : providers) {
                locationManager.requestLocationUpdates(provider, 10000, 100, this);
            }
        } else {
            LocationDialogFragment dialog = new LocationDialogFragment();
            dialog.show(getSupportFragmentManager(), LocationDialogFragment.class.getName());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        switch (state) {
            case PARKING:
            case LOCATED:
            case AUTO_INFO:
            case AUTO_SET:

                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                if (vehicleMarker == null) {
                    vehicleMarker = googleMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title(getString(R.string.vehicle_marker_title))
                            .snippet(getString(R.string.accuracy_abbreviation) + ": " + location.getAccuracy() + getString(R.string.meter_abbreviation))
                            .draggable(false)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_pin)));
                    vehicleMarker.showInfoWindow();

                    setState(State.LOCATED);
                } else {
                    if (location.getAccuracy() != 0.0f && location.getAccuracy() < bestAccuracy) {
                        if (location.getAccuracy() < Const.MIN_ACCURACY) {
                            locationManager.removeUpdates(this);
                        }

                        String snippet = getString(R.string.accuracy_abbreviation) + ": " + location.getAccuracy() + getString(R.string.meter_abbreviation);
                        vehicleMarker.setPosition(latLng);
                        vehicleMarker.setSnippet(snippet);
                        vehicleMarker.showInfoWindow();

                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(vehicleMarker.getPosition(), Const.ZOOM_LEVEL);

                        googleMap.animateCamera(cameraUpdate);
                    }
                }

                bestAccuracy = location.getAccuracy();
                break;
            default:
                InfoFragment infoFragment = getInfoFragment();
                infoFragment.distanceAnimator.start();
                float distance = vehicleLocation.distanceTo(location) * Const.METERS_TO_MILES;
                infoFragment.distanceTextView.setText(String.format("%.2f", distance) + getString(R.string.meter_abbreviation));
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        locationManager.removeUpdates(this);

        if (titleAnimator.isRunning()) {
            titleAnimator.end();
        }

        googleMap.stopAnimation();

        if (geoCoderAsyncTask != null) {
            geoCoderAsyncTask.cancel(true);
        }
    }

    public void onParkItem(View v) {
        endAnimation();

        Editor editor = getSharedPreferences(Const.SHARED_PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putString(Const.LAT_KEY, Double.toString(vehicleMarker.getPosition().latitude));
        editor.putString(Const.LONG_KEY, Double.toString(vehicleMarker.getPosition().longitude));
        editor.commit();

        setState(State.PARKED);
    }

    public void onAutoItem(View v) {
        endAnimation();


        setState(State.AUTO_SET);
    }

    public void onAutoSetItem(View v) {
        if (hasAutoPark) {
            AutoSetFragment frag = (AutoSetFragment) getDynamicFragment();
            frag.save();

            Toast.makeText(ParkActivity.this, R.string.auto_park_confirm, Toast.LENGTH_LONG).show();
            finish();
        }
        else {
            iabHelper = new IabHelper(this, Const.IAB_KEY);
            iabHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
                public void onIabSetupFinished(IabResult result) {
                    if (!result.isSuccess()) {
                        Log.e(getLocalClassName(), "Problem setting up In-app Billing: " + result);

                        hasBilling = false;

                        setInitialState();
                    }
                    else {
                        hasBilling = true;

                        iabHelper.launchPurchaseFlow(ParkActivity.this, Const.SKU_AUTO_PARK, 10001,
                                purchaseFinishedListener, "");
                    }
                }
            });

            // TODO implement recommended security features
        }
    }

    public void onAutoInfoItem(View v) {
        setState(State.AUTO_INFO);
    }

    public void onFindItem(View v) {
        endAnimation();

        startActivity(IntentLibrary.getFindIntent(this,
                vehicleMarker.getPosition().latitude, vehicleMarker.getPosition().longitude));
    }

    public void onScheduleItem(View v) {
        endAnimation();

        if (getDynamicFragment() instanceof TimedFragment) {
            setState(State.UNSCHEDULE);
        } else {
            setState(State.SCHEDULE);
        }
    }

    public void onUnscheduleItem(View v) {
        endAnimation();

        Editor edit = getSharedPreferences(Const.SHARED_PREFS_NAME, Context.MODE_PRIVATE).edit();
        edit.remove(Const.TIME_KEY);
        edit.commit();

        setState(State.PARKED);
    }

    public void onTimerItem(View v) {
        endAnimation();

        setState(State.TIMER);
    }

    public void onAlarmItem(View v) {
        endAnimation();

        setState(State.ALARM);
    }

    public void onBluetoothItem(View v) {
        Intent settingsIntent = new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
        startActivity(settingsIntent);
    }

    public void onSetItem(View v) {
        endAnimation();

        Editor editor = getSharedPreferences(Const.SHARED_PREFS_NAME, Context.MODE_PRIVATE).edit();

        DynamicFragment scheduleFragment = (DynamicFragment) getSupportFragmentManager().findFragmentById(R.id.dynamic_fl);
        if (scheduleFragment instanceof TimerFragment) {
            editor.putLong(Const.TIME_KEY, ((TimerFragment) scheduleFragment).getTime());
        } else if (scheduleFragment instanceof AlarmFragment) {
            editor.putLong(Const.TIME_KEY, ((AlarmFragment) scheduleFragment).getTime());

        } else {
            return;
        }
        editor.commit();

        saveData();

        setState(State.TIMED);
    }

    public void onResetItem(View v) {
        endAnimation();

        setState(State.CONFIRM);
    }

    public void onUnparkItem(View v) {
        endAnimation();

        Editor edit = getSharedPreferences(Const.SHARED_PREFS_NAME, Context.MODE_PRIVATE).edit();
        edit.remove(Const.LAT_KEY);
        edit.remove(Const.LONG_KEY);
        edit.remove(Const.TIME_KEY);
        edit.commit();

        setState(State.PARKING);
    }

    public void onShareItem(View v) {
        endAnimation();

        startActivity(Intent.createChooser(IntentLibrary.getShareIntent(this,
                vehicleMarker.getPosition().latitude, vehicleMarker.getPosition().longitude), getString(R.string.share_intent_chooser_title)));
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return startFindIntentActivity(marker);
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        startFindIntentActivity(marker);
    }

    private boolean startFindIntentActivity(Marker marker) {
        switch (state) {
            case PARKED:
            case TIMED:
                startActivity(Intent.createChooser(IntentLibrary.getFindIntent(this,
                        vehicleMarker.getPosition().latitude, vehicleMarker.getPosition().longitude),
                        getString(R.string.find_intent_chooser_title)));
                return true;
            default:
                return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Const.REQUEST_CODE_GOOGLE_PLAY) {
            if (resultCode == RESULT_CANCELED) {
                finish();
            } else {
                startActivity(getIntent());
            }
        } else if (requestCode == Const.REQUEST_ENABLE_BT) {
            if (requestCode == RESULT_OK) {
                startActivity(new Intent(this, BluetoothActivity.class));
            }
        }
    }

    private void showVehicle() {

        Double latitude = Double.parseDouble(prefs.getString(Const.LAT_KEY, "0"));
        Double longitude = Double.parseDouble(prefs.getString(Const.LONG_KEY, "0"));
        vehicleMarker = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(latitude, longitude))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_pin))
                .title(getString(R.string.vehicle_marker_title)));
        vehicleMarker.setDraggable(false);
        vehicleMarker.showInfoWindow();

        vehicleLocation = new Location(LocationManager.GPS_PROVIDER);
        vehicleLocation.setLatitude(latitude);
        vehicleLocation.setLongitude(longitude);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(vehicleMarker.getPosition(), Const.ZOOM_LEVEL);

        googleMap.animateCamera(cameraUpdate);
        googleMap.setMyLocationEnabled(true);

        InfoFragment infoFragment = getInfoFragment();
        if (infoFragment.addressTextView.getText().length() == 0) {
            geoCoderAsyncTask = infoFragment.new GeoCoderAsyncTask();
            geoCoderAsyncTask.execute(vehicleMarker.getPosition());
        }

        getUserLocation();
    }

    private void clearBackStack() {
        FragmentManager fm = getSupportFragmentManager();
        for (int i = 0; i < fm.getBackStackEntryCount(); i++) {
            fm.popBackStack();
        }
    }

    private void endAnimation() {
        if (titleAnimator.isRunning()) {
            titleAnimator.end();
        }
    }

    private SupportMapFragment getMapFragment() {
        return (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_frag);
    }

    private InfoFragment getInfoFragment() {
        return (InfoFragment) getSupportFragmentManager().findFragmentById(R.id.info_frag);
    }

    private DynamicFragment getDynamicFragment() {
        return (DynamicFragment) getSupportFragmentManager().findFragmentById(R.id.dynamic_fl);
    }

    private BarFragment getBarFragment() {
        return (BarFragment) getSupportFragmentManager().findFragmentById(R.id.bar_fl);
    }

    private boolean hasDynamicFragment() {
        return getSupportFragmentManager().findFragmentById(R.id.dynamic_fl) != null;
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    enum State {
        PARKING, LOCATED, PARKED, CONFIRM, SCHEDULE,
        UNSCHEDULE, ALARM, TIMER, TIMED, AUTO_SET, AUTO_INFO
    }
}
