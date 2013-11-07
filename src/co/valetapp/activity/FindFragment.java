package co.valetapp.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import co.valetapp.R;

import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_NORMAL;
import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_SATELLITE;

public class FindFragment extends DynamicFragment {

    interface Callback {
        void onSelectMapType(int type);
    }

    RadioGroup radioGroup;
    RadioButton normalRadioButton, satelliteRadioButton;
    Callback callback;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        callback = (Callback) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.find_fragment, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        radioGroup = (RadioGroup) view.findViewById(R.id.radioGroup);
        normalRadioButton = (RadioButton) view.findViewById(R.id.normalRadioButton);
        satelliteRadioButton = (RadioButton) view.findViewById(R.id.satelliteRadioButton);

        radioGroup.check(R.id.normalRadioButton);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.normalRadioButton:
                        callback.onSelectMapType(MAP_TYPE_NORMAL);
                        break;

                    case R.id.satelliteRadioButton:
                        callback.onSelectMapType(MAP_TYPE_SATELLITE);
                        break;
                }
            }
        });
    }
}
