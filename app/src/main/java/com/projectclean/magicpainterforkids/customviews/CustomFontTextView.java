package com.projectclean.magicpainterforkids.customviews;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.TextView;

import com.projectclean.magicpainterforkids.utils.FontCache;


public class CustomFontTextView extends TextView {

    public CustomFontTextView(Context context) {
        super(context);
        setCustomFont();
    }

    public CustomFontTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setCustomFont();
    }

    public CustomFontTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setCustomFont();
    }

    public void setCustomFont(){
        Typeface customFont = FontCache.getTypeface("littledays.ttf", getContext());
        setTypeface(customFont);
    }
}
