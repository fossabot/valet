package co.valetapp.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RadioButton;

import co.valetapp.R;

public class FontableRadioButton extends RadioButton {

    public FontableRadioButton(Context context) {
        super(context);
    }

    public FontableRadioButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (!isInEditMode()) {
            UiUtil.setCustomFont(this, context, attrs,
                    R.styleable.com_roqbot_client_view_FontableRadioButton,
                    R.styleable.com_roqbot_client_view_FontableRadioButton_font);
        }
    }

    public FontableRadioButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (!isInEditMode()) {
            UiUtil.setCustomFont(this, context, attrs,
                    R.styleable.com_roqbot_client_view_FontableRadioButton,
                    R.styleable.com_roqbot_client_view_FontableRadioButton_font);
        }
    }

}
