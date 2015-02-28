package co.valetapp.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import co.valetapp.R;

public class RateFragment extends DynamicFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return getActivity().getLayoutInflater().inflate(R.layout.rate_fragment, null);
    }
}
