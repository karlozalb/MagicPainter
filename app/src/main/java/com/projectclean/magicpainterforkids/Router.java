package com.projectclean.magicpainterforkids;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.projectclean.magicpainterforkids.activities.ImageSelectionActivity;
import com.projectclean.magicpainterforkids.activities.PaintActivity;
import com.projectclean.magicpainterforkids.activities.ShopActivity;
import com.projectclean.magicpainterforkids.customdialogs.AreYouSureDialog;
import com.projectclean.magicpainterforkids.customdialogs.BackgroundChooserDialog;
import com.projectclean.magicpainterforkids.customdialogs.PencilChooserDialog;

import java.util.ArrayList;

/**
 * Created by Carlos Albaladejo Pérez on 21/03/2016.
 */
public class Router {

    private static String PENCIL_DIALOG_TAG = "P_DIALOG_TAG";
    private static String BACKGROUND_DIALOG_TAG = "BG_DIALOG_TAG";
    private static String AUS_DIALOG_TAG = "AUS_DIALOG_TAG";

    public static void startImageSelectionActivity(Activity pcontext){
        Intent i = new Intent(pcontext, ImageSelectionActivity.class);
        pcontext.startActivityForResult(i, PaintActivity.IMAGE_GALLERY_REQUEST);
    }

    public static void startDrawingActivity(Activity pcontext,boolean pchooser){
        Intent i = new Intent(pcontext, PaintActivity.class);
        i.putExtra(PaintActivity.START_WITH_BACKGROUND_CHOOSER,pchooser);
        pcontext.startActivity(i);
    }

    public static void startShopActivity(Activity pcontext){
        Intent i = new Intent(pcontext, ShopActivity.class);
        pcontext.startActivity(i);
    }

    public static void showPencilChooserFragmentDialog(Activity pcontext){
        PencilChooserDialog tDialogFragment = new PencilChooserDialog();

        Bundle arguments = new Bundle();

        tDialogFragment.show(((AppCompatActivity) pcontext).getSupportFragmentManager(), PENCIL_DIALOG_TAG);
    }

    public static void showBackgroundChooserFragmentDialog(Activity pcontext,ArrayList<String> pproducts){
        BackgroundChooserDialog tDialogFragment = new BackgroundChooserDialog();

        Bundle arguments = new Bundle();
        arguments.putStringArrayList(BackgroundChooserDialog.PRODUCT_LIST,pproducts);
        tDialogFragment.setArguments(arguments);

        tDialogFragment.show(((AppCompatActivity) pcontext).getSupportFragmentManager(), BACKGROUND_DIALOG_TAG);
    }

    public static void showAreYouSureDeleteFragmentDialog(Activity pcontext){
        AreYouSureDialog tDialogFragment = new AreYouSureDialog();

        tDialogFragment.show(((AppCompatActivity) pcontext).getSupportFragmentManager(), AUS_DIALOG_TAG);
    }
}
