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

    TextView accuracyTextView;
    SharedPreferences prefs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return getActivity().getLayoutInflater().inflate(R.layout.located_fragment, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        accuracyTextView = (TextView) view.findViewById(R.id.accuracyTextView);

        prefs = getActivity().getSharedPreferences(Const.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
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
        String text;
        if (prefs.getBoolean(Const.IS_METRIC_UNITS, false)) {
            text = getString(R.string.accuracy_f, (int) accuracy, getText(R.string.meters));
        } else {
            text = getString(R.string.accuracy_f, (int) (accuracy * METERS_TO_YARDS), getText(R.string.yards));
        }
        accuracyTextView.setText(text);
    }

}
