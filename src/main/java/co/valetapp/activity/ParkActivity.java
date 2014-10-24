package co.valetapp.activity;

import android.app.AlarmManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import co.valetapp.BuildConfig;
import co.valetapp.R;
import co.valetapp.activity.BarFragment.BarItem;
import co.valetapp.activity.ParkedFragment.GeoCoderAsyncTask;
import co.valetapp.service.AutoParkService;
import co.valetapp.util.Const;
import co.valetapp.util.IntentLibrary;
import co.valetapp.util.Tools;
import com.colatris.sdk.Colatris;
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

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParkActivity extends FragmentActivity
        implements OnMarkerClickListener, GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener, LocationListener, FindFragment.Callback {

    private static final String PROVIDER = "flp";
    private static final double LAT = 37.377166;
    private static final double LNG = -122.086966;
    private static final float ACCURACY = 3.0f;

    public static final int RINGTONE_PICKER_RESULT = 11;

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
    public static final String STATE_STACK = "state";
    SharedPreferences prefs;
    Location vehicleLocation;
    float bestAccuracy;
    GoogleMap googleMap;
    Marker vehicleMarker;
    Stack<State> stateStack = new Stack<State>();
    AlarmManager am;
    GeoCoderAsyncTask geoCoderAsyncTask;
    // Define an object that holds accuracy and frequency parameters
    LocationRequest mLocationRequest;
    LocationClient mLocationClient;
    Uri mPictureUri;

    @Override protected void attachBaseContext(Context newBase) { super.attachBaseContext(Colatris.proxy(newBase)); }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        prefs = getSharedPreferences(Const.SHARED_PREFS_NAME, MODE_PRIVATE);
        if (!prefs.contains(Const.SHOW_RATING_KEY)) {
            prefs.edit().putBoolean(Const.SHOW_RATING_KEY, true);
        }

        if (!prefs.contains(Const.IS_STANDARD_UNITS) && !prefs.contains(Const.IS_METRIC_UNITS) && !prefs.contains(Const.IS_24_HOUR_CLOCK)) {
            SharedPreferences.Editor editor = prefs.edit();
            Locale locale = getResources().getConfiguration().locale;
            if (locale.equals(Locale.US)) {
                editor.putBoolean(Const.IS_STANDARD_UNITS, true);
                editor.putBoolean(Const.IS_METRIC_UNITS, false);
                editor.putBoolean(Const.IS_24_HOUR_CLOCK, false);
            } else {
                editor.putBoolean(Const.IS_STANDARD_UNITS, false);
                editor.putBoolean(Const.IS_METRIC_UNITS, true);
                editor.putBoolean(Const.IS_24_HOUR_CLOCK, true);
            }
            editor.commit();
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
            View root = findViewById(R.id.root);
            ViewTreeObserver obs = root.getViewTreeObserver();
            assert obs != null;
            obs.addOnGlobalLayoutListener(listener);

            googleMap.setOnMarkerClickListener(this);
            googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                @Override
                public void onMarkerDragStart(Marker marker) {
                    setState(State.MANUAL);
                }

                @Override
                public void onMarkerDrag(Marker marker) {

                }

                @Override
                public void onMarkerDragEnd(Marker marker) {

                }
            });
        }

        if (servicesConnected()) {
            if (isAlarmIntent()) lockScreen();

            if (!Tools.isManuallyParked(this) && prefs.getBoolean(Const.PARKING_SENSOR_KEY, false)) {
                Intent autoParkServiceIntent = new Intent(this, AutoParkService.class);
                autoParkServiceIntent.setAction(AutoParkService.ACTION_START);
                startService(autoParkServiceIntent);
            }

            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.dynamic_fl, new DynamicFragment());
            ft.replace(R.id.bar_fl, new BarFragment());
            ft.commit();


            if (savedInstanceState != null) {
                if (savedInstanceState.containsKey(STATE_STACK)) {
                    stateStack.addAll((Collection<State>) savedInstanceState.getSerializable(STATE_STACK));
                    setState(stateStack.peek(), false);
                }


                Handler h = new Handler();
                h.post(new Runnable() {
                    @Override
                    public void run() {
                        if (vehicleMarker != null) {
                            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(vehicleMarker.getPosition(), Const.ZOOM_LEVEL);
                            googleMap.animateCamera(cameraUpdate);
                        }
                    }
                });
            } else {
                setInitialState();
            }
        }
    }

    ViewTreeObserver.OnGlobalLayoutListener listener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            final float margin = Tools.convertDpToPixels(ParkActivity.this, 8);
            final Configuration config = getResources().getConfiguration();
            int orientation = config.orientation;
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {

                float barHeight = findViewById(R.id.bar_fl).getHeight();
                if (barHeight > 0) {
                    barHeight += margin;
                }
                float dynamicHeight = findViewById(R.id.dynamic_fl).getHeight();
                if (dynamicHeight > 0) {
                    dynamicHeight += margin;
                }
                int bottomPadding = (int) (barHeight + dynamicHeight);
                if (bottomPadding > 0) {
                    bottomPadding += margin;
                }

                googleMap.setPadding(0, 0, 0, bottomPadding);
            } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                int rightPadding = 0;
                float barWidth = findViewById(R.id.bar_fl).getWidth();
                if (barWidth > 0) {
                    rightPadding += (float) (barWidth + (margin * 2));
                } else {
                    float dynamicWidth = findViewById(R.id.dynamic_fl).getWidth();
                    if (dynamicWidth > 0) {
                        rightPadding += (float) (dynamicWidth + (margin * 2));
                    }
                }

                googleMap.setPadding(0, 0, rightPadding, 0);
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        if (vehicleMarker != null) {
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(vehicleMarker.getPosition(), Const.ZOOM_LEVEL);
            googleMap.animateCamera(cameraUpdate);
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
            if (!BuildConfig.DEBUG) {
                mockLocation();
                mLocationClient.connect();
            }
        }

        if (Tools.isParked(this)) {
            showVehicle();
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(STATE_STACK, stateStack);
        super.onSaveInstanceState(outState);
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

    @Override
    protected void onPause() {
        super.onPause();

        stopLocationUpdates();

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
                break;
            case IMAGE_CAPTURE_REQUEST:
                switch (resultCode) {
                    case RESULT_OK:
                        if (mPictureUri != null) {
                            prefs.edit().putString(Const.IMAGE_KEY, mPictureUri.toString()).commit();
                        }
                        break;
                }
                break;
            case RINGTONE_PICKER_RESULT:
                switch(resultCode) {
                    case RESULT_OK:
                        if (data != null) {
                            Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                            if (uri != null) {
                                if (BuildConfig.DEBUG) {
                                    Log.d("Ringtone", uri.toString());
                                }
                                prefs.edit().putString(Const.RINGTONE_URI_KEY, uri.toString()).commit();
                                break;
                            }
                        }
                }
                break;
        }
    }

    public void selectRingtone(View v) {
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_INCLUDE_DRM, true);
        }
        if (prefs.contains(Const.RINGTONE_URI_KEY)) {
            Uri uri = Uri.parse(prefs.getString(Const.RINGTONE_URI_KEY, ""));
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, uri);
        }
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL);
        startActivityForResult(intent, RINGTONE_PICKER_RESULT);

    }

    private void setSharedLocation() {
        Intent intent = getIntent();
        Uri googleMapsUri = intent.getData();
        if (googleMapsUri != null) {
            String q = googleMapsUri.getQueryParameter("q");
            if (q != null) {
                Pattern p = Pattern.compile("-?\\d{2}.\\d{2,6}");
                Matcher m = p.matcher(q);
                if (m.find()) {
                    String lat = m.group();
                    if (m.find()) {
                        String lng = m.group();

                        Tools.park(ParkActivity.this, lat, lng, true, true);
                    }
                }
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
    public boolean onMarkerClick(Marker marker) {
        if (Tools.isParked(this)) {
            startActivity(Intent.createChooser(IntentLibrary.getFindIntent(this),
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
        win.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override public void run() {
                win.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
            }
        }, 30 * 1000); // Let screen turn off after 30 seconds.
    }

    private boolean isAlarmIntent() {
        String action = getIntent().getAction();
        return action != null && action.equals(Const.ACTION_ALARM);
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
        Log.d("VALET", "-----> On Location Changed");
        if (Tools.isParked(this)) {
            if (getDynamicFragment() instanceof ParkedFragment) setParkedFragment(location);
        } else {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

            if (vehicleMarker == null) {
                if (googleMap != null) {
                    vehicleMarker = googleMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .draggable(true)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_pin)));
                }
            } else if (location.getAccuracy() != 0.0f && location.getAccuracy() < bestAccuracy) {
                vehicleMarker.setPosition(latLng);

                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(vehicleMarker.getPosition(), Const.ZOOM_LEVEL);

                googleMap.animateCamera(cameraUpdate);
            }

            bestAccuracy = location.getAccuracy();


            if (isParkingFragment() || BuildConfig.DEBUG) {
                setState(State.LOCATED);
            }

            setAccuracyFragment();
        }
    }

    private void setAccuracyFragment() {
        DynamicFragment dynamicFragment = getDynamicFragment();
        if (dynamicFragment != null && dynamicFragment instanceof LocatedFragment) {
            LocatedFragment locatedFragment = (LocatedFragment) dynamicFragment;
            locatedFragment.setAccuracyTextView(bestAccuracy);
        }
    }

    private void setParkedFragment(Location location) {
        DynamicFragment dynamicFragment = getDynamicFragment();
        if (dynamicFragment != null && dynamicFragment instanceof ParkedFragment) {
            ParkedFragment parkedFragment = (ParkedFragment) dynamicFragment;
            boolean isStandardUnits = false;
            isStandardUnits = prefs.getBoolean(Const.IS_STANDARD_UNITS, false);

            String distance;
            if (isStandardUnits) {
                float d = vehicleLocation.distanceTo(location) * Const.METERS_TO_MILES;
                distance = String.format("%.2f", d) + getString(R.string.mile_abbreviation);
            } else {
                float d = vehicleLocation.distanceTo(location) / 1000; // km
                distance = String.format("%.2f", d) + getString(R.string.kilometer_abbreviation);
            }

            CharSequence t = parkedFragment.distanceTextView.getText();
            String currentDistance = t == null ? "" : t.toString();
            if (!distance.equals(currentDistance)) {
                parkedFragment.distanceTextView.setText(distance);
                parkedFragment.distanceAnimator.start();
            }

            geoCoderAsyncTask = parkedFragment.new GeoCoderAsyncTask();
            geoCoderAsyncTask.execute(location);
        }
    }

    // Determine if Locating... screen is showing.
    private boolean isParkingFragment() {
        DynamicFragment dynamicFragment = getDynamicFragment();
        return dynamicFragment != null && dynamicFragment instanceof ParkingFragment;
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d("VALET", "-----> Location services connected");

        Location location = mLocationClient.getLastLocation();

        if (location != null) {
            if (!Tools.isParked(this)) {
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                        new LatLng(location.getLatitude(), location.getLongitude()), Const.ZOOM_LEVEL);
                if (googleMap != null) {
                    googleMap.animateCamera(cameraUpdate);
                }

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

    public void onLocatingItem(View v) {
        Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(settingsIntent);
    }

    public void onParkItem(View v) {
        if (vehicleMarker != null) {
            Tools.park(this, vehicleMarker.getPosition().latitude, vehicleMarker.getPosition().longitude, true, true);
        }

        setState(State.PARKED);
    }

    public void onSettingsItem(View v) {
        setState(State.SETTINGS);
    }

    public void onHelpItem(View v) {
        setState(State.HELP);
    }

    public void onTranslateItem(View v) {
//        Colatris.showTranslatorPrompt();
    }

    public void onFindItem(View v) {
        setState(State.FIND);
    }

    public void onScheduleItem(View v) {
        if (Tools.isTimed(this)) {
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
        Intent settingsIntent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
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
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        Tools.unpark(this);
        mPictureUri = null;

        if (prefs.getBoolean(Const.SHOW_RATING_KEY, true)) {
            setState(State.RATING);
        } else {
            setState(State.PARKING);
        }

        final Window win = getWindow();
        win.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
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

    public void onLocationItem(View v) {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);
    }

    private void dispatchTakePictureIntent(int actionCode) {

        File f = Tools.createExternalStoragePublicPicture();
        if (f != null) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            mPictureUri = Uri.fromFile(f);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mPictureUri);
            startActivityForResult(takePictureIntent, actionCode);
        }
    }

    public static boolean isIntentAvailable(Context context, String action) {
        final PackageManager packageManager = context.getPackageManager();
        assert packageManager != null;
        final Intent intent = new Intent(action);
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    public void onDeletePictureButton(View v) {
        Tools.deleteExternalStoragePublicPicture();
        mPictureUri = null;
        prefs.edit().remove(Const.IMAGE_KEY).commit();

        if (stateStack.peek() == State.PARKED) {
            ParkedFragment parkedFragment = (ParkedFragment) getDynamicFragment();
            parkedFragment.deletePictureButton.setVisibility(View.INVISIBLE);
            parkedFragment.cameraImageButton.setImageResource(R.drawable.camera_selector);
            parkedFragment.cameraImageButton.setContentDescription(getString(R.string.camera_button_cd));
        }

    }

    private void setInitialState() {
        if (prefs.contains(Const.LAT_KEY) && prefs.contains(Const.LONG_KEY)) {
            if (prefs.contains(Const.TIME_KEY)) {
                setState(State.TIMED);
            } else {
                setState(State.PARKED);
            }
        } else {
            setState(State.PARKING);
        }
    }

    void setState(State state) {
        setState(state, true);
    }

    void setState(State state, boolean shouldSave) {

        DynamicFragment dynamicFragment = new DynamicFragment();
        BarFragment barFragment = new BarFragment();

        switch (state) {
            case PARKING:
                Log.d("VALET", "------>  Parking");
                stateStack.clear();
                stateStack.push(State.PARKING);

                vehicleMarker = null;
                googleMap.setMyLocationEnabled(false);
                googleMap.clear();

                dynamicFragment = new ParkingFragment();

                barFragment.setItems(BarItem.LOCATING);

                startLocationUpdates();
                break;
            case LOCATED:
                barFragment.setItems(BarItem.PARK, BarItem.SETTINGS);

                dynamicFragment = new LocatedFragment();
                startLocationUpdates();
                break;
            case MANUAL:
                barFragment.setItems(BarItem.PARK);
                stopLocationUpdates();

                break;
            case PARKED:
                stateStack.clear();
                stateStack.push(State.PARKED);

                dynamicFragment = new ParkedFragment();

                barFragment.setItems(BarItem.FIND, BarItem.SCHEDULE, BarItem.SHARE, BarItem.RESET);

                break;
            case FIND:
                dynamicFragment = new FindFragment();

                break;
            case SCHEDULE:
                barFragment.setItems(BarItem.TIMER, BarItem.ALARM);

                break;
            case UNSCHEDULE:
                barFragment.setItems(BarItem.UNSCHEDULE);

                break;
            case TIMER:
                dynamicFragment = new TimerFragment();

                barFragment.setItems(BarItem.SET);

                break;
            case ALARM:
                dynamicFragment = new AlarmFragment();

                barFragment.setItems(BarItem.SET);

                break;
            case TIMED:
                stateStack.clear();
                stateStack.push(State.TIMED);

                dynamicFragment = new ParkedFragment();

                barFragment.setItems(BarItem.FIND, BarItem.SCHEDULE, BarItem.SHARE, BarItem.RESET);

                break;
            case CONFIRM:
                barFragment.setItems(BarItem.CONFIRM);

                break;

            case HELP:
                dynamicFragment = new HelpFragment();

                break;

            case SETTINGS:
                dynamicFragment = new SettingsFragment();

                barFragment.setItems(BarItem.TRANSLATE, BarItem.HELP);

                break;

            case RATING:
                stateStack.clear();
                stateStack.push(State.RATING);

                dynamicFragment = new RateFragment();
                barFragment.setItems(BarItem.YES, BarItem.NO, BarItem.NEVER);

                break;
        }

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.replace(R.id.bar_fl, barFragment);
        ft.replace(R.id.dynamic_fl, dynamicFragment);
        ft.commit();

        if (shouldSave) {
            stateStack.push(state);
        }

        if (Tools.isParked(this)) {
            showVehicle();
        }
    }

    private void startLocationUpdates() {
        if (BuildConfig.DEBUG) {
            if (vehicleMarker == null) {
                mockLocation();
            }
        } else if (servicesConnected()) {
            if (!mLocationClient.isConnected()) {
                mLocationClient.connect();
                Log.d("VALET", "-----> Starting location updates");
            }
        }
    }

    void mockLocation() {
        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                onLocationChanged(createLocation(LAT, LNG, ACCURACY));
            }
        });
    }

    @Override
    public void onBackPressed() {
        State state = null;
        if (!stateStack.isEmpty()) {
            state = stateStack.peek();
        }

        if (shouldSkipState(state)) {
            stateStack.pop();

            if (stateStack.isEmpty()) {
                super.onBackPressed();
            } else {
                onBackPressed();
            }
        } else {
            if (stateStack.isEmpty()) {
                super.onBackPressed();
            } else {
                stateStack.pop();
                setState(stateStack.peek(), false);
            }
        }
    }

    private boolean shouldSkipState(State s) {
        return s != null && (s == State.PARKING || s == State.LOCATED || s == State.PARKED || s == State.TIMED || s == State.RATING);
    }

    private void showVehicle() {
        if (BuildConfig.DEBUG && !Tools.isParked(this)) {
            throw new AssertionError("Showing vehicle while not parked");
        }

        Double latitude = Double.parseDouble(prefs.getString(Const.LAT_KEY, "0"));
        Double longitude = Double.parseDouble(prefs.getString(Const.LONG_KEY, "0"));

        if (vehicleMarker != null) {
            vehicleMarker.remove();
            vehicleMarker = null;
        }

        vehicleMarker = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(latitude, longitude))
                .draggable(false)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_pin)));

        vehicleLocation = new Location(LocationManager.GPS_PROVIDER);
        vehicleLocation.setLatitude(latitude);
        vehicleLocation.setLongitude(longitude);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(vehicleMarker.getPosition(), Const.ZOOM_LEVEL);
        googleMap.animateCamera(cameraUpdate);

        googleMap.setMyLocationEnabled(true);
    }

    private SupportMapFragment getMapFragment() {
        FragmentManager fm = getSupportFragmentManager();
        fm.executePendingTransactions();
        return (SupportMapFragment) fm.findFragmentById(R.id.map_frag);
    }

    private DynamicFragment getDynamicFragment() {
        FragmentManager fm = getSupportFragmentManager();
        return (DynamicFragment) fm.findFragmentById(R.id.dynamic_fl);
    }

    @Override
    public void onSelectMapType(int type) {
        googleMap.setMapType(type);
    }

    enum State {
        PARKING, LOCATED, PARKED, CONFIRM, SCHEDULE, FIND, MANUAL,
        UNSCHEDULE, ALARM, TIMER, TIMED, SETTINGS, HELP, RATING
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

    /*
   * From input arguments, create a single Location with provider set to
   * "flp"
   */
    public Location createLocation(double lat, double lng, float accuracy) {
        // Create a new Location
        Location newLocation = new Location(PROVIDER);
        newLocation.setLatitude(lat);
        newLocation.setLongitude(lng);
        newLocation.setAccuracy(accuracy);
        return newLocation;
    }
}
