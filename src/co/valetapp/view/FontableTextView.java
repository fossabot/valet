package co.valetapp.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;
import co.valetapp.R;
import co.valetapp.view.util.UiUtil;


public class FontableTextView extends TextView {

    public FontableTextView(Context context) {
        super(context);
    }

    public FontableTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            UiUtil.setCustomFont(this,context,attrs,
                    R.styleable.com_roqbot_client_view_FontableTextView,
                    R.styleable.com_roqbot_client_view_FontableTextView_font);
        }
    }

    public FontableTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (!isInEditMode()) {
            UiUtil.setCustomFont(this,context,attrs,
                    R.styleable.com_roqbot_client_view_FontableTextView,
                    R.styleable.com_roqbot_client_view_FontableTextView_font);
        }
    }
}