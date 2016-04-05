package com.projectclean.magicpainterforkids.customviews;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;

import com.projectclean.magicpainterforkids.utils.FontCache;

public class CustomFontButton extends Button {

    public CustomFontButton(Context context) {
        super(context);
        setCustomFont();
    }

    public CustomFontButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setCustomFont();
    }

    public CustomFontButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setCustomFont();
    }

    public void setCustomFont(){
        Typeface customFont = FontCache.getTypeface("littledays.ttf", getContext());
        setTypeface(customFont);
    }
}
