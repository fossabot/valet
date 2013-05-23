package co.valetapp.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;
import co.valetapp.R;
import co.valetapp.view.util.UiUtil;

public class FontableButton extends Button {
    public FontableButton(Context context) {
        super(context);
    }

    public FontableButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            UiUtil.setCustomFont(this, context, attrs,
                    R.styleable.com_roqbot_client_view_FontableButton,
                    R.styleable.com_roqbot_client_view_FontableButton_font);
        }
    }

    public FontableButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        if (!isInEditMode()) {
            UiUtil.setCustomFont(this, context, attrs,
                    R.styleable.com_roqbot_client_view_FontableButton,
                    R.styleable.com_roqbot_client_view_FontableButton_font);
        }
    }
}