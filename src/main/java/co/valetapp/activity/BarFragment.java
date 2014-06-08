package co.valetapp.activity;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import co.valetapp.R;

public class BarFragment extends Fragment implements View.OnLongClickListener {

    public enum BarItem {
        PARK(R.layout.park_bar_item),
        SETTINGS(R.layout.settings_bar_item),
        HELP(R.layout.help_bar_item),
        LOCATING(R.layout.locating_bar_item),
        CONFIRM(R.layout.confirm_bar_item),
        FIND(R.layout.find_bar_item),
        RESET(R.layout.reset_bar_item),
        SHARE(R.layout.share_bar_item),
        SCHEDULE(R.layout.schedule_bar_item),
        UNSCHEDULE(R.layout.unschedule_bar_item),
        TIMER(R.layout.timer_bar_item),
        ALARM(R.layout.alarm_bar_item),
        SET(R.layout.set_bar_item),
        YES(R.layout.yes_bar_item),
        NO(R.layout.no_bar_item),
        NEVER(R.layout.never_bar_item);
        final int mResourceId;

        BarItem(int resourceId) {
            mResourceId = resourceId;
        }

        int getResourceId() {
            return mResourceId;
        }
    }

    private LinearLayout barLinearLayout;
    private BarItem[] barItems;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.bar_fragment, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        barLinearLayout = (LinearLayout) view.findViewById(R.id.bar_ll);

        if (barItems != null) {
            setItems(barItems);
        }
    }

    public void setItems(BarItem... barItems) {
        if (barLinearLayout != null) {
            for (BarItem barItem : barItems) {
                View view = getActivity().getLayoutInflater().inflate(barItem.getResourceId(), barLinearLayout, false);
                assert view != null;
                view.setTag(barItem);
//                view.setOnLongClickListener(this);
                barLinearLayout.addView(view);
            }
        }

        this.barItems = barItems;
    }

    @Override
    public boolean onLongClick(View v) {
        CharSequence contentDescription = v.getContentDescription();
        if (contentDescription != null) {
            Toast.makeText(getActivity(), contentDescription, Toast.LENGTH_LONG).show();
        }

        return true;
    }
}
