package co.valetapp.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;
import co.valetapp.R;
import co.valetapp.view.util.UiUtil;


public class FontableEditText extends EditText {

    public FontableEditText(Context context) {
        super(context);
    }

    public FontableEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
         if (!isInEditMode()) {
             UiUtil.setCustomFont(this,context,attrs,
                     R.styleable.com_roqbot_client_view_FontableEditText,
                     R.styleable.com_roqbot_client_view_FontableEditText_font);
         }
    }

    public FontableEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (!isInEditMode()) {
            UiUtil.setCustomFont(this,context,attrs,
                    R.styleable.com_roqbot_client_view_FontableEditText,
                    R.styleable.com_roqbot_client_view_FontableEditText_font);
        }
    }
}