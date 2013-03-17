package co.valetapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import co.valetapp.DynamicFragment;
import co.valetapp.R;

public class AutoInfoFragment extends DynamicFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.auto_info_fragment, null);
    }
}
