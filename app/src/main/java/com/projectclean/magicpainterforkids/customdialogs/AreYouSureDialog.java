package com.projectclean.magicpainterforkids.customdialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

import com.projectclean.magicpainterforkids.R;
import com.projectclean.magicpainterforkids.activities.PaintActivity;
import com.projectclean.magicpainterforkids.customviews.CustomFontButton;
import com.projectclean.magicpainterforkids.customviews.CustomFontTextView;

/**
 * Created by Carlos Albaladejo Pérez on 29/03/2016.
 */
public class AreYouSureDialog extends DialogFragment {

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        View v = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_areyousure_dialog, null);

        CustomFontTextView cftv = (CustomFontTextView)LayoutInflater.from(getActivity()).inflate(R.layout.dialog_title_textview, null);
        cftv.setText(getString(R.string.delete_canvas));

        builder.setView(v).setCustomTitle(cftv);
        //builder.setView(v).setTitle(R.string.background_chooser);

        CustomFontButton yesButton = (CustomFontButton)v.findViewById(R.id.yes_button);
        CustomFontButton noButton = (CustomFontButton)v.findViewById(R.id.no_button);

        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((PaintActivity)getActivity()).clearCanvas();
                dismiss();
            }
        });

        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return builder.create();
    }
}
