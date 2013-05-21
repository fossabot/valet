package co.valetapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.AnimatorSet;

public class ParkedFragment extends DynamicFragment {

    ObjectAnimator parkedAnimatorIn, parkedAnimatorOut;
    TextView parkedTextView;
    AnimatorSet animatorSet;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.parked_fragment, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        parkedTextView = (TextView) view.findViewById(R.id.parked);

        parkedAnimatorIn = ObjectAnimator.ofFloat(parkedTextView, "alpha", 0f, 1f);
        parkedAnimatorIn.setDuration(1000);

        parkedAnimatorOut = ObjectAnimator.ofFloat(parkedTextView, "alpha", 1f, 0f);
        parkedAnimatorOut.setStartDelay(1000);
        parkedAnimatorOut.setDuration(1000);

        animatorSet = new AnimatorSet();
        animatorSet.playSequentially(parkedAnimatorIn, parkedAnimatorOut);
    }

    @Override
    public void onStart() {
        super.onStart();
        animatorSet.start();
    }

    @Override
    public void onStop() {
        super.onStop();

        animatorSet.end();
    }
}
