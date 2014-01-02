package co.valetapp.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Locale;

import co.valetapp.R;

/**
 * Created by jophde on 7/29/13.
 */
public class LocatedFragment extends DynamicFragment {

    static final double METERS_TO_YARDS = 1.09361;

    TextView accuracyUnitLabelTextView;
    TextView accuracyTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.located_fragment, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        accuracyUnitLabelTextView = (TextView) view.findViewById(R.id.accuracyUnitLabelTextView);
        accuracyTextView = (TextView) view.findViewById(R.id.accuracyTextView);

        if (getResources().getConfiguration().locale.equals(Locale.US)) {
            accuracyUnitLabelTextView.setText(R.string.yards);
        } else {
            accuracyUnitLabelTextView.setText(R.string.meters);
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

    void setAccuracyTextView(float accuracy) {
        if (getResources().getConfiguration().locale.equals(Locale.US)) {
            accuracyTextView.setText(Integer.toString((int) (accuracy * METERS_TO_YARDS)));
        } else {
            accuracyTextView.setText(Integer.toString((int) accuracy));
        }
    }

}
