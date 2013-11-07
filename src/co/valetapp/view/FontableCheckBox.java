package co.valetapp.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckBox;

import co.valetapp.R;

public class FontableCheckBox extends CheckBox {

    public FontableCheckBox(Context context) {
        super(context);
    }

    public FontableCheckBox(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (!isInEditMode()) {
            UiUtil.setCustomFont(this, context, attrs,
                    R.styleable.com_roqbot_client_view_FontableCheckBox,
                    R.styleable.com_roqbot_client_view_FontableCheckBox_font);
        }
    }

    public FontableCheckBox(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (!isInEditMode()) {
            UiUtil.setCustomFont(this, context, attrs,
                    R.styleable.com_roqbot_client_view_FontableCheckBox,
                    R.styleable.com_roqbot_client_view_FontableCheckBox_font);
        }
    }

}
