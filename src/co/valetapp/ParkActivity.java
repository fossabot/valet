package co.valetapp;

import android.app.AlarmManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.crittercism.app.Crittercism;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
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
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

import java.io.File;
import java.util.List;
import java.util.Locale;

import co.valetapp.BarFragment.BarItem;
import co.valetapp.InfoFragment.GeoCoderAsyncTask;
import co.valetapp.auto.AutoParkService;

public class ParkActivity extends FragmentActivity
        implements OnMarkerClickListener, GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener, LocationListener {

    // Update frequency in seconds
    public static final int UPDATE_INTERVAL_IN_SECONDS = 5;
    /*
     * Define a request code to send to Google Play services
     * This code is returned in Activity.onActivityResult
     */
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    // Milliseconds per second
    private static final int MILLISECONDS_PER_SECOND = 1000;
    // Update frequency in milliseconds
    private static final long UPDATE_INTERVAL =
            MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    // The fastest update frequency, in seconds
    private static final int FASTEST_INTERVAL_IN_SECONDS = 1;
    // A fast frequency ceiling in milliseconds
    private static final long FASTEST_INTERVAL =
            MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;
    private static final int IMAGE_CAPTURE_REQUEST = 1000;
    SharedPreferences prefs;
    Location vehicleLocation;
    float bestAccuracy;
    ObjectAnimator titleAnimator;
    TextView titleTextView;
    InfoFragment infoFragment = getInfoFragment();
    GoogleMap googleMap;
    Marker vehicleMarker;
    State state;
    AlarmManager am;
    GeoCoderAsyncTask geoCoderAsyncTask;
    // Define an object that holds accuracy and frequency parameters
    LocationRequest mLocationRequest;
    LocationClient mLocationClient;
    Uri mPictureUri;
    AnimatorListenerAdapter animationListener
            = new AnimatorListenerAdapter() {

        @Override
        public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);
            show();
        }

        @Override
        public void onAnimationStart(Animator animation) {
            super.onAnimationStart(animation);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Crittercism.init(getApplicationContext(), "5145fe5c4002050d07000002");
        Parse.initialize(this, "Rk1aoK66rLulnNtaALeL6PhQcGEDkmiudGItreof", "zcG1VzOhhxkQofbYaGNqbHC0BHKbw6myuNkZDeuq");

        prefs = getSharedPreferences(Const.SHARED_PREFS_NAME, MODE_PRIVATE);
        if (!prefs.contains(Const.SHOW_RATING_KEY)) {
            prefs.edit().putBoolean(Const.SHOW_RATING_KEY, true);
        }

        am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        setSharedLocation();

        mLocationRequest = LocationRequest.create();

        mLocationClient = new LocationClient(this, this, this);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.park_activity);

        googleMap = getMapFragment().getMap();
        if (googleMap != null) {
            googleMap.setOnMarkerClickListener(this);
        }

        infoFragment = getInfoFragment();

        titleTextView = (TextView) findViewById(R.id.title_ftv);
        titleTextView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent(ParkActivity.this, AboutActivity.class));
            }
        });


        titleAnimator = ObjectAnimator.ofFloat(titleTextView, "alpha", 0f, 1f);
        titleAnimator.setDuration(1 * 1000);
        titleAnimator.addListener(animationListener);

        if (servicesConnected()) {
            if (isAlarmIntent()) lockScreen();

            if (!Tools.isManuallyParked(this) && prefs.getBoolean(Const.PARKING_SENSOR_KEY, false)) {
                Intent autoParkServiceIntent = new Intent(this, AutoParkService.class);
                autoParkServiceIntent.setAction(AutoParkService.ACTION_START);
                startService(autoParkServiceIntent);
            }


            getSupportFragmentManager().beginTransaction().replace(R.id.bar_fl, new BarFragment()).commit();
            getSupportFragmentManager().executePendingTransactions();

            hide();

            setInitialState();
        }
    }


    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        if (isAlarmIntent()) {
            lockScreen();
            super.onNewIntent(intent);
        } else {
            super.onNewIntent(intent);
            setSharedLocation();

            if (Tools.isTimed(this)) {
                setState(State.TIMED);
            } else if (Tools.isParked(this)) {
                setState(State.PARKED);
            }
        }
    }

    private void setSharedLocation() {
        Intent intent = getIntent();
        Uri googleMapsUri = intent.getData();
        if (googleMapsUri != null) {
            String q = googleMapsUri.getQueryParameter("q");
            if (q != null) {
                int commaIndex = q.indexOf(",");
                String lat = q.substring(4, commaIndex);
                String lng = q.substring(commaIndex + 1);

                Tools.park(ParkActivity.this, lat, lng, true, true);
            }

            String t = googleMapsUri.getQueryParameter("t");
            if (t != null) {
                long timestamp = Long.parseLong(t);
                setAlarm(timestamp);
            } else {
                Editor edit = prefs.edit();
                edit.remove(Const.TIME_KEY);
                edit.commit();

                am.cancel(Tools.getAlarmIntent(this));
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (state != null && googleMap != null) {
            if (vehicleMarker != null) {
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(vehicleMarker.getPosition(), Const.ZOOM_LEVEL);
                googleMap.animateCamera(cameraUpdate);
            }
        }

        if (prefs.contains(Const.IMAGE_KEY)) {

            if (Tools.hasExternalStoragePublicPicture()) {
                mPictureUri = Uri.parse(prefs.getString(Const.IMAGE_KEY, ""));
            } else {
                mPictureUri = null;
                prefs.edit().remove(Const.IMAGE_KEY);
            }
        }

        if (servicesConnected()) {
            mLocationClient.connect();
        }


    }

    @Override
    protected void onPause() {
        super.onPause();

        stopLocationUpdates();

        if (titleAnimator != null) {
            titleAnimator.end();
        }

        if (googleMap != null) {
            googleMap.stopAnimation();
        }

        if (geoCoderAsyncTask != null) {
            geoCoderAsyncTask.cancel(true);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CONNECTION_FAILURE_RESOLUTION_REQUEST:

                switch (resultCode) {
                    case RESULT_OK:
                        startActivity(getIntent());
                        break;
                    default:
                        Toast.makeText(this, getString(R.string.not_supported), Toast.LENGTH_LONG).show();
                        finish();
                }

            case IMAGE_CAPTURE_REQUEST:
                switch (resultCode) {
                    case RESULT_OK:
                        if (mPictureUri != null) {
                            prefs.edit().putString(Const.IMAGE_KEY, mPictureUri.toString()).commit();
                        }
                        break;
                }
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (state != State.PARKING && state != State.LOCATED && state != State.AUTO_INFO && state != State.AUTO_SET) {
            startActivity(Intent.createChooser(IntentLibrary.getFindIntent(this,
                    vehicleMarker.getPosition().latitude, vehicleMarker.getPosition().longitude),
                    getString(R.string.find_intent_chooser_title)));

            return true;
        } else {
            return false;
        }

    }


    private void lockScreen() {
        final Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
    }

    private boolean isAlarmIntent() {
        String action = getIntent().getAction();
        if (action != null) {
            return action.equals(Const.ACTION_ALARM);
        } else {
            return false;
        }
    }

    private void stopLocationUpdates() {
        if (servicesConnected()) {
            // If the client is connected
            if (mLocationClient.isConnected()) {
            /*
             * Remove location updates for a listener.
             * The current Activity is the listener, so
             * the argument is "this".
             */
                mLocationClient.removeLocationUpdates(this);
            }
        /*
         * After disconnect() is called, the client is
         * considered "dead".
         */
            mLocationClient.disconnect();
        }
    }

    public boolean servicesConnected() {
        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d("Location Updates", "Google Play services is available.");
            // Continue
            return true;
            // Google Play services was not available for some reason
        } else {
            showErrorDialog(resultCode);

            return false;
        }
    }

    private void showErrorDialog(int resultCode) {
        // Get the error code
        // Get the error dialog from Google Play services
        Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                resultCode, this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
        // If Google Play services can provide an error dialog
        if (errorDialog != null) {
            // Create a new DialogFragment for the error dialog
            ErrorDialogFragment errorFragment = new ErrorDialogFragment();
            // Set the dialog in the DialogFragment
            errorFragment.setDialog(errorDialog);
            // Show the error dialog in the DialogFragment
            errorFragment.show(getSupportFragmentManager(), "Location Updates");
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
                            .draggable(false)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_pin)));
                } else if (location.getAccuracy() != 0.0f && location.getAccuracy() < bestAccuracy) {
                    vehicleMarker.setPosition(latLng);

                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(vehicleMarker.getPosition(), Const.ZOOM_LEVEL);

                    googleMap.animateCamera(cameraUpdate);
                }

                bestAccuracy = location.getAccuracy();

                if (state == State.PARKING &&
                        location.getAccuracy() != 0.0f
                        && location.getAccuracy() < Const.MIN_ACCURACY) {

                    setState(State.LOCATED);
                }

                break;
            default:
                if (infoFragment != null) {
                    InfoFragment infoFragment = getInfoFragment();
                    infoFragment.distanceAnimator.start();

                    if (getResources().getConfiguration().locale.equals(Locale.US)) {
                        float distance = vehicleLocation.distanceTo(location) * Const.METERS_TO_MILES;
                        infoFragment.distanceTextView.setText(String.format("%.2f", distance) + getString(R.string.mile_abbreviation));
                    } else {
                        float distance = vehicleLocation.distanceTo(location) / 1000; // km
                        infoFragment.distanceTextView.setText(String.format("%.2f", distance) + getString(R.string.kilometer_abbreviation));
                    }
                }

                break;
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Location location = mLocationClient.getLastLocation();
        if (location != null) {
            if (!isParked()) {
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                        new LatLng(location.getLatitude(), location.getLongitude()), Const.ZOOM_LEVEL);
                googleMap.animateCamera(cameraUpdate);

            }
        }
        mLocationClient.requestLocationUpdates(mLocationRequest, this);
    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                * Thrown if Google Play services canceled the original
                * PendingIntent
                */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            showErrorDialog(connectionResult.getErrorCode());
        }
    }

    public void onLocationItem(View v) {
        Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(settingsIntent);
    }

    public void onParkItem(View v) {
        Tools.park(this, vehicleMarker.getPosition().latitude, vehicleMarker.getPosition().longitude, true, true);

        setState(State.PARKED);
    }

    public void onAutoItem(View v) {
        setState(State.AUTO_SET);
    }

    public void onAutoSetItem(View v) {
        Toast.makeText(ParkActivity.this, R.string.auto_park_confirm, Toast.LENGTH_LONG).show();
        finish();
    }

    private DynamicFragment getDynamicFragment() {
        return (DynamicFragment) getSupportFragmentManager().findFragmentById(R.id.dynamic_fl);
    }

    public void onAutoInfoItem(View v) {
        setState(State.AUTO_INFO);
    }

    public void onFindItem(View v) {
        startActivity(Intent.createChooser(IntentLibrary.getFindIntent(this,
                vehicleMarker.getPosition().latitude, vehicleMarker.getPosition().longitude),
                getString(R.string.find_intent_chooser_title)));
    }

    public void onScheduleItem(View v) {
        if (getDynamicFragment() instanceof TimedFragment) {
            setState(State.UNSCHEDULE);
        } else {
            setState(State.SCHEDULE);
        }
    }

    public void onUnscheduleItem(View v) {
        Editor edit = prefs.edit();
        edit.remove(Const.TIME_KEY);
        edit.commit();


        am.cancel(Tools.getAlarmIntent(this));

        setState(State.PARKED);
    }

    public void onTimerItem(View v) {
        setState(State.TIMER);
    }

    public void onAlarmItem(View v) {
        setState(State.ALARM);
    }

    public void onBluetoothItem(View v) {
        Intent settingsIntent = new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
        startActivity(settingsIntent);
    }

    public void onSetItem(View v) {
        long time;
        DynamicFragment scheduleFragment = (DynamicFragment) getSupportFragmentManager().findFragmentById(R.id.dynamic_fl);
        if (scheduleFragment instanceof TimerFragment) {
            time = ((TimerFragment) scheduleFragment).getTime();

        } else if (scheduleFragment instanceof AlarmFragment) {
            time = ((AlarmFragment) scheduleFragment).getTime();
        } else {
            return;
        }

        setAlarm(time);

        setState(State.TIMED);
    }

    private void setAlarm(long time) {
        Editor editor = getSharedPreferences(Const.SHARED_PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putLong(Const.TIME_KEY, time);
        editor.putBoolean(Const.RELIABLY_PARKED_KEY, true);
        editor.commit();

        am.set(AlarmManager.RTC_WAKEUP, time, Tools.getAlarmIntent(this));

        Intent autoParkServiceIntent = new Intent(this, AutoParkService.class);
        autoParkServiceIntent.setAction(AutoParkService.ACTION_STOP);
        startService(autoParkServiceIntent);
    }

    public void onResetItem(View v) {
        setState(State.CONFIRM);
    }

    public void onUnparkItem(View v) {
        saveData();

        Tools.deleteExternalStoragePublicPicture();
        Tools.unpark(this);
        mPictureUri = null;
        infoFragment.cameraImageButton.setImageResource(R.drawable.camera_selector);
        infoFragment.cameraImageButton.setContentDescription(getString(R.string.camera_button_cd));
        infoFragment.deletePictureButton.setVisibility(View.INVISIBLE);

        infoFragment.noteLinearLayout.setVisibility(View.INVISIBLE);
        infoFragment.root.setVisibility(View.GONE);

        if (prefs.getBoolean(Const.SHOW_RATING_KEY, true)) {
            setState(State.RATING);
        } else {
            setState(State.PARKING);
        }

    }

    public void onYesItem(View v) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("market://details?id=co.valetapp"));
        startActivity(intent);

        prefs.edit().putBoolean(Const.SHOW_RATING_KEY, false).commit();

        setState(State.PARKING);
    }

    public void onNoItem(View v) {
        setState(State.PARKING);
    }

    public void onNeverItem(View v) {
        prefs.edit().putBoolean(Const.SHOW_RATING_KEY, false).commit();

        setState(State.PARKING);
    }


    public void onShareItem(View v) {
        startActivity(Intent.createChooser(IntentLibrary.getShareIntent(this,
                vehicleMarker.getPosition().latitude, vehicleMarker.getPosition().longitude), getString(R.string.share_intent_chooser_title)));
    }

    public void onCameraButton(View v) {
        if (mPictureUri == null) {
            if (isIntentAvailable(this, MediaStore.ACTION_IMAGE_CAPTURE)) {
                dispatchTakePictureIntent(IMAGE_CAPTURE_REQUEST);
            }
        } else {
            if (isIntentAvailable(this, Intent.ACTION_VIEW)) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(mPictureUri, "image/*");
                startActivity(intent);
            }
        }

    }

    private void dispatchTakePictureIntent(int actionCode) {

        File f = Tools.createExternalStoragePublicPicture();
        if (f != null) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            mPictureUri = Uri.fromFile(f);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mPictureUri);
            startActivityForResult(takePictureIntent, actionCode);
        } else {
            throw new NullPointerException("File is null");
        }

    }

    public static boolean isIntentAvailable(Context context, String action) {
        final PackageManager packageManager = context.getPackageManager();
        final Intent intent = new Intent(action);
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    public void onDeletePictureButton(View v) {
        Tools.deleteExternalStoragePublicPicture();
        mPictureUri = null;
        prefs.edit().remove(Const.IMAGE_KEY).commit();

        InfoFragment infoFragment = getInfoFragment();
        if (infoFragment != null) {
            infoFragment.deletePictureButton.setVisibility(View.INVISIBLE);
            infoFragment.cameraImageButton.setImageResource(R.drawable.camera_selector);
            infoFragment.cameraImageButton.setContentDescription(getString(R.string.camera_button_cd));
        }

    }

    private InfoFragment getInfoFragment() {
        return (InfoFragment) getSupportFragmentManager().findFragmentById(R.id.info_frag);
    }

    private void setInitialState() {
        titleTextView.setVisibility(View.VISIBLE);
        if (prefs.contains(Const.LAT_KEY) && prefs.contains(Const.LONG_KEY)) {
            show();

            if (prefs.contains(Const.TIME_KEY)) {
                setState(State.TIMED);
            } else {
                setState(State.PARKED);
            }
        } else {
            titleAnimator.start();
            setState(State.PARKING);
        }
    }

    private boolean isParked() {
        if (prefs.contains(Const.LAT_KEY) && prefs.contains(Const.LONG_KEY)) {
            return true;
        } else {
            return false;
        }
    }

    void setState(State state) {

        DynamicFragment dynamicFragment = null;
        BarFragment barFragment = new BarFragment();

        boolean addToBackStack = false;
        boolean keepDynamicFragment = false;

        switch (state) {
            case PARKING:
                clearBackStack();

                vehicleMarker = null;
                googleMap.setMyLocationEnabled(false);
                googleMap.clear();

                infoFragment.clear();

                barFragment.setItems(BarItem.LOCATING, BarItem.LOCATION);

                if (servicesConnected()) {
                    if (!mLocationClient.isConnected()) {
                        mLocationClient.connect();
                    }
                }


                break;
            case LOCATED:
                barFragment.setItems(BarItem.PARK, BarItem.AUTO);

                break;
            case PARKED:
                clearBackStack();

                dynamicFragment = new ParkedFragment();

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

                break;

            case AUTO_SET:
                addToBackStack = true;

                dynamicFragment = new AutoSetFragment();

                barFragment.setItems(BarItem.AUTO_SET, BarItem.AUTO_INFO);

                break;

            case RATING:
                clearBackStack();
                dynamicFragment = new RateFragment();
                barFragment.setItems(BarItem.YES, BarItem.NO, BarItem.NEVER);

        }

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.replace(R.id.bar_fl, barFragment);

        if (titleAnimator.isRunning()) {
            ft.hide(barFragment);
        }

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

        ft.commitAllowingStateLoss();

        if (state == State.PARKING) {
            if (servicesConnected()) {
                if (mLocationClient.isConnected()) {
                    Location location = mLocationClient.getLastLocation();
                    if (location != null) {
                        onLocationChanged(location);
                    }
                }
            }
        }

        this.state = state;
    }

    private void clearBackStack() {
        FragmentManager fm = getSupportFragmentManager();
        for (int i = 0; i < fm.getBackStackEntryCount(); i++) {
            fm.popBackStack();
        }
    }

    private void hide() {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.hide(getMapFragment());
        ft.hide(getInfoFragment());
        ft.hide(getBarFragment());
        ft.commitAllowingStateLoss();
    }

    private void show() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.show(getMapFragment());
        ft.show(getInfoFragment());
        ft.show(getBarFragment());
        ft.commitAllowingStateLoss();
    }

    private void showVehicle() {
        Double latitude = Double.parseDouble(prefs.getString(Const.LAT_KEY, "0"));
        Double longitude = Double.parseDouble(prefs.getString(Const.LONG_KEY, "0"));
        vehicleMarker = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(latitude, longitude))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_pin)));
        vehicleMarker.setDraggable(false);

        vehicleLocation = new Location(LocationManager.GPS_PROVIDER);
        vehicleLocation.setLatitude(latitude);
        vehicleLocation.setLongitude(longitude);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(vehicleMarker.getPosition(), Const.ZOOM_LEVEL);

        googleMap.animateCamera(cameraUpdate);
        googleMap.setMyLocationEnabled(true);

        InfoFragment infoFragment = getInfoFragment();
        infoFragment.root.setVisibility(View.VISIBLE);
        infoFragment.noteLinearLayout.setVisibility(View.VISIBLE);
        geoCoderAsyncTask = infoFragment.new GeoCoderAsyncTask();
        geoCoderAsyncTask.execute(vehicleMarker.getPosition());
    }

    private void saveData() {
        if (Tools.isParked(this)) {
            ParseGeoPoint point = new ParseGeoPoint(Double.parseDouble(prefs.getString(Const.LAT_KEY, "0")), Double.parseDouble(prefs.getString(Const.LONG_KEY, "0")));
            ParseObject park = new ParseObject("Park");
            park.put("location", point);
            if (Tools.isTimed(this)) {
                park.put("time", prefs.getLong(Const.TIME_KEY, 0));
            }
            park.saveEventually();
        }
    }

    private BarFragment getBarFragment() {
        return (BarFragment) getSupportFragmentManager().findFragmentById(R.id.bar_fl);
    }

    private SupportMapFragment getMapFragment() {
        return (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_frag);
    }

    private boolean hasDynamicFragment() {
        return getSupportFragmentManager().findFragmentById(R.id.dynamic_fl) != null;
    }

    enum State {
        PARKING, LOCATED, PARKED, CONFIRM, SCHEDULE,
        UNSCHEDULE, ALARM, TIMER, TIMED, AUTO_SET, AUTO_INFO, RATING
    }

    // Define a DialogFragment that displays the error dialog
    public static class ErrorDialogFragment extends DialogFragment {
        // Global field to contain the error dialog
        private Dialog mDialog;

        // Default constructor. Sets the dialog field to null
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        // Set the dialog to display
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        // Return a Dialog to the DialogFragment.
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }
}
