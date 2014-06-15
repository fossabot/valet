package co.valetapp.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import co.valetapp.R;
import co.valetapp.util.Const;

/**
 * Created by jophde on 7/29/13.
 */
public class LocatedFragment extends DynamicFragment {

    static final double METERS_TO_YARDS = 1.09361;

    TextView accuracyUnitLabelTextView;
    TextView accuracyTextView;
    SharedPreferences prefs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.located_fragment, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        accuracyUnitLabelTextView = (TextView) view.findViewById(R.id.accuracyUnitLabelTextView);
        accuracyTextView = (TextView) view.findViewById(R.id.accuracyTextView);

        prefs = getActivity().getSharedPreferences(Const.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        if (prefs.contains(Const.IS_METRIC_UNITS)) {
            Log.d("valet", "has metric unit key");
        }

        if (prefs.contains((Const.IS_STANDARD_UNITS))) {
            Log.d("valet", "has standard unit key");
        }

        if (prefs.getBoolean(Const.IS_METRIC_UNITS, false)) {
            accuracyUnitLabelTextView.setText(R.string.meters);
        } else {
            accuracyUnitLabelTextView.setText(R.string.yards);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        ParkActivity parkActivity = (ParkActivity) getActivity();
        if (parkActivity.bestAccuracy > 0) {
            setAccuracyTextView(parkActivity.bestAccuracy);
        }
    }

    public void setAccuracyTextView(float accuracy) {
        if (prefs.getBoolean(Const.IS_METRIC_UNITS, false)) {
            accuracyTextView.setText(Integer.toString((int) accuracy));
        } else {
            accuracyTextView.setText(Integer.toString((int) (accuracy * METERS_TO_YARDS)));
        }
    }

}
