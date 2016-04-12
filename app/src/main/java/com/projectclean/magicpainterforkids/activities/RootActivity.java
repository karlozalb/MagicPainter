package com.projectclean.magicpainterforkids.activities;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.projectclean.magicpainterforkids.R;
import com.projectclean.magicpainterforkids.analytics.AnalyticsApplication;
import com.projectclean.magicpainterforkids.billingutils.IabHelper;
import com.projectclean.magicpainterforkids.billingutils.IabResult;
import com.projectclean.magicpainterforkids.billingutils.Inventory;


/**
 * Created by Carlos Albaladejo Pérez on 13/03/2016.
 */
public abstract class RootActivity  extends AppCompatActivity {

    protected IabHelper mHelper;

    protected boolean mNoAds, mExtraPack1, mExtraPack2;

    public static String base64EncodedPublicKey  = PaintActivity.stringTransform("jnnenMfie@LVOLN`\u001EP\u0017efvbaffhdfv\u001Ffjnned@ldfvbfISj^\u0012VrAknmRcC]_J^@hb]r\u0017A\u0011arh\u0017RrKCRF\bqU\u001FNS}\u0012d`Ss\u0017khLa\u0011Whf\u001EQBciafKipE\u001E\u0013u\u001Fpo\u0015O}JoIs\u0017aUA\fdksAqp\u0011\u0012JiemI\u0014\u001F\u0017V\u001FrSwL\u0016ncj`c`Q\fwBfIL@\u007FQd\u0014j\u007F]\u0013l}UoCn\u007FC\u0011\u0011\u0010NqVu^\bMdhkK@\bW\u0017a@\u001Fl^o}dss\u007F\u007FAAH^ao@L_\u0011A\bNBHtk\u0016bUjS\u0015asdR~Rmh\u0012\u001F]\u007FME}aFJm~\bK\u0017\u0014_\u0010\f\u0016TdLD]NfNUsK\u0013SqB\u001EbEKr\u0013FtCrB\u0016nW\b\u0015S\u0016DrVV@SHFLQRm~hJ\u0012\bk~Fhf]BIEoE\u007Fav\u0013pUV\br\u0010\bn\u001FBiPQEU\u0010n\u007F\u0017iefM\u0012\u0011lbLK\u0010QUvmDdML_HFoON`oFOBqaPuf\u0016MPncfvfe", 0x27);

    protected Tracker mTracker;

    public static String MAINACTIVITY = "Main activity",SHOPACTIVITY = "Shop",PAINTACTIVITY = "Paint activity";

    protected void onCreate(Bundle psavedinstancestate){
        super.onCreate(psavedinstancestate);
        // Obtain the shared Tracker instance.
        mTracker = getDefaultTracker();
    }

    /**
     * Gets the default {@link Tracker} for this {@link Application}.
     * @return tracker
     */
    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            mTracker = analytics.newTracker(R.xml.global_tracker);
        }
        return mTracker;
    }

    protected void hideStatusBar(){

        if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        }else{
            final View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE;
            decorView.setSystemUiVisibility(uiOptions);

            decorView.setOnSystemUiVisibilityChangeListener
                    (new View.OnSystemUiVisibilityChangeListener() {
                        @Override
                        public void onSystemUiVisibilityChange(int visibility) {
                            // Note that system bars will only be "visible" if none of the
                            // LOW_PROFILE, HIDE_NAVIGATION, or FULLSCREEN flags are set.
                            if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN & View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN & View.SYSTEM_UI_FLAG_IMMERSIVE) == 0) {
                                // TODO: The system bars are visible. Make any desired
                                // adjustments to your UI, such as showing the action bar or
                                // other navigational controls.
                                int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE;
                                decorView.setSystemUiVisibility(uiOptions);
                            } else {
                                // TODO: The system bars are NOT visible. Make any desired
                                // adjustments to your UI, such as hiding the action bar or
                                // other navigational controls.
                            }
                        }
                    });

        }
    }

    public void getPurchasedProducts(){
        IabHelper.QueryInventoryFinishedListener mGotInventoryListener
                = new IabHelper.QueryInventoryFinishedListener() {
            public void onQueryInventoryFinished(IabResult result,Inventory inventory) {
                if (result.isFailure()) {
                    Toast.makeText(RootActivity.this, "Error, please check your internet connection.", Toast.LENGTH_SHORT);
                    SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
                    mNoAds = sharedPref.getBoolean(ShopActivity.NO_ADS_PRODUCT, false);
                    mExtraPack1 = sharedPref.getBoolean(ShopActivity.EXTRA_PACK_1_PRODUCT, false);
                    mExtraPack2 = sharedPref.getBoolean(ShopActivity.EXTRA_PACK_2_PRODUCT, false);
                    onPurchasedProductsResult();
                }
                else {

                   SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
                   SharedPreferences.Editor editor = sharedPref.edit();
                   if (inventory.hasPurchase(ShopActivity.NO_ADS_PRODUCT)){
                       mNoAds = true;
                       editor.putBoolean(ShopActivity.NO_ADS_PRODUCT, mNoAds);
                   }
                   if (inventory.hasPurchase(ShopActivity.EXTRA_PACK_1_PRODUCT)){
                       mExtraPack1 = true;
                       editor.putBoolean(ShopActivity.EXTRA_PACK_1_PRODUCT, mExtraPack1);
                   }
                   if (inventory.hasPurchase(ShopActivity.EXTRA_PACK_2_PRODUCT)){
                       mExtraPack2 = true;
                       editor.putBoolean(ShopActivity.EXTRA_PACK_2_PRODUCT, mExtraPack2);
                   }
                   onPurchasedProductsResult();
                   editor.commit();
                }
            }
        };

        mHelper.queryInventoryAsync(mGotInventoryListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mHelper != null) mHelper.dispose();
        mHelper = null;
    }

    public abstract void onPurchasedProductsResult();

}
